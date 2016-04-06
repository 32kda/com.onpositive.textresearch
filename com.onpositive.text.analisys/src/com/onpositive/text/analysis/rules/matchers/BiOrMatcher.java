package com.onpositive.text.analysis.rules.matchers;

import com.onpositive.text.analysis.IToken;

public class BiOrMatcher {

	BiMatcher[] matchers;
	public BiOrMatcher(BiMatcher... matchers) {
		super();
		this.matchers = matchers;
	}

	BiMatcher m2;
	
	public boolean match(IToken  t1,IToken t2){
		for(BiMatcher bm : matchers){
			if(bm.match(t1, t2)){
				return true;
			}
		}
		return false;
	}
}
