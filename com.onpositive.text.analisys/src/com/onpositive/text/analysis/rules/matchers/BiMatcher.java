package com.onpositive.text.analysis.rules.matchers;

import com.onpositive.text.analysis.IToken;

public class BiMatcher {

	UnaryMatcher<?>m1;
	public BiMatcher(UnaryMatcher<?> m1, UnaryMatcher<?> m2) {
		super();
		this.m1 = m1;
		this.m2 = m2;
	}

	UnaryMatcher<?>m2;
	
	public boolean match(IToken  t1,IToken t2){
		return this.m1.match(t1)&&this.m2.match(t2);
	}
}
