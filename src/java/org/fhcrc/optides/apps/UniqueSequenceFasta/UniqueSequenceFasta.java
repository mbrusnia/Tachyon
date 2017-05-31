package org.fhcrc.optides.apps.UniqueSequenceFasta;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;


public class UniqueSequenceFasta {

	public static void main(String[] args) {
		//UniqueSequenceFasta -input_fasta=input.fasta --mim_length=13
		String inputFasta = "";
		String outputFasta = "";
		String logFile = "";
		String outputDir = "";
		int minLength = 0;
        Boolean uniprotCheck = false;
		//get input params
		String[] curParam = null;
		for(int i = 0; i < args.length; i++){
			curParam = args[i].split("=");
			if(curParam[0].equals("--input_fasta")){
				inputFasta = curParam[1];
				Path p = Paths.get(inputFasta);
				outputDir = p.getParent().toString() + File.separator;//.getFileName().toString();
				outputFasta = outputDir + p.getFileName().toString().replaceAll(".fasta", "_unique.fasta");
				outputFasta = outputFasta.replaceAll(".FASTA", "_unique.FASTA");
				logFile = outputFasta.replaceAll(".fasta", ".log");
				logFile = logFile.replaceAll(".FASTA", ".log");
			}
			else if(curParam[0].equals("--min_length")){
				minLength =  Integer.parseInt(curParam[1]);
			}
			else if(curParam[0].equals("--uniprotCheckOn")){
				uniprotCheck = true;
			}
			else{
				System.out.println("Unrecognized command line parameter: " + curParam[0]);
				printUsage();
				return;
			}			
		}
		if(inputFasta.equals("")){
			System.out.println("Please specify the input fasta file on the command line using the parameter --input_fasta=path/to/your/fastaFile.fasta.  Please see the following USAGE:");
			printUsage();
			return;
		}

		try {
			doFilter(inputFasta, outputFasta, logFile, minLength, uniprotCheck);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static void doFilter(String inputFasta, String outputFasta, String logFile, int minLength, Boolean uniprotCheck) throws IOException {
		HashMap<String, String> sequences = new HashMap<String, String>();
		String line = null;
		
		//now print all the matching records in the inputFasta file
		//prepare the reading
		FileReader fastaReader = new FileReader(inputFasta);
		BufferedReader fastaBufferedReader = new BufferedReader(fastaReader);
		
		//prepare the writing
		File fout1 = new File(outputFasta);
		FileOutputStream fos1 = new FileOutputStream(fout1);
		BufferedWriter outputFastaFile = new BufferedWriter(new OutputStreamWriter(fos1));
		File fout2 = new File(logFile);
		FileOutputStream fos2 = new FileOutputStream(fout2);
		BufferedWriter logFileWriter = new BufferedWriter(new OutputStreamWriter(fos2));

		
		String curIdLine = "";
		String curSequence = "";
		String stampMarker = "NOCDP: ";
		int total_sequences = 0, filtered_out_sequences = 0;
		while ((line = fastaBufferedReader.readLine()) != null) {
			if(line.startsWith(">")){
				if(total_sequences++ > 0){
					if(curSequence.length() < minLength){
						logFileWriter.write("too small: " + curIdLine + "\n");
						logFileWriter.write(curSequence + "\n");
						filtered_out_sequences++;
					}
					else if(sequences.containsKey(curSequence)){
						if(uniprotCheck && !sequences.get(curSequence).contains(">sp|") && !sequences.get(curSequence).contains(">tr|")){
							logFileWriter.write(stampMarker + curIdLine + "\n");
							logFileWriter.write(stampMarker + curSequence + "\n");
						}
						else{
							logFileWriter.write(curIdLine + "\n");
							logFileWriter.write(curSequence + "\n");
						}
						//sequences.put(curSequence, sequences.get(curSequence) + 1);
						filtered_out_sequences++;
					}else{
						outputFastaFile.write(curIdLine + "\n");
						outputFastaFile.write(curSequence + "\n");
						sequences.put(curSequence, curIdLine);
					}
				}
				curIdLine = line;
				curSequence = "";
			}else{
				if(curSequence.equals(""))
					curSequence = line;
				else
					curSequence += line;
			}
		}
		//there's always one trailing protein...
		if(curSequence.length() < minLength) {
			logFileWriter.write("too small: " + curIdLine + "\n");
			logFileWriter.write(curSequence + "\n");
			filtered_out_sequences++;
		}else if(sequences.containsKey(curSequence)){
			if(uniprotCheck && !sequences.get(curSequence).contains(">sp|") && !sequences.get(curSequence).contains(">tr|")){
				logFileWriter.write(stampMarker + curIdLine + "\n");
				logFileWriter.write(stampMarker + curSequence + "\n");
			}
			else{
				logFileWriter.write(curIdLine + "\n");
				logFileWriter.write(curSequence + "\n");
			}
			//sequences.put(curSequence, sequences.get(curSequence) + 1);
			filtered_out_sequences++;
		}else{
			outputFastaFile.write(curIdLine + "\n");
			outputFastaFile.write(curSequence + "\n");
			sequences.put(curSequence, curIdLine);
		}

		fastaBufferedReader.close();
		outputFastaFile.close();
		logFileWriter.close();
		System.out.println("Number of sequences in the input: " + total_sequences);
		System.out.println("Number of sequences that were filtered out: " + filtered_out_sequences);
				
	}

	private static void printUsage() {
		System.out.println("USAGE: java UniqueSequenceFasta --input_fasta=path/to/fasta_file.fasta --min_length=5 --uniprotCheckOn");
		System.out.println("");
		System.out.println("OUTPUTS: same/path/fasta_file_unique.fasta AND same/path/fasta_file_unique.log");
	}

}
