package com.onpositive.semantic.wikipedia.abstracts;

import java.io.StringWriter;

public abstract class TextAbstractElement {

	protected abstract TextAbstractElement[] getChildren();

	public abstract void printElement(TextAbstractsPrinter printer);

	protected abstract TextAbstractElement addLine(String textLine);

	protected abstract TextAbstractElement getParent();

	protected boolean valid = true;

	public void accept(TextElementVisitor visitor) {
		visitor.visit(this);
	}

	public abstract void reparent(CompositeTextElement tm);

	static int counter = 0;

	public void doAdd(String textLine) {
		counter++;
		try {
			/*
			 * if (counter>10){ System.out.println("A"); }
			 */
			TextAbstractElement addLine = addLine(textLine);
			if (addLine != null) {
				if (addLine != this) {
					setupLast(addLine);
				}
			} else {
				getParent().doAdd(textLine);
			}
		} finally {
			counter--;
		}
	}

	protected void setupLast(TextAbstractElement addLine) {
		if (getParent() != null) {
			getParent().setupLast(addLine);
		}
	}

	@Override
	public String toString() {
		StringWriter out = new StringWriter();
		TextAbstractsPrinter printer = new TextAbstractsPrinter(out);
		printElement(printer);
		printer.close();
		return out.toString();
	}

	public abstract void setParent(CompositeTextElement compositeTextElement);

}
