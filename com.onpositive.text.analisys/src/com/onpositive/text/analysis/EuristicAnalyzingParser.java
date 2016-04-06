package com.onpositive.text.analysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.onpositive.text.analysis.lexic.WordFormToken;
import com.onpositive.text.analysis.projection.IProjectionCreator;
import com.onpositive.text.analysis.projection.Projection;
import com.onpositive.text.analysis.rules.RuleSet;
import com.onpositive.text.analysis.utils.AdditionalMetadataHandler;
import com.onpositive.text.analysis.utils.MorphologicUtils;
import com.onpositive.text.analysis.utils.Stats;

public class EuristicAnalyzingParser extends MorphologicParser{
	
	private static final boolean DEBUG = true;
	
	private static final int SEQUENCE_LENGTH = 2;
	
	private List<Euristic> euristics;
	
	private List<List<IToken>> possibleChains;
	
	private List<IProjectionCreator> projectionCreators = new ArrayList<IProjectionCreator>();
	
	private IToken checkedToken;

	private Map<IToken, Euristic> matchedEuristics = new HashMap<IToken, Euristic>(); //TODO remove it?
	
	public EuristicAnalyzingParser() {
		this.euristics = RuleSet.getFullRulesList();
	}
	
	public EuristicAnalyzingParser(List<Euristic> euristics) {
		this.euristics = euristics;
	}
	
	@Override
	public List<IToken> process(List<IToken> tokens) {
		Stats.startHeuristic();
		List<IToken> result = super.process(tokens);
		Stats.finishHeuristic();
		return result;
	}

//	public List<IToken> process(List<IToken> tokens) {
//		List<List<IToken>> possibleChains = calcVariants(tokens);
//		if (possibleChains.isEmpty()) {
//			this.possibleChains = Collections.emptyList(); 
//			return Collections.emptyList();
//		}
////		doPreFiltering(possibleChains);
//		this.possibleChains = applyEuristics(possibleChains);
//		List<List<IToken>> failedChains = new ArrayList<List<IToken>>(possibleChains);
//		failedChains.removeAll(this.possibleChains);
//		if (!projectionCreators.isEmpty()) {
//			this.possibleChains.addAll(applyToProjections(failedChains));
//		}
//		return this.possibleChains.get(0);
//	}
	
	public List<IToken> processPlain(List<IToken> tokens) {
		List<IToken> resultTokens = applyEuristicsNew(tokens);
		return resultTokens;
	}

	protected List<List<IToken>> applyToProjections(List<List<IToken>> possibleChains) {
		List<Projection> projections = new ArrayList<Projection>();
		for (List<IToken> chain : possibleChains) {
			List<IToken> workingCopy = new ArrayList<IToken>(chain);
			boolean wasApplied = false;
			for (IProjectionCreator creator : projectionCreators) {
				if (creator.isApplicable(workingCopy)) {
					wasApplied = true;
					creator.applyTo(workingCopy);
				}
				if (wasApplied) {
					projections.add(new Projection(chain,workingCopy));
				}
				
			}
		}
		List<List<IToken>> result = new ArrayList<List<IToken>>();
		for (Projection projection : projections) {
			if (projectionMatches(projection)) {
				result.add(projection.original);
			}
		}
		return result;
	}

	private boolean projectionMatches(Projection projection) {
		for (int i = 0; i < projection.tokens.size(); i++) {
			IToken token = projection.tokens.get(i);
			checkedToken = token;
			if (token instanceof WordFormToken && token.hasConflicts() && !tryMatchConflicting(getSequences(projection.tokens,i ))) {
				return false;
			}
		}
		if (DEBUG) {
			System.out.println("Проекция разобрана успешно");
			System.out.println("Оригинал " + projection.original);
			System.out.println("Проекция " + projection.tokens);
		}
		return true;
	}

	protected List<List<IToken>> applyEuristics(List<List<IToken>> possibleChains) {
		List<List<IToken>> result = new ArrayList<List<IToken>>(possibleChains);
		List<IToken> curChain = result.get(0);
		for (int i = 0; i < curChain.size(); i++) {
			IToken token = curChain.get(i);
			if (token instanceof WordFormToken && token.hasConflicts()) {
				List<List<IToken>> invalidChains = new ArrayList<List<IToken>>();
				for (int j = 0; j < result.size(); j++) {
					List<IToken> curResult = result.get(j);
					if (!tryMatchConflicting(getSequences(curResult,i))) {
						invalidChains.add(result.get(j));
					}
				}
				if (!invalidChains.isEmpty() && invalidChains.size() < result.size()) { //If nothing was matched - leave all possibilities for now 
					setTriggered(true);
					result.removeAll(invalidChains);
					if (!result.isEmpty()) {
						curChain = result.get(0);
					} else {
						return Collections.emptyList();
					}
				}
			} 
		}
		return result;
	}
	
	protected List<IToken> applyEuristicsNew(List<IToken> tokens) {
		doFiltering(tokens);
		List<IToken> chain = MorphologicUtils.getWithNoConflicts(tokens);
		for (int i = 0; i < chain.size(); i++) {
			IToken token = chain.get(i);
			if (token instanceof WordFormToken && token.hasConflicts()) {
				List<List<IToken>> localVariants = getLocalVariants(chain, i);
				int interestingIdx = i == 0 ? 0 : SEQUENCE_LENGTH - 1;
				Set<IToken> passed = new HashSet<IToken>();
				for (int j = 0; j < localVariants.size(); j++) {
					List<IToken> curResult = localVariants.get(j);
					checkedToken = curResult.get(interestingIdx);
					if (tryMatchConflicting(getLocalSequences(curResult))) {
						passed.add(checkedToken);
					}
				}
				List<IToken> conflicts = token.getConflicts();
				double failedCorrelation = passed.isEmpty() ? 1.0 / (conflicts.size() + 1) : 0;
				for (IToken passedToken : passed) {
					passedToken.setCorrelation(1, 1);
				}
				if (!passed.contains(token)) {
					setFailedCorrelation(token, failedCorrelation);
				}
				for (IToken curConflicting : conflicts) {
					if (!passed.contains(curConflicting)) {
						setFailedCorrelation(curConflicting, failedCorrelation);
					}
				}
			} 
		}
		return tokens;
	}

