package com.onpositive.text.analysis.filtering;

import com.onpositive.text.analysis.IToken;

public interface ITokenFilter {

	public boolean shouldFilterOut(IToken token);
	
}
