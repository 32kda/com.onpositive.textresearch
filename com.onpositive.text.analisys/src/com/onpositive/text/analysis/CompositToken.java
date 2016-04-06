package com.onpositive.text.analysis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CompositToken extends AbstractToken {

	public CompositToken(Collection<IToken> tokens, int tokenType, int startPosition, int endPosition) {
		super(tokenType, startPosition, endPosition);
		this.tokens = new ArrayList<IToken>(tokens);
	}
	
	private List<IToken> tokens;

	@Override
	public String getStringValue() {
		return tokens.toString();
	}
	@Override
	public String getShortStringValue() {
		StringBuilder bld = new StringBuilder("["); 
		
		for (IToken token: tokens) {
			bld.append(token.getShortStringValue());
			if (token != tokens.get(tokens.size()-1)) bld.append(", "); 
		}
		
		bld.append("]");
		
		return bld.toString();
	}

	public List<IToken> getTokens() {
		return tokens;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((tokens == null) ? 0 : tokens.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		CompositToken other = (CompositToken) obj;
		if (tokens == null) {
			if (other.tokens != null)
				return false;
		} else if (!tokens.equals(other.tokens))
			return false;
		return true;
	}
	
	

}