	protected void setFailedCorrelation(IToken token, double failedCorrelation) {
		if (failedCorrelation > 0) {
			token.setCorrelation(failedCorrelation,1);
		} else {
			token.setCorrelation(failedCorrelation,Double.POSITIVE_INFINITY);
		}
	}
	
	private List<List<IToken>> getLocalSequences(List<IToken> original) {
		List<List<IToken>> result = new ArrayList<List<IToken>>();
		for (int i = 0; i < original.size() - SEQUENCE_LENGTH + 1; i++) {
			List<IToken> curList = new ArrayList<IToken>();
			for (int j = i; j < i + SEQUENCE_LENGTH; j++) {
				curList.add(original.get(j));
			}
			result.add(curList);
		}
		return result;
	}

	protected boolean tryMatchConflicting(List<List<IToken>> sequences) {
		for (List<IToken> curSequence: sequences) {
			if (matchedNonConflict(euristics, curSequence)) {
				return true;
			}
		}
		return false;
	}
	
	public void addProjectionCreator(IProjectionCreator projectionCreator) {
		projectionCreators.add(projectionCreator);
	}
	
//	private void doPreFiltering(List<List<IToken>> chains) {
//		if (chains.size() > 1) {
//			List<IToken> curChain = chains.get(0);
//			for (int i = 0; i < curChain.size(); i++) {
//				IToken token = curChain.get(i);
//				if (token instanceof WordFormToken && token.hasConflicts()) {
//					List<List<IToken>> toRemove = new ArrayList<List<IToken>>();
//					for (IPossibleChainsFilter filter : possibleChainsFilters) {
//						toRemove.addAll(filter.getFilteredOut(i, chains));
//					}
//					if (!toRemove.isEmpty()) {
//						setTriggered(true);
//						chains.removeAll(toRemove);
//					}
//					if (chains.size() > 1) {
//						curChain = chains.get(0);
//					} else {
//						return;
//					}
//				}
//			}
//		}
//		
//	}

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
	
	private List<List<IToken>> getLocalVariants(List<IToken> tokens, int idx) {
		List<List<IToken>> result = new ArrayList<List<IToken>>();
		result.add(new ArrayList<IToken>());
		int start = Math.max(0, idx - SEQUENCE_LENGTH + 1);
		int end = Math.min(tokens.size() - 1, idx + SEQUENCE_LENGTH - 1);
		for (int i = start; i <= end; i++) {
			IToken curToken = tokens.get(i);
			if (curToken.hasConflicts()) {
				List<IToken> conflicts = new ArrayList<IToken>(curToken.getConflicts());
				conflicts.add(curToken);
				conflicts = conflicts.stream().filter(token -> !token.hasCorrelation() || token.getCorrelation() > 0).collect(Collectors.toList());
				result = generateVariants(result, conflicts);
			} else {
				addItem(result,curToken);
			}
//			List<IToken> curList = new ArrayList<IToken>();
//			for (int j = i; j < i + SEQUENCE_LENGTH; j++) {
//				curList.add(tokens.get(j));
//			}
//			result.add(curList);
		}
		return result;
	}
	
	private boolean matchedNonConflict(List<Euristic> euristicsToTry, List<IToken> tokens) {
		for (Euristic euristic : euristicsToTry) {
			if (euristic.match(tokens.toArray(new IToken[0]))) {
				if (DEBUG) {
					AdditionalMetadataHandler.store(checkedToken, AdditionalMetadataHandler.EURISTIC_KEY, euristic.getId());
					matchedEuristics.put(checkedToken, euristic);
				}
				return true;
			}
		}
		return false;
	}
	
	public Euristic getMatchedEuristic(IToken token) {
		return matchedEuristics.get(token);
	}

//	private List<List<IToken>> calcVariants(List<IToken> source) {
//		List<IToken> workingCopy = new ArrayList<IToken>(source);
//		List<List<IToken>> result = new ArrayList<List<IToken>>();
//		result.add(new ArrayList<IToken>());
//		for (int i = 0; i < workingCopy.size(); i++) {
//			if (workingCopy.get(i).getConflicts() == null || workingCopy.get(i).getConflicts().isEmpty()) {
//				addItem(result, workingCopy.get(i));
//			} else {
//				List<IToken> conflicts = new ArrayList<IToken>(workingCopy.get(i).getConflicts());
//				conflicts.add(0, workingCopy.get(i));
//				while (i+1 < workingCopy.size() && conflicts.contains(workingCopy.get(i+1))) {
//					workingCopy.remove(i+1);
//				}
//				result = generateVariants(result,conflicts);
//			}
//		}
//		return result;
//	}
	
	@Override
	public boolean isRecursive() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setHandleBounds(boolean b) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<IToken> getNewTokens() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setTokenIdProvider(TokenIdProvider tokenIdProvider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public TokenIdProvider getTokenIdProvider() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void clean() {
		// TODO Auto-generated method stub
		
	}

	public List<List<IToken>> getPossibleChains() {
		return possibleChains;
	}

}
