package com.onpositive.text.analisys.tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.onpositive.semantic.wordnet.AbstractWordNet;
import com.onpositive.semantic.wordnet.Grammem.PartOfSpeech;
import com.onpositive.text.analysis.IToken;
import com.onpositive.text.analysis.lexic.WordFormToken;
import com.onpositive.text.analysis.syntax.SentenceToken;
import com.onpositive.text.analysis.syntax.SyntaxParser;

public class LexicParserStatGenerator extends SyntaxParser {

	public LexicParserStatGenerator(AbstractWordNet wordnet) { 
		super(wordnet);
		parts = new HashMap<String, Integer>();
		parts.put("DELIMITER", 0);
		parts.put("СУЩ", 1);
		parts.put("ПРИЛ", 2);
		parts.put("КР_ПРИЛ", 4);
		parts.put("КОМП", 8);
		parts.put("ГЛ", 16);
		parts.put("ИНФ", 32);
		parts.put("ПРИЧ", 64);
		parts.put("КР_ПРИЧ", 128);
		parts.put("ДЕЕПР", 256);
		parts.put("ЧИСЛ", 512);
		parts.put("Н", 1024);
		parts.put("МС", 2048);
		parts.put("ПРЕДК", 4096);
		parts.put("ПР", 8192);
		parts.put("СОЮЗ", 16384);
		parts.put("ЧАСТ", 32768);
		parts.put("МЕЖД", 65536);
		parts.put("	", 131072);
	}
	
	private HashMap<String, Integer> parts = null;
	
	public List<IToken> parse(String str){
		setText(str);
		List<IToken> primitiveTokens = primitiveTokenizer.tokenize(str);
		List<IToken> lexicProcessed = lexicParsers.process(primitiveTokens);
		
		List<IToken> sentences = sentenceSplitter.split(lexicProcessed);
		
		return sentences;
	}
	
	public List<IToken> getFirsts(SentenceToken token) {
		IToken curr = token.getChildren().get(0);
		IToken next = curr.getNext();
		if (next == null && curr.getNextTokens() != null) next = curr.getNextTokens().get(0);
		if (next == null || (next.getPrevious() == curr && (next.getPreviousTokens() == null) || next.getPreviousTokens().size() == 0)) return Arrays.asList(curr);
		return next.getPreviousTokens();
	}
	
	public List<IToken> getNexts(List<IToken> curr) {
		if (curr == null || curr.size() == 0) return null;
		IToken token = curr.get(0);
		if (token.getNext() == null && (token.getNextTokens() == null || token.getNextTokens().size() == 0)) return null;
		if (token.getNext() != null) return Arrays.asList(token.getNext());
		else return token.getNextTokens();
	}
	
	public List<Integer[]> sentencestat(SentenceToken token) {
		List<Integer[]> result = new ArrayList<Integer[]>();		
		List<Integer> cresult = new ArrayList<Integer>();

		for (List<IToken> curr = getFirsts(token); curr != null; curr = getNexts(curr)) {			
			int res = 0;
			for (IToken t : curr) {
				if (!(t instanceof WordFormToken)) continue;
				WordFormToken to = (WordFormToken) t;
				PartOfSpeech topos = to.getPartOfSpeech();
				if (topos == null) continue;
				else res += parts.get(topos.alias); 
			}
			if (res == 0) {
				if (cresult.size() > 0)
					result.add(cresult.toArray(new Integer[0]));
				cresult.clear();
			} else {
				cresult.add(res);
			}
		}		
		return result;
	}
	
	public List<Integer[]> stats(List<IToken> sentences) { 
		List<Integer[]> result = new ArrayList<Integer[]>();
		for (IToken token : sentences) 
			result.addAll(sentencestat((SentenceToken) token));
		return result;
	}
}
