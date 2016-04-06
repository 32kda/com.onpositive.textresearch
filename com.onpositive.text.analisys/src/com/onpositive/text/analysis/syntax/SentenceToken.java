package com.onpositive.text.analysis.syntax;

import com.onpositive.text.analysis.AbstractToken;
import com.onpositive.text.analysis.IToken;

public class SentenceToken extends AbstractToken{

	public SentenceToken(int startPosition, int endPosition) {
		super(IToken.TOKEN_TYPE_SENTENCE, startPosition, endPosition);
	}

	@Override
	public String getStringValue() {
		if(children==null||children.isEmpty()){
			return "";
		}
		StringBuilder bld = new StringBuilder();
		IToken prev = children.get(0);
		bld.append(prev.getStringValue());
		int size = children.size();
		for(int i = 1 ; i < size ; i++){
			IToken token = children.get(i);
			if(prev.getEndPosition() != token.getStartPosition()){
				bld.append(" ");
			}
			bld.append(token.getStringValue());
			prev = token;
		}
		String result = bld.toString();
		return result;
	}
	
	@Override
	public String getShortStringValue() {
		if(children==null||children.isEmpty()){
			return "";
		}
		StringBuilder bld = new StringBuilder();
		IToken prev = children.get(0);
		bld.append(prev.getShortStringValue());
		int size = children.size();
		for(int i = 1 ; i < size ; i++){
			IToken token = children.get(i);
			if(prev.getEndPosition() != token.getStartPosition()){
				bld.append(" ");
			}
			bld.append(token.getShortStringValue());
			prev = token;
		}
		String result = bld.toString();
		return result;
	}
	
}
