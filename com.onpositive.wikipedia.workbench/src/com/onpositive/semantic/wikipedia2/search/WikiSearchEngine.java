package com.onpositive.semantic.wikipedia2.search;

import com.onpositive.semantic.search.core.ISearchResultCallback;
import com.onpositive.semantic.search.core.SearchRequest;
import com.onpositive.semantic.wikipedia2.WikiEngine2;

public class WikiSearchEngine {

	WikiEngine2 engine;
	private QuickSearcher qs;
	public WikiSearchEngine(WikiEngine2 wikiEngine2) {
		this.engine=wikiEngine2;
		qs=new QuickSearcher(engine);
	}
	public void search(SearchRequest request, ISearchResultCallback callback) {
		qs.search(engine, request, callback);
	}

	public void clearPopularity(){
		qs.clearPopularity();
	}
	public void clear(){
		qs.clear();
	}
	
}
