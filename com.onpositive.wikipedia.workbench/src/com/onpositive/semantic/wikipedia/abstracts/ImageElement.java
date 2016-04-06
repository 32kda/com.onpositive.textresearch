package com.onpositive.semantic.wikipedia.abstracts;

public class ImageElement extends OnelineTextElement{

	
	public ImageElement(String text, CompositeTextElement parent) {
		super(text, parent);
	}

	@Override
	public void printElement(TextAbstractsPrinter printer) {
		printer.println();
	}
}
