package org.fhcrc.optides.apps.CombinatorialPeptideGenerator;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

/*
 * This program takes the parameters and uses them as follows:
 * 	Create all AA permutations of length PeptideLength, writing NProtein proteins
 * per file, each filename beginning with FilePrefix.
 */
public class CombinatorialPeptideGenerator {
	public static String AAstr = "ARNDCEQGHILKMFPSTWYV"; 
	
	private int peptideLength=0, nProtein=0;
	private String filePrefix = "";
	private int counter = 0;
	private PrintWriter writer = null;
	int set = 0;
	
	public CombinatorialPeptideGenerator(int peptideLength, String filePrefix, int nProtein) {
		this.peptideLength = peptideLength;
		this.filePrefix = filePrefix;
		this.nProtein = nProtein;
	}

	public static void main(String[] args) {
		String[] curParam = null;
		int peptideLength=0;
		String filePrefix = "";
		int nProtein = 0;
		for(int i = 0; i < args.length; i++){
			curParam = args[i].split("=");
			if(curParam[0].equals("--PeptideLength"))
				peptideLength = Integer.parseInt(curParam[1]);
			else if(curParam[0].equals("--FilePrefix"))
				filePrefix = curParam[1];
			else if(curParam[0].equals("--NProtein"))
				nProtein = Integer.parseInt(curParam[1]);
			else{
				System.out.println("Unrecognized command line parameter: " + curParam[0]);
				printUsage();
				return;
			}
		}
		if(peptideLength == 0 || filePrefix == "" || nProtein == 0){
			System.out.println("A command line parameter is missing or incorrect.  Please look over your entered parameters: ");

			System.out.println("--PeptideLength: " + peptideLength);
			System.out.println("--FilePrefix: " + filePrefix);
			System.out.println("--NProtein: " + nProtein);
			System.out.println(""); 
			printUsage();
			return;
		}

		CombinatorialPeptideGenerator cpg = new CombinatorialPeptideGenerator(peptideLength, filePrefix, nProtein);
		StringBuffer sb = new StringBuffer();
		cpg.writePermutations(AAstr, peptideLength, sb);
		cpg.closePrintWriter();
		
	}

	public void writePermutations(String input, int length, StringBuffer output) {
        if (length == 0) {
        	if(counter % nProtein == 0){
        		//set writer to next set file
        		try {
        			if(writer != null) writer.close();
        			writer = new PrintWriter(filePrefix + "_Set" + ++set + ".fasta", "UTF-8");
        		} catch (FileNotFoundException e) {
        			e.printStackTrace();
        		} catch (UnsupportedEncodingException e) {
        			e.printStackTrace();
        		}
        	}
            writer.println(">" + output);
            writer.println(output);
            counter++;
        } else {
            for (int i = 0; i < input.length(); i++) {
                output.append(input.charAt(i));
                writePermutations(input, length - 1, output);
                output.deleteCharAt(output.length() - 1);
            }
        }
    }
	
	public void closePrintWriter(){
        if(writer != null) {writer.close(); writer=null;}
	}
	
	private static void printUsage() {
		System.out.println("USAGE: CombinatorialPeptideGenerator --PeptideLength=lengthOfPeptidesInt --FilePrefix=OutputFilesPrefix --NProtein=nOfProteinsPerFile");
	}

}
