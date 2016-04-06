package com.onpositive.text.analysis.basic.matchers;

import java.util.Arrays;
import java.util.List;

import com.onpositive.text.analysis.IToken;

public class TokenArrayMatcher implements ITokenArrayMatcher{
	
	public TokenArrayMatcher(String str) {
		this(TokenMatcher.forString(str));
	}
	
	public TokenArrayMatcher(List<ITokenMatcher> tokenMatchers) {
		super();
		this.tokenMatchers = tokenMatchers;
		tokenMatchers.size();
	}
	
	public TokenArrayMatcher(ITokenMatcher... tokenMatchers) {
		super();
		this.tokenMatchers = Arrays.asList(tokenMatchers);
		this.tokenMatchers.size();
	}
	
	public TokenArrayMatcher(List<ITokenMatcher> startPattens, int targetLength) {
		super();
		this.tokenMatchers = startPattens;
	}
	
	private List<ITokenMatcher> tokenMatchers;
	
	private boolean acceptStart;
	
	private boolean acceptEnd;
	
	public boolean match(List<IToken> tokens, int pos){
		
		if(acceptStart&&pos>=0){
			return false;
		}
		
		boolean result = true;
		int i = 0;
		if(acceptStart){
			pos = 0;
		}
		for(int j = 0 ; j < tokenMatchers.size() ; j++){
			ITokenMatcher mp = tokenMatchers.get(j);
			int ind = pos + i;
			if(ind<0){
				if(acceptStart){
					continue;
				}
				else{
					return false;
				}
			}
			if(tokens.size()<=ind){
				if(tokens.size()==ind && acceptEnd && j == tokenMatchers.size()-1){
					continue;
				}
				result = false;
				break;
			}
			if(!mp.match(tokens.get(ind))){
				result = false;
				break;
			}
			i++;
		}
		return result;
	}

	@Override
	public int length() {
		return tokenMatchers.size();
	}

	@Override
	public boolean isAcceptStart() {
		return acceptStart;
	}

	@Override
	public void setAcceptStart(boolean acceptStart) {
		this.acceptStart = acceptStart;
	}

	@Override
	public boolean isAcceptEnd() {
		return acceptEnd;
	}

	@Override
	public void setAcceptEnd(boolean acceptEnd) {
		this.acceptEnd = acceptEnd;
	}
	
	@Override
	public String toString() {
		return tokenMatchers.toString();
	}
}