package com.onpositive.text.analisys.tests.euristics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.onpositive.semantic.wordnet.AbstractWordNet;
import com.onpositive.semantic.wordnet.WordNetProvider;
import com.onpositive.text.analisys.tests.model.EuristicMatch;
import com.onpositive.text.analysis.Euristic;
import com.onpositive.text.analysis.IToken;
import com.onpositive.text.analysis.lexic.PrimitiveTokenizer;
import com.onpositive.text.analysis.lexic.WordFormParser;
import com.onpositive.text.analysis.lexic.WordFormToken;

public class EuristicTestingUtil {
	
	private static final int SEQUENCE_LENGTH = 2;
	private List<Euristic> euristics;
	private List<Map<IToken, EuristicMatch>> matchedEuristics = new ArrayList<Map<IToken, EuristicMatch>>();
	
	public EuristicTestingUtil(List<Euristic> euristics) {
		this.euristics = euristics;
	}
	
	public List<IToken> process(String str) {
		return process(getWordFormTokens(str));
	}
	
	public List<IToken> process(List<IToken> tokens) {
		List<List<IToken>> possibleChains = calcVariants(tokens);
		if (possibleChains.isEmpty()) {
			return Collections.emptyList();
		}
		List<IToken> curChain = possibleChains.get(0);
		for (int i = 0; i < curChain.size(); i++) {
			Map<IToken, EuristicMatch>curMap = new HashMap<IToken, EuristicMatch>();
			IToken token = curChain.get(i);
			if (token instanceof WordFormToken && token.hasConflicts()) {
				List<List<IToken>> invalidChains = new ArrayList<List<IToken>>();
				for (int j = 0; j < possibleChains.size(); j++) {
					List<List<IToken>> sequences = getSequences(possibleChains.get(j), i);
					boolean found = false;
					for (List<IToken> curSequence: sequences) {
						Euristic matchedEuristic = matchedNonConflict(euristics, curSequence);
						if (matchedEuristic != null) {
							curMap.put(possibleChains.get(j).get(i), new EuristicMatch(matchedEuristic, curSequence));
							found = true;
							break;
						}
					}
					if (!found) {
						invalidChains.add(possibleChains.get(j));
					}
				}
				if (!invalidChains.isEmpty()) {
					possibleChains.removeAll(invalidChains);
					if (!possibleChains.isEmpty()) {
						curChain = possibleChains.get(0);
					} else {
						return Collections.emptyList();
					}
				}
				IToken comparedToken = curChain.get(i);
				int idx = i;
				boolean hasDifferentOptions = possibleChains.stream().anyMatch(chain -> !comparedToken.equals(chain.get(idx)));
				if (hasDifferentOptions) {
					matchedEuristics.add(curMap);
				}
			} 
		}
		return possibleChains.get(0);
	}
	
	public List<IToken> getWordFormTokens(String str) {
		PrimitiveTokenizer pt = new PrimitiveTokenizer();
		AbstractWordNet instance = WordNetProvider.getInstance();
		WordFormParser wfParser = new WordFormParser(instance);
		List<IToken> tokens = pt.tokenize(str);		
		List<IToken> processed = wfParser.process(tokens);
		return processed;
	}
	
	private Euristic matchedNonConflict(List<Euristic> euristicsToTry, List<IToken> tokens) {
		for (Euristic euristic : euristicsToTry) {
			if (euristic.match(tokens.toArray(new IToken[0]))) {
				return euristic;
			}
		}
		return null;
	}
	
	private List<List<IToken>> getSequences(List<IToken> tokens, int idx) {
		List<List<IToken>> result = new ArrayList<List<IToken>>();
		int start = Math.max(0, idx - SEQUENCE_LENGTH + 1);
		for (int i = start; i <= idx && i + SEQUENCE_LENGTH <= tokens.size(); i++) {
			List<IToken> curList = new ArrayList<IToken>();
			for (int j = i; j < i + SEQUENCE_LENGTH; j++) {
				curList.add(tokens.get(j));
			}
			result.add(curList);
		}
		return result;
	}
	
	private List<List<IToken>> calcVariants(List<IToken> source) {
		List<IToken> workingCopy = new ArrayList<IToken>(source);
		List<List<IToken>> result = new ArrayList<List<IToken>>();
		result.add(new ArrayList<IToken>());
		for (int i = 0; i < workingCopy.size(); i++) {
			if (workingCopy.get(i).getConflicts() == null || workingCopy.get(i).getConflicts().isEmpty()) {
				addItem(result, workingCopy.get(i));
			} else {
				List<IToken> conflicts = new ArrayList<IToken>(workingCopy.get(i).getConflicts());
				conflicts.add(0, workingCopy.get(i));
				while (i+1 < workingCopy.size() && conflicts.contains(workingCopy.get(i+1))) {
					workingCopy.remove(i+1);
				}
				result = generateVariants(result,conflicts);
			}
		}
		return result;
	}
	
	private List<List<IToken>> generateVariants(List<List<IToken>> prevResult, List<IToken> conflicts) {
		List<List<IToken>> result = new ArrayList<List<IToken>>();
		for (List<IToken> list : prevResult) {
			for (IToken curToken : conflicts) {
				List<IToken> newList = new ArrayList<IToken>(list);
				newList.add(curToken);
				result.add(newList);
			}
		}
		return result;
	}

	private void addItem(List<List<IToken>> result, IToken token) {
		for (List<IToken> list : result) {
			list.add(token);
		}
	}
	
	public void printConflictingEuristics() {
		for (Map <IToken, EuristicMatch> curMap: matchedEuristics) {
			Map<IToken, EuristicMatch> map = curMap;
			List<String> euristicList = curMap.keySet().stream().map(token -> {
				return token.toString() + map.get(token).toString();
			}).collect(Collectors.toList());
			String joined = String.join(", ", euristicList);
			System.out.println("Конфликт: [" + joined + "]");
		}
	}

	public List<Map<IToken, EuristicMatch>> getMatchedEuristics() {
		return matchedEuristics;
	}

}
