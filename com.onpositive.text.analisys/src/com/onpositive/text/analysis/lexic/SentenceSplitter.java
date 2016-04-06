package com.onpositive.text.analysis.lexic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;

import com.onpositive.semantic.wordnet.Grammem;
import com.onpositive.semantic.wordnet.Grammem.SemanGramem;
import com.onpositive.text.analysis.IToken;
import com.onpositive.text.analysis.TokenRegistry;
import com.onpositive.text.analysis.syntax.SentenceToken;
import com.onpositive.text.analysis.syntax.SyntaxToken;

public class SentenceSplitter {
	
	private static final ArrayList<Grammem> abbr = new ArrayList<Grammem>(Arrays.asList(SemanGramem.ABBR));
	
	private static final HashSet<Character> sentenceTerminalSymbols = new HashSet<Character>(Arrays.asList(
			'.', '!', '?'
		));
	
	private static final HashSet<Character> regionBoundSymbols = new HashSet<Character>(Arrays.asList(
				'«', '»', '„', '“', '\"', '\'', '(', ')', '[', ']', '{', '}', '<', '>'
			));
	
	private static final HashSet<Character> openingRegionBoundSymbols = new HashSet<Character>(Arrays.asList(
			'«', '„', '(', '[', '{', '<'
		));
	
	private static final HashSet<Character> closingRegionBoundSymbols = new HashSet<Character>(Arrays.asList(
			'»', '“', ')', ']', '}', '>'
		));
	
	private static final HashMap<Character,Character> regionBoundPairMap = new HashMap<Character, Character>();
	static{
		regionBoundPairMap.put( '«',  '»');
		regionBoundPairMap.put( '»',  '«');
		regionBoundPairMap.put( '„',  '“');
		regionBoundPairMap.put( '“',  '„');
		regionBoundPairMap.put('\"', '\"');
		regionBoundPairMap.put('\'', '\'');
		regionBoundPairMap.put( '(',  ')');
		regionBoundPairMap.put( ')',  '(');
		regionBoundPairMap.put( '[',  ']');
		regionBoundPairMap.put( ']',  '[');
		regionBoundPairMap.put( '{',  '}');
		regionBoundPairMap.put( '}',  '{');
		regionBoundPairMap.put( '<',  '>');
		regionBoundPairMap.put( '>',  '<');
	}
	
	public List<IToken> split(List<IToken> tokens){
		
		ArrayList<IToken> sentences = new ArrayList<IToken>();
		
		int start = 0;
		int size = tokens.size();
		Stack<Character> quoteSymbols = new Stack<Character>(); 
		for(int i = 0 ; i < size ; i++ )
		{
			IToken token = tokens.get(i);
			if(token.getType()!=IToken.TOKEN_TYPE_SYMBOL){
				continue;
			}
			
			String stringValue = token.getStringValue();
			char charValue = stringValue.charAt(0);
			
//			boolean isInsideQuotes = ;						
			if(handleQuotes(charValue, quoteSymbols)){
				char pair = regionBoundPairMap.get(quoteSymbols.peek());
				boolean found = false;
				for (int q = i; q < tokens.size(); q++) {
					found = found || (pair == tokens.get(q).getStringValue().charAt(0));
					if (found) 
						break;
				}
				if (!found) {
					// If we're here, no closed bound found and that means that this is not region but just a symbol
					quoteSymbols.pop();
				}
			}
			
			if(!sentenceTerminalSymbols.contains(charValue)){
				continue;
			}
			
			if(i==0){
				continue;
			}
			
			if(stringValue.equals(".")){
				if (!quoteSymbols.isEmpty()) {
					continue;
				}
				IToken prev = tokens.get(i-1);
				String prevValue = prev.getStringValue();
				int prevType = prev.getType();
				if(prevType==IToken.TOKEN_TYPE_LETTER){
					if(prevValue.length()==1){
						continue;
					}
				}
				else if(prevType==IToken.TOKEN_TYPE_WORD_FORM){
					SyntaxToken wft = (SyntaxToken) prev;
					if(wft.hasAnyGrammem(abbr)){
						continue;
					}
				}
			}
			SentenceToken sent = new SentenceToken(tokens.get(start).getStartPosition(), token.getEndPosition());
			sent.setId(TokenRegistry.getVacantId());
			sent.setChildren(tokens.subList(start, i+1));
			sentences.add(sent);
			start = i + 1;
		}
		if(start<size){
			SentenceToken sent = new SentenceToken(tokens.get(start).getStartPosition(), tokens.get(size-1).getEndPosition());
			sent.setId(TokenRegistry.getVacantId());
			sent.setChildren(tokens.subList(start, size));
			sentences.add(sent);
		}
		return sentences;
	}

	private boolean handleQuotes(char charValue, Stack<Character> quoteSymbolStack)
	{
		
		if(!regionBoundSymbols.contains(charValue)){
			return !quoteSymbolStack.isEmpty();
		}
		
		if(openingRegionBoundSymbols.contains(charValue)){
			quoteSymbolStack.add(charValue);
			return true;
		}
		
		if(quoteSymbolStack.isEmpty()){
			if(!closingRegionBoundSymbols.contains(charValue)){		
				quoteSymbolStack.add(charValue);
				return true;
			}
			else{
				return false;
			}
		}
		else{
			Character pairCharacter = regionBoundPairMap.get(charValue);
			if(closingRegionBoundSymbols.contains(charValue)){				
				while(!quoteSymbolStack.isEmpty() && quoteSymbolStack.peek() == pairCharacter){
					quoteSymbolStack.pop();
				}
			}
			else{
				boolean gotMatch = false;
				while(!quoteSymbolStack.isEmpty() && quoteSymbolStack.peek() == pairCharacter){
					quoteSymbolStack.pop();
					gotMatch = true;
				}
				if(!gotMatch){
					quoteSymbolStack.add(charValue);
				}
			}
			return !quoteSymbolStack.isEmpty();
		}
	}

}
