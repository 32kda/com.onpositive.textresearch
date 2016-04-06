package com.onpositive.text.analysis.projection;

import java.util.List;

import com.onpositive.text.analysis.IToken;

public class Projection {
	
	public final List<IToken> tokens;
	
	public final List<IToken> original;

	public Projection(List<IToken> tokens, List<IToken> original) {
		super();
		this.tokens = tokens;
		this.original = original;
	}

}
