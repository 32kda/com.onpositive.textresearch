package com.onpositive.text.analysis.syntax;

import com.carrotsearch.hppc.IntOpenHashSet;
import com.onpositive.semantic.wordnet.AbstractWordNet;
import com.onpositive.semantic.wordnet.Grammem.PartOfSpeech;
import com.onpositive.text.analysis.IToken;

public class VerbAdverbParser extends VerbGroupParser{
	
	private static final IntOpenHashSet producedTokenTypes = new IntOpenHashSet();
	{
		producedTokenTypes.add(IToken.TOKEN_TYPE_VERB_ADVERB);
	}

	public VerbAdverbParser(AbstractWordNet wordNet) {
		super(wordNet);
	}

	@Override
	protected int getType(SyntaxToken token) {
		return IToken.TOKEN_TYPE_VERB_ADVERB;
	}

	@Override
	protected boolean checkAdditionalToken(IToken token) {		
		return and(hasAll(PartOfSpeech.ADVB), not(hasAny(PartOfSpeech.PREP, PartOfSpeech.CONJ))).match(token);
	}
	
	@Override
	protected IntOpenHashSet getProducedTokenTypes() {
		return producedTokenTypes;
	}

}
