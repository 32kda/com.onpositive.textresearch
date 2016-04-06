package com.onpositive.text.analysis.rules;

import com.onpositive.text.analysis.IToken;

public abstract class TernaryRule extends TokenRule {

	@Override
	public boolean execute(int pos) {		
		
		IToken token0 = pos < tokensSize ?  tokens.get(pos) : null;
		IToken token1 = pos < tokensSize-1 ?  tokens.get(pos+1) : null;
		IToken token2 = pos < tokensSize-2 ?  tokens.get(pos+2) : null;
				
		return execute(token0,token1,token2);
	}

	protected abstract boolean execute(IToken token0, IToken token1, IToken token2);

}
