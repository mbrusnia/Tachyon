package org.fhcrc.optides.apps.UniquenessGeneFinder;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * 
 * This program outputs the Ensemble numbers which belong to [cutoff] number of unique
 * values in the selectedCol column of a tsv spreadsheet.
 * 
 * @author Hector
 *
 */
public class UniquenessGeneFinder {

	//required parameters
	private String selectedCol;
	private String inputFile;
	private String outputFile;
	private String uniqueStatFile;
	private ArrayList<String> delimiters;
		
	//optional parameters
	private Integer cutoff = 1;  //default value
	
	//computed values we need
	private int ensemblColIdx = 0;
	private int selectedColIdx = 0;
	private BufferedReader inputFileBufferedReader;
	private BufferedWriter uniqueStatFileBufferedWriter;
	private BufferedWriter outputFileBufferedWriter;
	private String[] inputColHeaders;
	private HashMap<String, HashMap<String, Integer>> ensemblToSelectedColMap;
	private HashMap<String, HashMap<String, Integer>> selectedColToEnsemblMap;
	
	public static final String ENSEMBL_COLUMN_NAME = "Ensembl";
		
	UniquenessGeneFinder(UniquenessGeneFinderBuilder builder){
		this.selectedCol = builder.selectedCol;
		this.inputFile = builder.inputFile;
		this.outputFile = builder.outputFile;
		this.uniqueStatFile = builder.uniqueStatFile;
		this.delimiters = builder.delimiters;
		this.cutoff = builder.cutoff;
	}
	
	//for builder pattern
	public static class UniquenessGeneFinderBuilder{
		//required parameters
		private String selectedCol;
		private String inputFile;
		private String outputFile;
		private String uniqueStatFile;
		private int cutoff = 1;
		private ArrayList<String> delimiters;
		
		UniquenessGeneFinderBuilder(String selectedCol, String inputFile, String outputFile, String uniqueStatFile){
			if(selectedCol == null || selectedCol == "")
				throw new IllegalStateException("ERROR: Please provide a valid value for parameter: SelectedCol");
			if(inputFile == null || inputFile == "")
				throw new IllegalStateException("ERROR: Please provide a valid value for parameter: inputfile");
			if(outputFile == null || outputFile == "")
				throw new IllegalStateException("ERROR: Please provide a valid value for parameter: outputfile");
			if(uniqueStatFile == null || uniqueStatFile == "")
				throw new IllegalStateException("ERROR: Please provide a valid value for parameter: uniqueStat");
			this.selectedCol = selectedCol;
			this.inputFile = inputFile;
			this.outputFile = outputFile;
			this.uniqueStatFile = uniqueStatFile;
		}
		public UniquenessGeneFinderBuilder setCutoff(int cutoff){
			if(cutoff < 1)
				throw new IllegalStateException("ERROR: Please provide a valid value for parameter: Cutoff.  It must be greater than 0.");
			this.cutoff = cutoff;
			return this;
		}
		public UniquenessGeneFinderBuilder setDelimiters(String delimiters){
			if(delimiters == null || delimiters == "")
				throw new IllegalStateException("ERROR: Please provide a valid value for parameter: delimiters");
			this.delimiters = new ArrayList<String>();
			Collections.addAll(this.delimiters, delimiters.split(" "));
			return this;
		}
		
		public UniquenessGeneFinder build(){
			return new UniquenessGeneFinder(this);
		}
	}
	
