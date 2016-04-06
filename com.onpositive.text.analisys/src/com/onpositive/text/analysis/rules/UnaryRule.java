package com.onpositive.text.analysis.rules;

import com.onpositive.text.analysis.IToken;

public abstract class UnaryRule extends TokenRule {

	@Override
	public boolean execute(int pos) {		
		
		IToken token = pos < tokensSize ?  tokens.get(pos) : null;
		return execute(token);
	}

	protected abstract boolean execute(IToken token);

}
