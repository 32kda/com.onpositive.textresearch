package com.onpositive.text.analisys.tests.euristics;

class Pair implements Comparable<Pair>{
	int totalCount;
	int wrongCount;
	
	public Pair() {
		super();
	}
	
	public void incTotalCount() {
		totalCount++;
	}
	
	public void incWrongCount() {
		wrongCount++;
	}

	public int getTotalCount() {
		return totalCount;
	}

	public int getWrongCount() {
		return wrongCount;
	}

	@Override
	public int compareTo(Pair o) {
		return totalCount - o.totalCount;
	}
}