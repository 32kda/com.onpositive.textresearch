package com.onpositive.text.analysis.rules.matchers;

import com.onpositive.semantic.wordnet.Grammem;
import com.onpositive.text.analysis.syntax.SyntaxToken;

public class HasGrammem extends UnaryMatcher<SyntaxToken>{

	private Grammem grammems;

	public HasGrammem(Grammem gramems) {
		super(SyntaxToken.class);
		this.grammems=gramems;
	}

	@Override
	public boolean innerMatch(SyntaxToken token) {
		return token.hasGrammem(grammems);
	}

}
