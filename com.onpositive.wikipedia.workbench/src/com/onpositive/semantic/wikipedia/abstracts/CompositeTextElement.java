package com.onpositive.semantic.wikipedia.abstracts;

import java.util.ArrayList;

public abstract class CompositeTextElement extends TextAbstractElement {

	ArrayList<TextAbstractElement> elements = new ArrayList<TextAbstractElement>();
	TextAbstractElement parent;

	
	@Override
	public void setParent(CompositeTextElement compositeTextElement) {
		this.parent=compositeTextElement;
	}
	public CompositeTextElement(TextAbstractElement parent) {
		super();
		this.parent = parent;
	}

	public void reparent(CompositeTextElement tm) {
		this.parent = tm;
	}

	@Override
	public void accept(TextElementVisitor visitor) {
		super.accept(visitor);
		for (TextAbstractElement q : elements
				.toArray(new TextAbstractElement[elements.size()])) {
			q.accept(visitor);
		}
		if (visitor instanceof NotifyOnEndVisit){
			NotifyOnEndVisit n=(NotifyOnEndVisit) visitor;
			n.endVisit(this);
		}
	}

	public void replace(TextAbstractElement imageExtractor,
			TextAbstractElement imageElement) {
		int indexOf = elements.indexOf(imageExtractor);
		if (indexOf != -1) {
			elements.set(indexOf, imageElement);
		}
		/*else{
			throw new IllegalStateException();
		}*/
	}

	@Override
	protected TextAbstractElement getParent() {
		return parent;
	}

	@Override
	protected TextAbstractElement[] getChildren() {
		return elements.toArray(new TextAbstractElement[elements.size()]);
	}

	protected void addElement(TextAbstractElement element) {
		this.elements.add(element);
		element.setParent(this);
	}

	@Override
	public void printElement(TextAbstractsPrinter printer) {
		for (TextAbstractElement q : elements) {
			if (!q.valid){
				continue;
			}
			if (q instanceof Section) {
				Section c = (Section) q;
				String readLine = c.header;
				if (readLine.contains("Ссылки")) {
					break;
				}
				if (readLine.contains("Издания")) {
					break;
				}
				if (readLine.contains("Источники")) {
					break;
				}
				if (readLine.contains("См. также")) {
					break;
				}
				if (readLine.contains("Литература")) {
					break;
				}
			}
			q.printElement(printer);
		}
	}

	public static int getLevel(String textLine, char ca) {
		int level = -1;
		int listLevel = -1;
		for (int a = 0; a < textLine.length(); a++) {
			char c = textLine.charAt(a);
			if (c == ca) {
				level++;
			} else {
				if (level != -1) {
					if (c == ' ') {
						listLevel = level;
						break;
					} else {
						break;
					}
				}
			}
		}
		return listLevel;
	}

	@Override
	protected TextAbstractElement addLine(String textLine) {

		char c = textLine.length() > 0 ? textLine.charAt(0) : 'f';
		if (c == '=') {
			int level = getLevel(textLine, '=');
			StringBuilder bld = new StringBuilder();
			for (int a = 0; a <= level; a++) {
				bld.append('=');
			}
			if (level > this.level()) {
				String header = textLine.substring(level + 1,
						textLine.length() - level - 1).trim();
				Section sec = new Section(header, level, this);
				addElement(sec);
				return sec;
			}
			return null;
		}
		if (c == '*') {
			int level = getLevel(textLine, '*');
			
			AbstractList ul = new UnnumberedList(this, level);
			ul.doAdd(textLine);
			addElement(ul);
			return ul;
		}
		if (c == '#') {
			int level = getLevel(textLine, '#');
			AbstractList ul = new NumberedList(this, level);
			ul.doAdd(textLine);
			addElement(ul);
			return ul;
		}
		if (textLine.isEmpty()) {
			return this;
		}
		OnelineTextElement onelineTextElement = new OnelineTextElement(
				textLine, this);
		addElement(onelineTextElement);
		return this;
	}

	public int level() {
		return -1;
	}

	public void addAfter(TextAbstractElement imageElement,
			OnelineTextElement onelineTextElement) {
		int indexOf = elements.indexOf(imageElement);
		if (indexOf != -1) {
			elements.add(indexOf + 1, onelineTextElement);
		}
	}

	public void remove(TextAbstractElement element) {
		elements.remove(element);
	}

}
