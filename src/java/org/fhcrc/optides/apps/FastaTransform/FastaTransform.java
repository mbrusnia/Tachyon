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

	private ArrayList<String> regularExpressions = null;
	private Map<String, Integer> positionCutoffMap = null;
	private ArrayList<String> endOfLogSummary = null;
	
	public static void main(String[] args) {
		//--input=/fullpath/input.fasta --position_cutoff=position_cutoff_location.txt
		//--regular_expression=path.to.multi.reg.ex.file
		//--output=/fullpath/output.fasta --logfile=/fullpath/transformFasta.log
		//--prefix_length and --sufix_length
		
		//get input params
		String inputFasta = "";
		String positionCutoffFile = "";
		String regularExpression = "";
		String outputFile = "";
		String logFile = "";
		int prefix_length = -1;
		int sufix_length = -1;
		
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
			else if(curParam[0].equals("--prefix_length"))
				prefix_length = Integer.parseInt(curParam[1]);
			else if(curParam[0].equals("--sufix_length"))
				sufix_length = Integer.parseInt(curParam[1]);
			else{
				System.out.println("Unrecognized command line parameter: " + curParam[0]);
				printUsage();
				return;
			}
		}
		if(inputFasta.equals("") || (positionCutoffFile.equals("") && prefix_length == -1 && prefix_length == -1) || regularExpression.equals("") 
				|| outputFile.equals("") || logFile.equals("")){
			
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
		
		if(!positionCutoffFile.equals("") && (prefix_length > -1 || sufix_length > -1)){
			System.out.println("ERROR: you have used both the --position_cuttoff option and the --prefix_length (or --sufix_length) at the same time.  This is strictly prohibited.");
			System.out.println("");
			printUsage();
			return;
		}
		
		try {
			FastaTransform ft = new FastaTransform();
			ft.doTransform(inputFasta, positionCutoffFile, regularExpression, outputFile, logFile, prefix_length, sufix_length);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("End.");
	}

	private void readRegularExpressions(String regularExpressionsFile) throws IOException {
		regularExpressions = new ArrayList<String>();
		FileReader reReader = new FileReader(regularExpressionsFile);
		BufferedReader regexBufferedReader = new BufferedReader(reReader);

		String line;
		while((line = regexBufferedReader.readLine()) != null){
			if(line.equals(""))
				continue;
			regularExpressions.add(line);
		}	
		regexBufferedReader.close();
	}

	private void readPositionCutoffMap(String file) throws Exception{
		positionCutoffMap = new HashMap<String, Integer>();
		String line = null;
		String[] a = null;
		if(!file.equals("")){
			positionCutoffMap = new HashMap<String, Integer>();
			FileReader PCReader = new FileReader(file);
			BufferedReader positionCutoffBufferedReader = new BufferedReader(PCReader);
			while((line = positionCutoffBufferedReader.readLine()) != null){
				a = line.split("\\s+");
				if(a.length != 2)
					throw new Exception("This line in the position_cutoff file is not properly formatted: \n" + line);
				positionCutoffMap.put(a[0], Integer.parseInt(a[1]));
			}			
			PCReader.close();
			positionCutoffBufferedReader.close();
		}
		
	}
	private  void doTransform(String inputFasta, String positionCutoffFile, String regularExpressionsFile, String outputFile,
			String logFile, int prefix_length, int sufix_length) throws Exception {
		endOfLogSummary = new ArrayList<String>();
		FileReader inputFastaFile = null;
		BufferedReader inputFastaBufferedReader = null;
		
		readRegularExpressions(regularExpressionsFile);
		readPositionCutoffMap(positionCutoffFile);

		//prepare the writing
		File fout1 = new File(outputFile);
		FileOutputStream fos1 = new FileOutputStream(fout1);
		BufferedWriter outputFastaFile = new BufferedWriter(new OutputStreamWriter(fos1));
		File logout1 = new File(logFile);
		FileOutputStream los1 = new FileOutputStream(logout1);
		BufferedWriter logFileWriter = new BufferedWriter(new OutputStreamWriter(los1));
		
		
		inputFastaFile = new FileReader(inputFasta);
		inputFastaBufferedReader = new BufferedReader(inputFastaFile);
		String curId = "";
		String header_line = "";
		int starting_idx = 0;
		int ending_idx = 0;
		String line;
		String[] a;
		while((line = inputFastaBufferedReader.readLine()) != null){
			if(line.startsWith(">")){
				a = line.split(" ");
				//">" is the first character, so substring starting at second character
				curId = a[0].substring(1);
				header_line = line;
			}else if(!line.equals("")){
				String sequence = line;
				if(positionCutoffMap != null && positionCutoffMap.containsKey(curId)){
					starting_idx = positionCutoffMap.get(curId);
					sequence = line.substring(starting_idx);
				}
				
				Pattern pattern = null;
			    Matcher matcher = null;
				int i = 0;
				for(; i < regularExpressions.size(); i++){
					pattern = Pattern.compile(regularExpressions.get(i));
				    matcher = pattern.matcher(sequence);
				    if(matcher.find())
				    	break;
				}
				boolean matchedRegEx = i < regularExpressions.size();
				if(matchedRegEx){
					outputFastaFile.write(header_line + "\n");
					if(prefix_length > -1 || sufix_length > -1){
						if(prefix_length == -1 || prefix_length > matcher.start())
							starting_idx = 0;
						else
							starting_idx = matcher.start() - prefix_length;

						if(sufix_length == -1 || sufix_length + matcher.end() > sequence.length())
							ending_idx = sequence.length();
						else
							ending_idx = matcher.end() + sufix_length;
						sequence = sequence.substring(starting_idx, ending_idx);
					    matcher = pattern.matcher(sequence); //regenerate starting and ending indices
					    matcher.find();
					}
					outputFastaFile.write(sequence + "\n");
					logFileWriter.write(curId + ", " + matcher.start() + ", " + (sequence.length() - matcher.end()) + ", " + regularExpressions.get(i) + ", " + (matcher.end() - matcher.start()) +"\n");
			    	endOfLogSummary.add(curId + ", matched");
					//logFileWriter.write(newSeq.substring(0, matcher.start()) + "--" + newSeq.substring(matcher.start(), matcher.end())+ "--" +  newSeq.substring(matcher.end()) + "\n");
			    }else{
			    	logFileWriter.write(curId + ", unmatched\n");
			    	endOfLogSummary.add(curId + ", unmatched");
			    }
			}
			starting_idx = 0;
		}
		
		logFileWriter.write("\nSummary State:\n");
		for(int i = 0; i < endOfLogSummary.size(); i++)
			logFileWriter.write(endOfLogSummary.get(i) + "\n");
		outputFastaFile.close();
		logFileWriter.close();
		
		inputFastaFile.close();
		inputFastaBufferedReader.close();
	}

	private static void printUsage() {
		System.out.println("USAGE: java FastaTransform --input=pathToInputFasta --position_cutoff=pathToCutoffFile --regular_expression=path.to.multi.reg.exp.file --output=pathToOutputFastaFile --logfile=pathToLogFile [--prefix_length=-1 --sufix_length=-1]");
		System.out.println("");
		System.out.println("The argument --position_cutoff cannot be used in conjunction with --prefix_length AND/OR --sufix_length.");
	}

}
