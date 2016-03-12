/**
 * 
 */
package org.fhcrc.optides.apps.PepXmlFilter;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.SAXException;


/**
 * @author hramos, 3/11/2016
 * This class filters a PepXml file for search_hits that pass the expect threshold
 *
 */
public class PepXmlFilter{

	//contains all the info of interest to us from all the search hits in the pepxml file
	List<SearchHit> searchHitList;
	
	
	/**
	 * @param args
	 * -pepXmlFile    Full path to the pepXml file to be analyzed
	 * -expect_threshold     maximum expect value to report (defaults to 0.1)
	 */
	public static void main(String[] args) {
		double expect_threshold = .1;
		String pepxmlFilename = "";
		
		if(args.length < 1 || args.length > 2){
			usage();
			return;
		}
		//get input params
		for(int i = 0; i < args.length; i++){
			if(args[i].split("=")[0].equals("-pepXmlFile"))
				pepxmlFilename = args[i].split("=")[1];
			if(args[i].split("=")[0].equals("-expect_threshold"))
				expect_threshold = Double.parseDouble(args[i].split("=")[1]);
		}
		if(pepxmlFilename.equals("")){
			usage();
			return;
		}
		
		try {
			//parse pepxml
			PepXmlFilter pf = new PepXmlFilter(pepxmlFilename);
			
			//write output csv file
			Writer writer = new BufferedWriter(new OutputStreamWriter(
		              new FileOutputStream(pepxmlFilename.substring(0, pepxmlFilename.lastIndexOf(".") +1) + "csv"), "utf-8"));
			writer.write("spectrum,protein,peptide,precursor_neutral_mass,assumed_charge,num_matched_ions,bscore,yscore,expect,mod_position,mod_mass\n");
			for(int i =0; i < pf.searchHitList.size(); i++)
				if(Double.parseDouble(pf.searchHitList.get(i).expect) < expect_threshold)
					writer.write(((SearchHit)pf.searchHitList.get(i)).toString() + "\n");
			writer.close();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void usage(){
		System.out.println("USAGE: PepXmlFilter -pepXmlFile=fullPathToPepXmlFile -expect_threshold=yourDoubleValue");
		System.out.println("(expect_threshold defaults to 0.10)");
		System.out.println("output goes to file with the same name as the pepxml file, only with a .csv extension");
	}
	
	PepXmlFilter(String xmlFilepath) throws ParserConfigurationException, SAXException, IOException{
		SAXParserFactory spf = SAXParserFactory.newInstance();
		SAXParser parser = spf.newSAXParser();
		SaxHandler handler = new SaxHandler();
		parser.parse(new FileInputStream(xmlFilepath),  handler);
		searchHitList = handler.getSearchHitList();
	}
}
