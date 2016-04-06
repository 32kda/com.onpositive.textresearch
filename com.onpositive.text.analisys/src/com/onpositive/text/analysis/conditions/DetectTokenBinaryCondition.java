package com.onpositive.text.analysis.conditions;

import com.onpositive.text.analysis.IToken;

public class DetectTokenBinaryCondition extends BinaryCondition<IToken> {
	public DetectTokenBinaryCondition(int[] mask) {
		super();
		this.maskCondition = new TokenMaskBinaryCondition(mask);
	}

	private BinaryCondition<Boolean> maskCondition; 

	@Override
	public IToken compute(IToken token0,IToken token1) {
		if(!maskCondition.compute(token0,token1)){
			return null;
		}
		return token0;
	}
}