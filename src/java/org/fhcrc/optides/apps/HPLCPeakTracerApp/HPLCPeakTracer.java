package org.fhcrc.optides.apps.HPLCPeakTracerApp;

/**
 * Created by mbrusnia on 8/13/18.
 */
import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import org.jfree.chart.ChartUtilities;

public class HPLCPeakTracer {
    private static String nrFilename = "";
    private static String stdFilename = "";
    private static double maxMAUForPeak = 50.0;
    private static String outputdir = "";
    private static String sampleName = "";

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
        if (nrFilename == "" || stdFilename == "" || outputdir == "") {
            System.out.println("A command line parameter is missing or incorrect.  Please look over your entered parameters.");
            printUsage();
            return;
        }
        sampleName = nrFilename.substring(nrFilename.lastIndexOf(File.separator) + 1).replace(".CSV", "");
        // get peaks
        ArrayList<HPLCPeakComparable> fivePeaks = pickPeaks(stdFilename);

        ArrayList<HPLCPeakComparable> HPCLPeakList = acquireData(nrFilename);
         // draw peeks on the image
        XYLineChart_AWT chart = new XYLineChart_AWT(sampleName, sampleName, maxMAUForPeak,
                HPCLPeakList, fivePeaks);
        // save image
        int width = 640; /* Width of the image */
        int height = 480; /* Height of the image */
        File XYChart = new File(outputdir + getFilenameFromFullPath(nrFilename).replace(".CSV", ".jpg"));
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
        BufferedReader sc;
        String dataLine;
        try {
             sc = getBufferedReader(filename);
             HPLCPeakTracer.sampleName = sampleName.substring(0, sampleName.length());
             dataLine = sc.readLine();
            while (dataLine != null) {
                String[] splited = dataLine.split(",|\t");
                Double xCoord = Double.parseDouble(splited[0]);
                Double yCoord = Double.parseDouble(splited[1]);
                list.add(new HPLCPeakComparable(xCoord, yCoord));
                dataLine = sc.readLine();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
         return list;
    }

    public static ArrayList<HPLCPeakComparable> pickPeaks(String filename)
            throws FileNotFoundException {
        double STANDARD_INTESNITY_CUTOFF = 500.0;
        ArrayList<HPLCPeakComparable> list = acquireData(filename);
        ArrayList<HPLCPeakComparable> peaks = new ArrayList<HPLCPeakComparable>();
        for (int i = 1; i < list.size() - 1; i++) {
            if ((list.get(i).getAu() > (list.get(i - 1).getAu()) && list.get(i)
                    .getAu() > list.get(i + 1).getAu())) {
                    peaks.add(list.get(i));
            }
        }
        Collections.sort(peaks);
        ArrayList<HPLCPeakComparable> returnList = new ArrayList<HPLCPeakComparable>();
        for (int i = 0; i < peaks.size() - 1; i++) {
            if (peaks.get(i).getAu() >= STANDARD_INTESNITY_CUTOFF) {
                returnList.add(peaks.get(i));
            }
        }
        return returnList;
    }

    public static BufferedReader getBufferedReader(String filename) throws IOException {
        /*default encoding **/
        // FileReader reads text files in the default encoding.
        FileReader fileReader = new FileReader(filename);

        // Always wrap FileReader in BufferedReader.
        BufferedReader bufferedReader = new BufferedReader(fileReader);

        //a test to see if this is a UTF-16 encoded file
        String line = bufferedReader.readLine();
        Double number;
        boolean changeEncoding = false;
        try {
            if (line.contains(","))
                number = Double.parseDouble(line.split("\\w?,\\w?")[1]);
            else{
                changeEncoding = true;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            changeEncoding = true;
        } catch (NumberFormatException e1) {
            changeEncoding = true;
        }

        bufferedReader.close();

        if (changeEncoding) {
            /* UTF-16 encoding */
            File f = new File(filename);
            FileInputStream stream = new FileInputStream(f);
            bufferedReader = new BufferedReader(new InputStreamReader(stream, Charset.forName("UTF-16")));
        } else {
            fileReader = new FileReader(filename);
            bufferedReader = new BufferedReader(fileReader);
        }
        return bufferedReader;
    }
}

