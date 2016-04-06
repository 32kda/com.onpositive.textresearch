package com.onpositive.text.analysis.basic.matchers;

import java.util.ArrayList;
import java.util.List;

import com.onpositive.text.analysis.IToken;

public class NotChainMatcher extends ChainMatcher {
	
	public NotChainMatcher(String... args) {
		super(args);
		this.matchers = new ArrayList<ITokenArrayMatcher>();
		for(String str : args){
			ITokenArrayMatcher m = new TokenArrayMatcher(str);
			this.matchers.add(m);
		}
	}
	
	public NotChainMatcher(ITokenArrayMatcher... matchers) {
		super(matchers);
	}

	public NotChainMatcher(List<ITokenArrayMatcher> matchers) {
		super(matchers);
	}
	
	public boolean match(List<IToken> tokens, int pos){
		
		if(super.match(tokens, pos)){
			return false;
		}
		else{
			return true;
		}
	}
	
	@Override
	public String toString() {
		return "[!]" + super.toString();
	}

}
