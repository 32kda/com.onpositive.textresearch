package com.onpositive.text.analysis.rules.matchers;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.onpositive.semantic.wordnet.Grammem;
import com.onpositive.text.analysis.syntax.SyntaxToken;

public class HasAllGrammems extends UnaryMatcher<SyntaxToken>{

	private Set<Grammem> grammems;

	public HasAllGrammems(Grammem... gramems) {
		super(SyntaxToken.class);
		this.grammems=new HashSet<Grammem>(Arrays.asList(gramems));
	}

	@Override
	public boolean innerMatch(SyntaxToken token) {
		return token.hasAllGrammems(grammems);
	}

}
