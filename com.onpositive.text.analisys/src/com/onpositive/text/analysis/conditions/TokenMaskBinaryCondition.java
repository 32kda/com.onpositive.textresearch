package com.onpositive.text.analysis.conditions;

import com.onpositive.text.analysis.IToken;

public class TokenMaskBinaryCondition extends BinaryCondition<Boolean> {

	public TokenMaskBinaryCondition(int[] mask) {
		this.mask = mask;
	}

	private final int[] mask;
	
	@Override
	public Boolean compute(IToken token0, IToken token1) {
		
		if(token0.getType()!=mask[0]){
			return false;
		}
		if(token1.getType()!=mask[1]){
			return false;
		}
		return true;
	}

}
