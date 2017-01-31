package org.fhcrc.optides.apps.BatFileCreator;

public class BatFileCreator {
	public static String jarLocation = "C:/Program Files/OptidesSoftware/";
	

	public static void main(String[] args) {
		//BatFileCreator --lef.FileType="arw" --FileStartingNumber=3570 
		//--inputdir="C:/user/mbrusniak/input" --SN=.2 --MaxRTForPeak=11 
		//--classification=2 --MaxMAUForPeak=500 --outdir="C:/user/mbrusniak/output"

		String fileType = "";
		Integer fileStartingNumber = 0;
		String inputDir = "C:\\Users\\Admin\\Desktop\\";
		String outDir = "C:\\Users\\Admin\\Desktop\\";
		Double sn_ratio = 0.2;
		Double minRTForPeak = 0.8;
		Double maxRTForPeak = 2.7;
		Double maxMAUForPeak = 1.0;
		Integer classification = 2;

		
		
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
			else if(curParam[0].equals("--MinRTForPeak"))
				minRTForPeak = Double.parseDouble(curParam[1]);
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
		
		/***  FIRST, PRINT DOS2UNIX COMMANDS ***/
		//deal with quotes and ending "/"
		inputDir = inputDir.replace("\"", "");
		outDir = outDir.replace("\"", "");
		fileType = fileType.replace("\"", "");
		if(!inputDir.endsWith("/"))
			inputDir += "/";
		if(!outDir.endsWith("/"))
			outDir += "/";
		
		int curOffset = 0;
		for(int i = 0; i < (8 * 12 + 8*2) * 2 ;i++){
			System.out.println("\"C:/Program Files/OptidesSoftware/dos2unix.exe\" \"" + inputDir + (fileStartingNumber + curOffset) + "." + fileType + "\"");

			if(i == 0)
				curOffset += 5;
			else
				curOffset +=4;
		}
		
		/***  NEXT, PRINT RUN COMMANDS MATCHING UP SAMPLES ***/
		int r_nr_offset = (8*12 + 8*2) * 4;
		Integer h2o0 = 0, h2o1 = 0;
		curOffset = 0;
		for(int i = 0; i < (8 * 12 + 8*2);i++){
			if(i % 14 == 0){
				h2o0 = fileStartingNumber + curOffset;
			}else if(i % 14 == 1){
				h2o1 = fileStartingNumber + curOffset;				
			}else{
				System.out.println("java -jar \"" + jarLocation + "HPLCPeakClassifier.jar\" ^");
				System.out.println("--BLANK_R=\"" + inputDir + ((i % 2 == 0) ? h2o0:h2o1) + "." + fileType + "\" ^");
				System.out.println("--R=\"" + inputDir + (fileStartingNumber + curOffset) + "." + fileType + "\" ^");
				System.out.println("--BLANK_NR=\"" + inputDir + (((i % 2 == 0) ? h2o0:h2o1) + r_nr_offset + ((i % 2 == 0 && i < 14) ? 1:0)) + "." + fileType + "\" ^");
				System.out.println("--NR=\"" + inputDir + (fileStartingNumber + curOffset + r_nr_offset) + "." + fileType + "\" ^");
				System.out.println("--outdir=\"" + outDir + "\" ^");
				System.out.println("--SN=" + sn_ratio + " ^");
				System.out.println("--Classification=" + classification + " ^");
				System.out.println("--MaxRTForPeak=" + maxRTForPeak + " ^");
				System.out.println("--MaxMAUForPeak=" + maxMAUForPeak);
				
				System.out.println();
			}
			
			if(i == 0)
				curOffset += 5;
			else
				curOffset += 4;
		}
	}

	private static void printUsage() {
		System.out.println("BatFileCreator --lef.FileType=\"awr\" --FileStartingNumber=3570 --inputdir=path.to.input.dir --SN=.2 --MaxRTForPeak=11 --classification=2 --MaxMAUForPeak=500 --outdir=path.to.out.dir");
	}

}