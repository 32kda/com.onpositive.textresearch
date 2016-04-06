package com.onpositive.text.analysis.lexic;

import com.onpositive.text.analysis.AbstractToken;

public class StringToken extends AbstractToken{	

	protected StringToken( String value, int tokenType,	int startPosition, int endPosition) {
		super(tokenType, startPosition, endPosition);
		
		this.value = value;
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
		StringToken other = (StringToken) obj;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

}
