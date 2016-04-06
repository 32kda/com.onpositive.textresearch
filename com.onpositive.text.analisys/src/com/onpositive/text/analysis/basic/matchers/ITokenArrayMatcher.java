package com.onpositive.text.analysis.basic.matchers;

import java.util.List;

import com.onpositive.text.analysis.IToken;

public interface ITokenArrayMatcher{
	
	boolean match(List<IToken> tokens, int pos);
	
	int length();

	void setAcceptEnd(boolean acceptEnd);

	boolean isAcceptEnd();

	void setAcceptStart(boolean acceptStart);

	boolean isAcceptStart();
}