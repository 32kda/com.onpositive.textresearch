package com.onpositive.text.analisys.tests.euristics;

import java.util.List;

public abstract class AbstractNeutralizingProvider<T> {
	
	protected List<T> tokenList;
	
	private int curIndex;

	public AbstractNeutralizingProvider(List<T> tokenList) {
		super();
		this.tokenList = tokenList;
	}
	
	public T getToken(String word) {
		if (word == null) {
			return null;
		}
		word = word.toLowerCase().trim();
		for (int i = curIndex; i < tokenList.size(); i++) {
			if (getWord(i).toLowerCase().trim().equals(word)) {
				curIndex = i+1;
				return tokenList.get(i);
			}
		}
		return null;
	}

	protected abstract String getWord(int i);

}
