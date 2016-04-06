package com.onpositive.text.analysis.syntax;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import com.onpositive.semantic.wordnet.AbstractWordNet;
import com.onpositive.semantic.wordnet.Grammem;
import com.onpositive.semantic.wordnet.Grammem.PartOfSpeech;
import com.onpositive.text.analysis.IToken;

public class AdjectiveAdverbParser extends AbstractSyntaxParser{

	
	public AdjectiveAdverbParser(AbstractWordNet wordNet) {
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
		int tokenType = IToken.TOKEN_TYPE_ADJECTIVE_ADVERB;
		ArrayList<IToken> tokens = new ArrayList<IToken>();
		if(token0.hasAnyGrammem(adjectives)&&token1.hasGrammem(PartOfSpeech.ADVB)){
			tokens.add(new SyntaxToken(tokenType, token0, null, startPosition, endPosition));
		}
		if(token1.hasAnyGrammem(adjectives)&&token0.hasGrammem(PartOfSpeech.ADVB)){
			tokens.add(new SyntaxToken(tokenType, token1, null, startPosition, endPosition));
		}
		return tokens;
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
		if(token1.hasGrammem(PartOfSpeech.PREP)){
			return DO_NOT_ACCEPT_AND_BREAK;
		}
		
		SyntaxToken token0 = (SyntaxToken) sample.peek();		
		if(token0.hasAnyGrammem(adjectives)){
			if(token1.hasGrammem(PartOfSpeech.ADVB)){
				return ACCEPT_AND_BREAK;
			}
		}
		if(token0.hasGrammem(PartOfSpeech.ADVB)){
			if(token1.hasAnyGrammem(adjectives)){
				return ACCEPT_AND_BREAK;
			}
		}
		return DO_NOT_ACCEPT_AND_BREAK;
	}
	
	private static final Set<PartOfSpeech> acceptedPartsOfSpeech
			= new HashSet<Grammem.PartOfSpeech>(Arrays.asList(PartOfSpeech.ADJF, PartOfSpeech.ADJS, PartOfSpeech.ADVB));
	
	private static final Set<PartOfSpeech> adjectives
			= new HashSet<Grammem.PartOfSpeech>(Arrays.asList(PartOfSpeech.ADJF, PartOfSpeech.ADJS));

	@Override
	protected ProcessingResult checkToken(IToken newToken) {
		
		if(!(newToken instanceof SyntaxToken)){
			return DO_NOT_ACCEPT_AND_BREAK;
		}
		
		SyntaxToken token = (SyntaxToken) newToken;
		if(token.hasGrammem(PartOfSpeech.PREP)){
			return DO_NOT_ACCEPT_AND_BREAK;
		}
		if(token.hasAnyGrammem(acceptedPartsOfSpeech)){
			return CONTINUE_PUSH;
		}
		return DO_NOT_ACCEPT_AND_BREAK;
	}
}
