package com.onpositive.semantic.wikipedia.abstracts;

import java.util.ArrayList;

public class TableExtractor extends TextElementVisitor {

	@Override
	public void visit(TextAbstractElement element) {
		if (element instanceof CompositeTextElement) {
			CompositeTextElement t = (CompositeTextElement) element;
			TextAbstractElement[] children = t.getChildren();
			ArrayList<TextAbstractElement> newChildren = new ArrayList<TextAbstractElement>();
			ArrayList<TextAbstractElement> tableMembers = new ArrayList<TextAbstractElement>();
			boolean inTable = false;
			for (TextAbstractElement r : children) {
				if (r instanceof OnelineTextElement) {
					OnelineTextElement m = (OnelineTextElement) r;
					if (m.text.startsWith("{|")) {
						tableMembers.clear();
						inTable=true;
						continue;
					}
					if (m.text.startsWith("|}")) {
						inTable=false;
						TableElement tm=new TableElement(element);
						
						for (TextAbstractElement q:tableMembers){
							q.reparent(tm);
							tm.addElement(q);
						}
						tableMembers.clear();
						newChildren.add(tm);
						continue;
					}
				}
				if (inTable) {
					tableMembers.add(r);
				}
				else{
				newChildren.add(r);
				}
			}
			((CompositeTextElement) element).elements=newChildren;
		}
	}

}
