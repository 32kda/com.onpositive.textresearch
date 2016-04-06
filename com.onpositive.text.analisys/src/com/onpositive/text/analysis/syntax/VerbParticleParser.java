package com.onpositive.text.analysis.syntax;

import java.util.ArrayList;
import java.util.Stack;

import com.onpositive.semantic.wordnet.AbstractWordNet;
import com.onpositive.semantic.wordnet.Grammem.PartOfSpeech;
import com.onpositive.text.analysis.IToken;

public class VerbParticleParser extends AbstractSyntaxParser {

	public VerbParticleParser(AbstractWordNet wordNet) {
		super(wordNet);
	}

	@Override
	protected void combineTokens(Stack<IToken> sample, ProcessingData processingData) {
		if(sample.size()<2){
			return;
		}
		
		SyntaxToken token0 = (SyntaxToken) sample.get(0);
		SyntaxToken token1 = (SyntaxToken) sample.get(1);
		
		if(checkIfAlreadyProcessed(token0, token1)) {
			return;
		}
		
		ArrayList<IToken> rawTokens = matchMeanings(token0, token1);
		ArrayList<IToken> tokens = new ArrayList<IToken>();
		for(IToken newToken : rawTokens){
			if(checkParents(newToken,sample)){
				tokens.add(newToken);
			}
		}
		
		if(tokens.size()==1){
			processingData.addReliableToken(tokens.get(0));
		}
		else if(!tokens.isEmpty()){
			processingData.addDoubtfulTokens(tokens);
		}
	}
	
	private ArrayList<IToken> matchMeanings(SyntaxToken token0, SyntaxToken token1)
	{
		int startPosition = token0.getStartPosition();
		int endPosition = token1.getEndPosition();
		int tokenType = IToken.TOKEN_TYPE_VERB_PARTICLE;
		ArrayList<IToken> tokens = new ArrayList<IToken>();
		if(token0.hasGrammem(PartOfSpeech.PRCL)&&token1.hasGrammem(PartOfSpeech.VERB)){
			tokens.add(new SyntaxToken(tokenType, token1, null, startPosition, endPosition));
		}
		if(token0.hasGrammem(PartOfSpeech.VERB)&&token1.hasGrammem(PartOfSpeech.PRCL)){
			tokens.add(new SyntaxToken(tokenType, token0, null, startPosition, endPosition));
		}
		return tokens;
	}
	
	protected ProcessingResult continuePush(Stack<IToken> sample,IToken newToken) {
		
		if(!(newToken instanceof SyntaxToken)){
			return DO_NOT_ACCEPT_AND_BREAK;
		}
				
		SyntaxToken token1 = (SyntaxToken) newToken;
		
		if (isPrepOrConj(token1)) return DO_NOT_ACCEPT_AND_BREAK;
		
		SyntaxToken token0 = (SyntaxToken) sample.peek();		
		if(token0.hasGrammem(PartOfSpeech.VERB)){
			if(token1.hasGrammem(PartOfSpeech.PRCL)){
				return ACCEPT_AND_BREAK;
			}
		}
		if(token1.hasGrammem(PartOfSpeech.VERB)){
			if(token0.hasGrammem(PartOfSpeech.PRCL)){
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
		
		if (isPrepOrConj(token)) return DO_NOT_ACCEPT_AND_BREAK;
			
		if (token.hasGrammem(PartOfSpeech.VERB)) return CONTINUE_PUSH;
		if (token.hasGrammem(PartOfSpeech.PRCL) && isVerbParticiple(newToken)) return CONTINUE_PUSH;
	
		return DO_NOT_ACCEPT_AND_BREAK;
	}

}
