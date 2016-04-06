package com.onpositive.text.analysis.lexic.disambig;

import com.onpositive.text.analysis.IToken;
import com.onpositive.text.analysis.lexic.WordFormToken;

public interface ILexicLevelDisambiguator {

	/**
	 * 
	 * @param wordFormTokens
	 * @param tokens map of wormeaning id to token;
	 * @return new wordform tokens;
	 */
	WordFormToken[] disambiguate(WordFormToken[] wordFormTokens, IToken origToken);

}
