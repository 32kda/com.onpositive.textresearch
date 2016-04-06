package com.onpositive.text.analysis.conditions;

import com.onpositive.text.analysis.IToken;

public abstract class TernaryCondition<T> extends AbstractCondition<T> {
	
	public TernaryCondition() {
		super(3);
	}

	public abstract T compute(IToken token0, IToken token1, IToken token2);
	
	public T compute(int pos){
		IToken token0 = pos < tokensSize ?  tokens.get(pos) : null;
		IToken token1 = pos < tokensSize-1 ?  tokens.get(pos+1) : null;
		IToken token2 = pos < tokensSize-2 ?  tokens.get(pos+2) : null;
		if(token2==null&&!acceptsNull()){
			return null;
		}		
		return compute(token0, token1, token2);
	}
}