package com.onpositive.semantic.categorization.manual;

import java.io.Reader;
import java.io.Writer;

import com.onpositive.semantic.categorization.core.Category;
import com.onpositive.semantic.search.core.ICategorizable;
import com.onpositive.semantic.search.core.ICategory;
import com.onpositive.semantic.search.core.ISearchEngine;

public interface ICategorization {

	Category[] getCategories(ICategorizable doc);

	ICategory[] getCategories(Category cat);

	void loadCategorization(Reader reader);
	
	void dumpCategorization(Writer writer);

	ISearchEngine engine();
}
