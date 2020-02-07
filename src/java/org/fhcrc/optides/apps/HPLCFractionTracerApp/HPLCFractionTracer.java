package org.fhcrc.optides.apps.HPLCFractionTracerApp;

import org.jfree.chart.ChartUtilities;

import java.awt.*;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class HPLCFractionTracer {
    public static String REPORT_FILE = "Report.TXT";
    public static String MOLECULE_PREFIX = "SMT";
    private static double maxMAUForPeak = 100.0;
    private static Graphics g;
    /**
     * @param args
     */
    public static void main(String[] args) throws FileNotFoundException, IOException {
        String plateDirectory = null; //Absolute path to the plate top directory
        String outputDirectory = null;  //Absolute path file name of output directory where jpg files will be generated
        String batf = null;
        String reportf = null;
        String chromatogramf = null;
        String outputf = null;
        for (int i = 0; i < args.length; i++) {
            if (args[i].split("=")[0].equals("-plateDirectory")) {
                plateDirectory = args[i].split("=")[1];
            } else if (args[i].split("=")[0].equals("-outputDirectory")) {
                outputDirectory = args[i].split("=")[1];
            } else if (args[i].split("=")[0].equals("-batfile")) {
                batf = args[i].split("=")[1];
            } else if (args[i].split("=")[0].equals("-outputfile")) {
                outputf = args[i].split("=")[1];
            } else if (args[i].split("=")[0].equals("-fractionReport")) {
                reportf = args[i].split("=")[1];
            } else if (args[i].split("=")[0].equals("-chromatogram")) {
                chromatogramf = args[i].split("=")[1];
            } else if (args[i].split("=")[0].equals("-MaxMAUForPeak")) {
                maxMAUForPeak = Double.parseDouble(args[i].split("=")[1]);
            }
        }
        // Note: In PC, don't put the directory name finishing with backslash it is interpreted as espcape rather than directory (e.g., should be outputfile="C\outputfile" not  outputfile="C\outputfile\"
        //System.out.println("DEBUG: -plateDirectory:" + plateDirectory);
        //System.out.println("DEBUG: -outputDirectory:" + outputDirectory);
        //System.out.println("DEBUG: -batfile:" + batf);
        //System.out.println("DEBUG: -MaXMAUForPeak:" + maxMAUForPeak);
        if(batf != null){
            if (plateDirectory == null || outputDirectory == null) {
                System.out.println("USAGE: -HPLCPlateFractionTracer -plateDirectory=\"/Users/mbrusnia/Documents/ToolGuides/OptidesSoftware/HT_HPLC_FractionReporter/SMT-HT-010 2019-11-09 16-42-55\" -outputDirectory=\"/Users/mbrusnia/Desktop/HPLCFractionOut\" -batfile=\"/Users/mbrusnia/Desktop/HPLCPlateFracReporter.bat\"" + "-MaxMAUForPeak=" + maxMAUForPeak);
                System.out.println("Bat file should contain to run the following run after dos2unix of input files are executed");
                return;
            }
        }
        else{
            if(reportf == null || chromatogramf == null || outputf == null){
                System.out.println("USAGE: HPLCPlateFractionTracer -plateDirectory=\"/Users/mbrusnia/Documents/ToolGuides/OptidesSoftware/HT_HPLC_FractionReporter/SMT-HT-010 2019-11-09 16-42-55\" -outputDirectory=\"/Users/mbrusnia/Desktop/HPLCFractionOut\" -batfile=\"/Users/mbrusnia/Desktop/HPLCPlateFracReporter.bat\"" + "-MaxMAUForPeak=" + maxMAUForPeak);
                System.out.println("The input files should be run after dos2unix of input files are executed (please use -batfile option to create auto batch batfile");
                System.out.println("USAGE: HPLCPlateFracReporter -fractionReport=\"/Users/mbrusnia/Documents/ToolGuides/OptidesSoftware/HT_HPLC_FractionReporter/REPORT.TXT -Chromatogram=\"/Users/mbrusnia/Documents/ToolGuides/OptidesSoftware/HT_HPLC_FractionReporter/SMT-1000-SMT-101.CSV -outputfile=\"/Users/mbrusnia/Desktop/SMT-1000-SMT-101.jpg\""+ "-MaxMAUForPeak=" + maxMAUForPeak);
            }
        }
        String[] path;
        if (batf != null) { // generating bat file for window run
            HPLCFractionTracer tracer = new HPLCFractionTracer();
            List<File> reportfiles = new ArrayList<File>();
            List<File> chromatogramfiles = new ArrayList<File>();
            File fout = new File(batf);
            FileOutputStream fos1 = new FileOutputStream(fout);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos1));
            tracer.listf(plateDirectory, reportfiles, chromatogramfiles);
            System.out.println("DEBUG size of TXT file" + reportfiles.size());
            for (int i = 0; i < reportfiles.size(); i++) {
                System.out.println(reportfiles.get(i));
            }
            System.out.println("DEBUG size of Chromatogram file" + chromatogramfiles.size());
            for (int i = 0; i < chromatogramfiles.size(); i++) {
                System.out.println(chromatogramfiles.get(i));
            }
            for (int i = 0; i < reportfiles.size(); i++) {
                bw.write("\"C:\\Program Files\\OptidesSoftware\\dos2unix.exe\" \"" + reportfiles.get(i).getAbsolutePath() + "\"");
                bw.newLine();
                bw.write("\"C:\\Program Files\\OptidesSoftware\\dos2unix.exe\" \"" + chromatogramfiles.get(i).getAbsolutePath() + "\"");
                bw.newLine();
            }
            for (int i = 0; i < chromatogramfiles.size(); i++) {
                path = chromatogramfiles.get(i).getName().split("/");
                outputf = path[path.length - 1].replace("CSV", "jpg");
                bw.write("java -jar \"C:\\Program Files\\OptidesSoftware\\HPLCFractionTracer.jar\" " +
                        " -fractionReport=\"" + reportfiles.get(i).getAbsolutePath() + "\"" +
                        " -chromatogram=\"" + chromatogramfiles.get(i).getAbsolutePath() +
                        "\" -outputfile=\"" + outputDirectory + "\\" + outputf + "\"" + " -MaxMAUForPeak=" + maxMAUForPeak);
                bw.newLine();
            }
            bw.newLine();
            bw.close();
        }
        // When it is running as Tracer generator mode
        if(reportf !=null && chromatogramf !=null && outputf !=null){
            //Parse report to get fraction list
            FileReader fileReader;
            BufferedReader br;
            //System.out.println("DEBUG:" + reportf + "," + chromatogramf);
            String line = null;
            String[] items;
            String vial = "", sampleName = "";
            ArrayList<FractionTime> fractions = new ArrayList<FractionTime>();
            // get fraction
            File report = new File(reportf);
            fileReader = new FileReader(report.getAbsoluteFile());
            br = new BufferedReader(fileReader);
            while ((line = br.readLine()) != null) {
                if (line.contains("Data File")) {
                    items = line.split("\\\\");
                    vial = items[items.length - 1].substring(4, 10);
                    sampleName = items[items.length - 1].substring(11, items[items.length - 1].length() - 2);
                    if(!sampleName.startsWith(MOLECULE_PREFIX)){
                        sampleName = sampleName.substring(sampleName.indexOf(MOLECULE_PREFIX));
                    }
                } else if (line.contains("Frac  Well  Location   Volume    BeginTime  EndTime    Reason        Mass")) {
                    //discard two following lines.
                    br.readLine();
                    br.readLine();
                    while (!(line = br.readLine()).contains("===========")) {
                        items = line.split("\\s+");
                        //System.out.println("DEBUG:" + vial + "," + sampleName + "," + items[3] + "," + items[5] + "," + items[6] + "," + items[4] + ",0");
                        fractions.add(new FractionTime(Float.parseFloat(items[5]), Float.parseFloat(items[6])));
                    }
                    //System.out.println("DEBUG: Num fractions " + fractions.size() + "in " + report.getName());
                }
            }
            ArrayList<HPLCFractionPeakComparable> HPCLPeakList = acquireData(chromatogramf);

            XYLineChart_AWT chart = new XYLineChart_AWT("HPLCFractionTracerApp", sampleName, maxMAUForPeak,
                    HPCLPeakList, fractions);
            int width = 640; /* Width of the image */
            int height = 480; /* Height of the image */
            File XYChart = new File(outputf);
            ChartUtilities.saveChartAsJPEG(XYChart, chart.getChart(), width, height);
        }
    }

    private static void listf(String directoryName, List<File> reportFiles, List<File> chromatogramFiles) {
        File directory = new File(directoryName);
        // Get all files from a directory.
        File[] fList = directory.listFiles();

        if (fList != null)
            for (File file : fList) {
                if (file.isFile()) {
                    if (!file.getAbsolutePath().contains("Blank") && file.getName().contains(REPORT_FILE)) {
                        reportFiles.add(file);
                    } else if (file.getName().contains("SMT-") && file.getName().contains(".CSV")) {
                        chromatogramFiles.add(file);
                    }
                } else if (file.isDirectory()) {
                    listf(file.getAbsolutePath(), reportFiles, chromatogramFiles);
                }
            }
    }

    private static BufferedReader getBufferedReader(String filename) throws IOException {
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

    private static ArrayList<HPLCFractionPeakComparable> acquireData(String filename)
            throws FileNotFoundException {
        ArrayList<HPLCFractionPeakComparable> list = new ArrayList<HPLCFractionPeakComparable>();
        File file = new File(filename);
        BufferedReader sc;
        String dataLine;
        try {
            sc = getBufferedReader(filename);
            dataLine = sc.readLine();
            while (dataLine != null) {
                String[] splited = dataLine.split(",|\t");
                Double xCoord = Double.parseDouble(splited[0]);
                Double yCoord = Double.parseDouble(splited[1]);
                list.add(new HPLCFractionPeakComparable(xCoord, yCoord));
                dataLine = sc.readLine();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }
}

