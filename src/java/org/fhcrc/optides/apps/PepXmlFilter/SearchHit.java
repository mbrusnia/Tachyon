package org.fhcrc.optides.apps.PepXmlFilter;

public class SearchHit {

	String spectrum = "";
	String protein = "";
	String peptide = "";
	String precursor_neutral_mass = "";
	String assumed_charge = "";
	String num_matched_ions = "";
	String bscore = "";
	String yscore = "";	  
	String expect = "";
	String mod_position = "";
	String mod_mass = "";

  @Override
  public String toString() {
    return spectrum + "," + protein + "," + peptide + "," + precursor_neutral_mass + "," + assumed_charge + "," + num_matched_ions + "," + bscore + "," + yscore + "," + expect + "," + mod_position + "," + mod_mass ;
  }
}
