package com.onpositive.semantic.wikipedia2.search;

import com.carrotsearch.hppc.IntOpenHashSet;
import com.onpositive.semantic.search.core.SearchRequest;

public abstract class SearchParticipant {

	static class SearchResult{
		
		protected IntOpenHashSet goodMatches;
		protected IntOpenHashSet allMatches;
	}
	
	abstract SearchResult getMatchingResults(SearchRequest query);
	

	/**
	 * 1. Retain results from all participants
	 * 2. Sort
	 */
}
