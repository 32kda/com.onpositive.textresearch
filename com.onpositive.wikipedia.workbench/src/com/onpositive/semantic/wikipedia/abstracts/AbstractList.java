package com.onpositive.semantic.wikipedia.abstracts;

public abstract class AbstractList extends CompositeTextElement {

	protected int listLevel;

	public AbstractList(TextAbstractElement parent) {
		super(parent);
	}
	public abstract char sign();

	@Override
	protected TextAbstractElement addLine(String textLine) {
		int level = -1;
		for (int a = 0; a < textLine.length(); a++) {
			char c = textLine.charAt(a);
			char sign = sign();
			if (c == sign||c=='#'||c==':'||c==';') {
				level++;
			} else {
				if (level != -1) {
					if (true) {
						if (level == listLevel) {
							ListLine e = new ListLine(
									textLine.substring(a), this);
							elements.add(e);
							return this;
						}
						if (level > this.listLevel) {
							
							AbstractList e = createList(level);
							ListLine e1 = new ListLine(
									textLine.substring(a), this);
							e.addElement(e1);
							elements.add(e);
							return this;
						} else {
							return null;
						}
						// it is list element
					}
				} else {
					return null;
				}
			}
		}
		if (level>=0){
			return this;
		}
		return null;
	}
	protected abstract AbstractList createList(int level) ;
}