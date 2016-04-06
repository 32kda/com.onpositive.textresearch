package com.onpositive.text.analysis.projection.creators;

import java.util.ArrayList;
import java.util.List;

import com.onpositive.text.analysis.IToken;
import com.onpositive.text.analysis.projection.IProjectionCreator;

public abstract class TokenRemover implements IProjectionCreator {

	@Override
	public boolean isApplicable(List<IToken> chain) {
		for (IToken token : chain) {
			if (shouldBeRemoved(token)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void applyTo(List<IToken> chain) {
		List<IToken> toRemove = new ArrayList<IToken>();
		for (IToken token : chain) {
			if (shouldBeRemoved(token)) {
				toRemove.add(token);
			}
		}
		chain.removeAll(toRemove);
	}

	protected abstract boolean shouldBeRemoved(IToken token);

}
