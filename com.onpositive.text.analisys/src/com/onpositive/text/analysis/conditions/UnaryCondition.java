package com.onpositive.text.analysis.conditions;

import com.onpositive.text.analysis.IToken;

public abstract class UnaryCondition<T> extends AbstractCondition<T> {

	public UnaryCondition() {
		super(1);
	}
	
	public abstract T compute(IToken token);
	
	public T compute(int pos){
		IToken token = pos < tokensSize ?  tokens.get(pos) : null;
		if(token==null&&!acceptsNull()){
			return null;
		}
		return compute(token);
	}

}
