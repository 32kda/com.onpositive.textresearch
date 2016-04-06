package com.onpositive.text.analysis;

public class DefaultRelationEvaluator extends AbstractRelationEvaluator {

	@Override
	public void clear() {
		// Do nothing
	}

	@Override
	protected WeightedProbability calculate(IToken token) {
		if (!token.hasCorrelation()) {
			if (!token.hasConflicts()) {
				return WeightedProbability.True; 
			} else {
				int conflictsSize = token.getConflicts().size();
				return new WeightedProbability(1.0 / conflictsSize, 1.0);
			}
		} else {
			return new WeightedProbability(token.getCorrelation(), 1.0);
		}
	}

}
