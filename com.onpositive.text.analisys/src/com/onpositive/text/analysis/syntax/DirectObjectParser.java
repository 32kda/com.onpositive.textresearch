package com.onpositive.text.analysis.syntax;

import com.carrotsearch.hppc.IntOpenHashSet;
import com.onpositive.semantic.wordnet.AbstractWordNet;
import com.onpositive.semantic.wordnet.Grammem;
import com.onpositive.semantic.wordnet.Grammem.Case;
import com.onpositive.semantic.wordnet.Grammem.PartOfSpeech;
import com.onpositive.text.analysis.IToken;
import com.onpositive.text.analysis.rules.matchers.UnaryMatcher;

public class DirectObjectParser extends VerbGroupParser {

	private static final UnaryMatcher<SyntaxToken> nomnCaseMatcher = hasAny(Case.NOMN);
	
	private static final IntOpenHashSet producedTokenTypes = new IntOpenHashSet();
	{
		producedTokenTypes.add(IToken.TOKEN_TYPE_DIRECT_OBJECT_INF);
		producedTokenTypes.add(IToken.TOKEN_TYPE_DIRECT_OBJECT_NAME);
	}


	public DirectObjectParser(AbstractWordNet wordNet) {
		super(wordNet);
	}

	private static final UnaryMatcher<SyntaxToken> acceptedNames = hasAny(
			PartOfSpeech.NOUN, PartOfSpeech.NPRO/*, PartOfSpeech.ADJF*/);
	private static final UnaryMatcher<SyntaxToken> acceptedAcc = hasAny(caseMatchMap
			.get(Case.ACCS));

	private static final UnaryMatcher<SyntaxToken> acceptedGC = hasAny(caseMatchMap
			.get(Case.GENT));

	protected static final UnaryMatcher<SyntaxToken> directObjectCasesMatch
		= or(acceptedAcc, and(acceptedGC, not(has(Grammem.SingularPlural.SINGULAR))));
	
	private final UnaryMatcher<SyntaxToken> checkName = and(
			acceptedNames,
			directObjectCasesMatch);
	

	@Override
	protected boolean checkVerb(IToken token) {
		return transitiveVerbMatch.match(token);
	}

	@Override
	protected boolean checkAdditionalToken(IToken token) {
		return and(checkName,not(prepConjMatch)).match(token);
	}

	@Override
	protected int getType(SyntaxToken objToken) {
		int subjType = infnMatch.match(objToken) ? IToken.TOKEN_TYPE_DIRECT_OBJECT_INF
				: IToken.TOKEN_TYPE_DIRECT_OBJECT_NAME;
		return subjType;
	}
	
	@Override
	protected boolean isDoubtful(SyntaxToken[] orderedTokens) {		
		SyntaxToken objToken = orderedTokens[1];
		boolean result = nomnCaseMatcher.match(objToken);
		return result;
	}

	@Override
	protected IntOpenHashSet getProducedTokenTypes() {
		return producedTokenTypes;
	}
}
