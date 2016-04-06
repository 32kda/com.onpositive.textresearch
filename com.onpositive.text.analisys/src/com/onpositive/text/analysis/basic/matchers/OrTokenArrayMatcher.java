package com.onpositive.text.analysis.basic.matchers;

import java.util.ArrayList;
import java.util.List;

import com.onpositive.text.analysis.IToken;

public class OrTokenArrayMatcher implements ITokenArrayMatcher{

	public OrTokenArrayMatcher(List<ITokenArrayMatcher> markers) {
		super();
		this.matchers = markers;
	}
	
	public OrTokenArrayMatcher(String... args) {
		super();
		this.matchers = new ArrayList<ITokenArrayMatcher>();
		for(String s : args){
			ITokenArrayMatcher m = new TokenArrayMatcher(s);
			this.matchers.add(m);
		}
	}

	private List<ITokenArrayMatcher> matchers;

	@Override
	public boolean match(List<IToken> tokens, int pos) {
		
		if(matchers==null){
			return false;
		}
		
		for(ITokenArrayMatcher m : matchers){
			boolean matched = m.match(tokens, pos);
			if(matched){
				return true;
			}
		}
		return false;
	}

	@Override
	public int length() {
		return matchers.get(0).length();
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
		return "[OR]" + matchers.toString();
	}
	
}