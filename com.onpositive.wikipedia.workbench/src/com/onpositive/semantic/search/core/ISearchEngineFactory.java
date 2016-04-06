package com.onpositive.semantic.search.core;

import com.onpositive.semantic.categorization.core.SubSystemConfiguration;

public interface ISearchEngineFactory {
	ISearchEngine create(SubSystemConfiguration configuration);
}
