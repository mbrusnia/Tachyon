package org.fhcrc.optides.apps.DatFileMining;

public class DatFileMiningBoolean extends AbsCondition {

	private boolean value;

	DatFileMiningBoolean(LogicFunction parent, boolean invertResult, boolean value) {
		super(parent, invertResult);
		this.value = value;
	}
	
	@Override
	public boolean passesConditions(DatFileRecord record) {
		if(invertResult)
			return !value;
		
		return value;
	}

	@Override
	public boolean passesConditions(String[] record) {
		if(invertResult)
			return !value;
		
		return value;
	}

}
