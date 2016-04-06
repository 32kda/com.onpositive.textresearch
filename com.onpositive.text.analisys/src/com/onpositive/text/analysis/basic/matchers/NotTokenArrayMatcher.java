package com.onpositive.text.analysis.basic.matchers;

import java.util.List;

import com.onpositive.text.analysis.IToken;

public class NotTokenArrayMatcher extends TokenArrayMatcher{
	
	public NotTokenArrayMatcher(String str) {
		super(str);
	}
	
	public NotTokenArrayMatcher(ITokenMatcher... tokenMatchers) {
		super(tokenMatchers);
	}
	
	public NotTokenArrayMatcher(List<ITokenMatcher> startPattens) {
		super(startPattens);
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