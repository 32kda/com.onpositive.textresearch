package com.onpositive.text.analysis.lexic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.onpositive.text.analysis.IToken;
import com.onpositive.text.analysis.TokenRegistry;
import com.onpositive.text.analysis.utils.Exponent;
import com.onpositive.text.analysis.utils.Utils;
import com.onpositive.text.analysis.utils.VulgarFraction;

public class PrimitiveTokenizer {

	private static final HashMap<Character,Integer> map = new HashMap<Character, Integer>();
	
	static{
		fillMap();
	}
	
	private ArrayList<ITokenizerExtension> extensions = new ArrayList<ITokenizerExtension>();
	
	public boolean add(ITokenizerExtension e) {
		return extensions.add(e);
	}

	public boolean remove(ITokenizerExtension o) {
		return extensions.remove(o);
	}

	private boolean isSettingId;
	
	public List<IToken> tokenize(String str) { return tokenize(str, true); }
	
	public List<IToken> tokenize(String str, boolean setIds) {
		
		isSettingId = setIds;
		
		if( Utils.isEmptyString(str) ){
			return new ArrayList<IToken>();
		}
		
		List<IToken> tokens;
		if(extensions.isEmpty()){
			tokens = tokenizeSimply(str);			
		}
		else{
			tokens = tokenizeExtensively(str);
		}
		
		for(int i = 1 ; i < tokens.size() ; i++){
			IToken prev = tokens.get(i-1);
			IToken curr = tokens.get(i);
			
			prev.addNextToken(curr);
			curr.addPreviousToken(prev);
		}
		
		return tokens;
	}

	private List<IToken> tokenizeSimply(String str) {
		
		ArrayList<IToken> list = new ArrayList<IToken>();		
		
		int start = 0 ;
		int type = detectType(str.charAt(start));		
		int l = str.length();
		for(int i = 1 ; i < l; i++){			
			
			char ch = str.charAt(i);
			int cType = detectType(ch);
			if(areCompatible(cType, type)){
				continue;
			}
			
			if(type != IToken.TOKEN_TYPE_OTHER_WHITESPACE){
				String segment = str.substring(start,i);
				tokenizeSegment(list, start, type, segment);
			}
			start = i;
			type = cType;
		}
		String segment = str.substring(start,l);
		tokenizeSegment(list, start, type, segment);
		
		return list;
	}

	protected boolean areCompatible(int cType, int type) {
		if(cType==type){
			return true;
		}
		if(type==IToken.TOKEN_TYPE_LETTER&&cType==IToken.TOKEN_TYPE_DIGIT){
			return true;
		}
		return false;
	}

	private final void tokenizeSegment(ArrayList<IToken> list, int start, int type, String segment)
	{
		int sl = segment.length();
		if(type == IToken.TOKEN_TYPE_SYMBOL){
			for(int i = 0 ; i < sl ; i++){
				char ch = segment.charAt(i); 
				SymbolToken pt = new SymbolToken( ch, start+i, start+i+1 );
				if (isSettingId) pt.setId(TokenRegistry.getVacantId());
				list.add(pt);
			}
		}
		else if(type == IToken.TOKEN_TYPE_VULGAR_FRACTION){
			for(int i = 0 ; i < sl ; i++){
				char ch = segment.charAt(i); 
				StringToken pt = new StringToken( ""+ch, type, start+i, start+i+1 );
				if (isSettingId) pt.setId(TokenRegistry.getVacantId());
				list.add(pt);
			}
		}
		else{
			StringToken pt = new StringToken(segment, type, start, start + sl );
			if (isSettingId) pt.setId(TokenRegistry.getVacantId());
			list.add(pt);
		}		
	}


	private List<IToken> tokenizeExtensively(String str) {
		
		ArrayList<IToken> list = new ArrayList<IToken>();		
		
		int type = -1;
		int start = 0 ;
		int l = str.length();
		for(int i = 0 ; i < l; i++){			
			
			IToken extendedUnit = null;			
			for(ITokenizerExtension te : extensions){
				extendedUnit = te.readUnit(str, i);
				if(extendedUnit!=null){
					break;
				}
			}
			
			if(extendedUnit!=null){				
				if( type >=0 && type != IToken.TOKEN_TYPE_OTHER_WHITESPACE ){
					String segment = str.substring(start,i);
					tokenizeSegment(list, start, type, segment);
					type = -1;
				}				
				list.add(extendedUnit);
				
				start = extendedUnit.getEndPosition();
				i = extendedUnit.getEndPosition()-1;
			}
			else{
				char ch = str.charAt(i);
				int cType = detectType(ch);
				if(type < 0){
					type = cType;
					continue;
				}
				if(areCompatible(type, cType)){
					continue;
				}				
				if(type != IToken.TOKEN_TYPE_OTHER_WHITESPACE){
					String segment = str.substring(start,i);
					tokenizeSegment(list, start, type, segment);
				}
				start = i;
				type = cType;
			}
		}
		if(start<l){
			String segment = str.substring(start,l);
			tokenizeSegment(list, start, type, segment);
		}		
		return list;
	}
	
	public static int detectType(char ch) {
		
		Integer type = map.get(ch);
		if(type !=null)
			return type;
		
		if(Character.isWhitespace(ch)){
			return IToken.TOKEN_TYPE_OTHER_WHITESPACE;
		}
		
		if(Character.isDigit(ch)){
			return IToken.TOKEN_TYPE_DIGIT;
		}
		
		if(Character.isLetter(ch)){
			return IToken.TOKEN_TYPE_LETTER;
		}
		if(VulgarFraction.isVulgarFraction(ch)){
			return IToken.TOKEN_TYPE_VULGAR_FRACTION;
		}
		if(Exponent.isExponent(""+ch)){
			return IToken.TOKEN_TYPE_EXPONENT;
		}
		return IToken.TOKEN_TYPE_UNDEFINED;
	}
	
	private static void fillMap() {
		
		for(char ch : new char[]{'\r', '\n'}){
			map.put(ch, IToken.TOKEN_TYPE_LINEBREAK);
		}
		
		for(char ch : new char[]{'\u00A0', '\u2007', '\u202F'}){
			map.put(ch, IToken.TOKEN_TYPE_NON_BREAKING_SPACE);
		}
		
		char[] symbols = new char[]{
				'`', '~', '!',  '@',  '#', '$', '%', '^', '&', '*', '(', ')',
				'-', '_', '=',  '+', '\\', '|', ',', '.', '<', '>', '/', '?',
				';', ':', '\'', '"', '[', ']', '{', '}' ,  '—', '«', '»', '„', '“', '“', '”', '‘', '’'
			};
		for(char ch : symbols){
			map.put(ch, IToken.TOKEN_TYPE_SYMBOL);
		}
	}
	
	
	public static boolean isSymbols(char ch){
		Integer type = map.get(ch);
		if(type==null){
			return false;		
		}
		boolean result = type == IToken.TOKEN_TYPE_SYMBOL;
		return result;
	}
}
