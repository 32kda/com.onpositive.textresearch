package com.onpositive.text.analysis.rules;

import java.util.List;

import com.onpositive.text.analysis.IToken;

abstract public class TokenRule {
	
	protected List<IToken> tokens;
	
	protected int tokensSize;
	
	public abstract boolean execute(int pos);

	public void setTokens(List<IToken> tokens) {
		this.tokens = tokens;
		this.tokensSize = tokens.size();
	}	
}
