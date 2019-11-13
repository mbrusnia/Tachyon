package org.fhcrc.optides.apps.HPLCPlateFracReporter;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class HPLCPlateFracReporter {
    public static String REPORT_FILE = "Report.TXT";

    /**
     * @param args
     */
    public static void main(String[] args) {
        String plateDirectory = null; //Absolute path to the plate top directory
        String outputf = null;  //Absolute path file name of output file
        String batf = null;
        for(int i = 0; i < args.length; i++) {
            //System.out.println("DEBUG number of args: " + args[i]);
            if (args[i].split("=")[0].equals("-plateDirectory")) {
                plateDirectory = args[i].split("=")[1];
            } else if (args[i].split("=")[0].equals("-outputfile")) {
                outputf = args[i].split("=")[1];
            }
            else if (args[i].split("=")[0].equals("-batfile")){
                batf = args[i].split("=")[1];
            }
        }
        if(plateDirectory == null || (outputf == null && batf == null)) {
            System.out.println("USAGE: HPLCPlateFracReporter -plateDirectory=\"/Users/mbrusnia/Documents/ToolGuides/OptidesSoftware/HT_HPLC_FractionReporter\" -batfile=\"/Users/mbrusnia/Desktop/HPLCPlateFracReporter.bat\"");
            System.out.println("Bat file should contain to run the following run after dos2unix of input files are executed");
            System.out.println("USAGE: HPLCPlateFracReporter -plateDirectory=\"/Users/mbrusnia/Documents/ToolGuides/OptidesSoftware/HT_HPLC_FractionReporter\" -outputfile=\"/Users/mbrusnia/Desktop/ReportSummary.csv\"");
            return;
        }
        //System.out.println("DEBUG " +  plateDirectory + " " + outputf);
        try {
            if(batf != null) { // generating bat file for window run
                File fout = new File(batf);
                FileOutputStream fos1 = new FileOutputStream(fout);
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos1));
                HPLCPlateFracReporter report = new HPLCPlateFracReporter();
                List<File> files = new ArrayList<File>();
                report.listf(plateDirectory, files);
                //System.out.println("DEBUG size of returned file" + files.size());
                for (int i = 0; i < files.size(); i++) {
                    bw.write("\"C:\\Program Files\\OptidesSoftware\\dos2unix.exe\" \"" + files.get(i).getAbsolutePath() +"\"");
                    bw.newLine();
                }
                bw.write("\"C:\\Program Files\\OptidesSoftware\\HPLCPlateFracReporter.jar\" -plateDirectory=\"" + plateDirectory + "\"" + " -outputfile=\"" + outputf + "\"" );
                bw.newLine();
                bw.close();
            }
            else {
                File fout = new File(outputf);
                FileOutputStream fos1 = new FileOutputStream(fout);
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos1));
                // Write out the header to reporter
                bw.write("Vial,Sample Name,Location,Start [min],End [min],Volume [uL],Peak selection");
                bw.newLine();
                //System.out.println("DEBUG write heading to output file");
                HPLCPlateFracReporter report = new HPLCPlateFracReporter();
                List<File> files = new ArrayList<File>();
                String[] path;
                FileReader fileReader;
                BufferedReader br;
                Process command;
                report.listf(plateDirectory, files);
                //System.out.println("DEBUG size of returned file: " + files.size());
                for (int i = 0; i < files.size(); i++) {
                    path = files.get(i).getName().split("/");
                    if (path[path.length - 1].equals(REPORT_FILE)) {
                        fileReader = new FileReader(files.get(i).getAbsoluteFile());
                        br = new BufferedReader(fileReader);
                        report.extractFractionInfo(br, bw);
                        br.close();
                    }
                }
                bw.close();
            }
        }catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void listf(String directoryName, List<File> files) {
        File directory = new File(directoryName);
        // Get all files from a directory.
        File[] fList = directory.listFiles();
        if(fList != null)
            for (File file : fList) {
                if (file.isFile() && file.getName().contains(REPORT_FILE)) {
                    files.add(file);
                } else if (file.isDirectory()) {
                    listf(file.getAbsolutePath(), files);
                }
            }
    }

    public void extractFractionInfo(BufferedReader br, BufferedWriter bw) throws IOException {
        String line = null;
        String [] items;
        String vial = "", sampleName = "";
        while ((line = br.readLine()) != null) {
            if(line.contains("Data File")){
                items=line.split("\\\\");
                vial = items[items.length-1].substring(4,10);
                sampleName = items[items.length-1].substring(11,items[items.length-1].length() - 2);
            }
            else if(line.contains("Frac  Well  Location   Volume    BeginTime  EndTime    Reason        Mass")){
                //discard two following lines.
                br.readLine();
                br.readLine();
                while(!(line = br.readLine()).contains("===========")){
                    items = line.split("\\s+");
                    System.out.println(vial + "," + sampleName + "," + items[3] + "," + items[5] + "," + items[6] + "," +items[4] + ",0");
                    bw.write(vial + "," + sampleName + "," + items[3] + "," + items[5] + "," + items[6] + "," +items[4] + ",0");
                    bw.newLine();
                }
                return; // we don't need to go end of file after we found and extract fractionation information
            }
        }

    }
}


