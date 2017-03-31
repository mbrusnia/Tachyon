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
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class FastaTransform {

	private ArrayList<String> regularExpressions = null;
	private Map<String, ArrayList<Integer>> matchedRegexAAspreadStats = null; // needs to hold 1) regex 2)how many matches 3) sum of length of all matches
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
		matchedRegexAAspreadStats = new HashMap<String, ArrayList<Integer>>();

		ArrayList<String> matchedRegularExpressions = null;
		
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
			}else if(!line.equals("")){  //sequence
				String sequence = line;
				matchedRegularExpressions = new ArrayList<String>();
				if(positionCutoffMap != null && positionCutoffMap.containsKey(curId)){
					starting_idx = positionCutoffMap.get(curId);
					sequence = line.substring(starting_idx);
				}
				
				Pattern pattern = null;
			    Matcher matcher = null;
				for(int i = 0; i < regularExpressions.size(); i++){
					pattern = Pattern.compile(regularExpressions.get(i));
				    matcher = pattern.matcher(sequence);
				    if(matcher.find())
				    	if(matchedRegularExpressions.size() == 0)  //first regex match
					    	matchedRegularExpressions.add(regularExpressions.get(i));
				    	else	//not first regex match
				    		for(int j = 0; j < matchedRegularExpressions.size(); j++)
				    			//if curRegex has been matched or a more specific (longer) regex has been previously matched, do nothing
				    			if(matchedRegularExpressions.get(j).contains(regularExpressions.get(i)))
				    				break; // do nothing
				    			//if curRexEx is a longer/more specific version of one we've matched already, replace the old one with the new one
				    			else if(regularExpressions.get(i).contains(matchedRegularExpressions.get(j))){
				    				matchedRegularExpressions.set(j, regularExpressions.get(i));
				    				break;
				    			//if we've gotten to the end and neither of the two preceeding cases were true, simply add the regex to our collection of matched regexs
				    			}else if(j == matchedRegularExpressions.size() - 1)
				    				matchedRegularExpressions.add(regularExpressions.get(i));
				}

				if(matchedRegularExpressions.size() == 0){
			    	logFileWriter.write(curId + ", unmatched\n");
			    	endOfLogSummary.add(curId + ", unmatched");
			    }else{
			    	int match_leftmost_idx = Integer.MAX_VALUE;
			    	int match_rightmost_idx = Integer.MIN_VALUE;
			    	for(int i = 0; i < matchedRegularExpressions.size(); i++){
						pattern = Pattern.compile(matchedRegularExpressions.get(i));
					    matcher = pattern.matcher(sequence);
					    matcher.find();
					    if(matcher.start() < match_leftmost_idx)
					    	match_leftmost_idx = matcher.start();
					    if(matcher.end() > match_rightmost_idx)
					    	match_rightmost_idx = matcher.end();
			    	}
					outputFastaFile.write(header_line + "\n");
					if(prefix_length > -1 || sufix_length > -1){
						if(prefix_length == -1 || prefix_length > match_leftmost_idx)
							starting_idx = 0;
						else
							starting_idx = match_leftmost_idx - prefix_length;

						if(sufix_length == -1 || sufix_length + match_rightmost_idx > sequence.length())
							ending_idx = sequence.length();
						else
							ending_idx = match_rightmost_idx + sufix_length;
						sequence = sequence.substring(starting_idx, ending_idx);
					}
					outputFastaFile.write(sequence + "\n");

			    	for(int i = 0; i < matchedRegularExpressions.size(); i++){
			    		String regex = matchedRegularExpressions.get(i);
						pattern = Pattern.compile(regex);
					    matcher = pattern.matcher(sequence);
					    matcher.find();
						logFileWriter.write(curId + ", " + matcher.start() + ", " + (sequence.length() - matcher.end()) + ", " + matchedRegularExpressions.get(i) + ", " + (matcher.end() - matcher.start()) +"\n");
				    	endOfLogSummary.add(curId + ", matched");
				    	
				    	//collect stats for average spread between regex specific AAs
						if(!matchedRegexAAspreadStats.containsKey(regex)){
							ArrayList<Integer> tmp = new ArrayList<Integer>();
							tmp.add(0);
							matchedRegexAAspreadStats.put(regex, tmp);
						}
						ArrayList<Integer> curValues = matchedRegexAAspreadStats.get(regex);
						curValues.set(0, curValues.get(0) + 1);
						curValues.addAll(getAAgapCollection(sequence.substring(matcher.start(), matcher.end()), regex));  // += matcher.end() - matcher.start() - countAAs(regex);
						matchedRegexAAspreadStats.put(regex, curValues);
			    	}	
			    }
			}
			starting_idx = 0;
		}
		
		logFileWriter.write("\nSummary State:\n");
		for(int i = 0; i < endOfLogSummary.size(); i++)
			logFileWriter.write(endOfLogSummary.get(i) + "\n");

		logFileWriter.write("\nRegEx AA Pair Average Distance:\n");
		for(String regex : matchedRegexAAspreadStats.keySet()){
			ArrayList<Integer> values = matchedRegexAAspreadStats.get(regex);
			logFileWriter.write(regex + " ");
			for(int i = 1; i < values.size(); i++){
				if(i > 1)
					logFileWriter.write(", ");
				logFileWriter.write(values.get(i).toString());
			}
			logFileWriter.write("\n");
		}
		
		outputFastaFile.close();
		logFileWriter.close();
		
		inputFastaFile.close();
		inputFastaBufferedReader.close();
	}

	private ArrayList<Integer> getAAgapCollection(String sequence, String regex) {
		ArrayList<Integer> retVal = new ArrayList<Integer>();
		String anchorAAs = getRegexAnchorAAs(regex);
		int counter = 0;
		int anchor_cur_idx = 0;
		for(int i = 0; i < sequence.length(); i++){
			String seqChar = sequence.substring(i,  i + 1);
			String regexChar = anchorAAs.substring(anchor_cur_idx,  anchor_cur_idx + 1);
			if(i == 0)
				if (!seqChar.equals(regexChar))
					throw new Error("The first character of the sequence string does not match the first character of the regex!");
				else 
					anchor_cur_idx++;
			else if (!seqChar.equals(regexChar))
				counter++;
			else if (seqChar.equals(regexChar)){
				retVal.add(counter);
				counter = 0;
				anchor_cur_idx++;
			}
		}
		return retVal;
	}

	//strip a regex of all [A-Z]'s and {1,15}'s, etc.  leave only the specified AAs
	private String getRegexAnchorAAs(String regex) {
		String retVal = null;
		boolean inBrackets = false;
		boolean inCurlyBrackets = false;
		for(int i = 0; i < regex.length(); i++){
			String curChar = regex.substring(i, i + 1);
			if(curChar.equals("[") && !inBrackets)
				inBrackets = true;
			else if(curChar.equals("]") && inBrackets)
				inBrackets = false;
			else if(curChar.equals("{") && !inCurlyBrackets)
				inCurlyBrackets = true;
			else if(curChar.equals("}") && inCurlyBrackets)
				inCurlyBrackets = false;
			else if(!inBrackets && !inCurlyBrackets)
				if(retVal == null)
					retVal = curChar;
				else
					retVal += curChar;
		}
		return retVal;
	}

	private static void printUsage() {
		System.out.println("USAGE: java FastaTransform --input=pathToInputFasta --position_cutoff=pathToCutoffFile --regular_expression=path.to.multi.reg.exp.file --output=pathToOutputFastaFile --logfile=pathToLogFile [--prefix_length=-1 --sufix_length=-1]");
		System.out.println("");
		System.out.println("The argument --position_cutoff cannot be used in conjunction with --prefix_length AND/OR --sufix_length.");
	}

}
