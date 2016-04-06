package com.onpositive.text.analisys.tools;

import java.util.*;
import java.util.stream.Stream;

import com.onpositive.semantic.wordnet.AbstractWordNet;
import com.onpositive.text.analysis.IToken;
import com.onpositive.text.analysis.lexic.WordFormToken;
import com.onpositive.text.analysis.syntax.SyntaxParser;


public class WFTConflictStat extends SyntaxParser {

	public class ConflictInfo {		
		public WordFormToken wft;
		public Set<String> tokenTypes;
		
		public ConflictInfo(WordFormToken wft, Set<String> parents) { 
			this.wft = wft; 
			this.tokenTypes = parents;
		}
	}
	
	public WFTConflictStat(AbstractWordNet wordnet) { super(wordnet); }

	public Set<String> parents(IToken token) {
		HashSet<String> result = new HashSet<String>();
		if (token.getParents() == null) return null;
		for (IToken prnt : token.getParents()) {
			result.add(prnt.getClass().getSimpleName() + "|" + prnt.getParserName());
			Set<String> pp = parents(prnt); 
			if (pp != null) result.addAll(pp);
		}
		
		return result;
	}
	
	public List<ConflictInfo[]> getConflicts(IToken sentence) {
		Map<IToken, Boolean> used = new HashMap<IToken, Boolean>();
		
		List<IToken> tokens = new ArrayList<IToken>(sentence.getChildren());
		Set<WordFormToken> wftset = new HashSet<WordFormToken>(); 
		while (tokens.size() > 0) {
			IToken token = tokens.remove(0);
			if (token instanceof WordFormToken) wftset.add((WordFormToken) token);
			if (used.get(token) != null) continue; 
			if (token.getChildren() != null) tokens.addAll(token.getChildren());
			if (token.getParents() != null) tokens.addAll(token.getParents());
			if (token.getNextTokens() != null) tokens.addAll(token.getNextTokens());
			if (token.getPreviousTokens() != null) tokens.addAll(token.getPreviousTokens());
			if (token.getNext() != null) tokens.add(token.getNext());
			if (token.getPrevious() != null) tokens.add(token.getPrevious());
			used.put(token, true);
		}
		
		ArrayList<ConflictInfo[]> result = new ArrayList<WFTConflictStat.ConflictInfo[]>();
		
		WordFormToken[] wfts = wftset.stream().sorted((x,y)->x.getStartPosition() - y.getStartPosition()).toArray(WordFormToken[]::new);
		
		for (int i = 0; i < wfts.length; i++) {
			int j = i;
			while (j < wfts.length -1 && wfts[i].getStartPosition() == wfts[j + 1].getStartPosition()) j += 1;
			if (i == j) continue;
			ConflictInfo[] ci = new ConflictInfo[j - i + 1];
			for (int cii = i; cii <= j; cii ++) 
				ci[cii-i] = new ConflictInfo(wfts[cii], parents(wfts[cii]));
			result.add(ci);
			i = j;
		}		
		
		return result;
	}
	
	public List<List<ConflictInfo[]>> getStatistic(List<IToken> tokens) {
		int completed = 0, count = tokens.size();
		List<List<ConflictInfo[]>> result = new ArrayList<List<ConflictInfo[]>>();
		for (IToken token: tokens) {
			onProcess.accept(++completed, count);
			result.add(getConflicts(token));
		}
		return result;
	
	}
	
}
