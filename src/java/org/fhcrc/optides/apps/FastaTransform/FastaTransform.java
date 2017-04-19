package org.fhcrc.optides.apps.FastaTransform;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.statistics.HistogramType;

public class FastaTransform {

	private ArrayList<String> regularExpressions = null;
	private Map<String, ArrayList<Integer>> matchedRegexAAspreadStats = null; // needs to hold 1) regex 2)how many matches 3) sum of length of all matches
	private Map<String, Integer> positionCutoffMap = null;
	private ArrayList<String> endOfLogSummary = null;
	
	private String distanceAA;
	private String outputDir;
	
	private int prefix_length;
	private int sufix_length;
	public FastaTransform(String distanceAA, int prefix_length, int sufix_length) {
		this.distanceAA = distanceAA;
		this.prefix_length = prefix_length;
		this.sufix_length = sufix_length;
	}


	public static void main(String[] args) {
		//--input=/fullpath/input.fasta --position_cutoff=position_cutoff_location.txt
		//--regular_expression=path.to.multi.reg.ex.file
		//--output=/fullpath/output.fasta --logfile=/fullpath/transformFasta.log
		//--prefix_length and --sufix_length --DistanceAA
		
		//get input params
		String inputFasta = "";
		String positionCutoffFile = "";
		String regularExpression = "";
		String outputFile = "";
		String logFile = "";
		int prefix_length = -100;
		int sufix_length = -100;
		String distanceAA = "-Z";
		
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
			else if(curParam[0].equals("--DistanceAA"))
				distanceAA = curParam[1];
			else{
				System.out.println("Unrecognized command line parameter: " + curParam[0]);
				printUsage();
				return;
			}
		}
		if(inputFasta.equals("") || (positionCutoffFile.equals("") && prefix_length == -100 && prefix_length == -100) || regularExpression.equals("")
				|| outputFile.equals("") || logFile.equals("")){
			
			System.out.println("One or more parameters are missing from the command line: ");
			System.out.println("");
			System.out.println("--input: " + inputFasta);
			System.out.println("--position_cutoff: " + positionCutoffFile);
			System.out.println("--regular_expression: " + regularExpression);
			System.out.println("--output: " + outputFile);
			System.out.println("--logfile: " + logFile);
			System.out.println("--DistanceAA: " + distanceAA);
			System.out.println("");
			printUsage();
			return;
		}
		
		if(!positionCutoffFile.equals("") && (prefix_length > -1 || sufix_length > -1)){
			System.out.println("ERROR: you have used both the --position_cuttoff option and the --prefix_length (or --sufix_length) at the same time.  This is strictly prohibited.");
			System.out.println("");
			printUsage();
			return;
		}
		
