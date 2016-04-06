package com.onpositive.text.analysis.basic.matchers;

import com.onpositive.text.analysis.IToken;

public interface ITokenMatcher {

	public abstract boolean match(IToken token);

}