package com.onpositive.semantic.wikipedia2;

import com.onpositive.semantic.categorization.core.SubSystemConfiguration;
import com.onpositive.semantic.search.core.ISearchEngine;
import com.onpositive.semantic.search.core.ISearchEngineFactory;
import com.onpositive.semantic.wikipedia2.WikiEngine2;

public class WikiEngineFactory implements ISearchEngineFactory {

	@Override
	public ISearchEngine create(SubSystemConfiguration configuration) {
		WikiEngine2 wikiEngine = new WikiEngine2(
				configuration.getProperty("dir"),
				configuration.getProperty("state"),
				configuration.getProperty("url"),configuration.getProperty("kind"));
		return wikiEngine;
	}

}
