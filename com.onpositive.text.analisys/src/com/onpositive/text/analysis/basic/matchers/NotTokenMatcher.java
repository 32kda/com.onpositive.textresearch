package com.onpositive.text.analysis.basic.matchers;

import com.carrotsearch.hppc.IntOpenHashSet;
import com.onpositive.text.analysis.IToken;

public class NotTokenMatcher extends TokenMatcher{
	
	public NotTokenMatcher( String value, int... tokenTypes) {
		super(value, tokenTypes);
	}

	IntOpenHashSet tokenTypes;
	
	String value;

	@Override
	public boolean match(IToken token){
		return !super.match(token);
	}
	
	
	@Override
	public String toString() {
		return "[!]"+value;
	}
}