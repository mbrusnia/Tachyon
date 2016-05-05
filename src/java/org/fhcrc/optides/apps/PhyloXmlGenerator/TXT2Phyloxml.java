package org.fhcrc.optides.apps.PhyloXmlGenerator;

import javax.xml.parsers.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class TXT2Phyloxml {

	//files
	private String taxonomyFilepath;
	private String pathToClassSetFile;
	private String outputPhyloXmlFilepath;
	private String debugOutputFilepath = "C:/Users/hramos/Documents/HRInternetConsulting/Clients/FHCRC/Project14 - PhylogeneticTree/debugOut.txt";
	private boolean debug = false;
	
	//keeps the paths of each leaf of the text taxonomy file
	private ArrayList<String[]> path;
	
	//holds all of the kingdoms specified in the class_set file
	private HashMap<String, String> kingdoms;

	//holds all of the classes specified in the class_set file
	private HashMap<String, String> classes;
	private int level_cutoff;
	
	//counters
	private int total_class_set_count = 0;
	private int matched_class_set_count = 0;
	private int total_leaves = 0;
	

	TXT2Phyloxml(String xmlFilepath2, String pathToClassSetFile, String outputPhyloXmlFilepath, int level_cutoff) throws ParserConfigurationException, FileNotFoundException, IOException {
		this.taxonomyFilepath = xmlFilepath2;
		this.pathToClassSetFile = pathToClassSetFile;
		this.level_cutoff = level_cutoff;
		this.outputPhyloXmlFilepath = outputPhyloXmlFilepath;
		path = new ArrayList<String[]>();
		kingdoms = new HashMap<String, String>();
		classes = new HashMap<String, String>();
	}

	/*
	 * Read the text file and parse out: Kingdom, Phylum, Class, Order, Family, Genus
	 * (currently does not handle sub, super, or infra K,P,C,O,F, or G's -turn on debug
	 * mode to see complete list of ignored taxonomic categories)
	 */
	private void parseHierarchy() throws IOException {
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ": ";

		String kingdom = "";
		String phylum = "";
		String klass = "";
		String order = "";
		String family = "";
		String genus = "";
		String curClass = "";
		
		br = new BufferedReader(new FileReader(this.taxonomyFilepath));
		Writer writer = null;
		HashMap<String, Integer> unmatchedTaxonomy = null;
		if(debug) {
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(debugOutputFilepath), "utf-8"));
			unmatchedTaxonomy = new HashMap<String, Integer>();
		}
		while ((line = br.readLine()) != null) {
		    // use tab as separator
			String[] dividedLine = line.split(cvsSplitBy);
			if(dividedLine.length > 1){
				int spaceCount = dividedLine[0].indexOf(dividedLine[0].trim());
				dividedLine[0] = dividedLine[0].trim();
				dividedLine[1] = dividedLine[1].trim();
				if(dividedLine[0].equals("Kingdom")){
					kingdom = dividedLine[1];
					phylum = "";
					klass = "";
					order = "";
					family = "";
					genus = "";
				}else if(dividedLine[0].equals("Phylum")){
					phylum = dividedLine[1];
					klass = "";
					order = "";
					family = "";
					genus = "";
				}else if(dividedLine[0].equals("Class")){
					klass = dividedLine[1];
					order = "";
					family = "";
					genus = "";
				}else if(dividedLine[0].equals("Order")){
					order = dividedLine[1];
					family = "";
					genus = "";
				}else if(dividedLine[0].equals("Family")){
					family = dividedLine[1];
					genus = "";
				}else if(dividedLine[0].equals("Genus")){
					genus = dividedLine[1];
				}else{
					if(debug)
						if(unmatchedTaxonomy.containsKey(dividedLine[0]))
								unmatchedTaxonomy.put(dividedLine[0], unmatchedTaxonomy.get(dividedLine[0]) +1);
						else
								unmatchedTaxonomy.put(dividedLine[0], 0);
				}
				if(classes.containsKey(klass) && !genus.equals("") && spaceCount == 108){
					if(!curClass.equals(klass)) 
						matched_class_set_count++;
					curClass = klass;
					String[] newline = new String[]{kingdom, phylum, klass, order, family, genus};
					path.add(newline);
					if(debug) 
						writer.write(kingdom + " - " + phylum + " - " + klass + " -  " + order + " -  " + family + " -  " + genus + "\n");
				}
			}
		}
		br.close();
		if(debug) {
			writer.close();
			System.out.println("unmatched taxonomy terms:");
			Iterator it = (Iterator) unmatchedTaxonomy.entrySet().iterator();
		    while (it.hasNext()) {
		        Map.Entry pair = (Map.Entry)it.next();
		        System.out.println(pair.getKey() + " = " + pair.getValue());
		        it.remove(); // avoids a ConcurrentModificationException
		    } 
		}
	}

	/*
	 * Fills the Kingdoms map and the Classes map with the kingdoms and classes
	 * specified by the csv file
	 */
	private void fillMaps() throws IOException{
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ", ";

		br = new BufferedReader(new FileReader(this.pathToClassSetFile));
		while ((line = br.readLine()) != null) {
			total_class_set_count++;

		        // use comma as separator
			String[] kingdom_class = line.split(cvsSplitBy);
			if(!kingdoms.containsKey(kingdom_class[0])){
				kingdoms.put(kingdom_class[0], kingdom_class[0]);
			}
			if(!classes.containsKey(kingdom_class[1])){
				classes.put(kingdom_class[1], kingdom_class[1]);
			}
		}
		br.close();
	}

	/*
	 * Print the final PhyloXML
	 */
	private void printPhyloXML() throws UnsupportedEncodingException, FileNotFoundException, IOException {
		Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(this.outputPhyloXmlFilepath), "utf-8"));
		
		//header stuff
		writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		writer.write("<phyloxml xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.phyloxml.org http://www.phyloxml.org/1.10/phyloxml.xsd\" xmlns=\"http://www.phyloxml.org\">\n");
		writer.write("<phylogeny rooted=\"true\">\n");	
		
		String[] line = path.get(0);
		String[] nextLine = null;
		String indent = "";
		
		//print the first element, from root to the leaf
		for(int i = 0; i < level_cutoff; i++){
			indent += "\t";
			
			writer.write(indent + "<clade>\n");
			writer.write(indent + "	<taxonomy>\n");
			writer.write(indent + "		<scientific_name>" + line[i] + "</scientific_name>\n");
			writer.write(indent + "	</taxonomy>\n");
		}
		total_leaves++;
		
		//for the rest of the elements, trace the path from the root to the most recent ancestor,
		//then close the previous branch and write the new/current branch
		for(int i = 1; i < path.size(); i++){
			nextLine = path.get(i);
			
			int j = 0;
			for(; line[j].equals(nextLine[j]) && j < level_cutoff; j++);
			int k = j;
			for(; k < level_cutoff; k++){
				writer.write(indent + "</clade>\n");
				indent = indent.substring(0, indent.lastIndexOf("\t"));
			}
			for(; j < level_cutoff; j++){
				indent += "\t";
				writer.write(indent + "<clade>\n");
				writer.write(indent + "	<taxonomy>\n");
				writer.write(indent + "		<scientific_name>" + nextLine[j] + "</scientific_name>\n");
				writer.write(indent + "	</taxonomy>\n");
				if(j == level_cutoff -1)
					total_leaves++;
			}
			line = nextLine;
		}
		
		
		//closing stuff
		for(int i = 0; i < level_cutoff; i++){
			writer.write(indent + "</clade>\n");
			indent = indent.substring(0, indent.lastIndexOf("\t"));
		}
		writer.write("</phylogeny>\n");
		writer.write("</phyloxml>");
		writer.close();
	}

	public static void main(String[] args) {
		String xmlFilepath = "";
		String pathToClassSetFile = "";
		String outputPhyloXmlFilepath = "";
		int level_cutoff = 6; //genus
		if(args.length != 3 && args.length != 4){
			printUsage();
			return;
		}else{
			//get input params
			for(int i = 0; i < args.length; i++){
				if(args[i].split("=")[0].equals("-taxonomyFile"))
					xmlFilepath = args[i].split("=")[1];
				if(args[i].split("=")[0].equals("-classSetFile"))
					pathToClassSetFile = args[i].split("=")[1];
				if(args[i].split("=")[0].equals("-outputPhyloXmlFilepath"))
					outputPhyloXmlFilepath = args[i].split("=")[1];
				if(args[i].split("=")[0].equals("-level_cutoff")){
					String level = args[i].split("=")[1];
					if(level.toLowerCase().equals("phylum"))
						level_cutoff = 2;
					else if(level.toLowerCase().equals("class"))
							level_cutoff = 3;
					else if (level.toLowerCase().equals("order"))
						level_cutoff = 4;
					else if (level.toLowerCase().equals("family"))
						level_cutoff = 5;
				}
			}
		}	
						
		try {
			TXT2Phyloxml ttp = new TXT2Phyloxml(xmlFilepath, pathToClassSetFile, outputPhyloXmlFilepath, level_cutoff);

			ttp.fillMaps();
			ttp.parseHierarchy();
			ttp.printPhyloXML();

			System.out.println("Taxonomy level cutoff: " + ttp.getLevelCuttoff());
			System.out.println("Total class set items FOUND: " + ttp.getTotalClassSetCount());
			System.out.println("Total class set items MATCHED: " + ttp.getMatchedClassSetCount());
			System.out.println("Total leaves reported: " + ttp.getTotalLeaves());
		} catch (IOException | ParserConfigurationException e) {
			e.printStackTrace();
		} 
	}

	private int getTotalLeaves() {
		return total_leaves;
	}

	private int getMatchedClassSetCount() {
		return matched_class_set_count;
	}

	private int getTotalClassSetCount() {
		return total_class_set_count;
	}

	private int getLevelCuttoff() {
		return level_cutoff;
	}

	private static void printUsage() {
			System.out.println("USAGE: TXT2Phyloxml -taxonomyFile=pathToTaxonomyFile -classSetFile=pathToClassSetFile -outputPhyloXmlFilepath=filepath [-level_cutoff=genus]");
	}

}
