package com.onpositive.semantic.search.core;

public interface ISearchResultCallback {

	void acceptDocument(SearchMatch d);
	
	void done(int totals);
}

