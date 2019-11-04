package org.fhcrc.optides.apps.Seq2Fasta;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Seq2Fasta {
	public static void main(String[] args) throws IOException {
        //Seq2Fasta -input sequence.txt
        String inputFile = "";
        String outputFasta = "";

        //get input params
        String[] curParam = null;
        if (args.length != 1) {
            System.out.println("USAGE: Seq2Fasta -input=SequenceInput.txt");
            return;
        }
        inputFile = null;
        if (args[0].split("=")[0].equals("-input"))
            inputFile = args[0].split("=")[1];
        outputFasta = inputFile.replace(".txt", ".fasta");

        //prepare the reading
        FileReader fileReader = new FileReader(inputFile);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        String line = null;

        //prepare the writing
        File fout = new File(outputFasta);
        FileOutputStream fos1 = new FileOutputStream(fout);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos1));
        int seqId = 1;
        while ((line = bufferedReader.readLine()) != null) {
            bw.write(">" + seqId +"\n");
            bw.write(line.replace("\n", "").replace("\r", "") + "\n");
            seqId += 1;
        }
        bw.close();
        bufferedReader.close();
    }
}


