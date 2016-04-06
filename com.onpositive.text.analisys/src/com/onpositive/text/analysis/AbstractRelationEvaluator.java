package com.onpositive.text.analysis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.onpositive.text.analysis.lexic.SymbolToken;
import com.onpositive.text.analysis.lexic.WordFormToken;

public abstract class AbstractRelationEvaluator {
	
	private static HashMap<Class<? extends AbstractRelationEvaluator>, AbstractRelationEvaluator> instances = new HashMap<>();

	public static void register(Class<? extends AbstractRelationEvaluator> type) {
		AbstractRelationEvaluator localInstance = instances.get(type);
		if (localInstance != null) return;
		
		synchronized (AbstractRelationEvaluator.class) {
			localInstance = instances.get(type);
            if (localInstance == null) {
                try {
					localInstance = type.newInstance();
					instances.put(type, localInstance);
				} catch (Exception e) { e.printStackTrace(); }
            }
		}
	}
	
	public static void bprocess(IToken token, boolean propagate) {
		for (Class<?> ins : instances.keySet()) {
			instances.get(ins).process(token, propagate);
		}
	}
	
	public static AbstractRelationEvaluator getInstance(Class<? extends AbstractRelationEvaluator> type) {
		AbstractRelationEvaluator localInstance = instances.get(type);
        if (localInstance == null) {
            synchronized (AbstractRelationEvaluator.class) {
                localInstance = instances.get(type);
                if (localInstance == null) {
                    try {
						localInstance = type.newInstance();
						instances.put(type, localInstance);
					} catch (Exception e) {
						return null;
					}
                }
            }
        }
        return localInstance;
	}
	
	private WordFormToken[] toArray(List<IToken> list) {
		return list
				.stream()
				.filter(x->x instanceof WordFormToken)
				.map(x->(WordFormToken)x)
				.toArray(WordFormToken[]::new);
	}
	
	protected WordFormToken[] nexts(IToken token) {
		if (token.getNextTokens() == null) return new WordFormToken[0];
		return toArray(token.getNextTokens());
	}
	
	protected WordFormToken[] prevs(IToken token) {
		if (token.getPreviousTokens() == null) return new WordFormToken[0];
		return toArray(token.getPreviousTokens());
	}
	

	
	
	private HashMap<IToken, WeightedProbability> mem = new HashMap<>();
	
	void save(IToken token, WeightedProbability value) { mem.put(token, value);	}
	
	WeightedProbability load(IToken token) {
		if (!mem.containsKey(token)) 
			mem.put(token, calculate(token));
		return mem.get(token);
	}
	
	public abstract void clear();
	
	protected abstract WeightedProbability calculate(IToken token);

	private Set<IToken> used = new HashSet<IToken>(); 

	private void go(IToken token, boolean propagate) {
		if (token.childrenCount() > 0)
			for (IToken child : token.getChildren())
				go(child, propagate);
		
		if (used.contains(token)) return;
		if (propagate && (token instanceof WordFormToken || token instanceof SymbolToken)) {
			used.add(token);
			return;
		}
		
		WeightedProbability rel = load(token);
		token.setCorrelation(rel.probability, rel.weight);
		used.add(token);
	}
	
	
	
	
	public void process(IToken token, boolean propagate) {
		clear();
		mem.clear();
		used.clear();
		
		go(token, propagate);
		
	}	
}
