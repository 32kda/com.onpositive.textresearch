package com.onpositive.text.analysis.basic.matchers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.onpositive.text.analysis.IToken;

public class ChainMatcher implements ITokenArrayMatcher {
	
	public ChainMatcher(String... args) {
		super();
		this.matchers = new ArrayList<ITokenArrayMatcher>();
		for(String str : args){
			ITokenArrayMatcher m = new TokenArrayMatcher(str);
			this.matchers.add(m);
		}
	}

	public ChainMatcher(List<ITokenArrayMatcher> matchers) {
		super();
		this.matchers = matchers;
	}
	
	public ChainMatcher(ITokenArrayMatcher... matchers) {
		super();
		this.matchers = Arrays.asList(matchers);
	}

	List<ITokenArrayMatcher> matchers;	
	
	@Override
	public boolean match(List<IToken> tokens, int pos) {
		int i = 0;
		for(ITokenArrayMatcher m : this.matchers){
			if(!m.match(tokens, pos+i)){
				return false;
			}
			i += m.length();
		}
		return true;
	}

	@Override
	public int length() {
		int l = 0 ;
		for(ITokenArrayMatcher m : this.matchers){
			l += m.length();
		}
		return l;
	}

	public void setAcceptEnd(boolean acceptEnd) {
	}

	public boolean isAcceptEnd() {
		return false;
	}

	public void setAcceptStart(boolean acceptStart) {
	}

	public boolean isAcceptStart() {
		return false;
	}
	@Override
	public String toString() {
		return "[chain]" + matchers.toString();
	}
}
