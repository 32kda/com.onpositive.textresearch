package com.onpositive.text.analysis.filtering;

import java.util.List;

import com.onpositive.semantic.wordnet.Grammem.PartOfSpeech;
import com.onpositive.text.analysis.IToken;
import com.onpositive.text.analysis.lexic.WordFormToken;

public abstract class VariantsFilter implements ITokenFilter {

	public VariantsFilter() {
		super();
	}

	protected boolean hasAnotherVariants(IToken token) {
		List<IToken> conflicts = token.getConflicts();
		return conflicts.stream().anyMatch(curToken -> !curToken.hasCorrelation() || curToken.getCorrelation() > 0.01);
	}
	
	protected PartOfSpeech getPartOfSpeech(IToken token) {
		if (!(token instanceof WordFormToken)) {
			return null;
		}
		return ((WordFormToken) token).getPartOfSpeech();
	}

}