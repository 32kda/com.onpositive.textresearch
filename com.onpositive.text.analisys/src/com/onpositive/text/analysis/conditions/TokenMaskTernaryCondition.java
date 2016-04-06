package com.onpositive.text.analysis.conditions;

import com.onpositive.text.analysis.IToken;

public class TokenMaskTernaryCondition extends TernaryCondition<Boolean> {

	public TokenMaskTernaryCondition(int[] mask) {
		this.mask = mask;
	}

	private final int[] mask;
	
	@Override
	public Boolean compute(IToken token0, IToken token1, IToken token2) {
		
		if(token0.getType()!=mask[0]){
			return false;
		}
		if(token1.getType()!=mask[1]){
			return false;
		}
		if(token2.getType()!=mask[2]){
			return false;
		}
		return true;
	}

}
