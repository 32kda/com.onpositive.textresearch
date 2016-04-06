package com.onpositive.text.analysis.rules.matchers;

import com.onpositive.text.analysis.IToken;

public class OrMatcher<T extends IToken> extends UnaryMatcher<T> {

	protected UnaryMatcher<T>[]matchers;
	
	@SafeVarargs
	public OrMatcher(Class<T> clazz,UnaryMatcher<T>... matchers) {
		super(clazz);
		this.matchers=matchers;
	}
	

	@Override
	public boolean innerMatch(T token) {
		for (UnaryMatcher<T>m:matchers){
			if (m.innerMatch(token)){
				return true;
			}
		}
		return false;
	}
	
}
