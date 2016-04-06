package com.onpositive.text.analysis.lexic;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Stack;

import com.onpositive.text.analysis.BasicParser;
import com.onpositive.text.analysis.IToken;
import com.onpositive.text.analysis.syntax.SyntaxToken;

public class IndexAttachingPasrser extends BasicParser {

	HashSet<Character> puncuationMarks = new HashSet<Character>(Arrays.asList(
			'!', '(', ')', ',', '.', '?', ';', ':', '"', '[', ']', '{', '}', '—', '«', '»', '„', '“'
		)); 
	
	HashSet<Integer> acceptedTokenTypes = new HashSet<Integer>(Arrays.asList(
			IToken.TOKEN_TYPE_LETTER,
			IToken.TOKEN_TYPE_EXPONENT,
			IToken.TOKEN_TYPE_DIGIT,
			IToken.TOKEN_TYPE_SCALAR
		)); 
	
	
	@Override
	protected void combineTokens(Stack<IToken> sample, ProcessingData processingData) {
		
		if(sample.size()<2){
			return;
		}
		IToken firstToken = sample.get(0);
		int startPosition = firstToken.getStartPosition();
		int endPosition = sample.peek().getEndPosition();
		
		SyntaxToken newToken = new SyntaxToken(IToken.TOKEN_TYPE_WORD_WITH_INDEX, (SyntaxToken) firstToken, null, startPosition, endPosition);
		processingData.addReliableToken(newToken);
	}
	
	@Override
	protected ProcessingResult continuePush(Stack<IToken> sample,IToken newToken) {
		
		int type = newToken.getType();
		boolean accept = false;
		if(type==IToken.TOKEN_TYPE_SYMBOL){
			String stringValue = newToken.getStringValue();
			char charValue = stringValue.charAt(0);
			accept = !puncuationMarks.contains(charValue);
		}
		else {
			accept = acceptedTokenTypes.contains(type);
		}
			
		if(accept){
			if(newToken.hasSpaceAfter()){
				return ACCEPT_AND_BREAK;
			}
			else{
				return CONTINUE_PUSH;
			}
		}
		return DO_NOT_ACCEPT_AND_BREAK;
	}

	@Override
	protected ProcessingResult checkToken(IToken newToken) {
		
		if(newToken.getType() == IToken.TOKEN_TYPE_WORD_FORM){
			if(newToken.hasSpaceAfter()){
				return DO_NOT_ACCEPT_AND_BREAK;
			}
			return CONTINUE_PUSH;
		}
		return DO_NOT_ACCEPT_AND_BREAK;
	}

}
