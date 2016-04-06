package com.onpositive.semantic.wikipedia.abstracts;

public class UnnumberedList extends AbstractList {

	public UnnumberedList(TextAbstractElement parent, int level) {
		super(parent);
		if (level<0){
			level=0;//one character case
		}
		this.listLevel = level;		
		
	}

	public char sign() {
		return '*';
	}
	protected UnnumberedList createList(int level) {
		return new UnnumberedList(this, level);
	}

}
