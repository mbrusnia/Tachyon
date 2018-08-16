package org.fhcrc.optides.apps.UPLCPeakTracerApp;

/**
 * Created by mbrusnia on 8/13/18.
 */
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

import org.jfree.chart.ChartUtilities;

public class UPLCPeakTracer {
    private static String nrFilename = "";
    private static String stdFilename = "";
    private static double maxMAUForPeak = 50.0;
    private static String outputdir = "";
    private static String sampleName ="";
    public static void main(String[] args) throws IOException {
        // read file
        String[] curParam = null;
        for (int i = 0; i < args.length; i++) {
            curParam = args[i].split("=");
            if (curParam[0].equals("--STD")) {
                stdFilename = curParam[1];
            } else if (curParam[0].equals("--NR")) {
                nrFilename = curParam[1];
            } else if (curParam[0].equals("--MaxMAUForPeak")) {
                maxMAUForPeak = Double.parseDouble(curParam[1]);
            } else if (curParam[0].equals("--outputdir")) {
                outputdir = curParam[1];
            } else {
                System.out.println("invalid input");
            }
        }
        if(nrFilename == "" || stdFilename == "" || outputdir == ""){
            System.out.println("A command line parameter is missing or incorrect.  Please look over your entered parameters.");
            printUsage();
            return;
        }

        // get peaks
        ArrayList<HPLCPeakComparable> fivePeaks = pickPeaks(stdFilename);
        ArrayList<HPLCPeakComparable> HPCLPeakList = acquireData(nrFilename);
        Collections.sort(fivePeaks);
        // draw peeks on the image
        XYLineChart_AWT chart = new XYLineChart_AWT(sampleName, sampleName, maxMAUForPeak,
                HPCLPeakList, fivePeaks);
        // save image
        int width = 640; /* Width of the image */
        int height = 480; /* Height of the image */
        File XYChart = new File(outputdir + getFilenameFromFullPath(nrFilename).replace(".arw", ".jpg"));
        ChartUtilities.saveChartAsJPEG(XYChart, chart.getChart(), width, height);
    }

    private static String getFilenameFromFullPath(String nrFilename2) {
        return nrFilename2.substring(nrFilename2.lastIndexOf("/") + 1);
    }

    private static void printUsage() {
        System.out.println("USAGE: UPLCPeakClassifier --NR=pathToNRarwFile --STD=pathToStandardArwFile --outdir=pathToOutputDir --MaxMAUForPeak=upperYvalueOnChart");
        System.out.println("");
        System.out.println("note: MaxMAUForPeak is defaulted to 50.0 if not entered.");
    }

    // data acquisition
    public static ArrayList<HPLCPeakComparable> acquireData(String filename)
            throws FileNotFoundException {
        ArrayList<HPLCPeakComparable> list = new ArrayList<HPLCPeakComparable>();
        File file = new File(filename);
        Scanner sc = new Scanner(file);
        System.out.println(sc.nextLine());
        sampleName = sc.nextLine();
        //remove input quotes from the sample name
        UPLCPeakTracer.sampleName = sampleName.substring(1,sampleName.length()-1);
        System.out.println(UPLCPeakTracer.sampleName);

        while (sc.hasNextLine()) {
            String dataLine = sc.nextLine();
            String[] splited = dataLine.split("\\s+");
            Double xCoord = Double.parseDouble(splited[0]);
            Double yCoord = Double.parseDouble(splited[1]);

            list.add(new HPLCPeakComparable(xCoord, yCoord));
        }
        return list;
    }

    public static ArrayList<HPLCPeakComparable> pickPeaks(String filename)
            throws FileNotFoundException {
        ArrayList<HPLCPeakComparable> list = acquireData(filename);
        ArrayList<HPLCPeakComparable> returnList = new ArrayList<HPLCPeakComparable>();
        for (int i = 1; i < list.size() - 1; i++) {
            if ((list.get(i).getAu() > (list.get(i - 1).getAu()) && list.get(i)
                    .getAu() > list.get(i + 1).getAu())) {
                returnList.add(list.get(i));
            }
        }
        Collections.sort(returnList);
        for (int i = 0; i < returnList.size() - 1; i++) {
            if (returnList.get(i).getRt() - returnList.get(i + 1).getRt() < 0.1) {
                returnList.remove(i + 1);
            }
        }
        return returnList;
    }

}