		try {
			FastaTransform ft = new FastaTransform(distanceAA, prefix_length, sufix_length);
			ft.doTransform(inputFasta, positionCutoffFile, regularExpression, outputFile, logFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("End.");
	}
	
	
	/**
	 * @param inputFasta - fasta file to transform
	 * @param positionCutoffFile - a file with id-position pairs; used to trim the starting sequences
	 * @param regularExpressionsFile - a file containing all the regular expressions to be used as a filter for the inputFasta
	 * @param outputFile
	 * @param logFile - contains info about what was observed and done
	 * @throws Exception
	 */
	private  void doTransform(String inputFasta, String positionCutoffFile, String regularExpressionsFile, String outputFile,
			String logFile) throws Exception {
		endOfLogSummary = new ArrayList<String>();
		matchedRegexAAspreadStats = new HashMap<String, ArrayList<Integer>>();
    	int sequencesCounter = 0;  //count given sequences inside input.fasta
    	int matchedCounter = 0;  //count sequence -> regex matches
		
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
		Path p = Paths.get(outputFile);
		outputDir = p.getParent().toString() + File.separator;//.getFileName().toString();
		
		
		inputFastaFile = new FileReader(inputFasta);
		inputFastaBufferedReader = new BufferedReader(inputFastaFile);
		String curId = "";
		String header_line = "";
		int ending_idx = 0;
		String line;
		String[] a;
		logFileWriter.write("Matches:\n");
		String sequence = null;
		while((line = inputFastaBufferedReader.readLine()) != null){
			if(line.startsWith(">")){
				if(!curId.equals("")){ //if not the first ">"
					sequence = doPositionCutoff(curId, sequence);
									
					//find all matching regexs for this sequence
					ArrayList<String> matchedRegularExpressions = findMatchingRegularExpressions(sequence);
					
					//print output
					if(matchedRegularExpressions.size() == 0){
				    	endOfLogSummary.add(curId + ", unmatched and ommitted from output file");
				    }else{
						matchedCounter++;
				    	//write fasta output and log output (log output is written within the printOutput function
				    	writeOutput(matchedRegularExpressions, curId, sequence, header_line, outputFastaFile, logFileWriter);
				    }
				}
				header_line = line;
				a = line.split(" ");
				//">" is the first character, so substring starting at second character
				curId = a[0].substring(1);
				sequencesCounter++;
				sequence = null;
			}else if(!line.equals("")){  //sequence
				if(sequence == null)
					sequence = line;
				else
					sequence += line;
			}
		}
		/** P1: there is no ">" at the end of the file to trigger another analysis, so do it manually: **/
		sequence = doPositionCutoff(curId, sequence);
		
		//find all matching regexs for this sequence
		ArrayList<String> matchedRegularExpressions = findMatchingRegularExpressions(sequence);
		
		//print output
		if(matchedRegularExpressions.size() == 0){
	    	endOfLogSummary.add(curId + ", unmatched and ommitted from output file");
	    }else{
			matchedCounter++;
	    	//write fasta output and log output (log output is written within the printOutput function
	    	writeOutput(matchedRegularExpressions, curId, sequence, header_line, outputFastaFile, logFileWriter);
	    }
		/**** end P1  ****/
		
		System.out.println(matchedCounter + " of " + sequencesCounter + " sequences matched 1 or more given regular Expressions.");
		
		//write more log info
		logFileWriter.write("\nSummary State:\n");
		for(int i = 0; i < endOfLogSummary.size(); i++)
			logFileWriter.write(endOfLogSummary.get(i) + "\n");

		//histograms of distances between Anchor AAs of each regex
		if("ABRNDCQEGHILKMFPSTWYVZ".contains(distanceAA)){
			logFileWriter.write("\nDistances Observed Between " + distanceAA + "'s in the Given Regular Expressions:\n");
			for(String regex : matchedRegexAAspreadStats.keySet()){
				ArrayList<Integer> values = matchedRegexAAspreadStats.get(regex);
				logFileWriter.write(regex + " ");
				for(int i = 1; i < values.size(); i++){
					if(i > 1)
						logFileWriter.write(", ");
					logFileWriter.write(values.get(i).toString());
				}
				logFileWriter.write("\n");
				drawHistogram(regex, distanceAA, values);
			}
		}
		
		outputFastaFile.close();
		logFileWriter.close();
		
		inputFastaFile.close();
		inputFastaBufferedReader.close();
	}
	
	
	/***
	 * writes to outputFasta and also to logFile
	 * **
	 * @param matchedRegularExpressions
	 * @param curId
	 * @param sequence
	 * @param header_line
	 * @param outputFastaFile
	 * @param logFileWriter
	 * @throws IOException
	 */
	private void writeOutput(ArrayList<String> matchedRegularExpressions, String curId, String sequence, String header_line, BufferedWriter outputFastaFile, BufferedWriter logFileWriter) throws IOException {
		String regex = null;
		String trimmedSeq = null;
		for(int i = 0; i < matchedRegularExpressions.size(); i++){
			regex = matchedRegularExpressions.get(i);
			Pattern pattern = Pattern.compile(regex);
		    Matcher matcher = pattern.matcher(sequence);
		    int j = 1;
		    while (matcher.find()) {
				outputFastaFile.write(modifyHeaderLine(header_line, regex, j++) + "\n");
				trimmedSeq = sequence;
				if(prefix_length > -1 || sufix_length > -1){
		    		int prefix_starting_idx = 0;
		    		int sufix_ending_idx = sequence.length();
					if(!(prefix_length == -1) && !(prefix_length > matcher.start()))
						prefix_starting_idx = matcher.start() - prefix_length;
		
					if(!(sufix_length == -1) && !(sufix_length + matcher.end() > sequence.length()))
						sufix_ending_idx = matcher.end() + sufix_length;
					
					trimmedSeq = sequence.substring(prefix_starting_idx, sufix_ending_idx);
				}
				outputFastaFile.write(trimmedSeq + "\n");
				
				//write log info
			    writeLogInfo(curId, trimmedSeq, regex, logFileWriter);
		      }
		}
	}


	//for a given sequence, return all matching regexes
	private ArrayList<String> findMatchingRegularExpressions(String sequence) {
		ArrayList<String> retVal = new ArrayList<String>();
		Pattern pattern = null;
	    Matcher matcher = null;
		for(int i = 0; i < regularExpressions.size(); i++){
			pattern = Pattern.compile(regularExpressions.get(i));
		    matcher = pattern.matcher(sequence);
		    if(matcher.find())
			    retVal.add(regularExpressions.get(i));
		}
		return retVal;
	}


	private String doPositionCutoff(String curId, String sequence) {
		if(positionCutoffMap != null && positionCutoffMap.containsKey(curId)){
			sequence = sequence.substring(positionCutoffMap.get(curId));
		}
		return sequence;
	}


	private void writeLogInfo(String curId, String sequence, String regex, BufferedWriter logFileWriter) throws IOException{
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(sequence);
	    matcher.find();
		logFileWriter.write(curId + ", " + matcher.start() + ", " + (sequence.length() - matcher.end()) + ", " + regex + ", " + (matcher.end() - matcher.start()) +"\n");
    	endOfLogSummary.add(curId + ", matched with " + regex);
    	
    	//collect stats for average spread between regex specific AA given in --DistanceAA
		if(!matchedRegexAAspreadStats.containsKey(regex)){
			ArrayList<Integer> tmp = new ArrayList<Integer>();
			tmp.add(0);
			matchedRegexAAspreadStats.put(regex, tmp);
		}
		ArrayList<Integer> curValues = matchedRegexAAspreadStats.get(regex);
		curValues.set(0, curValues.get(0) + 1);
		curValues.addAll(getAAgapCollection(sequence.substring(matcher.start(), matcher.end()), distanceAA));  // += matcher.end() - matcher.start() - countAAs(regex);
		matchedRegexAAspreadStats.put(regex, curValues);
	}

	//to a fasta header line, add the matching regex to the line, plus the match  number
	private String modifyHeaderLine(String header_line, String regex, int matchNum) {
		String retVal = "";
		String[] tmp = header_line.split("\\s+");
		retVal = tmp[0] + "|" + regex + "_match" + matchNum;
		for(int i = 1; i < tmp.length; i++)
			retVal += " " + tmp[i];
		return retVal;
	}


	private void drawHistogram(String regex, String AA, ArrayList<Integer> values) {
		Integer[] arr = (Integer[])values.toArray(new Integer[values.size()]);
		double [] dValues = new double[values.size() - 1];
		for(int i = 1; i < values.size(); i++) {
			dValues[i-1] = (double) values.get(i);
		}
       HistogramDataset dataset = new HistogramDataset();
       int bins = getMaxValue(dValues);
       dataset.addSeries("Histogram", dValues, bins);
       String plotTitle = "Histogram for gaps between " + AA + " in the regex " + regex + " which had " + values.get(0) + " matches"; 
       String xaxis = "number";
       String yaxis = "value"; 
       PlotOrientation orientation = PlotOrientation.VERTICAL; 
       boolean show = false; 
       boolean toolTips = false;
       boolean urls = false; 
       JFreeChart chart = ChartFactory.createHistogram( plotTitle, xaxis, yaxis, 
                dataset, orientation, show, toolTips, urls);
       int width = 700;
       int height = 400; 
        try {
        	Random r = new Random();
        	String filename = outputDir + "histogram-" + AA + "--" + getRegexAnchorAAs(regex) + "--" + r.nextInt(100000) + ".PNG";
        	ChartUtilities.saveChartAsPNG(new File(filename), chart, width, height);
        } catch (IOException e) {
        }
		
	}
	private int getMaxValue(double[] dValues) {
		int max = 0;
		int curVal = 0;
		for(int i = 0; i < dValues.length; i++){
			curVal = (int) dValues[i];
			if(curVal > max)
				max = curVal;
		}
		return max;
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
	
	private ArrayList<Integer> getAAgapCollection(String sequence, String AA) {
		ArrayList<Integer> retVal = new ArrayList<Integer>();
		int counter = 0;
		for(int i = 0; i < sequence.length(); i++){
			String seqChar = sequence.substring(i,  i + 1);
			if(i == 0){
				if (!seqChar.equals(AA))
					throw new Error("The first character of the sequence matching string (" + sequence + ") does not match the letter given for --DistanceAA (" + AA + ")!");
			}else if (!seqChar.equals(AA))
				counter++;
			else if (seqChar.equals(AA)){
				retVal.add(counter);
				counter = 0;
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
			else if(!inBrackets && !inCurlyBrackets && "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".contains(curChar))
				if(retVal == null)
					retVal = curChar;
				else
					retVal += curChar;
		}
		return retVal;
	}

	private static void printUsage() {
		System.out.println("USAGE: java FastaTransform --input=pathToInputFasta --position_cutoff=pathToCutoffFile --regular_expression=path.to.multi.reg.exp.file --output=pathToOutputFastaFile --logfile=pathToLogFile [--prefix_length=-1 --sufix_length=-1 --DistanceAA=C]");
		System.out.println("");
		System.out.println("The argument --position_cutoff cannot be used in conjunction with --prefix_length AND/OR --sufix_length.");
	}

}
