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
		boolean retValue = false;
		// TODO order of op: NOT AND OR
		if(conditions.size() == 1 && operators.size() == 0){
			retValue = conditions.get(0).passesConditions(record);
			if(invertResult)
				return !retValue;
			return retValue;
		}
		
		ArrayList<AbsCondition> tmpConditions = new ArrayList<AbsCondition>();
		ArrayList<String> tmpOperators = new ArrayList<String>();
		ArrayList<AbsCondition> tmpConditions2 = new ArrayList<AbsCondition>();
		ArrayList<String> tmpOperators2 = new ArrayList<String>();
	
		int i = 0;
		for(; i < operators.size(); i++){
			String curOperator = operators.get(i);
			if(curOperator.equals("AND")){
				tmpConditions.add(new DatFileMiningBoolean(conditions.get(i).parent, false, conditions.get(i).passesConditions(record) && conditions.get(i+1).passesConditions(record)));
			}else{
				tmpConditions.add(conditions.get(i));
				tmpOperators.add(operators.get(i));
			}
		}
		//always one more condition than operators
		tmpConditions.add(conditions.get(i));
		
		if(tmpOperators.size() == 0){ //all operations were ANDs
			retValue = true;
			for(i=0; i < tmpConditions.size(); i++)
				retValue = retValue && tmpConditions.get(i).passesConditions(record);
			if(invertResult)
				return !retValue;
			return retValue;
		}
		
		for(i = 0; i < tmpOperators.size(); i++){
			String curOperator = tmpOperators.get(i);
			if(curOperator.equals("OR")){
				tmpConditions2.add(new DatFileMiningBoolean(tmpConditions.get(i).parent, false, tmpConditions.get(i+1).passesConditions(record) || tmpConditions.get(i).passesConditions(record)));
			}else{
				tmpConditions2.add(tmpConditions.get(i));
				tmpOperators2.add(tmpOperators.get(i));
			}
		}
		//always one more condition than operator
		tmpConditions2.add(tmpConditions.get(i));

		assert(tmpOperators2.size() == 0);
		retValue = true;
		for(i=0; i < tmpConditions2.size(); i++)
			retValue = retValue && tmpConditions2.get(i).passesConditions(record);
		
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
		if(conditions.size() == 1 && operators.size() == 0)
			return conditions.get(0).passesConditions(rec);
		
		ArrayList<AbsCondition> tmpConditions = new ArrayList<AbsCondition>();
		ArrayList<String> tmpOperators = new ArrayList<String>();
		ArrayList<AbsCondition> tmpConditions2 = new ArrayList<AbsCondition>();
		ArrayList<String> tmpOperators2 = new ArrayList<String>();
	
		int i = 0;
		for(; i < operators.size(); i++){
			String curOperator = operators.get(i);
			if(curOperator.equals("AND")){
				tmpConditions.add(new DatFileMiningBoolean(conditions.get(i).parent, false, conditions.get(i).passesConditions(rec) && conditions.get(i+1).passesConditions(rec)));
			}else{
				tmpConditions.add(conditions.get(i));
				tmpOperators.add(operators.get(i));
			}
		}
		//always one more condition than operators
		if(!operators.get(i-1).equals("AND"))
			tmpConditions.add(conditions.get(i));
		
		if(tmpOperators.size() == 0){ //all operations were ANDs
			boolean retValue = true;
			for(i=0; i < tmpConditions.size(); i++)
				retValue = retValue && tmpConditions.get(i).passesConditions(rec);
			return retValue;
		}
		
		for(i = 0; i < tmpOperators.size(); i++){
			String curOperator = tmpOperators.get(i);
			if(curOperator.equals("OR")){
				tmpConditions2.add(new DatFileMiningBoolean(tmpConditions.get(i).parent, false, tmpConditions.get(i).passesConditions(rec) || tmpConditions.get(i+1).passesConditions(rec)));
			}else{
				tmpConditions2.add(tmpConditions.get(i));
				tmpOperators2.add(tmpOperators.get(i));
			}
		}

		assert(tmpOperators2.size() == 0);
		boolean retValue = false;
		for(i=0; i < tmpConditions2.size(); i++)
			retValue = retValue || tmpConditions2.get(i).passesConditions(rec);
		
		if(invertResult)
			return !retValue;
		
		return retValue;
	}
	
}
