package org.fhcrc.optides.apps.DatFileMining;

import java.util.ArrayList;
import java.util.Arrays;

public class DatFileRecord {
	protected String ID = "";
	protected String geneName = ""; 
	protected String taxID = ""; 
	protected String organism = ""; 
	protected String anyfield = "";
	protected String GO = "";
	protected String subcellularLocation = "";
	protected String function = "";
	protected ArrayList<String> accessions;

	public DatFileRecord(String[] record) {
		boolean readingSubcellular = false;
		boolean readingFunction = false;
		boolean readingGeneName = false;
		boolean readingTaxID = false;
		boolean readingGO = false;
		boolean readingOrganism = false;
		
		accessions = new ArrayList<String>();

		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < record.length; i++){
			//first, close any currently open reading fields:
			//"TAXID"
			if(readingTaxID && !(record[i].startsWith("OX   NCBI_TaxID=") || record[i].startsWith("OH   NCBI_TaxID="))){
				taxID = sb.toString();
				sb = new StringBuilder();
				readingTaxID = false;
			}
			//"GENE":
			if(readingGeneName && !record[i].startsWith("GN   ")){
				readingGeneName = false;
				geneName = sb.toString();
				sb = new StringBuilder();
			}
			//"ORGANISM":
			if(readingOrganism && !record[i].startsWith("OS   ")){
				organism = sb.toString();
				sb = new StringBuilder();
				readingOrganism = false;
			}
			//"GO":
			if(readingGO && !record[i].startsWith("DR   GO; ")){
				readingGO = false;
				GO = sb.toString();
				sb = new StringBuilder();
			}
			//"SUBCELLULAR":
			if(readingSubcellular && !record[i].startsWith("CC       ")){
				subcellularLocation = sb.toString();
				sb = new StringBuilder();
				readingSubcellular = false;
			}
			//case "FUNCTION":
			if(readingFunction && !record[i].startsWith("CC       ")){
				function = sb.toString();
				sb = new StringBuilder();
				readingFunction = false;
			}
			
			//NOW FILL IN THE FIELDS FROM THE DATA
			//case "ANYFIELD":
			anyfield = anyfield + " " + record[i].substring(5);
			
			//case  "ID"
			if(record[i].startsWith("ID   ")){
				ID = (record[i].substring(5, 5 + record[i].substring(5).indexOf(' ')));
				continue;
			}
			
			//case  "AC"
			if(record[i].startsWith("AC   ")){
				accessions.addAll(Arrays.asList(record[i].substring(5).split(";")));
				continue;
			}
			
			//case "TAXID":
			if(record[i].startsWith("OX   NCBI_TaxID=") || record[i].startsWith("OH   NCBI_TaxID=")){
				readingTaxID = true;
				sb.append(" " + record[i].substring(16));
				continue;
			}
			
			//case "GENE":
			if(record[i].startsWith("GN   Name=")){
				sb.append(record[i].substring(10));
				readingGeneName = true;
				continue;
			}else if(readingGeneName && record[i].startsWith("GN   ")){
					sb.append(record[i].substring(5));
					continue;
			}
			
			//case "ORGANISM":
			if(record[i].startsWith("OS   ")){
				sb.append(record[i].substring(5));
				readingOrganism = true;
				continue;
			}
					
			//case "GO":
			if(record[i].startsWith("DR   GO; ")){
				sb.append(record[i].substring(8));
				readingGO = true;
				continue;
			}
				
			//case "SUBCELLULAR":
			if(record[i].startsWith("CC   -!- SUBCELLULAR LOCATION:")){
				if(readingFunction){
					function = sb.toString();
					sb = new StringBuilder();
					readingFunction = false;
				}
				sb.append(record[i].substring(31));
				readingSubcellular = true;
				continue;
			}else if(readingSubcellular && record[i].startsWith("CC       ")){
				sb.append(record[i].substring(8));
				continue;
			}
					
			//case "FUNCTION":
			if(record[i].startsWith("CC   -!- FUNCTION:")){
				sb.append(record[i].substring(18));
				readingFunction = true;
				continue;
			}else if(readingFunction && record[i].startsWith("CC       ")){
				sb.append(record[i].substring(8));
				continue;
			}
		}
	}

	public String getFieldData(String fieldID) {
		if(fieldID.equals("ANYFIELD"))
			return anyfield;
		if(fieldID.equals("ID"))
			return ID;
		if(fieldID.equals("GENE"))
			return geneName;
		if(fieldID.equals("TAXID"))
			return taxID;
		if(fieldID.equals("ORGANISM"))
			return organism;
		if(fieldID.equals("GO"))
			return GO;
		if(fieldID.equals("SUBCELLULAR"))
			return subcellularLocation;
		if(fieldID.equals("FUNCTION"))
			return function;
		
		return "";
	}

}
