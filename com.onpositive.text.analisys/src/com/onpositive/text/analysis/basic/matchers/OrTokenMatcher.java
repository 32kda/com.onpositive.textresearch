package com.onpositive.text.analysis.basic.matchers;

import java.util.Arrays;
import java.util.List;

import com.onpositive.text.analysis.IToken;

public class OrTokenMatcher implements ITokenMatcher {

	
	public OrTokenMatcher(List<ITokenMatcher> matchers) {
		super();
		this.matchers = matchers;
	}
	
	public OrTokenMatcher(String str) {
		super();		
		this.matchers = TokenMatcher.forString(str);
	}
	
	public OrTokenMatcher(String str, boolean acceptStart, boolean acceptEnd) {
		super();
		this.matchers = TokenMatcher.forString(str);
	}


	public OrTokenMatcher(ITokenMatcher... matchers) {
		super();
		this.matchers = Arrays.asList(matchers);
	}


	List<ITokenMatcher> matchers;
	
	
	@Override
	public boolean match(IToken token) {
		
		for(ITokenMatcher m : matchers){
			if(m.match(token)){
				return true;
			}
		}
		return false;
	}
	
	@Override
	public String toString() {
		StringBuilder bld = new StringBuilder();
		for(ITokenMatcher m : matchers){
			bld.append(m.toString()).append("|");
		}
		String result = bld.substring(0, bld.length()-1);
		return result;
	}

}
