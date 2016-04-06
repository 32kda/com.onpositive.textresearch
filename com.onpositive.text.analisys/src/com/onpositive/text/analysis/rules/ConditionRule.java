package com.onpositive.text.analysis.rules;

import java.util.List;

import com.onpositive.text.analysis.IToken;
import com.onpositive.text.analysis.conditions.AbstractCondition;

public class ConditionRule extends TokenRule {
	
	public ConditionRule(AbstractCondition<?> condition,RuleCallback<?> callBack) {
		this.callBack = callBack;
		this.condition = condition;
	}

	protected final RuleCallback<?> callBack;
	
	protected final AbstractCondition<?> condition;

	@SuppressWarnings("rawtypes")
	public RuleCallback getCallBack() {
		return callBack;
	}

	public AbstractCondition<?> getCondition() {
		return condition;
	}
	
	public int getDimension(){
		return this.condition.getDimension();
	}
	
	public boolean acceptsNull(){return false;}

	@SuppressWarnings("unchecked")
	@Override
	public boolean execute(int pos) {

		Object result = condition.compute(pos);
		if(result!=null){
			getCallBack().execute(result);
			return true;
		}
		else{
			return false;
		}
	}
	
	@Override
	public void setTokens(List<IToken> tokens) {
		super.setTokens(tokens);
		this.condition.setTokens(tokens);
	}

}
