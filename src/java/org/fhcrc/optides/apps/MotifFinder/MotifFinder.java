package org.fhcrc.optides.apps.MotifFinder;

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
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.plaf.InputMapUIResource;

public class MotifFinder {

	//input parameters
	private String inputFile;
	private String outputFile = "";
	private String logFile = "";
	private int minFreq = 0;
	
	private String consensus;
	private String motif;
	private String numberedMotif;

	//array list containing all IDs from input file in the same order as the inputFile
	ArrayList<String> inputIDs;
	
	//hashmap containing all key -> sequence pairs from input file
	private Map<String, String> idToSeqMap;
	
	public MotifFinder(String inputFile, String outputFile, String logFile, int minFreq) {
		this.inputFile = inputFile; 
		this.outputFile = outputFile;
		this.logFile = logFile;
		this.minFreq = minFreq;
		
		inputIDs = new ArrayList<String>();
		idToSeqMap = new HashMap<String, String>();
	}

	public static void main(String[] args) {
		// --input_file_with_concensus=project32_Input.txt --output_file=project32_motif.txt
		// --log=project32.log --min_freq=2 (default value = 0)
		
		String inputFile = "";
		String outputFile = "";
		String logFile = "";
		int minFreq = 0;
		
		//get input params
		String[] curParam = null;
		for(int i = 0; i < args.length; i++){
			curParam = args[i].split("=");
			if(curParam[0].equals("--input_file_with_concensus"))
				inputFile = curParam[1];
			else if(curParam[0].equals("--output_file"))
				outputFile = curParam[1];
			else if(curParam[0].equals("--log"))
				logFile = curParam[1];
			else if(curParam[0].equals("--min_freq"))
				minFreq = Integer.parseInt(curParam[1]);
			else{
				System.out.println("Unrecognized command line parameter: " + curParam[0]);
				printUsage();
				return;
			}
		}
		if(inputFile.equals("") || outputFile.equals("") || logFile.equals("")){
			System.out.println("One or more of the required parameters were not set correctly.  Here are the current values:");
			System.out.println("input_file_with_concensus: " + inputFile);
			System.out.println("output_file: " + outputFile);
			System.out.println("log: " + logFile);
			System.out.println("min_freq: " + minFreq);
			System.out.println();
			printUsage();
			return;
		}
		
		MotifFinder mf = new MotifFinder(inputFile, outputFile, logFile, minFreq);
		
		try {
			mf.parseInputFile();
			mf.buildMotifs();
			mf.writeMotifsToFile(outputFile);
			mf.writeLog(logFile);
			//System.out.println(mf.motif);
			//System.out.println(mf.numberedMotif);
			//mf.printMap();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	private void writeLog(String logFile) throws IOException {
		//prepare the writing
		File fout1 = new File(logFile);
		FileOutputStream fos1 = new FileOutputStream(fout1);
		BufferedWriter outputFileWriter = new BufferedWriter(new OutputStreamWriter(fos1));

		//write motif and numberedMotif
		outputFileWriter.write(motif + "\n");
		outputFileWriter.write(numberedMotif + "\n\n");

		//run our newly created motif as a regular expression against all input sequences
		//(minus the "."s) and report if they matched against it or not
		Pattern pattern = Pattern.compile(motif);
		Matcher matcher = null;
		for(String curID : inputIDs){
			outputFileWriter.write(curID + "\t");
		    matcher = pattern.matcher(idToSeqMap.get(curID).replace(".", "")); //remove the "."s
		    outputFileWriter.write((matcher.find()) ? "Matched\n" : "Unmatched\n");
		}
		
		outputFileWriter.close();
	}
	
	private void writeMotifsToFile(String outputFile) throws IOException {
		//prepare the writing
		File fout1 = new File(outputFile);
		FileOutputStream fos1 = new FileOutputStream(fout1);
		BufferedWriter outputFileWriter = new BufferedWriter(new OutputStreamWriter(fos1));

		outputFileWriter.write(motif + "\n");
		
		outputFileWriter.close();
	}
	
	public void buildMotifs(){
		consensus = idToSeqMap.get("Consensus");
		motif = "";
		numberedMotif = "";

		boolean firstConsensusLetter = true;
		boolean secondConsensusLetter = false;
		int dotCounter = 0;
		int lastConsensusMatch_idx = 0;
		String curSeq;
		String curSeqLetter;
		String curConsensusLetter;
		int matchWindow = 0;
		Map<String, Integer> columnLetters = null;
		
		//outer loop through consensus sequence
		for(int i=0; i < consensus.length(); i++){
			curConsensusLetter = consensus.substring(i, i+1).toUpperCase();
			if(curConsensusLetter.equals(".")){
				dotCounter++;
			}else{
				matchWindow=0;
				columnLetters = new HashMap<String, Integer>();
				columnLetters.put(curConsensusLetter, 0);
				
				//inner loop through each input sequence
				for(String curID : inputIDs){
					curSeq = idToSeqMap.get(curID);
					curSeqLetter = curSeq.substring(i, i+1);
					
					//if we're on the first letter of the consensus, then we use a window of 4 on the N terminus to allow for a match
					if(firstConsensusLetter){
						int j = 0;
						boolean matchFound = false;
						while(j <= 4 && i - (j+1) >= 0 && !matchFound){
							j++;
							matchFound = curSeq.substring(i - j, i + 1 - j).equals(curConsensusLetter);
						}
						if(matchFound){
							if(j > matchWindow)
								matchWindow = j;
							curSeqLetter = curConsensusLetter;
						}
					}else{
						//how many dots did this sequence have since the last consensus matching letter?
						int seqDotCount = curSeq.substring(lastConsensusMatch_idx+1, i+1).length() - curSeq.substring(lastConsensusMatch_idx+1, i+1).replace(".", "").length();
						//if more than any other sequence so far, save this value
						if(seqDotCount > matchWindow)
							matchWindow = seqDotCount;
					}
					
					//give a 4AA window at the end to match the final consensus letter as we did at the beginning
					boolean isLastConsensusLetter = i == consensus.length() -1;
					if(curSeqLetter.equals(".") && isLastConsensusLetter){
						int backwards_idx = 1;
						while(backwards_idx <= 4 && curSeq.substring(i - backwards_idx, i - backwards_idx + 1).equals(".")) backwards_idx++;
						curSeqLetter = curSeq.substring(i - backwards_idx, i - backwards_idx + 1);
					}
					if(!curSeqLetter.equals(".")){
						if(!columnLetters.containsKey(curSeqLetter))
							columnLetters.put(curSeqLetter, 0);
						columnLetters.put(curSeqLetter, columnLetters.get(curSeqLetter) + 1);
					}
				}
				
				//write the number of occurrences of wildcards between consensus matches
				if(!firstConsensusLetter && !secondConsensusLetter){
					if(matchWindow == 0){
						motif += "[A-Z]{" + dotCounter + "}";
						numberedMotif += "[A-Z]{" + dotCounter + "}";
					}else {
						motif += "[A-Z]{" + (dotCounter-matchWindow)  + "," + dotCounter + "}";
						numberedMotif += "[A-Z]{" + (dotCounter-matchWindow)  + "," + dotCounter + "}";
					}	
				}

				//write the column letters if they were seen more than min_freq times
				int numLettersPassingFreqThreshold = 0;
				for(String letter : columnLetters.keySet()){
					if(columnLetters.get(letter) >= minFreq)
						numLettersPassingFreqThreshold++;
				}
				if(numLettersPassingFreqThreshold == 1){
					motif += curConsensusLetter;
					numberedMotif += columnLetters.get(curConsensusLetter);
				}else{
					motif += "[";
					numberedMotif += "[";
					int j = 0;
					for(String letter : columnLetters.keySet()){
						if(columnLetters.get(letter) >= minFreq){
							motif += letter;
							if(j++ > 0)
								numberedMotif +=  ",";
							numberedMotif += columnLetters.get(letter);
						}
					}
					motif += "]";
					numberedMotif += "]";
				}
				
				if(firstConsensusLetter){
					//if it's the first letter, we have to count all the dots from the 
					//beginning of the consensus all the way up to the second letter
					int secondLetter_idx = 0;
					while(consensus.charAt(secondLetter_idx) == '.') secondLetter_idx++;
					secondLetter_idx++;
					while(consensus.charAt(secondLetter_idx) == '.') secondLetter_idx++;
					dotCounter = consensus.substring(0, secondLetter_idx).length() 
								- consensus.substring(0, secondLetter_idx).replace(".", "").length();

					if(matchWindow == 0){
						motif += "[A-Z]{" + dotCounter + "}";
						numberedMotif += "[A-Z]{" + dotCounter + "}";
					}else {
						motif += "[A-Z]{" + (dotCounter-matchWindow)  + "," + dotCounter + "}";
						numberedMotif += "[A-Z]{" + (dotCounter-matchWindow)  + "," + dotCounter + "}";
					}	

					firstConsensusLetter = false;
					secondConsensusLetter = true;
				}else
					secondConsensusLetter = false;
				
				dotCounter = 0;
				lastConsensusMatch_idx = i;
			}
		}
	}
	
	public void parseInputFile() throws IOException{
		FileReader inputFileReader = new FileReader(inputFile);
		BufferedReader inputFiletBufferedReader = new BufferedReader(inputFileReader);
		
		String line = null;
		String[] a = null;
		String curProtId = null;
		String curSeq = null;
		while((line = inputFiletBufferedReader.readLine()) != null){
			curProtId = "";
			curSeq = "";
			a = line.split("\\s+");
			for(int i = 0; i < a.length; i++){
				if(a[i].equals(""))
					continue;
				else if(curProtId.equals(""))
					curProtId = a[i];
				else
					curSeq += a[i];
			}
			
			if(curProtId.equals(""))
				continue;
			if(!isInteger(curProtId, 10)){
				if(!idToSeqMap.containsKey(curProtId)){
					idToSeqMap.put(curProtId, "");
					if(!curProtId.equals("Consensus"))
						inputIDs.add(curProtId);
				}
				idToSeqMap.put(curProtId, idToSeqMap.get(curProtId) + curSeq);
			}
		}
		
		inputFileReader.close();
		inputFiletBufferedReader.close();
	}

	public static void printUsage(){
		System.out.println("USAGE: java MotifFinder --input_file_with_concensus=project32_Input.txt --output_file=project32_motif.txt --log=project32.log --min_freq=2 (default value = 0)");
	}

	public void printMap() {
		for (String curProtId: inputIDs) {
			System.out.println(curProtId + " = (" + ((String)idToSeqMap.get(curProtId)).length() + ") " + idToSeqMap.get(curProtId));
        }
	}
	
	public boolean isInteger(String s, int radix) {
	    if(s.isEmpty()) return false;
	    for(int i = 0; i < s.length(); i++) {
	        if(i == 0 && s.charAt(i) == '-') {
	            if(s.length() == 1) return false;
	            else continue;
	        }
	        if(Character.digit(s.charAt(i),radix) < 0) return false;
	    }
	    return true;
	}
}
