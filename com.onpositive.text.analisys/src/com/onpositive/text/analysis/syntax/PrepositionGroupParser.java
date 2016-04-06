package com.onpositive.text.analysis.syntax;

import java.util.Stack;

import com.onpositive.semantic.wordnet.AbstractWordNet;
import com.onpositive.semantic.wordnet.Grammem.PartOfSpeech;
import com.onpositive.text.analysis.IToken;
import com.onpositive.text.analysis.rules.matchers.UnaryMatcher;

public class PrepositionGroupParser extends AbstractSyntaxParser {
	
	private static final UnaryMatcher<SyntaxToken> matcher = hasAny(PartOfSpeech.NOUN, PartOfSpeech.ADJF, PartOfSpeech.ADVB);

	public PrepositionGroupParser(AbstractWordNet wordNet) {
		super(wordNet);
	}

	@Override
	protected void combineTokens(Stack<IToken> sample,ProcessingData processingData) {
		
		if(sample.size()<2){
			processingData.setStop(true);
			return;
		}
		
		SyntaxToken prepToken = (SyntaxToken) sample.get(0);
		SyntaxToken word = (SyntaxToken) sample.peek();
		int startPosition = prepToken.getStartPosition();
		int endPosition = word.getEndPosition();
		PrepositionGroupToken pgt = new PrepositionGroupToken(prepToken, word, startPosition, endPosition);
		
		processingData.addReliableToken(pgt);
	}

	
	@Override
	protected ProcessingResult continuePush(Stack<IToken> sample, IToken newToken)
	{
		if(!matcher.match(newToken)){
			return DO_NOT_ACCEPT_AND_BREAK;
		}
		
		String prepString = ((SyntaxToken)sample.get(0)).getBasicForm();
		UnaryMatcher<SyntaxToken> matcher = getPrepConjRegistry().getPrepCaseMatcher(prepString);
		if(matcher==null){
			return ACCEPT_AND_BREAK;
		}
		if(!matcher.match(newToken)){
			return DO_NOT_ACCEPT_AND_BREAK;
		}
		return ACCEPT_AND_BREAK;
	}

	@Override
	protected ProcessingResult checkToken(IToken newToken) {
		
		if(prepMatch.match(newToken)){
			return CONTINUE_PUSH;
		}		
		return DO_NOT_ACCEPT_AND_BREAK;
	}

}
