package org.fhcrc.optides.apps.DatFileMining;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

public class DatFileMining {
	private String inputFile = null;
	private String outputFile = null;
	private String miningKey = null;
	private boolean debug = false;
	
	private Map<String, ArrayList<String>> firstMap = null;
	private Map<String, ArrayList<String>> andMap = null;
	private Map<String, ArrayList<String>> orMap = null;
	private Map<String, ArrayList<String>> notMap = null;

	public DatFileMining(String inputFile, String outputFile, String miningKey, boolean debug) {
		this.inputFile = inputFile;
		this.outputFile = outputFile;
		this.miningKey = miningKey;
		this.debug = debug;

		firstMap = new HashMap<String, ArrayList<String>>();
		andMap = new HashMap<String, ArrayList<String>>();
		orMap = new HashMap<String, ArrayList<String>>();
		notMap = new HashMap<String, ArrayList<String>>();
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void doFilter() throws IOException {
		LineIterator it = null;
		it = FileUtils.lineIterator(new File(inputFile), "UTF-8");
		String [] curRecord = null;
		int matchCounter = 0;
		
		//prepare the writing
		File fout1 = new File(outputFile);
		FileOutputStream fos1 = new FileOutputStream(fout1);
		BufferedWriter outputFile = new BufferedWriter(new OutputStreamWriter(fos1));
		
		while(it.hasNext()){
			curRecord = getNextRecord(it);
				
			if(passesMapFilter(curRecord, "AND", andMap) &&
				passesMapFilter(curRecord, "OR", orMap) &&
				passesMapFilter(curRecord, "NOT", notMap)){

				String outputString = curRecord[1].substring(5);
				String [] outputArr = outputString.split(";");
				for(int i = 0; i < outputArr.length; i++)
					outputFile.write(outputArr[i].trim() + "\n");
				
				if(debug){
					System.out.println("ID: " + getFieldDataFromDatRecord("ID", curRecord));
					System.out.println("GENE: " + getFieldDataFromDatRecord("GENE", curRecord));
					System.out.println("TAXID: " + getFieldDataFromDatRecord("TAXID", curRecord));
					System.out.println("ORGANISM: " + getFieldDataFromDatRecord("ORGANISM", curRecord));
					System.out.println("GO: " + getFieldDataFromDatRecord("GO", curRecord));
					System.out.println("SUBCEL: " + getFieldDataFromDatRecord("SUBCELLULAR", curRecord));
					System.out.println("FUNCTION: " + getFieldDataFromDatRecord("FUNCTION", curRecord));
					for(int j = 0; j < curRecord.length; j++){
						System.out.println(curRecord[j]);
					}
					System.out.println("");
				}
				matchCounter++;
			}
		}
		outputFile.close();
		System.out.println("Number of matching records: " + matchCounter);
	    LineIterator.closeQuietly(it);
	}

	private boolean passesMapFilter(String[] record, String operator, Map<String, ArrayList<String>> map) {
		boolean retVal = operator.equals("AND") || operator.equals("NOT");  //if "and" or "NOT", we'll start with true, if "or" we'll start with false
		
		if(map.size() == 0)
			return true;
		
		for(String fieldID : map.keySet()){
			boolean innerMatchFlag = false;
			String fieldData = getFieldDataFromDatRecord(fieldID, record);
			for(String keyphrase : map.get(fieldID)){
				if(fieldData.toUpperCase().matches(".*\\b" + keyphrase.toUpperCase() + "\\b.*")){
					innerMatchFlag = true;
					if(operator.equals("NOT"))
						return false;
					break;
				}
			}
			if(operator.equals("AND"))
				retVal = retVal && innerMatchFlag;
			else if(operator.equals("OR"))
				retVal = retVal || innerMatchFlag;
		}
		return retVal;
	}

	// Field: Key     Field will be one of the following TaxID, GO, SUBCELLULAR, FUNCTION, ANYFIELD
	private String getFieldDataFromDatRecord(String fieldID, String[] record) {
		StringBuilder retVal = new StringBuilder();
		boolean readingSubcellularOrFunction = false;

		for(int i = 0; i < record.length; i++){
			switch (fieldID){
				case "ID":
					if(record[i].startsWith("ID   "))
						retVal.append(record[i].substring(5, 5 + record[i].substring(5).indexOf(' ')));
					break;
				case "GENE":
					if(record[i].startsWith("GN   Name="))
						retVal.append(record[i].substring(10, 10 + record[i].substring(10).indexOf(';')));
					break;

				case "ORGANISM":
					if(record[i].startsWith("OS   "))
						retVal.append(record[i].substring(5));
					break;
				case "ANYFIELD":
					retVal.append(" " + record[i].substring(5));
					break;
				case "GO":
					if(record[i].startsWith("DR   GO; "))
						retVal.append(record[i].substring(8));
					break;
				case "TAXID":
					if(record[i].startsWith("OX   NCBI_TaxID=") || record[i].startsWith("OH   NCBI_TaxID="))
						retVal.append(" " + record[i].substring(16));
					break;
				case "SUBCELLULAR":
					if(record[i].startsWith("CC   -!- SUBCELLULAR LOCATION:")){
						retVal.append(record[i].substring(31));
						readingSubcellularOrFunction = true;
					}else if(readingSubcellularOrFunction && record[i].startsWith("CC       "))
							retVal.append(record[i].substring(8));
					else
						readingSubcellularOrFunction = false;
					break;
				case "FUNCTION":
					if(record[i].startsWith("CC   -!- FUNCTION:")){
						retVal.append(record[i].substring(18));
						readingSubcellularOrFunction = true;
					}else if(readingSubcellularOrFunction && record[i].startsWith("CC       "))
							retVal.append(record[i].substring(8));
					else
						readingSubcellularOrFunction = false;
					break;
			}
		}
		return retVal.toString();
	}

	private String[] getNextRecord(LineIterator it) {
		ArrayList<String> retVal = new ArrayList();
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

		String line;
		Map<String, ArrayList<String>> curMap = firstMap;
		while((line = miningKeyBR.readLine()) != null){
			if(line.toUpperCase().equals("AND")){
				if(firstMap != null){
					andMap = firstMap;
					firstMap = null;
				}
				curMap = andMap;
			}else if(line.toUpperCase().equals("OR")){
				if(firstMap != null){
					orMap = firstMap;
					firstMap = null;
				}
				curMap = orMap;
			}else if(line.toUpperCase().equals("NOT")){
				if(firstMap != null){
					andMap = firstMap;
					firstMap = null;
				}
				curMap = notMap;
			}else if(line.equals("")){
				continue;
			}else{ // Field: Key     Field will be one of the following TaxID, GO, SUBCELLULAR, FUNCTION, ANYFIELD
				String[] a = line.split(": ");
				String colID = a[0].toUpperCase();
				ArrayList<String> keywordList = new ArrayList<String>(Arrays.asList(a[1]));
				
				if(!curMap.containsKey(colID))
					curMap.put(colID, keywordList);
				else
					curMap.get(colID).addAll(keywordList);
			}
		}	
		if(firstMap != null){
			andMap = firstMap;
			firstMap = null;
		}
		miningKeyBR.close();
	}

	private static void printUsage() {
		System.out.println("");
		System.out.println("USAGE: java -jar DatFileMining.jar --inputfile=path/to/uniprot.dat --outputfile=path/to/outputfile.txt --miningKey=path/to/Key.txt [--Debug]");
		System.out.println("");
	}

}
