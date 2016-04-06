package com.onpositive.text.analysis.syntax;

import java.util.ArrayList;
import java.util.Stack;

import com.carrotsearch.hppc.IntOpenHashSet;
import com.onpositive.semantic.wordnet.AbstractWordNet;
import com.onpositive.semantic.wordnet.Grammem.PartOfSpeech;
import com.onpositive.semantic.wordnet.Grammem.TransKind;
import com.onpositive.text.analysis.IToken;
import com.onpositive.text.analysis.rules.matchers.UnaryMatcher;

public abstract class VerbGroupParser extends AbstractSyntaxParser {

	public VerbGroupParser(AbstractWordNet wordNet) {
		super(wordNet);
	}

	protected abstract int getType(SyntaxToken token);

	protected abstract boolean checkAdditionalToken(IToken token);

	protected boolean checkVerb(IToken token) {
		return verbLikeMatch.match(token);
	}
	
	protected static final UnaryMatcher<SyntaxToken> transitiveVerbMatch = and(verbLikeMatch,hasAny(TransKind.tran));

	protected static final UnaryMatcher<SyntaxToken> infnMatch = has(PartOfSpeech.INFN);
	
	abstract protected IntOpenHashSet getProducedTokenTypes();

	@Override
	protected void combineTokens(Stack<IToken> sample, ProcessingData processingData) {
		if (sample.size() < 2) {
			return;
		}
	
		SyntaxToken[] orderedTokens = extractMainTokens(sample);
		if(orderedTokens==null){
			return;
		}
		
		if (checkIfAlreadyProcessed(orderedTokens[0], orderedTokens[1])) {
			return;
		}
		if(isContained(orderedTokens[0], orderedTokens[1])){
			return;
		}
		
		ClauseToken clauseToken = null;
		SyntaxToken predToken = null;
		if(orderedTokens[0].getType()==IToken.TOKEN_TYPE_CLAUSE){
			clauseToken = (ClauseToken) orderedTokens[0];
			predToken = clauseToken.getPredicate();
		}
		else{
			predToken = orderedTokens[0];
		}	
		int objType = getType(orderedTokens[1]);
		
		boolean isDoubtful = isDoubtful(orderedTokens);
		
		int startPosition = Integer.MAX_VALUE;
		int endPosition = Integer.MIN_VALUE;
		for(IToken t : sample) {
			if(t==clauseToken) {
				t = predToken;
			}
			startPosition = Math.min(startPosition, t.getStartPosition());
			endPosition = Math.max(endPosition, t.getEndPosition());
		}
			
		SyntaxToken newToken = new SyntaxToken(objType, predToken, null, startPosition, endPosition,isDoubtful);
	
		if(clauseToken!=null){
//			boolean doSet = false;
			ArrayList<IToken> children = new ArrayList<IToken>();
			for(IToken t : sample){
				children.add(t==clauseToken?predToken:t);
			}
			if(!checkParents(newToken, children)){
				return;
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
				predToken.removeParent(clauseToken);
				for(IToken ch : children){
					ch.addParent(newToken);
				}
				newToken.setId(getTokenIdProvider().getVacantId());
				newToken.addChildren(children);
				clauseToken.setPredicate(newToken);
				clauseToken.replaceChild(predToken, newToken);
//			}
		}
		else if (checkParents(newToken, sample)) {
			if(isDoubtful){
				processingData.addDoubtfulToken(newToken);
			}
			else{
				processingData.addReliableToken(newToken);
			}
		}
	}

	protected boolean isDoubtful(SyntaxToken[] orderedTokens) {
		return false;
	}

	private SyntaxToken[] extractMainTokens(Stack<IToken> sample) {
		if(sample.size()==2){
			return fillMainTokenArray(sample.get(0),sample.get(1),new SyntaxToken[2]); 
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
		
		if(prepMatch.match(newToken)){			
			return DO_NOT_ACCEPT_AND_BREAK;
		}
		
		IToken last = sample.peek();
		if(checkAdditionalToken(last)){
			if(checkVerb(newToken)){
				if(!allowsMultiple()){
					if(checkIfProducedByThisParser((SyntaxToken) newToken)){
						return DO_NOT_ACCEPT_AND_BREAK;
					}
				}
				return ACCEPT_AND_BREAK;
			}
			if(newToken.getType()==IToken.TOKEN_TYPE_CLAUSE){
				SyntaxToken predicate = ((ClauseToken)newToken).getPredicate();
				if(checkVerb(predicate)){
					if(!allowsMultiple()){
						if(checkIfProducedByThisParser(predicate)){
							return DO_NOT_ACCEPT_AND_BREAK;
						}
					}
					return CONTINUE_PUSH;
				}
			}
		}
		if(checkAdditionalToken(newToken)&&(checkVerb(last)||last.getType()==IToken.TOKEN_TYPE_CLAUSE)){

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
		if (checkVerb(newToken)) {
			if(!allowsMultiple()){
				if(checkIfProducedByThisParser((SyntaxToken) newToken)){
					return DO_NOT_ACCEPT_AND_BREAK;
				}
			}
			return CONTINUE_PUSH;
		}
		if(checkAdditionalToken(newToken)){
			return CONTINUE_PUSH;
		}
		if(newToken.getType()==IToken.TOKEN_TYPE_CLAUSE){
			SyntaxToken predicate = ((ClauseToken)newToken).getPredicate();
			if(checkVerb(predicate)){
				if(!allowsMultiple()){
					if(checkIfProducedByThisParser(predicate)){
						return DO_NOT_ACCEPT_AND_BREAK;
					}
				}
				return CONTINUE_PUSH;
			}
		}
		return DO_NOT_ACCEPT_AND_BREAK;
	}
	
	private boolean checkIfProducedByThisParser(SyntaxToken token) {

		if(getProducedTokenTypes().contains(token.getType())){
			return true;
		}
		SyntaxToken mainGroup = token.getMainGroup();
		if(mainGroup==null||mainGroup==token){
			return false;
		}
		return checkIfProducedByThisParser(mainGroup);		
	}

	protected boolean allowsMultiple(){
		return true;
	}

}