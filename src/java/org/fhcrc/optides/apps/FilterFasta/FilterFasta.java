package org.fhcrc.optides.apps.FilterFasta;

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
import java.util.Map.Entry;

public class FilterFasta {
	
	public static void main(String[] args) {
		//FilterFasta.java -input_fasta=input.fasta -filter_criteria_col_name=percIdntity 
		//-filter_input=Blast.csv -ceiling_value=10.0 -floor_value=-1 -output_fasta=filtered.fasta
		//-exclude_fasta=path/to/exclusionList -stats_output_file=full.path.to.desired.output.file

		String inputFasta = "";
		String outputFasta = "";
		String filterInput = "";
		String excludeFasta = "";
		String outputStats = "";
		String filterCriteriaColName = "";
		Double ceilingValue = 0.0;
		Double floorValue = 0.0;
		boolean max=false;
		boolean min=false;
		
		if(args.length < 7 || args.length > 9){
			System.out.println("This program requires six to eight parameters to Run.  Please see the following USAGE:");
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
			else if(curParam[0].equals("-stats_output_file"))
				outputStats = curParam[1];
			else if(curParam[0].equals("-exclude_fasta"))
				excludeFasta = curParam[1];
			else if(curParam[0].equals("-max")){
				if(min==true){
					System.out.println("You just blew my mind.  I cannot do both 'min' and 'max' at the same time.  Please choose one or the other and try again.");
					return;
				}
				max = true;
			}else if(curParam[0].equals("-min")){
				if(max==true){
					System.out.println("You just blew my mind.  I cannot do both 'min' and 'max' at the same time.  Please choose one or the other and try again.");
					return;
				}
				min = true;
			}else{
				System.out.println("Unrecognized command line parameter: " + curParam[0]);
				printUsage();
				return;
			}			
		}
		
		try {
			FilterFasta.doFilter(inputFasta, outputFasta, filterInput, excludeFasta, outputStats, filterCriteriaColName, ceilingValue, floorValue, max, min);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	static int doFilter(String inputFasta,	String outputFasta, String filterInput, String excludeFasta, String outputStats, String filterCriteriaColName, Double ceilingValue, Double floorValue, boolean max, boolean min) throws IOException{
		//prepare the reading
		FileReader filterReader = new FileReader(filterInput);
		BufferedReader filterInputBufferedReader = new BufferedReader(filterReader);
		
		String line = null;
		
		//get the index of filterCriteriaColName
		line = filterInputBufferedReader.readLine();
		String[] a = line.split(",");
		int colFilterIdx = 0;
		for(; colFilterIdx < a.length; colFilterIdx++)
			if(a[colFilterIdx].equals(filterCriteriaColName))
				break;
		
		if(colFilterIdx == a.length){
			filterInputBufferedReader.close();
			throw new Error("The specified filter_criteria_col_name (" + filterCriteriaColName + ") was not found in the Blast output table heading.");
		}
		
		//find all BLAST matches that pass given filter
		//prepare the reading
		FileReader fastaReader = new FileReader(inputFasta);
		BufferedReader fastaBufferedReader = new BufferedReader(fastaReader);
		
		HashMap<String, ArrayList<String>> subjectIdMap = new HashMap<String, ArrayList<String>>();
		String curQueryId = "";
		Double maxValue = Double.MIN_VALUE;
		Double minValue = Double.MAX_VALUE;
		Double filterVal = null;
		ArrayList<String[]> curQueryStore = new ArrayList<String[]>();
		while ((line = filterInputBufferedReader.readLine()) != null) {
			a = line.split(",");
			filterVal = Double.parseDouble(a[colFilterIdx]);
			
            if(		(floorValue==-1 && filterVal <= ceilingValue) ||
            		(ceilingValue == -1 && filterVal >= floorValue) ||
            		(filterVal >= floorValue && filterVal <= ceilingValue)
            	){

    			if(max || min){
    				if(!a[0].equals(curQueryId)){
    					for(int i = 0; i < curQueryStore.size(); i++){
    						String id = curQueryStore.get(i)[0];
    						Double val = Double.parseDouble(curQueryStore.get(i)[1]);
    					
	    					if(min && val.equals(minValue)){
	    						if(subjectIdMap.containsKey(id))
	    							subjectIdMap.get(id).add(val.toString());
	    						else
	    							subjectIdMap.put(id, new ArrayList<String>(Arrays.asList(val.toString())));
	    					}else if(max && val.equals(maxValue)){
	    						if(subjectIdMap.containsKey(id))
	    							subjectIdMap.get(id).add(val.toString());
	    						else
	    							subjectIdMap.put(id, new ArrayList<String>(Arrays.asList(val.toString())));
	        				}
    					}
    					
	    				curQueryId = a[0];
	    				curQueryStore = new ArrayList<String[]>();
	    				maxValue = Double.MIN_VALUE;
	    				minValue = Double.MAX_VALUE;
	    				if(!a[1].equals(a[0])){ //make sure we do not have the identical match being counted as a match
	    					if(min)
		    					minValue = filterVal;
		    				else if(max)
		    					maxValue = filterVal;
		    				curQueryStore.add(new String[] {a[1], filterVal.toString()});
	    				}
        			}else if(!a[1].equals(a[0])){  //make sure we do not have the identical match being counted as a match
        				if(max && maxValue < filterVal){
        					maxValue = filterVal;
        				}else if(min && minValue > filterVal){
        					minValue = filterVal;
        				}
        				curQueryStore.add(new String[] {a[1], filterVal.toString()});
        			}
    			}else if(!a[1].equals(a[0])){
	            	if(!subjectIdMap.containsKey(a[1])){
	            		subjectIdMap.put(a[1], new ArrayList<String>());
	            	}
	            	subjectIdMap.get(a[1]).add(a[colFilterIdx]);
    			}
            }
        }
		//take care of the last queryId, which didn't get a chance to finish above because EOF
		if(curQueryStore.size() > 0){
			for(int i = 0; i < curQueryStore.size(); i++){
				String id = curQueryStore.get(i)[0];
				Double val = Double.parseDouble(curQueryStore.get(i)[1]);
			
				if(min && val.equals(minValue)){
					if(subjectIdMap.containsKey(id))
						subjectIdMap.get(id).add(val.toString());
					else
						subjectIdMap.put(id, new ArrayList<String>(Arrays.asList(val.toString())));
				}else if(max && val.equals(maxValue)){
					if(subjectIdMap.containsKey(id))
						subjectIdMap.get(id).add(val.toString());
					else
						subjectIdMap.put(id, new ArrayList<String>(Arrays.asList(val.toString())));
				}
			}
		}
		filterInputBufferedReader.close();
		
		//prepare exclusion list from exclude_fasta option
		HashMap<String, String> exclusionMap = new HashMap<String, String>();
		if(!excludeFasta.equals("")){
			FileReader fastaReader2 = new FileReader(excludeFasta);
			BufferedReader fastaBufferedReader2 = new BufferedReader(fastaReader2);
			
			while ((line = fastaBufferedReader2.readLine()) != null) {
				if(line.startsWith(">")){
					a = line.split(" ");
					//">" is the first character, so substring starting at second character
					exclusionMap.put(a[0].substring(1), a[0].substring(1));
				}		
			}
			fastaBufferedReader2.close();
		}
		
		//now print all the matching records in the inputFasta file
		//prepare the writing
		File fout1 = new File(outputFasta);
		FileOutputStream fos1 = new FileOutputStream(fout1);
		BufferedWriter outputFastaFile = new BufferedWriter(new OutputStreamWriter(fos1));
		boolean writing = false;
		while ((line = fastaBufferedReader.readLine()) != null) {
			if(line.startsWith(">")){
				a = line.split(" ");
				//">" is the first character, so substring starting at second character
				if(subjectIdMap.containsKey(a[0].substring(1)) && !exclusionMap.containsKey(a[0].substring(1)))
					writing = true;
				else
					writing = false;
			}
			
			if(writing == true)
				outputFastaFile.write(line + "\n");
		}
		fastaBufferedReader.close();
		outputFastaFile.close();
		System.out.println("Number of proteins that are filtered: " + subjectIdMap.size());
		
		//now write out the stats
		File fout2 = new File(outputStats);
		FileOutputStream fos2 = new FileOutputStream(fout2);
		BufferedWriter outputStatsFile = new BufferedWriter(new OutputStreamWriter(fos2));
		Iterator<Entry<String, ArrayList<String>>> it = subjectIdMap.entrySet().iterator();
    	outputStatsFile.write("subjectId\t" + filterCriteriaColName + "\n");
	    while (it.hasNext()) {
	        Map.Entry<String, ArrayList<String>> pair = it.next();
	        ArrayList<String> filterColValues = pair.getValue();
	        for(int i = 0; i < filterColValues.size(); i++)
	        	outputStatsFile.write(pair.getKey() + "\t" + filterColValues.get(i) + "\n");
	        it.remove(); // avoids a ConcurrentModificationException
	    }
		outputStatsFile.close();
		return 0;
	}

	static void printUsage(){
		System.out.println("USAGE: java FilterFasta -input_fasta=input.fasta -filter_criteria_col_name=percIdntity -filter_input=Blast.csv -ceiling_value=10.0 -floor_value=-1 -output_fasta=filtered.fasta -exclude_fasta=path/to/exclusionList -stats_output_file=full.path.to.desired.output.file");
	}
}
