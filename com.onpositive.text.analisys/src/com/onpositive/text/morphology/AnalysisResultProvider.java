package com.onpositive.text.morphology;

import java.util.List;

import com.onpositive.text.analisys.tests.euristics.AbstractNeutralizingProvider;

public class AnalysisResultProvider extends AbstractNeutralizingProvider<AnalisysResult> {

	public AnalysisResultProvider(List<AnalisysResult> tokenList) {
		super(tokenList);
	}

	@Override
	protected String getWord(int i) {
		AnalisysResult result = tokenList.get(i);
		if(result != null) {
			return result.content;
		}
		return null;
	}

}
