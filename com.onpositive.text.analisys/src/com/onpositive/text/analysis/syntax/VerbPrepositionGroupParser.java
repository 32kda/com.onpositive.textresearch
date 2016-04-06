package com.onpositive.text.analysis.syntax;

import java.util.ArrayList;
import java.util.Stack;

import com.onpositive.semantic.wordnet.AbstractWordNet;
import com.onpositive.semantic.wordnet.Grammem.PartOfSpeech;
import com.onpositive.semantic.wordnet.Grammem.TransKind;
import com.onpositive.text.analysis.IToken;
import com.onpositive.text.analysis.rules.matchers.UnaryMatcher;

public abstract class VerbPrepositionGroupParser extends AbstractSyntaxParser {

	public VerbPrepositionGroupParser(AbstractWordNet wordNet) {
		super(wordNet);
	}

	protected abstract int getType(SyntaxToken token);

	protected abstract boolean checkAdditionalToken(IToken token);

	protected boolean checkVerb(IToken token) {
		return verbLikeMatch.match(token);
	}
	
	protected static final UnaryMatcher<SyntaxToken> transitiveVerbMatch = and(verbLikeMatch,hasAny(TransKind.tran));

	protected static final UnaryMatcher<SyntaxToken> infnMatch = has(PartOfSpeech.INFN);

	@Override
	protected void combineTokens(Stack<IToken> sample, ProcessingData processingData) {
		if (sample.size() < 3) {
			return;
		}
	
		SyntaxToken[] orderedTokens = extractMainTokens(sample);
		if(orderedTokens==null){
			return;
		}
		
		if (checkIfAlreadyProcessed(orderedTokens[0], orderedTokens[1], orderedTokens[2])) {
			return;
		}
		if(isContained(orderedTokens[0], orderedTokens[1])){
			return;
		}
		
		int startPosition = sample.get(0).getStartPosition();
		int endPosition = sample.peek().getEndPosition();
		
		ClauseToken clauseToken = null;
		SyntaxToken predToken = null;
		if(orderedTokens[0].getType()==IToken.TOKEN_TYPE_CLAUSE){
			clauseToken = (ClauseToken) orderedTokens[0];
			predToken = clauseToken.getPredicate();
		}
		else{
			predToken = orderedTokens[0];
		}	
		int subjType = getType(orderedTokens[1]);
			
		SyntaxToken newToken = new SyntaxToken(subjType, predToken, null, startPosition, endPosition, true);
	
		if(clauseToken!=null){
//			boolean doSet = false;
			ArrayList<IToken> children = new ArrayList<IToken>();
			for(IToken t : sample){
				children.add(t==clauseToken?predToken:t);
			}
//			if(!isContinuous){				
//				if (checkParents(newToken, children)) {
//					newToken.addChildren(children);
//					for(IToken ch: children){
//						ch.addParent(newToken);
//					}
//					doSet = true;
//				}
//			}
//			else{
//				doSet=true;
//			}
//			if(doSet){
			newToken.addChildren(children);
			if(!checkParents(newToken, children)){
				return;
			}				
			clauseToken.setPredicate(newToken);
//			}
		}
		else if (checkParents(newToken, sample)) {
			processingData.addReliableToken(newToken);
		}
	}

	private SyntaxToken[] extractMainTokens(Stack<IToken> sample) {
		if(prepMatch.match(sample.get(0))){
			SyntaxToken[] result = fillMainTokenArray(sample.get(1),sample.get(2),new SyntaxToken[3]);
			if(result!=null){
				result[2]=(SyntaxToken) sample.get(0);
			}
			return result;
		}
		else if(prepMatch.match(sample.get(1))){
			SyntaxToken[] result = fillMainTokenArray(sample.get(0),sample.get(2),new SyntaxToken[3]);
			if(result!=null){
				result[2]=(SyntaxToken) sample.get(1);
			}				
			return result;
		}
		return null;
	}

	private SyntaxToken[] fillMainTokenArray(IToken token0, IToken token1,	SyntaxToken[] arr) {
		
		if (checkVerb(token0) && checkAdditionalToken(token1)) {
			arr[0] = (SyntaxToken) token0;
			arr[1] = (SyntaxToken) token1;
		} else if (checkVerb(token1) && checkAdditionalToken(token0)) {
			arr[0] = (SyntaxToken) token1;
			arr[1] = (SyntaxToken) token0;
		}
		else if(token0.getType()==IToken.TOKEN_TYPE_CLAUSE && checkAdditionalToken(token1)){
			arr[0] = (SyntaxToken) token0;
			arr[1] = (SyntaxToken) token1;
		}
		else if(token1.getType()==IToken.TOKEN_TYPE_CLAUSE && checkAdditionalToken(token0)){
			arr[0] = (SyntaxToken) token1;
			arr[1] = (SyntaxToken) token0;
		}
		else{
			return null;
		}
		return arr;
	}

	@Override
	protected ProcessingResult continuePush(Stack<IToken> sample, IToken newToken) {
		
		IToken last = sample.peek();
		int size = sample.size();
		if(prepMatch.match(newToken)){			
			if(size>1){
				return DO_NOT_ACCEPT_AND_BREAK;
			}
			if(checkVerb(last)||last.getType() == IToken.TOKEN_TYPE_CLAUSE){
				return CONTINUE_PUSH;
			}
			return DO_NOT_ACCEPT_AND_BREAK;
		}
		if((checkVerb(newToken)||(newToken.getType()==IToken.TOKEN_TYPE_CLAUSE))&&checkAdditionalToken(last)){
			return size == 2 ? ACCEPT_AND_BREAK : CONTINUE_PUSH;
		}

		if(checkAdditionalToken(newToken)&&prepMatch.match(last)){
			return size == 2 ? ACCEPT_AND_BREAK : CONTINUE_PUSH;
		}
		return DO_NOT_ACCEPT_AND_BREAK;
	}

	protected boolean matchTokensCouple(Stack<IToken> sample) {
		return true;
	}

	@Override
	protected ProcessingResult checkToken(IToken newToken) {
		
		if(prepMatch.match(newToken)){
			return CONTINUE_PUSH;
		}
		if (checkVerb(newToken)) {
			return CONTINUE_PUSH;
		}
		if(newToken.getType()==IToken.TOKEN_TYPE_CLAUSE){
			return CONTINUE_PUSH;
		}
		return DO_NOT_ACCEPT_AND_BREAK;
	}

}