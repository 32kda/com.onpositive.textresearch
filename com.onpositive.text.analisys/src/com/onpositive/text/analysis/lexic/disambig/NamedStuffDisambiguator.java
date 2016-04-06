package com.onpositive.text.analysis.lexic.disambig;

import java.util.ArrayList;
import java.util.HashSet;

import com.onpositive.semantic.wordnet.Grammem;
import com.onpositive.text.analysis.IToken;
import com.onpositive.text.analysis.lexic.WordFormToken;

public class NamedStuffDisambiguator implements ILexicLevelDisambiguator {

	static HashSet<Grammem> named = new HashSet<Grammem>();

	static {
		named.add(Grammem.SemanGramem.PATR);
		named.add(Grammem.SemanGramem.SURN);
		//named.add(Grammem.SemanGramem.NAME);
		named.add(Grammem.SemanGramem.TOPONIM);
	}
	//TODO FIXME

	@Override
	public WordFormToken[] disambiguate(WordFormToken[] wordFormTokens,
			IToken origToken) {
		ArrayList<WordFormToken> ll = new ArrayList<WordFormToken>();
		for (WordFormToken c : wordFormTokens) {
			boolean lowerCase = Character.isLowerCase(origToken
					.getStringValue().charAt(0));
			if (c.hasAnyGrammem(named) && lowerCase) {
				continue;
			}
			ll.add(c);
		}
		if (!ll.isEmpty()) {
			return ll.toArray(new WordFormToken[ll.size()]);
		}
		return wordFormTokens;
	}

}