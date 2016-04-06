package com.onpositive.semantic.categorization.manual;

import com.onpositive.semantic.categorization.core.SubSystemConfiguration;
import com.onpositive.semantic.search.core.ISearchEngine;

public interface ICategorizationFactory {
	
	ICategorization create(SubSystemConfiguration config,ISearchEngine engine);

}
