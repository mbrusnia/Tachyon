package org.fhcrc.optides.apps.BatFileCreatorForMDT;

/**
 * Created by mbrusnia on 8/13/18.
 */
public class BatFileCreatorForMDT {
    public static String fileSeparator = "/";
    public static String optidesSoftwareDir = "C:" + fileSeparator + "Users" + fileSeparator + "Admin" + fileSeparator + "Desktop" + fileSeparator + "OptidesSoftware" + fileSeparator;

    public static void main(String[] args) {
        //BatFileCreatorForMDT
        //--FileStartingNumber=3078
        //--STD=/Users/mbrusnia/Documents/ToolGuides/OptidesSoftware/HT_UPLC_PeakTracer/HT_UPLC_Standard.arw
        //--NR=/Users/mbrusnia/Input/HT_UPLC_Sample1.arw
        //--MaxMAUForPeak=30
        //--outputdir=/Users/mbrusnia/Output/
        //--filenamePrefix=HT0118_

        Integer fileStartingNumber = 0;
        String fileType = "arw";
        String inputDir = "C:\\Users\\Admin\\Desktop\\HT_UPLC_Export";
        String outDir = "C:\\Users\\Admin\\Desktop\\HT_UPLC_Reports";
        Double maxMAUForPeak = 50.0;
        String filenamePrefix = "HT_UPLC_Export";
        String standardRunFileName = "";


        if(args.length < 1){
            System.out.println("This program REQUIRES 1 parameter to run, and up to 8 other OPTIONAL parameters:");
            printUsage(fileStartingNumber, standardRunFileName, inputDir, outDir, maxMAUForPeak, filenamePrefix);
            return;
        }

        //get input params
        String[] curParam = null;
        for(int i = 0; i < args.length; i++){
            curParam = args[i].split("=");
            if(curParam[0].equals("--FileStartingNumber"))
                fileStartingNumber = Integer.parseInt(curParam[1]);
            else if(curParam[0].equals("--standard"))
                standardRunFileName = curParam[1];
            else if(curParam[0].equals("--inputdir"))
                inputDir = curParam[1];
            else if(curParam[0].equals("--MaxMAUForPeak"))
                maxMAUForPeak = Double.parseDouble(curParam[1]);
            else if(curParam[0].equals("--outdir"))
                outDir = curParam[1];
            else if(curParam[0].equals("--filenamePrefix"))
                filenamePrefix = curParam[1];
            else{
                System.out.println("Unrecognized command line parameter: " + curParam[0]);
                printUsage(fileStartingNumber, standardRunFileName, inputDir, outDir,
                        maxMAUForPeak, filenamePrefix);
                return;
            }
        }

        if(fileStartingNumber == 0 || standardRunFileName.equals("")){
            System.out.println("Required Parameter \"FileStartingNumber\" missing:");
            printUsage(fileStartingNumber, standardRunFileName, inputDir, outDir, maxMAUForPeak, filenamePrefix);
            return;
        }

        /***  FIRST, PRINT DOS2UNIX COMMANDS ***/
        //deal with quotes and ending "/"
        inputDir = inputDir.replace("\"", "");
        outDir = outDir.replace("\"", "");
        fileType = fileType.replace("\"", "");
        if(!inputDir.endsWith(fileSeparator))
            inputDir += fileSeparator;
        if(!outDir.endsWith(fileSeparator))
            outDir += fileSeparator;
        System.out.println("\"" + optidesSoftwareDir + "dos2unix.exe\" \"" + standardRunFileName + "\"");
        int currFileNumber = fileStartingNumber;
        for(int i = 0; i < 96 ;i++){
            System.out.println("\"" + optidesSoftwareDir + "dos2unix.exe\" \"" + inputDir + filenamePrefix + (currFileNumber) + "." + fileType + "\"");
            if(i == 0){
                currFileNumber += 6;
            }
            else {
                currFileNumber += 5;
            }
        }
        for(int i = 0; i < 96 ;i++) {
            /***  NEXT, PRINT RUN COMMANDS MATCHING UP SAMPLES ***/
            System.out.println("java -jar \"" + optidesSoftwareDir + "UPLCPeakTracer.jar\" ^");
            System.out.println("--STD=\"" + standardRunFileName + "\" ^");
            System.out.println("--NR=\"" + inputDir + filenamePrefix + (currFileNumber) + "." + fileType + "\" ^");
            System.out.println("--outdir=\"" + outDir + "\" ^");
            System.out.println("--MaxMAUForPeak=" + maxMAUForPeak + " ^");
            System.out.println();
            if(i == 0){
                currFileNumber += 6;
            }
            else {
                currFileNumber += 5;
            }
        }
    }

    private static void printUsage(Integer fileStartingNumber, String standardFileName, String inputDir, String outDir, Double maxMAUForPeak, String filenamePrefix) {
        System.out.println("BatFileCreatorMDT --FileStartingNumber=3570 --standard=C:\\User\\mbrusnia\\Desktop\\BioRad_Standardrun.arw --inputdir=path.to.input.dir --MaxMAUForPeak=0.8 --outdir=path.to.out.dir --filenamePrefix=XYZ");
        System.out.println();
        System.out.println("Only --FileStartingNumber and --standard are REQUIRED.  All other parameters are optional and have default values.  These are the currently set values:");
        System.out.println("--FileStartingNumber=\t"+fileStartingNumber);
        System.out.println("--standard=\t" + standardFileName);
        System.out.println("--inputdir=\t"+inputDir);
        System.out.println("--outdir=\t"+outDir);
        System.out.println("--MaxMAUForPeak=\t"+maxMAUForPeak);
        System.out.println("--filenamePrefix=\t"+filenamePrefix);
        System.out.println();
    }
}
