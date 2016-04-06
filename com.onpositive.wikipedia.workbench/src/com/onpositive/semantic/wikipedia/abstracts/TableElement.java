package com.onpositive.semantic.wikipedia.abstracts;

public class TableElement extends CompositeTextElement{

	public TableElement(TextAbstractElement parent) {
		super(parent);
		this.valid=false;
	}
	
	@Override
	public void printElement(TextAbstractsPrinter printer) {
		
	}
}
