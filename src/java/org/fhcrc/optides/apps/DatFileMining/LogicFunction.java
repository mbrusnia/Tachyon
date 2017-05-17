package org.fhcrc.optides.apps.DatFileMining;

import java.util.ArrayList;

public class LogicFunction extends AbsCondition {

	private ArrayList<AbsCondition> conditions;
	private ArrayList<String> operators;
	
	LogicFunction(LogicFunction parent, boolean invertResult) {
		super(parent, invertResult);
		conditions = new ArrayList<AbsCondition>();
		operators = new ArrayList<String>();
	}


	@Override
	public boolean passesConditions(DatFileRecord record) {
		return passesConditions(record, 0);
	}
	
	public boolean passesConditions(DatFileRecord record, int opsOffset) {
		boolean retValue = conditions.get(opsOffset).passesConditions(record);
		for(int i = opsOffset; i < operators.size(); i++){
			if(operators.get(i).equals("OR")){
				retValue = retValue || this.passesConditions(record, i + 1);
				break;
			}else{
				retValue = retValue && conditions.get(i+1).passesConditions(record);
			}
		}
		
		if(invertResult)
			return !retValue;
		return retValue;
	}
	
	public void addCondition(AbsCondition condition){
		conditions.add(condition);
	}
	
	public void addOperator(String operator){
		operators.add(operator);
	}


	public boolean passesConditions(String[] rec) {
		return passesConditions(rec, 0);
	}

	public boolean passesConditions(String[] rec, int opsOffset) {		
		boolean retValue = conditions.get(opsOffset).passesConditions(rec);
		for(int i = opsOffset; i < operators.size(); i++){
			if(operators.get(i).equals("OR")){
				retValue = retValue || this.passesConditions(rec, i + 1);
				break;
			}else{
				retValue = retValue && conditions.get(i+1).passesConditions(rec);
			}
		}
		
		if(invertResult)
			return !retValue;
		return retValue;
	}
	
}
