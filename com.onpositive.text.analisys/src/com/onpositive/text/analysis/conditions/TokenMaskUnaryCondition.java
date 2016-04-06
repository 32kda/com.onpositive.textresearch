package com.onpositive.text.analysis.conditions;

import com.onpositive.text.analysis.IToken;

public class TokenMaskUnaryCondition extends UnaryCondition<Boolean> {
	
	public TokenMaskUnaryCondition(int mask) {
		this.mask = mask;
	}

	public TokenMaskUnaryCondition(int[] mask) {
		this.mask = mask[0];
	}

	private final int mask;
	
	@Override
	public Boolean compute(IToken token) {
		
		if(token.getType()!=mask){
			return false;
		}
		return true;
	}

}
