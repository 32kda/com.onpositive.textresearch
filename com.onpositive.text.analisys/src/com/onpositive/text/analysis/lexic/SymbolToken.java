package com.onpositive.text.analysis.lexic;

import com.onpositive.text.analysis.AbstractToken;
import com.onpositive.text.analysis.IToken;

public class SymbolToken extends AbstractToken {

	protected SymbolToken(String value, int startPosition, int endPosition) {
		super(IToken.TOKEN_TYPE_SYMBOL, startPosition, endPosition);
		
		this.value = value.intern();
	}
	
	public SymbolToken(char ch, int startPosition, int endPosition) {
		super(IToken.TOKEN_TYPE_SYMBOL, startPosition, endPosition);
		
		this.value = ("" + ch).intern();
	}

	private final String value;

	@Override
	public String getStringValue() {
		return value;
	}
	
	@Override
	public String getShortStringValue() {
		return value;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((value == null) ? 0 : value.hashCode());
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
		SymbolToken other = (SymbolToken) obj;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}
	
	public boolean isUnaryOperator() {
		return value.equals("-") || value.equals("+");
	}	
}
