package com.onpositive.text.analysis.conditions;

import java.util.List;

import com.onpositive.text.analysis.IToken;

public abstract class AbstractCondition<T> {
	
	protected List<IToken> tokens;
	
	protected int tokensSize;
	
	public AbstractCondition(int dimension) {
		this.dimension = dimension;
	}
	private final int dimension;
	
	public int getDimension(){
		return this.dimension;
	}

	public void setTokens(List<IToken> tokens) {
		this.tokens = tokens;
		this.tokensSize = tokens.size();
	}
	
	public abstract T compute(int pos);
	
	public boolean acceptsNull(){return false;}
}