	public static void main(String[] args) {
		String selectedCol = "";
		String inputFile = "";
		String outputFile = "";
		String uniqueStatFile = "";
		String delimiters = "; , <br>";
		Integer cutoff = 1;

		//check command line input parameters
		//if(args.length !=3){
		//	System.out.println("This program requires three parameters to Run.  Please see the following USAGE:");
		//	printUsage();
		//	return;
		//}
		
		//get input params
		String[] curParam = null;
		for(int i = 0; i < args.length; i++){
			curParam = args[i].split("=");
			if(curParam[0].equals("--inputfile"))
				inputFile = curParam[1];
			else if(curParam[0].equals("--SelectedCol"))
				selectedCol = curParam[1].toUpperCase();
			else if(curParam[0].equals("--Cutoff"))
				cutoff = Integer.parseInt(curParam[1]);
			else if(curParam[0].equals("--Delimiters"))
				delimiters = curParam[1];
			else if(curParam[0].equals("--uniqueStat"))
				uniqueStatFile = curParam[1];
			else if(curParam[0].equals("--outputfile"))
				outputFile = curParam[1];
			else{
				System.out.println("Unrecognized command line parameter: " + curParam[0]);
				printUsage();
				return;
			}			
		}
		
		//attempt to build the UniquenessGeneFinder object (error if user parameters are invalid)
		UniquenessGeneFinder ugf;
		try{
			ugf= new UniquenessGeneFinder.UniquenessGeneFinderBuilder(
				selectedCol, inputFile, outputFile, uniqueStatFile).setCutoff(cutoff)
				.setDelimiters(delimiters).build();
		}catch (IllegalStateException e){
			System.out.println(e.getMessage());
			System.out.println("");
			printUsage();
			return;
		}
		//open files for reading and writing
		try{
			ugf.setInputFileBufferedReader(
					ugf.openFileForReading(
							ugf.getInputFilename()
							)
					);
			
			ugf.setOutputFileBufferedWriter(
					ugf.openFileForWriting(
							ugf.getOutputFilename()
							)
					);
			ugf.setUniqueStatFileBufferedWriter(
					ugf.openFileForWriting(
							ugf.getUniqueStatFilename()
							)
					);
		}catch(Exception e){
			e.printStackTrace();
			System.out.println("");
			printUsage();
			ugf.close();
			return;
		}
		
		//get the column Indexes we need for our processing
		ugf.setEnsemblColIdx(ugf.getColIdx(UniquenessGeneFinder.ENSEMBL_COLUMN_NAME));
		ugf.setSelectedColIdx(ugf.getColIdx(ugf.getSelectedCol()));
				
		try {
			ugf.processInput();
			ugf.writeUniqueStatReport(ugf.getSelColMapToEnsembl());
			ugf.writeOutput(ugf.getEnsemblToSelColMap(), ugf.getCutoff());
		} catch (IOException e) {
			e.printStackTrace();
			ugf.close();
		}
		
		//close all read/write files
		ugf.close();
	}
	
	private void writeOutput(HashMap<String, HashMap<String, Integer>> map, int cutoff2) throws IOException {
		outputFileBufferedWriter.write(UniquenessGeneFinder.ENSEMBL_COLUMN_NAME + "\tNumber of Matching Categories (cutoff=" + cutoff2 +")\n");
				
		//close and reopen the input bufferedreader to reset it back to 1st position
		if(inputFileBufferedReader != null)
			inputFileBufferedReader.close();
		this.setInputFileBufferedReader(
			this.openFileForReading(
				this.getInputFilename()
			)
		);
		
		String curEnsembl = "";
		//skip first line of headers
		String line = inputFileBufferedReader.readLine();
		String[] lines;
		while((line = inputFileBufferedReader.readLine()) != null){
			lines = line.split("\t");
			curEnsembl = lines[ensemblColIdx];
			
			HashMap<String, Integer> hm = map.get(curEnsembl);
			
			if(hm != null && hm.size() <= cutoff2)
				outputFileBufferedWriter.write(line + "\n");
		}
	}

	private int getCutoff() {
		return cutoff;
	}

	private void writeUniqueStatReport(HashMap<String, HashMap<String, Integer>> map) throws IOException {
		SortedSet<String> keys = new TreeSet<String>(map.keySet());
		uniqueStatFileBufferedWriter.write("Category\tNumber of Genes\n");
		for (String key : keys) { 
		   HashMap<String, Integer> value = map.get(key);
		   uniqueStatFileBufferedWriter.write(key + "\t" + value.size() + "\n");
		}
	}

	private HashMap<String, HashMap<String, Integer>> getSelColMapToEnsembl() {
		return selectedColToEnsemblMap;
	}

	private void printMapNumbers(HashMap<String, HashMap<String, Integer>> map) {
		SortedSet<String> keys = new TreeSet<String>(map.keySet());
		for (String key : keys) { 
		   HashMap<String, Integer> value = map.get(key);
		   System.out.println(key + " " + value.size());
		}
	}

	private HashMap<String, HashMap<String, Integer>> getEnsemblToSelColMap() {
		return ensemblToSelectedColMap;
	}

