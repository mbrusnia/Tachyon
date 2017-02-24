package org.fhcrc.optides.apps.FastaTransform;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.fhcrc.optides.apps.FilterFasta.FilterFasta;

public class FastaTransform {

	public static void main(String[] args) {
		//--input=/fullpath/input.fasta --position_cutoff=position_cutoff_location.txt
		//--regular_expression= "C[A-Z]{0,15}C[A-Z]{0,15}C[A-Z]{0,15}C[A-Z]{0,15}C[A-Z]{0,15}C"
		//--output=/fullpath/output.fasta --logfile=/fullpath/transformFasta.log
		//get input params
		String inputFasta = "";
		String positionCutoffFile = "";
		String regularExpression = "";
		String outputFile = "";
		String logFile = "";
		
		String[] curParam = null;
		for(int i = 0; i < args.length; i++){
			curParam = args[i].split("=");
			if(curParam[0].equals("--input"))
				inputFasta = curParam[1];
			else if(curParam[0].equals("--position_cutoff"))
				positionCutoffFile = curParam[1];
			else if(curParam[0].equals("--regular_expression"))
				regularExpression = curParam[1];
			else if(curParam[0].equals("--output"))
				outputFile = curParam[1];
			else if(curParam[0].equals("--logfile"))
				logFile = curParam[1];
			else{
				System.out.println("Unrecognized command line parameter: " + curParam[0]);
				printUsage();
				return;
			}
		}
		if(inputFasta == "" || positionCutoffFile == "" || regularExpression == "" 
				|| outputFile == "" || logFile == ""){
			
			System.out.println("One or more parameters are missing from the command line: ");
			System.out.println("");
			System.out.println("--input: " + inputFasta);
			System.out.println("--position_cutoff: " + positionCutoffFile);
			System.out.println("--regular_expression: " + regularExpression);
			System.out.println("--output: " + outputFile);
			System.out.println("--logfile: " + logFile);
			System.out.println("");
			printUsage();
		}
		
		try {
			FastaTransform.doTransform(inputFasta, positionCutoffFile, regularExpression, outputFile, logFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("End.");
	}

	private static void doTransform(String inputFasta, String positionCutoffFile, String regularExpression, String outputFile,
			String logFile) throws Exception {

		Map<String, Integer> positionCutoffMap = new HashMap<String, Integer>();
		FileReader inputFastaFile = new FileReader(inputFasta);
		BufferedReader inputFastaBufferedReader = new BufferedReader(inputFastaFile);
		
		FileReader PCReader = new FileReader(positionCutoffFile);
		BufferedReader positionCutoffBufferedReader = new BufferedReader(PCReader);
		
		//prepare the writing
		File fout1 = new File(outputFile);
		FileOutputStream fos1 = new FileOutputStream(fout1);
		BufferedWriter outputFastaFile = new BufferedWriter(new OutputStreamWriter(fos1));
		File logout1 = new File(logFile);
		FileOutputStream los1 = new FileOutputStream(logout1);
		BufferedWriter logFileWriter = new BufferedWriter(new OutputStreamWriter(los1));
		
		String line = null;
		String[] a = null;
		while((line = positionCutoffBufferedReader.readLine()) != null){
			a = line.split("\\s+");
			if(a.length != 2)
				throw new Exception("This line in the position_cutoff file is not properly formatted: \n" + line);
			positionCutoffMap.put(a[0], Integer.parseInt(a[1]));
		}		

		String curId = "";
		String header_line = "";
		int starting_idx = 0;
		while((line = inputFastaBufferedReader.readLine()) != null){
			if(line.startsWith(">")){
				a = line.split(" ");
				//">" is the first character, so substring starting at second character
				curId = a[0].substring(1);
				header_line = line;
			}else if(!line.equals("")){
				if(positionCutoffMap.containsKey(curId))
					starting_idx = positionCutoffMap.get(curId);
				
				String newSeq = line.substring(starting_idx);				
				Pattern pattern = Pattern.compile(regularExpression);
			    Matcher matcher = pattern.matcher(newSeq);
			    if(matcher.find()){
					outputFastaFile.write(header_line + "\n");
					outputFastaFile.write(newSeq + "\n");
					logFileWriter.write(curId + "\t" + matcher.start() + "\t" + (newSeq.length() - matcher.end()) + "\n");
					//logFileWriter.write(newSeq.substring(0, matcher.start()) + "--" + newSeq.substring(matcher.start(), matcher.end())+ "--" +  newSeq.substring(matcher.end()) + "\n");
			    }
			}
			starting_idx = 0;
		}		
		outputFastaFile.close();
		logFileWriter.close();
		
		PCReader.close();
		positionCutoffBufferedReader.close();
		
		inputFastaFile.close();
		inputFastaBufferedReader.close();
	}

	private static void printUsage() {
		System.out.println("USAGE: java FastaTransform --input=pathToInputFasta --position_cutoff=pathToCutoffFile --regular_expression=\"regularExpression\" --output=pathToOutputFastaFile --logfile=pathToLogFile");
	}

}
