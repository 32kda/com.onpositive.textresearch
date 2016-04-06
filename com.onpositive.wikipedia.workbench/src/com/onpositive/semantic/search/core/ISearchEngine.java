package com.onpositive.semantic.search.core;

import com.onpositive.semantic.categorization.manual.ICategorization;

public interface ISearchEngine {
	String id();

	void search(SearchRequest request,ISearchResultCallback callback);
	
	IDocument  getDocument(String id);

	ICategory[] getCategories(String startingWith);
	
	ICategory[] getCategoriesByName(String startingWith);
	
	void learn(ICategorization manual,ILearningCallback callback);
	
	void postInit();

	IDocument[] getDocumentsStartingWith(String filter);

	ScoredTopic[] matchTopics(String text);

	void clearBlackList();
}
