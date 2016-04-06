package com.onpositive.text.analysis.syntax;

import java.util.Stack;

import com.onpositive.semantic.wordnet.AbstractWordNet;
import com.onpositive.semantic.wordnet.Grammem.PartOfSpeech;
import com.onpositive.text.analysis.IToken;
import com.onpositive.text.analysis.lexic.DimensionToken;
import com.onpositive.text.analysis.lexic.ScalarToken;
import com.onpositive.text.analysis.rules.matchers.UnaryMatcher;

public class NounNamePrepositionParser extends AbstractSyntaxParser {

	public NounNamePrepositionParser(AbstractWordNet wordNet) {
		super(wordNet);
	}

	protected boolean checkName(IToken token){
		if(token instanceof DimensionToken){
			return true;
		}
		if(token instanceof ScalarToken){
			return true;
		}
		return nameMatch.match(token);
	}

	protected boolean checkNoun(IToken token) {
		return nounMatch.match(token);
	}
	
	protected static final UnaryMatcher<SyntaxToken> nameMatch = hasAny( PartOfSpeech.NOUN, PartOfSpeech.ADJF );
	
	private static final UnaryMatcher<SyntaxToken> matcher = hasAny(PartOfSpeech.ADJF,PartOfSpeech.NOUN,PartOfSpeech.ADVB);
	
	@Override
	public boolean isRecursive() {
		return true;
	}

	@Override
	protected void combineTokens(Stack<IToken> sample, ProcessingData processingData) {

		if (sample.size() < 3) {
			return;
		}
		
		if(!checkParents(null, sample)){
			return;
		}
		
		SyntaxToken token0 = (SyntaxToken) sample.get(0);
		IToken token2 = sample.peek();

		int startPosition = token0.getStartPosition();		
		int endPosition = token2.getEndPosition();
		
		SyntaxToken newToken = new SyntaxToken(IToken.TOKEN_TYPE_NOUN_NAME_PREP, (SyntaxToken) token0, null, startPosition, endPosition, true);
	
		if (checkParents(newToken, sample)) {
			processingData.addReliableToken(newToken);
		}
	}

	@Override
	protected ProcessingResult continuePush(Stack<IToken> sample, IToken newToken) {
		
		int size = sample.size();
		if(prepMatch.match(newToken)&&size==1){
			return CONTINUE_PUSH;
		}
		else if(matcher.match(newToken)&&size==2){
			return ACCEPT_AND_BREAK;
		}
		return DO_NOT_ACCEPT_AND_BREAK;
	}

	protected boolean matchTokensCouple(Stack<IToken> sample) {
		return true;
	}

	@Override
	protected ProcessingResult checkToken(IToken newToken) {
		if(prepMatch.match(newToken)){
			return DO_NOT_ACCEPT_AND_BREAK;
		}
		if (checkNoun(newToken)) {
			return CONTINUE_PUSH;
		}
		return DO_NOT_ACCEPT_AND_BREAK;
	}

}