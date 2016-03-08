/**
 * 
 */
package org.fhcrc.optides.apps.FastaSplit;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

/**
 * @author hramos, 3/7/2016
 * 
 * This class will split a FASTA file on keywords.  i.e., given a list
 * of keywords, it places all the sequences which contain those keywords
 * in one (new) file and all the sequences which do not contain any of
 * those keywords in another file.
 */
public class FastaSplit {

	private String inputFile;
	private String keywordFile;
	private String outputSuffix;
	
	private ArrayList<String> keywords;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if(args.length != 3){
			System.out.println("USAGE: FastaSplit -input=Library.fasta -keyword=keyword.txt -output_surfix=contain");
			return;
		}
		String inputFile = null; 
		String keywordFile = null;
		String outputSuffix = null;
		for(int i = 0; i < args.length; i++){
			if(args[i].split("=")[0].equals("-input"))
				inputFile = args[i].split("=")[1];
			if(args[i].split("=")[0].equals("-keyword"))
				keywordFile = args[i].split("=")[1];
			if(args[i].split("=")[0].equals("-output_surfix"))
				outputSuffix = args[i].split("=")[1];
		}
		FastaSplit fs = new FastaSplit(inputFile, keywordFile, outputSuffix);

		String outputFile1 = inputFile.substring(0, inputFile.lastIndexOf(".")) + 
				"_" + outputSuffix + inputFile.substring(inputFile.lastIndexOf("."), inputFile.length());
		String outputFile2 = inputFile.substring(0, inputFile.lastIndexOf(".")) + 
				"_not" + outputSuffix + inputFile.substring(inputFile.lastIndexOf("."), inputFile.length());
		
		try {
			fs.parseKeywordFile(fs.getKeywordFile());
			fs.fastaSplit(fs.getInputFile(), outputFile1, outputFile2);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void fastaSplit(String inputFile2, String outputContains, String outputNotContains) throws IOException {
		//prepare the reading
		FileReader fileReader = new FileReader(inputFile2);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        String line = null;
        		
		//prepare the writing
		File fout1 = new File(outputContains);
		File fout2 = new File(outputNotContains);
		FileOutputStream fos1 = new FileOutputStream(fout1);
		FileOutputStream fos2 = new FileOutputStream(fout2);

		BufferedWriter bw1 = new BufferedWriter(new OutputStreamWriter(fos1));
		BufferedWriter bw2 = new BufferedWriter(new OutputStreamWriter(fos2));
	 
		BufferedWriter curWriter = null;
		while ((line = bufferedReader.readLine()) != null) {
            if(line.startsWith(">")){
            	//default to bw2
    			curWriter = bw2;
    			
    			//unless we match a keyword
            	for(int i = 0; i < keywords.size(); i++){
            		if(line.contains(keywords.get(i))){
            			curWriter = bw1;
            			break;
            		}
            	}
            }
            curWriter.write(line);
            curWriter.newLine();
        }
		curWriter = null;
		bw1.close();
		bw2.close();
        bufferedReader.close();
	}

	private FastaSplit(String inputFile, String keywordFile, String outputSuffix) {
		this.inputFile = inputFile;
		this.keywordFile = keywordFile;
		this.outputSuffix = outputSuffix;
	}

	private void parseKeywordFile(String kwf) throws IOException {
		keywords = new ArrayList<String>();
		FileReader fileReader = new FileReader(kwf);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        String line = null;
        while ((line = bufferedReader.readLine()) != null) {
        	if(!line.equals(""))
        		keywords.add(line);
        }
        bufferedReader.close();
	}

	private String getKeywordFile() {
		return keywordFile;
	}

	private String getInputFile() {
		return inputFile;
	}

}
