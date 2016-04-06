package com.onpositive.text.analysis.basic.matchers;

import java.util.ArrayList;
import java.util.List;

import com.carrotsearch.hppc.IntOpenHashSet;
import com.onpositive.text.analysis.IToken;
import com.onpositive.text.analysis.lexic.PrimitiveTokenizer;

public class TokenMatcher implements ITokenMatcher{
	
	public static List<ITokenMatcher> forString(String str){
		List<ITokenMatcher> list = new ArrayList<ITokenMatcher>();
		for(int i = 0 ; i < str.length(); i++){
			char ch = str.charAt(i);
			if(PrimitiveTokenizer.isSymbols(ch)){
				list.add(new TokenMatcher(""+ch, IToken.TOKEN_TYPE_SYMBOL));
			}
			else if(Character.isLetter(ch)){
				list.add(new TokenMatcher(null, IToken.TOKEN_TYPE_LETTER, IToken.TOKEN_TYPE_WORD_FORM));
			}
		}
		return list;
	}
	
	
	public TokenMatcher( String value, int... tokenTypes) {
		super();
		this.tokenTypes = new IntOpenHashSet();
		for(int val : tokenTypes){
			this.tokenTypes.add(val);
		}
		this.value = value;
	}

	private IntOpenHashSet tokenTypes;
	
	private String value;
	
	/* (non-Javadoc)
	 * @see com.onpositive.text.analysis.basic.matchers.ITokenMatcher#match(com.onpositive.text.analysis.IToken)
	 */
	@Override
	public boolean match(IToken token){
		if(!tokenTypes.contains(token.getType())){
			return false;
		}
		if(this.value==null){
			return true;
		}
		if(!this.value.equals(token.getStringValue())){
			return false;
		}
		return true;
	}
	
	@Override
	public String toString() {
		return value;
	}
}