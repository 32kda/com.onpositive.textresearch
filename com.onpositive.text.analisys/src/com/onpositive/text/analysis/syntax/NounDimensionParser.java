package com.onpositive.text.analysis.syntax;

import java.util.Stack;

import com.onpositive.semantic.wordnet.AbstractWordNet;
import com.onpositive.semantic.wordnet.Grammem.PartOfSpeech;
import com.onpositive.text.analysis.IToken;

public class NounDimensionParser extends AbstractSyntaxParser{

	
	public NounDimensionParser(AbstractWordNet wordNet) {
		super(wordNet);
	}

	@Override
	protected void combineTokens(Stack<IToken> sample, ProcessingData processingData)
	{
		if(sample.size()<2){
			return;
		}
		SyntaxToken token0 = (SyntaxToken) sample.get(0);
		SyntaxToken token1 = (SyntaxToken) sample.get(1);
		
		if(checkIfAlreadyProcessed(token0, token1)){
			return;
		}
		
		int startPosition = token0.getStartPosition();
		int endPosition = token1.getEndPosition();
		
		boolean isDimension0 = token0.hasMainDescendant(IToken.TOKEN_TYPE_UNIT);
		SyntaxToken mainGroup = isDimension0 ? token1 : token0;
		
		SyntaxToken newToken = new SyntaxToken(IToken.TOKEN_TYPE_MEASURED_NOUN, mainGroup, null, startPosition, endPosition);		
		if(checkParents(newToken,sample)){
			processingData.addReliableToken(newToken);
		}
	}
	
//	@Override
//	protected boolean keepInputToken() {
//		return false;
//	}

	@Override
	protected ProcessingResult continuePush(Stack<IToken> sample,IToken newToken) {
		
		if(!(newToken instanceof SyntaxToken)){
			return DO_NOT_ACCEPT_AND_BREAK;
		}
		
		SyntaxToken token1 = (SyntaxToken) newToken;
		if(isPrepOrConj(token1)){
			return DO_NOT_ACCEPT_AND_BREAK;
		}
		if(!token1.hasGrammem(PartOfSpeech.NOUN)){
			return DO_NOT_ACCEPT_AND_BREAK;
		}
		SyntaxToken token0 = (SyntaxToken) sample.peek();
		boolean isDimension0 = token0.hasMainDescendant(IToken.TOKEN_TYPE_UNIT);
		boolean isDimension1 = token1.hasMainDescendant(IToken.TOKEN_TYPE_UNIT);
		if(isDimension0||isDimension1){
			if(isDimension0!=isDimension1){
				return ACCEPT_AND_BREAK;
			}
		}
		return DO_NOT_ACCEPT_AND_BREAK;
	}

	@Override
	protected ProcessingResult checkToken(IToken newToken) {
		
		if(!(newToken instanceof SyntaxToken)){
			return DO_NOT_ACCEPT_AND_BREAK;
		}
		
		SyntaxToken token = (SyntaxToken) newToken;
		if(token.hasGrammem(PartOfSpeech.PREP)){
			return DO_NOT_ACCEPT_AND_BREAK;
		}
		if(token.hasGrammem(PartOfSpeech.NOUN)){
			return CONTINUE_PUSH;
		}
		return DO_NOT_ACCEPT_AND_BREAK;
	}
}
