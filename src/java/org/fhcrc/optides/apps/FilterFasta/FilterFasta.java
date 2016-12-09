package org.fhcrc.optides.apps.FilterFasta;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;

public class FilterFasta {

	public static void main(String[] args) {
		//FilterFasta.java -input_fasta=input.fasta -filter_criteria_col_name=percIdntity 
		//-filter_input=Blast.csv -ceiling_value=10.0 -floor_value=-1 -output_fasta=filtered.fasta

		String inputFasta = "";
		String outputFasta = "";
		String filterInput = "";
		String filterCriteriaColName = "";
		Double ceilingValue = 0.0;
		Double floorValue = 0.0;
		
		if(args.length != 6){
			System.out.println("This program requires six parameters to Run.  Please see the following USAGE:");
			printUsage();
			return;
		}
		//get input params
		String[] curParam = null;
		for(int i = 0; i < args.length; i++){
			curParam = args[i].split("=");
			if(curParam[0].equals("-input_fasta"))
				inputFasta = curParam[1];
			else if(curParam[0].equals("-filter_criteria_col_name"))
				filterCriteriaColName = curParam[1];
			else if(curParam[0].equals("-filter_input"))
				filterInput = curParam[1];
			else if(curParam[0].equals("-ceiling_value"))
				ceilingValue = Double.parseDouble(curParam[1]);
			else if(curParam[0].equals("-floor_value"))
				floorValue = Double.parseDouble(curParam[1]);
			else if(curParam[0].equals("-output_fasta"))
				outputFasta = curParam[1];
			else{
				System.out.println("Unrecognized command line parameter: " + curParam[0]);
				printUsage();
				return;
			}			
		}
		
		try {
			FilterFasta.doFilter(inputFasta, outputFasta, filterInput, filterCriteriaColName, ceilingValue, floorValue);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	static int doFilter(String inputFasta,	String outputFasta, String filterInput, String filterCriteriaColName, Double ceilingValue, Double floorValue) throws IOException{
		//prepare the reading
		FileReader fastaReader = new FileReader(inputFasta);
		BufferedReader fastaBufferedReader = new BufferedReader(fastaReader);
		FileReader filterReader = new FileReader(filterInput);
		BufferedReader filterInputBufferedReader = new BufferedReader(filterReader);
		
		String line = null;
		  
		//prepare the writing
		File fout1 = new File(outputFasta);
		FileOutputStream fos1 = new FileOutputStream(fout1);
		BufferedWriter outputFastaFile = new BufferedWriter(new OutputStreamWriter(fos1));
		
		//get the index of filterCriteriaColName
		line = filterInputBufferedReader.readLine();
		String[] a = line.split(",");
		int colFilterIdx = 0;
		for(; colFilterIdx < a.length; colFilterIdx++)
			if(a[colFilterIdx].equals(filterCriteriaColName))
				break;
		
		//find all BLAST matches that pass given filter
		HashMap<String, String> map = new HashMap<String, String>();
		while ((line = filterInputBufferedReader.readLine()) != null) {
			a = line.split(",");
			Double filterVal = Double.parseDouble(a[colFilterIdx]);
            if(		(floorValue==-1 && filterVal <= ceilingValue) ||
            		(ceilingValue == -1 && filterVal >= floorValue) ||
            		(filterVal >= floorValue && filterVal <= ceilingValue)
            	)
            	map.put(a[0], a[colFilterIdx]);
        }
		filterInputBufferedReader.close();
		
		//now print all the matching records in the inputFasta file
		boolean writing = false;
		while ((line = fastaBufferedReader.readLine()) != null) {
			if(line.startsWith(">")){
				a = line.split(" ");
				//">" is the first character, so substring starting at second character
				if(map.containsKey(a[0].substring(1)))
					writing = true;
				else
					writing = false;
			}
			
			if(writing == true)
				outputFastaFile.write(line + "\n");
		}
		fastaBufferedReader.close();
		outputFastaFile.close();
		System.out.println("Number of proteins that are filtered: " + map.size());
		return 0;
	}

	static void printUsage(){
		System.out.println("USAGE: java FilterFasta -input_fasta=input.fasta -filter_criteria_col_name=percIdntity -filter_input=Blast.csv -ceiling_value=10.0 -floor_value=-1 -output_fasta=filtered.fasta");
	}
}
