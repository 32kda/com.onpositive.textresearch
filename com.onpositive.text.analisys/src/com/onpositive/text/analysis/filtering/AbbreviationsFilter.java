package com.onpositive.text.analysis.filtering;

import com.onpositive.semantic.wordnet.Grammem.PartOfSpeech;
import com.onpositive.semantic.wordnet.Grammem.SemanGramem;
import com.onpositive.text.analysis.IToken;
import com.onpositive.text.analysis.lexic.WordFormToken;
import com.onpositive.text.analysis.syntax.SyntaxToken;
import com.onpositive.text.analysis.utils.AdditionalMetadataHandler;

public class AbbreviationsFilter extends VariantsFilter {

	@Override
	public boolean shouldFilterOut(IToken token) {
		boolean filtered = hasAnotherVariants(token) && token instanceof WordFormToken &&
			((SyntaxToken) token).hasGrammem(PartOfSpeech.NOUN) && 
			((SyntaxToken) token).hasGrammem(SemanGramem.ABBR);
		if (filtered) {
			AdditionalMetadataHandler.store(token, AdditionalMetadataHandler.FILTER_KEY, "Аббревиатура");
		}
		return filtered;
	}

}
