package org.fhcrc.optides.apps.DatFileMining;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

public class DatFileMining {
	private String inputFile = null;
	private String outputFile = null;
	private String miningKey = null;
	private boolean debug = false;
	
	private LogicFunction topLevelLogicFunction; 

	public DatFileMining(String inputFile, String outputFile, String miningKey, boolean debug) {
		this.inputFile = inputFile;
		this.outputFile = outputFile;
		this.miningKey = miningKey;
		this.debug = debug;
		topLevelLogicFunction = new LogicFunction(null, false);
	}

	public static void main(String[] args) {
		String inputFile = null;
		String outputFile = null;
		String miningKey = null;
		boolean debug = false;
		
		//--inputfile=uniprot.dat --outputfile=outputfile.txt --miningKey=Key.txt
		//Get the Parameters
		String[] curParam = null;
		for(int i = 0; i < args.length; i++){
			if(args[i].equals("--Debug"))
				debug = true;
			else{
				curParam = args[i].split("=");
				if(curParam[0].equals("--inputfile"))
					inputFile = curParam[1];
				else if(curParam[0].equals("--outputfile"))
					outputFile = curParam[1];
				else if(curParam[0].equals("--miningKey"))
					miningKey = curParam[1];
				else{
					System.out.println("Unrecognized command line parameter: " + curParam[0]);
					printUsage();
					return;
				}
			}
		}
		
		//make sure we have all the values we need
		if(inputFile == null || outputFile == null || miningKey == null){
			System.out.println("One or more of your commandline parameters are null:");
			System.out.println("--inputfile: " + inputFile);
			System.out.println("--outputfile: " + outputFile);
			System.out.println("--miningKey: " + miningKey);
			printUsage();
			return;
		}
		
		DatFileMining dfm = new DatFileMining(inputFile, outputFile, miningKey, debug);
		try {
			dfm.readMiningKeyFile();
			dfm.doFilter();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void doFilter() throws IOException {
		LineIterator it = null;
		it = FileUtils.lineIterator(new File(inputFile), "UTF-8");
		DatFileRecord curRecord = null;
		int matchCounter = 0;
		
		//prepare the writing
		File fout1 = new File(outputFile);
		FileOutputStream fos1 = new FileOutputStream(fout1);
		BufferedWriter outputFile = new BufferedWriter(new OutputStreamWriter(fos1));
		
		int j = 0;
		while(it.hasNext()){
			String[] rec = getNextRecord(it);
			//curRecord = new DatFileRecord(rec);
				
			if(topLevelLogicFunction.passesConditions(rec)){
			/*	for(int i = 0; i < curRecord.accessions.size(); i++)
					outputFile.write(curRecord.accessions.get(i).trim() + "\n"); */
				ArrayList<String> accessions = getAccessions(rec);
				for(int i = 0; i < accessions.size(); i++)
					outputFile.write(accessions.get(i).trim() + "\n");
				
				if(debug){
					System.out.println("ID: " + AbsCondition.getFieldDataFromDatRecord("ID", rec));
					System.out.println("GENE: " + AbsCondition.getFieldDataFromDatRecord("GENE", rec));
					System.out.println("TAXID: " + AbsCondition.getFieldDataFromDatRecord("TAXID", rec));
					System.out.println("ORGANISM: " + AbsCondition.getFieldDataFromDatRecord("ORGANISM", rec));
					System.out.println("GO: " + AbsCondition.getFieldDataFromDatRecord("GO", rec));
					System.out.println("SUBCEL: " + AbsCondition.getFieldDataFromDatRecord("SUBCELLULAR", rec));
					System.out.println("FUNCTION: " + AbsCondition.getFieldDataFromDatRecord("FUNCTION", rec));
					/*
					System.out.println("ID: " + curRecord.ID);
					System.out.println("GENE: " + curRecord.geneName);
					System.out.println("TAXID: " + curRecord.taxID);
					System.out.println("ORGANISM: " + curRecord.organism);
					System.out.println("GO: " + curRecord.GO);
					System.out.println("SUBCEL: " + curRecord.subcellularLocation);
					System.out.println("FUNCTION: " + curRecord.function);
					//for(int j = 0; j < curRecord.length; j++){
					//	System.out.println(curRecord[j]);
					//}*/
					System.out.println("");
				}
				matchCounter++;
			}
		}
		outputFile.close();
		System.out.println("Number of matching records: " + matchCounter);
	    LineIterator.closeQuietly(it);
	}

	private String[] getNextRecord(LineIterator it) {
		ArrayList<String> retVal = new ArrayList<String>();
		String line = "";
	    while (it.hasNext()) {
	        line = it.nextLine();
	        if(line.equals("//"))
	        	break;
	        else
	        	retVal.add(line);
	    }
		return retVal.toArray(new String[retVal.size()]);
	}

	private void readMiningKeyFile() throws IOException {
		FileReader reReader = new FileReader(miningKey);
		BufferedReader miningKeyBR = new BufferedReader(reReader);
		
		topLevelLogicFunction = makeLogicFunction(miningKeyBR, null, false);
		
		miningKeyBR.close();
	}
	
	private LogicFunction makeLogicFunction(BufferedReader miningKeyBR, LogicFunction parentLF, boolean invertResult) throws IOException{
		LogicFunction retVal = new LogicFunction(parentLF, invertResult);
		boolean invertNextCondition = false;
		String line;
		while((line = miningKeyBR.readLine()) != null){
			if(line.equals(")")){
				return retVal;
			}else if(line.equals("(")){
				retVal.addCondition(makeLogicFunction(miningKeyBR, retVal, invertNextCondition));
				invertNextCondition = false;
			}else if(line.equals("NOT")){
				invertNextCondition = true;
			}else if(line.equals("AND")){
				retVal.addOperator("AND");
			}else if(line.equals("OR")){
				retVal.addOperator("OR");
			}else{
				String[] a = line.split(": ");
				retVal.addCondition(new DatFileCondition(parentLF, a[0], a[1], invertNextCondition));
				invertNextCondition = false;
			}
		}
		return retVal;
	}

	public static ArrayList<String> getAccessions(String[] rec) {
		ArrayList<String> accessions = new ArrayList<String>();
		for(int i=1; i < 5; i++)
			if(rec[i].startsWith("AC   ")){
				accessions.addAll(Arrays.asList(rec[i].substring(5).split(";")));
		}
		return accessions;
	}
	
	private static void printUsage() {
		System.out.println("");
		System.out.println("USAGE: java -jar DatFileMining.jar --inputfile=path/to/uniprot.dat --outputfile=path/to/outputfile.txt --miningKey=path/to/Key.txt [--Debug]");
		System.out.println("");
	}

}
