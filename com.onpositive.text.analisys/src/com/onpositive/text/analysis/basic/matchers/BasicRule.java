package com.onpositive.text.analysis.basic.matchers;

import java.util.Arrays;
import java.util.List;

import com.onpositive.text.analysis.IToken;


public class BasicRule{
	
	public static class BoundData{
		
		public BoundData() {
			super();
		}

		public BoundData(int boundOffset, int matchOffset, int length) {
			super();
			this.boundOffset = boundOffset;
			this.matchOffset = matchOffset;
			this.length = length;
		}

		private int boundOffset = 0;
		
		private int matchOffset = 0;
		
		private int length = 1;

		public int getBoundOffset() {
			return boundOffset;
		}

		public void setBoundOffset(int boundOffset) {
			this.boundOffset = boundOffset;
		}

		public int getMatchOffset() {
			return matchOffset;
		}

		public void setMatchOffset(int matchOffset) {
			this.matchOffset = matchOffset;
		}

		public int getLength() {
			return length;
		}

		public void setLength(int length) {
			this.length = length;
		}
		
	}
	
	
	
	public BasicRule(ITokenArrayMatcher startMatcher,
			ITokenArrayMatcher endMatcher) {
		super();
		this.startMatcher = startMatcher;
		this.endMatcher = endMatcher;
	}
	
	public BasicRule(ITokenMatcher startMatcher,
			ITokenMatcher endMatcher) {
		super();
		this.startMatcher = new TokenArrayMatcher(Arrays.asList(startMatcher));
		this.endMatcher = new TokenArrayMatcher(Arrays.asList(endMatcher));
	}
	
	public BasicRule(ITokenMatcher startMatcher,
			ITokenArrayMatcher endMatcher) {
		super();
		this.startMatcher = new TokenArrayMatcher(Arrays.asList(startMatcher));
		this.endMatcher = endMatcher;
	}
	
	public BasicRule(String leftString, String rightString) {
		super();
		this.startMatcher = new TokenArrayMatcher(leftString);
		this.endMatcher = new TokenArrayMatcher(rightString);
	}

	protected ITokenArrayMatcher startMatcher;
	
	protected ITokenArrayMatcher endMatcher;
	
	protected BoundData startBound = new BoundData();
	
	protected BoundData endBound = new BoundData();
	
	public boolean matchStart(List<IToken> tokens, int pos){
		return startMatcher.match(tokens, pos +  startBound.getMatchOffset());
	}
	
	public boolean matchEnd(List<IToken> tokens, int pos){
		return endMatcher.match(tokens, pos + endBound.getMatchOffset());
	}
	
	@Override
	public String toString() {		
		return "start: " + startMatcher.toString() + "; end: " + endMatcher.toString();
	}

	public BoundData getStartBound() {
		return startBound;
	}

	public void setStartBound(BoundData startBound) {
		this.startBound = startBound;
	}

	public BoundData getEndBound() {
		return endBound;
	}

	public void setEndBound(BoundData endBound) {
		this.endBound = endBound;
	}


}