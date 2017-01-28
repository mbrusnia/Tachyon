package org.fhcrc.optides.apps.BatFileCreator;

public class BatFileCreator {

	public static void main(String[] args) {
		//BatFileCreator --lef.FileType=”arw” --FileStartingNumber=3570 
		//--inputdir=”C:/user/mbrusniak/input” --SN=.2 --MaxRTForPeak=11 
		//--classification=2 --MaxMAUForPeak=500 --outdir=”C:/user/mbrusniak/output”

		String fileType = "";
		Integer fileStartingNumber = 3570;
		String inputDir = "";
		String outDir = "";
		Double sn_ratio = 0.0;
		Double maxRTForPeak = 0.0;
		Double maxMAUForPeak = 0.0;
		Integer classification = 0;
		
		
		
		if(args.length != 8){
			System.out.println("This program requires 8 parameters to run:");
			printUsage();
			return;
		}
		
		//get input params
		String[] curParam = null;
		for(int i = 0; i < args.length; i++){
			curParam = args[i].split("=");
			if(curParam[0].equals("--lef.FileType"))
				fileType = curParam[1];
			else if(curParam[0].equals("--FileStartingNumber"))
				fileStartingNumber = Integer.parseInt(curParam[1]);
			else if(curParam[0].equals("--inputdir"))
				inputDir = curParam[1];
			else if(curParam[0].equals("--SN"))
				sn_ratio = Double.parseDouble(curParam[1]);
			else if(curParam[0].equals("--MaxRTForPeak"))
				maxRTForPeak = Double.parseDouble(curParam[1]);
			else if(curParam[0].equals("--MaxMAUForPeak"))
				maxMAUForPeak = Double.parseDouble(curParam[1]);		
			else if(curParam[0].equals("--outdir"))
				outDir = curParam[1];	
			else if(curParam[0].equals("--classification"))  
				classification = Integer.parseInt(curParam[1]);
			else{
				System.out.println("Unrecognized command line parameter: " + curParam[0]);
				printUsage();
				return;
			}
		}
		
		System.out.println(fileStartingNumber);
		for(int i = 0; i < 4 * 7; i++){
			
		}
	}

	private static void printUsage() {
		System.out.println("BatFileCreator --lef.FileType=\"awr\" --FileStartingNumber=3570 --inputdir=path.to.input.dir --SN=.2 --MaxRTForPeak=11 --classification=2 --MaxMAUForPeak=500 --outdir=path.to.out.dir");
	}

}
