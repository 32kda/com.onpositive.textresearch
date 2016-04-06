package com.onpositive.text.analisys.tests.euristics;

import java.util.List;

public class EtalonTokenProvider extends AbstractNeutralizingProvider<SimplifiedToken> {

	public EtalonTokenProvider(List<SimplifiedToken> tokenList) {
		super(tokenList);
	}

	@Override
	protected String getWord(int i) {
		return tokenList.get(i).getWord();
	}	

}
