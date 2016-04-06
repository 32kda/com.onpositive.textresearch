package com.onpositive.text.analysis.conditions;

import com.onpositive.text.analysis.IToken;

public abstract class BinaryCondition<T> extends AbstractCondition<T> {
	
	public BinaryCondition() {
		super(2);
	}

	public abstract T compute(IToken token0, IToken token1);
	
	public T compute(int pos){
		IToken token0 = pos < tokensSize ?  tokens.get(pos) : null;
		IToken token1 = pos < tokensSize-1 ?  tokens.get(pos+1) : null;
		if(token1==null&&!acceptsNull()){
			return null;
		}
		return compute(token0, token1);
	}
}