	public void processInput() throws IOException {
		//set up our hashes that will organize Ensembl to SelectedCol mappings and vice versa
		ensemblToSelectedColMap = new HashMap<String, HashMap<String, Integer>>();
		selectedColToEnsemblMap = new HashMap<String, HashMap<String, Integer>>();
		String line;
		String[] lineArr;
		ArrayList<String> separatedSelectedColValues;
		String ensemblVal;
		HashMap<String, Integer> curExistingSelColValues;
		HashMap<String, Integer> curExistingEnsemblValues;
			//now iterate through the whole input file, capturing the data we need.
		while((line = inputFileBufferedReader.readLine()) != null){
			lineArr = line.split("\t");
			ensemblVal = lineArr[ensemblColIdx];
			separatedSelectedColValues = breakupStringByDelimiters(lineArr[selectedColIdx]);
			
			//update ensembl -> selectedCol counters
			if(ensemblToSelectedColMap.containsKey(ensemblVal))
				curExistingSelColValues = ensemblToSelectedColMap.get(ensemblVal);
			else
				curExistingSelColValues = new HashMap<String, Integer>();
				
			for(String selColVal : separatedSelectedColValues){
				if(selColVal.equals(""))
					continue;
				if(curExistingSelColValues.containsKey(selColVal)){
						curExistingSelColValues.replace(selColVal, curExistingSelColValues.get(selColVal) + 1);
				}else{
					curExistingSelColValues.put(selColVal, 1);
				}
			}
			if(!curExistingSelColValues.isEmpty())
				ensemblToSelectedColMap.put(ensemblVal, curExistingSelColValues);
			
			//update selectedCol -> ensembl counters
			for(String selColVal : separatedSelectedColValues){
				if(selColVal.equals(""))
					continue;
				if(selectedColToEnsemblMap.containsKey(selColVal))
					curExistingEnsemblValues = selectedColToEnsemblMap.get(selColVal);
				else
					curExistingEnsemblValues = new HashMap<String, Integer>();
					
				
				if(curExistingEnsemblValues.containsKey(ensemblVal)){
					curExistingEnsemblValues.replace(ensemblVal, curExistingEnsemblValues.get(ensemblVal) + 1);
				}else{
					curExistingEnsemblValues.put(ensemblVal, 1);
				}
				
				if(!curExistingEnsemblValues.isEmpty())
					selectedColToEnsemblMap.put(selColVal, curExistingEnsemblValues);
			}
		}
	}

	public ArrayList<String> breakupStringByDelimiters(String string) {
		ArrayList<String> retval = new ArrayList<String>();
		ArrayList<String> temp = new ArrayList<String>();
		String foundDelimiter = "";
		for(String d : delimiters){
			if(string.contains(d)){
				foundDelimiter = d;
			}
		}
		if(!foundDelimiter.equals(""))
			Collections.addAll(temp, string.split(foundDelimiter));
		else
			temp.add(string);
		
		for(String val : temp){
				Collections.addAll(retval, val.split(":")[0].trim());
		}
		
		return retval;
	}

	private void setSelectedColIdx(int colIdx) {
		this.selectedColIdx = colIdx;
	}

	public String getSelectedCol() {
		return selectedCol;
	}

	private void setUniqueStatFileBufferedWriter(BufferedWriter openFileForWriting) {
		this.uniqueStatFileBufferedWriter = openFileForWriting;
	}

	private void setOutputFileBufferedWriter(BufferedWriter openFileForWriting) {
		this.outputFileBufferedWriter = openFileForWriting;
	}

	public BufferedWriter openFileForWriting(String filename) throws IOException {
		return new BufferedWriter(new FileWriter(filename));
	}

	//set inputfile bufferedreader and get the column headers
	public void setInputFileBufferedReader(BufferedReader fileForReading) throws IOException {
		this.inputFileBufferedReader = fileForReading;
		
		//get column headers
		String line = inputFileBufferedReader.readLine();
		inputColHeaders = line.split("\t");
	}

	public String getInputFilename() {
		return inputFile;
	}
	
	public String getOutputFilename() {
		return outputFile;
	}
	
	public String getUniqueStatFilename() {
		return uniqueStatFile;
	}

	public BufferedReader openFileForReading(String filename) throws FileNotFoundException {
		File file = new File(filename);
		FileReader fileReader = new FileReader(file);
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		return bufferedReader;
	}

	public int getColIdx(String columnName) {
		for(int i=0; i < inputColHeaders.length; i++)
			if(inputColHeaders[i].toUpperCase().equals(columnName.toUpperCase()))
				return i;
		throw new IllegalStateException("ERROR: Column with name " + columnName + " was not found in input file!");
	}
	public void setEnsemblColIdx(int i) {
		this.ensemblColIdx = i;
	}
	public static void printUsage() {
		System.out.println("USAGE: UniquenessGeneFinder.java --SelectedCol=\"Sample\" --Cutoff=3 --Delimiters=\", <br> ;\" --inputfile=\"C:/input/Sample1_tissue.tsv\" --outputfile=\"C:/result/run1.tsv\" --uniqueStat=\"C:/result/Sample1_resultstat.tsv\"");		
		System.out.println("");
		System.out.println("Delimiters are separated by a single space.  default value: ; , <br>");
	}
	
	public void close(){
		try {
		if(inputFileBufferedReader != null)
				inputFileBufferedReader.close();
		if(uniqueStatFileBufferedWriter != null)
			uniqueStatFileBufferedWriter.close();
		if(outputFileBufferedWriter != null)
			outputFileBufferedWriter.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}
