package com.onpositive.semantic.wikipedia.abstracts;

public class RootElement extends CompositeTextElement implements ISimpleContentElement{

	public RootElement() {
		super(null);
	}

	protected TextAbstractElement currentEnd;

	@Override
	protected void setupLast(TextAbstractElement addLine) {
		currentEnd=addLine;
		super.setupLast(addLine);
	}

	@Override
	protected TextAbstractElement addLine(String textLine) {
		if (currentEnd!=null){
			currentEnd.doAdd(textLine);
			return this;
		}
		TextAbstractElement addLine = super.addLine(textLine);
		if (addLine instanceof CompositeTextElement){
			if (addLine!=this){
			this.currentEnd=addLine;
			}
		}
		return addLine;
	}
	
	public void doAdd(String textLine) {
		TextAbstractElement addLine = super.addLine(textLine);
		if (addLine instanceof CompositeTextElement){
			if (addLine!=this){
			this.currentEnd=addLine;
			}
		}
	}
}
