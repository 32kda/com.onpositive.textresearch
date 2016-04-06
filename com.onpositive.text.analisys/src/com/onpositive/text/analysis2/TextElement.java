package com.onpositive.text.analysis2;

public abstract class TextElement {

	protected final int start;
	protected final int end;
	protected final int kind;
	
	public TextElement(int start, int end,int kind) {
		super();
		this.start = start;
		this.end = end;
		this.kind=kind;
	}
}
