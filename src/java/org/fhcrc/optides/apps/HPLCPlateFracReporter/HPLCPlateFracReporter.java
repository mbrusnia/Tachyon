package org.fhcrc.optides.apps.HPLCPlateFracReporter;

import org.fhcrc.optides.apps.FastaSplit.FastaSplit;

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
        if(args.length != 2){
            System.out.println("USAGE: HPLCPlateFracReporter -plateDirectory=\"/Users/mbrusnia/Documents/ToolGuides/OptidesSoftware/HT_HPLC_FractionReporter\" -outputfile=\"/Users/mbrusnia/Desktop/ReportSummary.csv\"");
            return;
        }
        for(int i = 0; i < args.length; i++){
            if(args[i].split("=")[0].equals("-plateDirectory")) {
                plateDirectory = args[i].split("=")[1];
            }
            if(args[i].split("=")[0].equals("-outputfile"))
                outputf = args[i].split("=")[1];
        }
        try {
            File fout = new File(outputf);
            FileOutputStream fos1 = new FileOutputStream(fout);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos1));
            // Write out the header to reporter
            bw.write("Vial,Sample Name,Location,Start [min],End [min],Volume [µL],tPeak selection");
            bw.newLine();
            HPLCPlateFracReporter report = new HPLCPlateFracReporter();
            List<File> files = new ArrayList<File>();
            String[] path;
            FileReader fileReader;
            BufferedReader br;
            Process command;
            report.listf(plateDirectory, files);
            for(int i = 0;  i < files.size(); i++){
                path = files.get(i).getName().split("/");
                if(path[path.length-1].equals(REPORT_FILE)) {
                    fileReader = new FileReader(files.get(i).getAbsoluteFile());
                    br = new BufferedReader(fileReader);
                    report.extractFractionInfo(br,bw);
                    br.close();
                }
            }
            bw.close();
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
                if (file.isFile()) {
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


