package com.onpositive.semantic.search.core;

public class ScoredTopic implements Comparable<ScoredTopic>{

	public ScoredTopic(String id, double compareScore) {
		this.topic=id;
		this.initial=compareScore;
	}
	public String topic;
	public int score;
	public double initial;
	@Override
	public int compareTo(ScoredTopic o) {
		return this.initial<o.initial?-1:1;
	}
}
