package org.fhcrc.optides.apps.DatFileMining;

public class DatFileCondition extends AbsCondition{
	private String fieldName = null;
	private String fieldValue = null;
	private boolean invertResult = false;
	
	DatFileCondition(LogicFunction parent, String fieldName, String fieldValue, boolean invertResult){
		super(parent, invertResult);
		this.fieldName = fieldName;
		this.fieldValue = fieldValue;
		this.invertResult = invertResult;
	}

	@Override
	public boolean passesConditions(DatFileRecord record) {
		boolean retVal = record.getFieldData(fieldName.toUpperCase()).toUpperCase().matches(".*\\b" + fieldValue.toUpperCase() + "\\b.*");
		
		if(invertResult)
			return !retVal;
		
		return retVal;
	}

	@Override
	public boolean passesConditions(String[] record) {
		//private boolean passesMapFilter(String[] record, String operator, Map<String, ArrayList<String>> map) {
		boolean retVal = getFieldDataFromDatRecord(fieldName.toUpperCase(), record).toUpperCase().matches(".*\\b" + fieldValue.toUpperCase() + "\\b.*");
					
		if(invertResult)
			return !retVal;
		
		return retVal;
	}
}
