package com.onpositive.text.analysis.syntax;

import java.util.Stack;

import com.onpositive.semantic.wordnet.AbstractWordNet;
import com.onpositive.text.analysis.IToken;

public class CompositeVerbParser extends AbstractSyntaxParser {

	public CompositeVerbParser(AbstractWordNet wordNet) {
		super(wordNet);
	}

	@Override
	protected void combineTokens(Stack<IToken> sample, ProcessingData processingData)
	{
		if(sample.size()<2){
			return;
		}
		SyntaxToken mainToken = (SyntaxToken) sample.get(0);
		int startPosition = mainToken.getStartPosition();
		int endPosition = sample.peek().getEndPosition();
		
		SyntaxToken newToken = new SyntaxToken(IToken.TOKEN_TYPE_COMPOSITE_VERB, mainToken, null, startPosition, endPosition);
		if(checkParents(newToken, sample)){
			processingData.addReliableToken(newToken);
		}
	}
	
	@Override
	protected ProcessingResult continuePush(Stack<IToken> sample, IToken newToken) {
		
		if(infnMatch.match(newToken)){
			return CONTINUE_PUSH;
		}
		return DO_NOT_ACCEPT_AND_BREAK;
	}

	@Override
	protected ProcessingResult checkToken(IToken newToken) {
		
		if(verbLikeMatch.match(newToken) && isModalLikeVerb((SyntaxToken) newToken))
			return CONTINUE_PUSH;
		
		return DO_NOT_ACCEPT_AND_BREAK;
	}

}
