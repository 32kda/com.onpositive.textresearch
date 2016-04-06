package com.onpositive.text.analysis.filtering;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.onpositive.text.analysis.IToken;

public abstract class AbstractChainsFilter implements IPossibleChainsFilter {

	@Override
	public List<List<IToken>> getFilteredOut(int tokenIndex, List<List<IToken>> original) {
		if (original.size() > 1 && hasDifferentTokens(original, tokenIndex)) {
			List<List<IToken>> toRemove = new ArrayList<List<IToken>>();
			for (int i = 0; i < original.size(); i++) {
				IToken token = original.get(i).get(tokenIndex);
				if (shouldFilterOut(token)) {
					toRemove.add(original.get(i));
				}
			}
			return toRemove;
		}
		return Collections.emptyList();
	}

	private boolean hasDifferentTokens(List<List<IToken>> original, int tokenIndex) {
		IToken token = original.get(0).get(tokenIndex);
		if (!token.hasConflicts()) {
			return false;
		}
		for (int i = 1; i < original.size(); i++) {
			IToken curToken = original.get(i).get(tokenIndex);
			if (!curToken.equals(token)) {
				return true;
			}
		}
		return false;
	}

	protected abstract boolean shouldFilterOut(IToken token);

}
