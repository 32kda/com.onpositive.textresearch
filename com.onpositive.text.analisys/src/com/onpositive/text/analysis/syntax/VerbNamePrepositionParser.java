package com.onpositive.text.analysis.syntax;

import com.onpositive.semantic.wordnet.AbstractWordNet;
import com.onpositive.semantic.wordnet.Grammem.PartOfSpeech;
import com.onpositive.text.analysis.IToken;
import com.onpositive.text.analysis.rules.matchers.UnaryMatcher;

public class VerbNamePrepositionParser extends VerbPrepositionGroupParser {
	

	public VerbNamePrepositionParser(AbstractWordNet wordNet) {
		super(wordNet);
	}
	
	private static final UnaryMatcher<SyntaxToken> matcher = hasAny(PartOfSpeech.ADJF,PartOfSpeech.NOUN,PartOfSpeech.ADVB,PartOfSpeech.NPRO);

	@Override
	protected int getType(SyntaxToken token) {
		
		if(nounMatch.match(token)||nproMatch.match(token)){
			return IToken.TOKEN_TYPE_VERB_NOUN_PREP;
		}
		else if(adjectiveMatch.match(token)){
			return IToken.TOKEN_TYPE_VERB_ADJECTIVE_PREP;
		}
		else if(adverbMatch.match(token)){
			return IToken.TOKEN_TYPE_VERB_ADVERB_PREP;
		}
		return -1;
	}

	@Override
	protected boolean checkAdditionalToken(IToken token) {		
		boolean result = matcher.match(token);
		return result;
	}

}
