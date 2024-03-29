package com.onpositive.text.analysis.syntax;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import com.onpositive.semantic.wordnet.AbstractWordNet;
import com.onpositive.semantic.wordnet.Grammem;
import com.onpositive.semantic.wordnet.Grammem.PartOfSpeech;
import com.onpositive.text.analysis.IToken;

public class NounAdjectiveParser extends AbstractSyntaxParser{

	
	public NounAdjectiveParser(AbstractWordNet wordNet) {
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
		
		SyntaxToken newToken = matchMeanings(token0, token1);
		if(newToken==null){
			return;
		}
		if(checkParents(newToken,sample)){
			if(newToken.hasMainDescendant(IToken.TOKEN_TYPE_UNIT)){
				processingData.addDoubtfulToken(newToken);
			}
			else{
				processingData.addReliableToken(newToken);
			}
		}
	}

	private SyntaxToken matchMeanings(SyntaxToken token0, SyntaxToken token1)
	{
		int tokenType = IToken.TOKEN_TYPE_NOUN_ADJECTIVE;
		SyntaxToken result = null;
		if(token0.hasGrammem(PartOfSpeech.NOUN)&&hasAny(adjectiveLike).match(token1)){
			result = combineNames(token0,token1,tokenType);
		}
		if(token1.hasGrammem(PartOfSpeech.NOUN)&&hasAny(adjectiveLike).match(token0)){
			result = combineNames(token1,token0,tokenType);
		}
		return result;
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
		if(token0.hasGrammem(PartOfSpeech.NOUN)){
			if(hasAny(adjectiveLike).match(token1)){
				return ACCEPT_AND_BREAK;
			}
		}
		if(hasAny(adjectiveLike).match(token0)){
			if(token1.hasGrammem(PartOfSpeech.NOUN)){
				return ACCEPT_AND_BREAK;
			}
		}
		return DO_NOT_ACCEPT_AND_BREAK;
	}
	
	private static final Set<PartOfSpeech> adjectiveLike
			= new HashSet<Grammem.PartOfSpeech>(Arrays.asList(PartOfSpeech.ADJF, PartOfSpeech.NUMR));
	
	private static final Set<PartOfSpeech> acceptedPartsOfSpeech
			= new HashSet<Grammem.PartOfSpeech>(Arrays.asList(PartOfSpeech.NOUN, PartOfSpeech.ADJF, PartOfSpeech.NUMR));

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
