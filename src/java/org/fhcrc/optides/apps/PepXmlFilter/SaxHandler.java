package org.fhcrc.optides.apps.PepXmlFilter;

import org.xml.sax.helpers.DefaultHandler;
import java.util.ArrayList;
import java.util.List;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/*
 * Handles events triggered by the xml SAXParser
 * Here we build up a list of all the search_hits in the pepxml file
 */
public class SaxHandler extends DefaultHandler {
	private List<SearchHit> shList = new ArrayList<>();
	
	
	private SearchHit sh = null;
	private String curSpectrum = null;
	private String curAssumed_charge = null;
	private String content = null;
	
	  @Override
	  //Triggered when the start of tag is found.
	  public void startElement(String uri, String localName, 
	                           String qName, Attributes attributes) 
	                           throws SAXException {

	    switch(qName){
	      //Create a new spectrum_query object when the start tag is found
	    case "spectrum_query":
	        curSpectrum = attributes.getValue("spectrum");
	        curAssumed_charge = attributes.getValue("assumed_charge");
	        break;
	    case "search_hit":
	        sh = new SearchHit();
	        sh.spectrum = curSpectrum;
	        sh.assumed_charge = curAssumed_charge;
		    sh.protein = attributes.getValue("protein");
		    sh.peptide = attributes.getValue("peptide");
		    sh.precursor_neutral_mass = attributes.getValue("calc_neutral_pep_mass"); //TODO, check w MY to see if this is correct
		    sh.num_matched_ions = attributes.getValue("num_matched_ions"); 
	    	break;
	    case "search_score":
	    	  if(attributes.getValue("name").equals("bscore"))
	    		  sh.bscore = attributes.getValue("value");
	    	  else if(attributes.getValue("name").equals("yscore"))
	    		  sh.yscore = attributes.getValue("value");
	    	  else if(attributes.getValue("name").equals("expect"))
	    		  sh.expect = attributes.getValue("value");
	    	  break;
	    case "mod_aminoacid_mass":
	    	  if(sh.mod_position.length() > 0){
	    		  sh.mod_position += "; ";
	    		  sh.mod_mass += "; ";
	    	  }
    		  sh.mod_position += attributes.getValue("position");
    		  sh.mod_mass += attributes.getValue("mass");
	    	  break;
	    }
	  }

	  @Override
	  public void endElement(String uri, String localName, 
	                         String qName) throws SAXException {
	   switch(qName){
	     //Add the spectrum_query to list once end tag is found
	     case "search_hit":
	       shList.add(sh);
	       break;
	     //For all other end tags to be updated.
	   }
	  }

	  @Override
	  public void characters(char[] ch, int start, int length) 
	          throws SAXException {
	    content = String.copyValueOf(ch, start, length).trim();
	  }
	  
	  public List<SearchHit> getSearchHitList(){
			return shList;
		}
}
