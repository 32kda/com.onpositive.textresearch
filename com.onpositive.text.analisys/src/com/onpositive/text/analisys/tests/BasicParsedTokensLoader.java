package com.onpositive.text.analisys.tests;

import java.util.List;

public abstract class BasicParsedTokensLoader extends AbstractTokensLoader {

	public BasicParsedTokensLoader() {
		super();
	}

	public abstract List<String> getSentences();

}