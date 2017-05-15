package org.fhcrc.optides.apps.DatFileMining;

public abstract class AbsCondition {
	protected LogicFunction parent;
	protected boolean invertResult = false;

	public abstract boolean passesConditions(DatFileRecord record);
	public abstract boolean passesConditions(String[] record);
	
	AbsCondition(LogicFunction parent, boolean invertResult){
		this.parent = parent;
		this.invertResult = invertResult;
	}
	
	// Field: Key     Field will be one of the following TaxID, GO, SUBCELLULAR, FUNCTION, ANYFIELD
	protected static String getFieldDataFromDatRecord(String fieldID, String[] record) {
			StringBuilder retVal = new StringBuilder();
			boolean readingSubcellular = false;
			boolean readingFunction = false;
			boolean readingGeneName = false;

			for(int i = 0; i < record.length; i++){
				switch (fieldID){
					case "ID":
						if(record[i].startsWith("ID   "))
							retVal.append(record[i].substring(5, 5 + record[i].substring(5).indexOf(' ')));
						break;
					case "GENE":
						if(record[i].startsWith("GN   Name=")){
							retVal.append(record[i].substring(10));
							readingGeneName = true;
						}else if(readingGeneName && record[i].startsWith("GN   "))
								retVal.append(record[i].substring(5));
						else
							readingGeneName = false;
						break;
					case "ORGANISM":
						if(record[i].startsWith("OS   "))
							retVal.append(record[i].substring(5));
						break;
					case "ANYFIELD":
						retVal.append(" " + record[i].substring(5));
						break;
					case "GO":
						if(record[i].startsWith("DR   GO; "))
							retVal.append(record[i].substring(8));
						break;
					case "TAXID":
						if(record[i].startsWith("OX   NCBI_TaxID=") || record[i].startsWith("OH   NCBI_TaxID="))
							retVal.append(" " + record[i].substring(16));
						break;
					case "SUBCELLULAR":
						if(record[i].startsWith("CC   -!- SUBCELLULAR LOCATION:")){
							retVal.append(record[i].substring(31));
							readingSubcellular = true;
						}else if(readingSubcellular && record[i].startsWith("CC       "))
								retVal.append(record[i].substring(8));
						else
							readingSubcellular = false;
						break;
					case "FUNCTION":
						if(record[i].startsWith("CC   -!- FUNCTION:")){
							retVal.append(record[i].substring(18));
							readingFunction = true;
						}else if(readingFunction && record[i].startsWith("CC       "))
								retVal.append(record[i].substring(8));
						else
							readingFunction = false;
						break;
				}
			}
			return retVal.toString();
		}
		
}
