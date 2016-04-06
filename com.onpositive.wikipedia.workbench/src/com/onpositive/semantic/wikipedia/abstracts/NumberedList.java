package com.onpositive.semantic.wikipedia.abstracts;

public class NumberedList extends AbstractList {

	public NumberedList(TextAbstractElement parent, int level) {
		super(parent);
		this.listLevel = level;
		
	}

	public char sign() {
		return '#';
	}
	protected NumberedList createList(int level) {
		return new NumberedList(this, level);
	}
}
