package org.fhcrc.optides.apps.PhyloXmlGenerator;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.*;
import java.io.*;

public class PhyloXmlGenerator {

	private String xmlFilepath;
	private String pathToClassSetFile;
	

	public PhyloXmlGenerator(String xmlFilepath2, String pathToClassSetFile) {
		this.xmlFilepath = xmlFilepath2;
		this.pathToClassSetFile = pathToClassSetFile;
	}

	public static void main(String[] args) {
		String xmlFilepath = "";
		String pathToClassSetFile = "";
		if(args.length != 2){
			printUsage();
			return;
		}else{
			//get input params
			for(int i = 0; i < args.length; i++){
				if(args[i].split("=")[0].equals("-ncbiPhyloXmlFile"))
					xmlFilepath = args[i].split("=")[1];
				if(args[i].split("=")[0].equals("-classSetFile"))
					pathToClassSetFile = args[i].split("=")[1];
			}
		}	
		DocumentBuilderFactory factory =
				DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		Document doc = null;
		try {
			builder = factory.newDocumentBuilder();
			doc = builder.parse(new File(xmlFilepath));
		} catch (ParserConfigurationException | SAXException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// get the first element
        Element element = doc.getDocumentElement();

        // get all child nodes
        NodeList nodes = element.getChildNodes();

        // print the text content of each child

        System.out.println(nodes.getLength());
        PrintWriter writer = null;
		try {
			writer = new PrintWriter("C:/Users/hramos/Documents/HRInternetConsulting/Clients/FHCRC/Project14 - PhylogeneticTree/the-file-name.txt", "UTF-8");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        for (int i = 0; i < nodes.getLength(); i++) {
        	writer.println("i=" + i + " " + nodes.item(i).getTextContent());
        }
        writer.close();
				
		PhyloXmlGenerator ptt = new PhyloXmlGenerator(xmlFilepath, pathToClassSetFile);
		//TRY FORESTER.JAR TO PARSE PHYLOXML
	}

	private static void printUsage() {
			System.out.println("USAGE: PhyloXmlGenerator -ncbiPhyloXmlFile=pathToXmlFilename -classSetFile=pathToClassSetFile");
	}

}
