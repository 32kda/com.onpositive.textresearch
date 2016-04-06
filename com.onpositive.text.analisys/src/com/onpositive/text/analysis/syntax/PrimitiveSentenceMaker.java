package com.onpositive.text.analysis.syntax;

import java.util.ArrayList;
import java.util.List;

import com.onpositive.text.analysis.IToken;

public class PrimitiveSentenceMaker {
	
	public List<IToken> formSentences(List<IToken> tokens){
		
		if(tokens==null || tokens.isEmpty()){
			return null;
		}
		int startPosition = tokens.get(0).getStartPosition();
		int endPosition = tokens.get(tokens.size()-1).getEndPosition();
		SentenceToken sentence = new SentenceToken(startPosition, endPosition);
		sentence.addChildren(tokens);
		ArrayList<IToken> list = new ArrayList<IToken>();
		list.add(sentence);		
		return list;
	}

}
