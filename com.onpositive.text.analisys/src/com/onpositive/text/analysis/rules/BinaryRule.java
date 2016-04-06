package com.onpositive.text.analysis.rules;

import com.onpositive.text.analysis.IToken;

public abstract class BinaryRule extends TokenRule {

	@Override
	public boolean execute(int pos) {		
		
		IToken token0 = pos < tokensSize ?  tokens.get(pos) : null;
		IToken token1 = pos < tokensSize-1 ?  tokens.get(pos+1) : null;
				
		return execute(token0,token1);
	}

	protected abstract boolean execute(IToken token0, IToken token1);

}
