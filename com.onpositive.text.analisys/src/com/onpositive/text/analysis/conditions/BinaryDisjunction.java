package com.onpositive.text.analysis.conditions;

import java.util.List;

import com.onpositive.text.analysis.IToken;

public class BinaryDisjunction<T> extends BinaryCondition<T> {

	public BinaryDisjunction(BinaryCondition<T>[] components) {
		super();
		this.components = components;
	}

	private BinaryCondition<T>[] components;	
	
	@Override
	public T compute(IToken token0, IToken token1) {
		
		for(BinaryCondition<T> bc : components){
			T value = bc.compute(token0, token1);
			if(value != null){
				return value;
			}
		}
		return null;
	}
	
	@Override
	public void setTokens(List<IToken> tokens) {
		super.setTokens(tokens);
		for(BinaryCondition<T> bc : components){
			bc.setTokens(tokens);
		}
	}

}
