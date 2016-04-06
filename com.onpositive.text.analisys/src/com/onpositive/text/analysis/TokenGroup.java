package com.onpositive.text.analysis;

import java.util.List;

public class TokenGroup {
	
	public TokenGroup(List<IToken> tokens) {
		super();
		this.tokens = tokens;
	}

	private int start;
	
	private int last;
	
	private int current;
	
	private List<IToken> tokens;
	
	public IToken getCurrentToken(){
		return tokens.get(current);
	}
	
	protected IToken getStartToken() {
		return tokens.get(start);
	}

	protected IToken getLastToken() {
		return tokens.get(last);
	}
	
	public IToken getNextToken(){
		if(current >= last){
			return null;
		}
		return tokens.get(current+1);
	}
	
	public IToken getPreviousToken(){
		if(current <= start){
			return null;
		}
		return tokens.get(current-1);
	}
	
	public void shiftForward(){
		current++;
	}
	
	public void shiftBack(){
		current--;
	}

	protected int getStartPosition() {
		return start;
	}

	protected int getLastPosition() {
		return last;
	}

	protected int getCurrentPosition() {
		return current;
	}
	
	protected void reset(int start, int last){
		this.last = last;
		this.start = start;
		this.current = start;
	}
}
