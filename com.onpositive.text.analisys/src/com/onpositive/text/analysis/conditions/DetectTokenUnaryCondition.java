package com.onpositive.text.analysis.conditions;

import com.onpositive.text.analysis.IToken;

public class DetectTokenUnaryCondition extends UnaryCondition<IToken> {
	public DetectTokenUnaryCondition(int type) {
		super();
		this.maskCondition = new TokenMaskUnaryCondition(type);
	}

	private UnaryCondition<Boolean> maskCondition; 

	@Override
	public IToken compute(IToken token) {
		if(!maskCondition.compute(token)){
			return null;
		}
		return token;
	}
}
