package com.onpositive.text.analysis.syntax;

import java.util.List;
import java.util.Stack;

import com.onpositive.semantic.wordnet.AbstractWordNet;
import com.onpositive.text.analysis.IToken;
import com.onpositive.text.analysis.lexic.ComplexClause;
import com.onpositive.text.analysis.lexic.PrepConjRegistry;

public class ComplexClauseParser extends AbstractSyntaxParser{

	public ComplexClauseParser(AbstractWordNet wordNet) {
		super(wordNet);
	}

	@Override
	protected void combineTokens(Stack<IToken> sample, ProcessingData processingData)
	{
		if(sample.size()<3){
			return;
		}
		int startPosition = sample.get(0).getStartPosition();
		int endPosition = sample.peek().getEndPosition();
		
		SyntaxToken newToken = new ComplexClause(sample,startPosition,endPosition);		
		
		if(checkParents(newToken, sample)){
			processingData.addReliableToken(newToken);
		}
	}

	protected int computeEndPoosition(SyntaxToken token) {
		
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
	protected ProcessingResult continuePush(Stack<IToken> sample,IToken newToken) {
		IToken token0 = sample.peek();
		
		if(newToken instanceof SyntaxToken){
			SyntaxToken st = (SyntaxToken) newToken;
			String basicForm = st.getBasicForm();
			PrepConjRegistry pcr = getPrepConjRegistry();
			if(newToken.getType()==IToken.TOKEN_TYPE_CLAUSE){
				if(token0 instanceof SyntaxToken){
					SyntaxToken st0 = (SyntaxToken) token0;
					String bf0 = st0.getBasicForm();
					if(pcr.isSubordinateConjunction(bf0) || pcr.isCoordinateConjunction(bf0)){
						return CONTINUE_PUSH;
					}
					else{
						return rollBack(sample);
					}
				}
			}
			else if(pcr.isSubordinateConjunction(basicForm) || pcr.isCoordinateConjunction(basicForm)){
				if(isComma(token0)||token0.getType()==IToken.TOKEN_TYPE_CLAUSE){
					return CONTINUE_PUSH;
				}
				else{
					return rollBack(sample);
				}
			}			
		}
		else if(isComma(newToken)){
			if(token0 instanceof SyntaxToken){
				SyntaxToken st0 = (SyntaxToken) token0;
				String bf0 = st0.getBasicForm();
				if(getPrepConjRegistry().isSubordinateConjunction(bf0)||token0.getType()==IToken.TOKEN_TYPE_CLAUSE){
					return CONTINUE_PUSH;
				}
				else{
					return rollBack(sample);
				}
			}			
		}
		return DO_NOT_ACCEPT_AND_BREAK;
	}

	

	private ProcessingResult rollBack(Stack<IToken> sample) {
		
		int count = 0;
		for(int i = sample.size() ; i > 0 ; i--){
			IToken t = sample.get(i-1);
			if(t.getType() == IToken.TOKEN_TYPE_CLAUSE){
				break;
			}
			count++;
		}		
		return new ProcessingResult(count, true, true);
	}

	@Override
	protected ProcessingResult checkToken(IToken newToken) {
		
		int type = newToken.getType();
		if (type == IToken.TOKEN_TYPE_CLAUSE) {
			return CONTINUE_PUSH;
		}
		return DO_NOT_ACCEPT_AND_BREAK;
	}
	
	@Override
	public boolean isIterative() {
		return false;
	}

}
