package org.fhcrc.optides.apps.MHCII_Processor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MHCII_Processor {
	//passed into program from command line:
	private String inputFile = "";
	private String weightFile = "";
	private String outputFile = "";

	//This is the map containing all identity -> MHCIIProperties_objects
	private Map<String, ArrayList<MHCIIProperties>> theMap;

	//This map contains all allowable values for MHCII alleles.  Initiated in the
	//constructor.  Add new values there (in the future).
	private Map<String, Double> AllelesAndWeights;
	
	public MHCII_Processor(String inputFile, String weightFile, String outputFile) {
		this.inputFile = inputFile;
		this.weightFile = weightFile;
		this.outputFile = outputFile;
		
		theMap = new HashMap<String, ArrayList<MHCIIProperties>>();
		
		AllelesAndWeights = new HashMap<String, Double>();
		AllelesAndWeights.put("H-2-IAb", 1.0);
		AllelesAndWeights.put("H-2-IAd", 1.0);
		AllelesAndWeights.put("HLA-DPA10103-DPB10201", 1.0);
		AllelesAndWeights.put("HLA-DPA101-DPB10401", 1.0);
		AllelesAndWeights.put("HLA-DPA10201-DPB10101", 1.0);
		AllelesAndWeights.put("HLA-DPA10201-DPB10501", 1.0);
		AllelesAndWeights.put("HLA-DPA10301-DPB10402", 1.0);
		AllelesAndWeights.put("HLA-DPB10301-DPB10401", 1.0);
		AllelesAndWeights.put("HLA-DQA10101-DQB10501", 1.0);
		AllelesAndWeights.put("HLA-DQA10102-DQB10602", 1.0);
		AllelesAndWeights.put("HLA-DQA10301-DQB10302", 1.0);
		AllelesAndWeights.put("HLA-DQA10401-DQB10402", 1.0);
		AllelesAndWeights.put("HLA-DQA10501-DQB10201", 1.0);
		AllelesAndWeights.put("HLA-DQA10501-DQB10301", 1.0);
		AllelesAndWeights.put("HLA-DRB10101", 1.0);
		AllelesAndWeights.put("HLA-DRB10301", 1.0);
		AllelesAndWeights.put("HLA-DRB10401", 1.0);
		AllelesAndWeights.put("HLA-DRB10404", 1.0);
		AllelesAndWeights.put("HLA-DRB10405", 1.0);
		AllelesAndWeights.put("HLA-DRB10701", 1.0);
		AllelesAndWeights.put("HLA-DRB10802", 1.0);
		AllelesAndWeights.put("HLA-DRB10901", 1.0);
		AllelesAndWeights.put("HLA-DRB11101", 1.0);
		AllelesAndWeights.put("HLA-DRB11302", 1.0);
		AllelesAndWeights.put("HLA-DRB11501", 1.0);
		AllelesAndWeights.put("HLA-DRB30101", 1.0);
		AllelesAndWeights.put("HLA-DRB40101", 1.0);
		AllelesAndWeights.put("HLA-DRB50101", 1.0);
		
		//these are actually BindLevels, not alleles.  but we are using them in the same way
		//to calculate the score, so we put them in this map for convenience
		AllelesAndWeights.put("SB", 1.0);
		AllelesAndWeights.put("SW", 1.0);
		AllelesAndWeights.put("WB", 1.0);
		AllelesAndWeights.put("", 1.0);
	}


	public static void main(String[] args) {
		//call: MHCII_Processor --inputfile=Project30_example.fasta.out --weightfile=projectWeight.csv 
		//--outputfile=/Users/mbrusnia/Desktop/Project30_Weight.csv
		String inputFile = "";
		String weightFile = "";
		String outputFile = "";
		
		if(args.length !=3){
			System.out.println("This program requires three parameters to Run.  Please see the following USAGE:");
			printUsage();
			return;
		}
		//get input params
		String[] curParam = null;
		for(int i = 0; i < args.length; i++){
			curParam = args[i].split("=");
			if(curParam[0].equals("--inputfile"))
				inputFile = curParam[1];
			else if(curParam[0].equals("--weightfile"))
				weightFile = curParam[1];
			else if(curParam[0].equals("--outputfile"))
				outputFile = curParam[1];
			else{
				System.out.println("Unrecognized command line parameter: " + curParam[0]);
				printUsage();
				return;
			}			
		}
		
		MHCII_Processor mp = new MHCII_Processor(inputFile, weightFile, outputFile);
		try {
			mp.readWeightFile();
			mp.readInputFile();
			mp.writeOutputFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private void writeOutputFile() throws IOException {
		File fout1 = new File(outputFile);
		FileOutputStream fos1 = new FileOutputStream(fout1);
		BufferedWriter outputFileWriter = new BufferedWriter(new OutputStreamWriter(fos1));

		//header line
		outputFileWriter.write("Identity" + "," + "Score" + "\n");
		
		//write out all data
		for (String key : theMap.keySet()) {
			outputFileWriter.write(key + "," + getScore(key) + "\n");
		}

		outputFileWriter.close();
	}


	private Double getScore(String identity) {
		ArrayList<MHCIIProperties> list = theMap.get(identity);
		Double sum = 0.0;
		for(int i = 0; i < list.size(); i++)
			sum += list.get(i).score;
		return sum;
	}


	//reads the file containing all the allele weights to use for the score calculations
	private void readWeightFile() throws IOException {
		//prepare the reading
		FileReader weightFileReader = new FileReader(weightFile);
		BufferedReader weightfileBufferedReader = new BufferedReader(weightFileReader);

		//parseLine is true when we are on a line of data that needs to be parsed into an MHCIIProperties object
		boolean parseLine = false;
		String line = null;
		String[] lineElements = null;
		while ((line = weightfileBufferedReader.readLine()) != null) {
			lineElements = line.split(",");
			
			if(!AllelesAndWeights.containsKey(lineElements[0]))
				throw new Error("Error: Your Weightsfile data contains an unknown allele: " + lineElements[0] + ". At the present time, this program does not accept this allele.  Please contact the administrator.");
			
			AllelesAndWeights.put(lineElements[0], Double.parseDouble(lineElements[1]));
		}
		weightFileReader.close();
		weightfileBufferedReader.close();
	}
		
	//reads the file containing all the MHCII hits and identities, etc.
	private void readInputFile() throws IOException {
		//prepare the reading
		FileReader inputFileReader = new FileReader(inputFile);
		BufferedReader inputfileBufferedReader = new BufferedReader(inputFileReader);

		//parseLine is true when we are on a line of data that needs to be parsed into an MHCIIProperties object
		boolean parseLine = false;
		String line = null;
		while ((line = inputfileBufferedReader.readLine()) != null) {
			String[] lineElements = null;
			if(parseLine){
				lineElements = line.split("\\s+");
				if(lineElements[0].equals(""))
					lineElements = Arrays.copyOfRange(lineElements, 1, lineElements.length);
				String allele = lineElements[0];
				Integer pos = Integer.parseInt(lineElements[1]);          
				String peptide = lineElements[2];       
				String core = lineElements[3]; 
				Double log50k = Double.parseDouble(lineElements[4]); 
				Double affinity_nM = Double.parseDouble(lineElements[5]);
				String bindLevel = "";
				Double randomPercent = 0.0;    
				String identity = "";
				
				//When bindlevel is "", the split function above returns 1 less element in
				//the array.  So here we account for that case
				if(lineElements.length == 8){
					randomPercent = Double.parseDouble(lineElements[6]);
					identity = lineElements[7];
				}else{
					bindLevel = lineElements[6];
					randomPercent = Double.parseDouble(lineElements[7]);
					identity = lineElements[8];
				}

				if(!AllelesAndWeights.containsKey(allele))
					throw new Error("Error: Your input data contains an unknown allele: " + identity + ". At the present time, this program does not accept this allele.  Please contact the administrator.");
				if(!theMap.containsKey(identity))
					theMap.put(identity, new ArrayList<MHCIIProperties>());
				
				MHCIIProperties mhcp = new MHCIIProperties(allele, pos, peptide, core, log50k, affinity_nM, bindLevel, randomPercent, identity, AllelesAndWeights.get(allele), AllelesAndWeights.get(bindLevel));
				theMap.get(identity).add(mhcp);
				
				parseLine = false;
			}else{
				lineElements = line.split("\\s+");
				if(lineElements.length > 1)
					if(lineElements[1].equals("Allele")){
						line = inputfileBufferedReader.readLine();
						if(line.startsWith("----------"))
								parseLine = true;
					}
			}
		}
		inputFileReader.close();
		inputfileBufferedReader.close();
	}


	private static void printUsage() {
		System.out.println("USAGE: MHCII_Processor --inputfile=example.fasta.out --weightfile=projectWeight.csv --outputfile=out_Weights.csv");
		
	}

}
