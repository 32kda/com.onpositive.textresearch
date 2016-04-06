package com.onpositive.semantic.search.core;

public class MatchDescription {

	public final int kind;
	public final int position;
	public final int length;

	public static final int LOCATION=1;
	
	public MatchDescription(int kind, int position, int length) {
		super();
		this.kind = kind;
		this.position = position;
		this.length = length;
	}
}
