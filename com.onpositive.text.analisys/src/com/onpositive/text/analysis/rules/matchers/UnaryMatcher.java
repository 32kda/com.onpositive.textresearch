package com.onpositive.text.analysis.rules.matchers;

import com.onpositive.text.analysis.IToken;

public abstract class UnaryMatcher<T extends IToken> {

	private final Class<T> clazz;

	public UnaryMatcher(Class<T> clazz) {
		super();
		this.clazz = clazz;
	}
	
	public final boolean match(IToken token){
		if (clazz.isInstance(token)){
			return innerMatch(clazz.cast(token));
		}
		return false;
	}

	protected abstract boolean innerMatch(T token);
	
	public Class<T>getTokenClass(){
		return clazz;
	}
}
