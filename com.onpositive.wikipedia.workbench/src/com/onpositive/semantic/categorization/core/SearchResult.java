package com.onpositive.semantic.categorization.core;

import com.onpositive.semantic.search.core.SearchMatch;

public class SearchResult {

	public SearchResult(int totalCount, SearchMatch[] results,String queryId) {
		super();
		this.totalCount = totalCount;
		this.results = results;
		this.queryId=queryId;
	}
	public final int totalCount;
	public final SearchMatch[] results;
	public final String queryId;
}
