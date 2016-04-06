package com.onpositive.text.analysis.syntax;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import com.onpositive.semantic.wordnet.AbstractWordNet;
import com.onpositive.semantic.wordnet.Grammem.Case;
import com.onpositive.semantic.wordnet.Grammem.PartOfSpeech;
import com.onpositive.text.analysis.IToken;
import com.onpositive.text.analysis.rules.matchers.UnaryMatcher;
import com.onpositive.text.analysis.syntax.SyntaxToken.GrammemSet;

public class ClauseParser extends AbstractSyntaxParser{
	
	private static final UnaryMatcher<SyntaxToken> isNoun
			= hasAny( PartOfSpeech.NOUN, PartOfSpeech.NPRO);
	
	private static final UnaryMatcher<SyntaxToken> acceptedNomn
			= hasAny(caseMatchMap.get(Case.NOMN));

	@SuppressWarnings("unchecked")
	private static final UnaryMatcher<SyntaxToken> checkNoun = and(isNoun, acceptedNomn, not(prepConjMatch));
	
	private static final UnaryMatcher<SyntaxToken> verbMatchGrammems
			= hasAll(PartOfSpeech.VERB);
	

	@SuppressWarnings("unchecked")
	private static UnaryMatcher<SyntaxToken> verbOrNoun = or(verbMatchGrammems,checkNoun);

	public ClauseParser(AbstractWordNet wordNet) {
		super(wordNet);
	}

	@Override
	protected void combineTokens(Stack<IToken> sample, ProcessingData processingData)
	{
		if(sample.size()<2){
			return;
		}
		
		SyntaxToken token0 = (SyntaxToken) sample.get(0);
		SyntaxToken token1 = (SyntaxToken) sample.peek();
		
		SyntaxToken verbToken = null;
		SyntaxToken nounToken = null;
		if (verbMatchGrammems.match(token0)) {
			verbToken = token0;
			nounToken = token1;
		} else {
			verbToken = token1;
			nounToken = token0;
		}
		if(!matchSP(nounToken, verbToken)){
			return;
		}
		int startPosition = token0.getStartPosition();
		int endPosition = token1.getEndPosition();//computeEndPosition(token1);		
		
		IToken newToken = new ClauseToken(nounToken, verbToken, startPosition, endPosition);
		if(checkParents(newToken, sample)){
			processingData.addReliableToken(newToken);
		}
	}

	protected int computeEndPosition(SyntaxToken token) {
		
		int endPosition = token.getEndPosition();
		IToken next = token.getNext();
		if(next!=null){
			if(next.getType()==IToken.TOKEN_TYPE_SYMBOL&&next.getStringValue().equals(".")){
				endPosition = next.getEndPosition();
			}
		}
		else{
			List<IToken> nextTokens = token.getNextTokens();
			if(nextTokens!=null){
				for(IToken n : nextTokens){
					if(n.getType()==IToken.TOKEN_TYPE_SYMBOL&&n.getStringValue().equals(".")){
						endPosition = n.getEndPosition();
						break;
					}					
				}
			}
		}
		return endPosition;
	}

	@Override
	protected ProcessingResult continuePush(Stack<IToken> sample,
			IToken newToken) {
		IToken token0 = sample.get(0);
		IToken token1 = newToken;
		if (verbMatchGrammems.match(token0)	&& checkNoun.match(token1)){
			List<GrammemSet> nGramems = getNomnGrammems((SyntaxToken) token1);
			List<GrammemSet> vGrammems = ((SyntaxToken)token0).getGrammemSets();
			if(!matchSP(vGrammems, nGramems)){
				return DO_NOT_ACCEPT_AND_BREAK;
			}
			return ACCEPT_AND_BREAK;
		} else if (verbMatchGrammems.match(token1) && checkNoun.match(token0)) {
			List<GrammemSet> nGramems = getNomnGrammems((SyntaxToken) token0);
			List<GrammemSet> vGrammems = ((SyntaxToken)token1).getGrammemSets();
			if(!matchSP(vGrammems, nGramems)){
				return DO_NOT_ACCEPT_AND_BREAK;
			}
			return ACCEPT_AND_BREAK;
		}		
		return DO_NOT_ACCEPT_AND_BREAK;
	}

	private List<GrammemSet> getNomnGrammems(SyntaxToken token) {
		ArrayList<GrammemSet> list = new ArrayList<SyntaxToken.GrammemSet>();
		for(GrammemSet gs : token.getGrammemSets()){
			if(gs.hasAnyGrammem(caseMatchMap.get(Case.NOMN))){
				list.add(gs);
			}
		}
		return list;
	}

	@Override
	protected ProcessingResult checkToken(IToken newToken) {
		
		if (verbOrNoun.match(newToken)) {
			return CONTINUE_PUSH;
		}
		return DO_NOT_ACCEPT_AND_BREAK;
	}
	
	@Override
	public boolean isIterative() {
		return false;
	}

}
