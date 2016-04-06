package com.onpositive.text.analysis.lexic;

import com.onpositive.text.analysis.IToken;
import com.onpositive.text.analysis.lexic.dimension.Unit;
import com.onpositive.text.analysis.syntax.SyntaxToken;


public class DimensionToken extends SyntaxToken {

	public DimensionToken(IToken scalar, UnitToken unitToken, int startPosition, int endPosition) {
		super(IToken.TOKEN_TYPE_DIMENSION, unitToken, null, startPosition, endPosition);
		this.scalar = scalar;
		this.unit = unitToken.getUnit();
	}

	private final IToken scalar;
	
	private final Unit unit;
	
	public double getValue(){
		return ((ScalarToken)scalar).getValue();
	}
	
	@Override
	public String getStringValue() {
		return scalar.getStringValue()+' '+unit.toString();
	}

	public IToken getScalar() {
		return scalar;
	}

	public Unit getUnit() {
		return unit;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((scalar == null) ? 0 : scalar.hashCode());
		result = prime * result + ((unit == null) ? 0 : unit.hashCode());
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
		DimensionToken other = (DimensionToken) obj;
		if (scalar == null) {
			if (other.scalar != null)
				return false;
		} else if (!scalar.equals(other.scalar))
			return false;
		if (unit == null) {
			if (other.unit != null)
				return false;
		} else if (!unit.equals(other.unit))
			return false;
		return true;
	}

}
