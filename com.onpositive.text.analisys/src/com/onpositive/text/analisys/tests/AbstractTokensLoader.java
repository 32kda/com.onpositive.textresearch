package com.onpositive.text.analisys.tests;

import java.util.ArrayList;
import java.util.List;

import com.onpositive.text.analisys.tests.euristics.SimplifiedToken;

public abstract class AbstractTokensLoader {

	protected List<List<SimplifiedToken>> chains = new ArrayList<List<SimplifiedToken>>();
	protected String initialText;

	public AbstractTokensLoader() {
		super();
	}

	public List<List<SimplifiedToken>> getChains() {
		return chains;
	}

	public String getInitialText() {
		return initialText;
	}

	public List<SimplifiedToken> getTokens() {
		List<SimplifiedToken> result = new ArrayList<SimplifiedToken>();
		chains.stream().forEach(list -> result.addAll(list));
		return result;
	}

}