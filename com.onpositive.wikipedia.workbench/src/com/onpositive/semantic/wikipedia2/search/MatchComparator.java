package com.onpositive.semantic.wikipedia2.search;

import com.onpositive.compactdata.IntComparator;

public class MatchComparator extends IntComparator {
	
	
	protected SearchParticipant.SearchResult[]results;
	
	public int compare(int d1, int d2) {
		int cat1 = category(d1);
		int cat2 = category(d2);

		if (cat1 != cat2) {
			return cat1 - cat2;
		}
		return compareInsideRank(d1, d2);
	}

	private int compareInsideRank(int d1, int d2) {
		return 0;
	}

	private int category(int e1) {
		int cat=0;
		for (SearchParticipant.SearchResult a:results){
			if (a.goodMatches.contains(e1)){
				cat++;
			}
		}
		return 0;
	}
}
