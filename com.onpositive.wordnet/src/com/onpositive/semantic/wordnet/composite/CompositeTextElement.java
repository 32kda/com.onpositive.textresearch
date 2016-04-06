package com.onpositive.semantic.wordnet.composite;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.onpositive.semantic.wordnet.AbstractWordNet;
import com.onpositive.semantic.wordnet.MeaningElement;
import com.onpositive.semantic.wordnet.TextElement;

public class CompositeTextElement extends TextElement {

	TextElement original;
	TextElement extra;
	private int id;

	public CompositeTextElement(AbstractWordNet owner, int id, TextElement t1,
			TextElement t2) {
		super(owner);
		this.original = t1;
		this.extra = t2;
		this.id = id;
	}

	@Override
	public int id() {
		return id;
	}

	@Override
	public String getBasicForm() {
		return original!=null?original.getBasicForm():extra.getBasicForm();
	}

	@Override
	public MeaningElement[] getConcepts() {
		MeaningElement[] concepts = original!=null?original.getConcepts():null;
		MeaningElement[] extras = extra!=null?extra.getConcepts():null;
		ArrayList<MeaningElement> el = new ArrayList<MeaningElement>();
		el.addAll(convertMeanings(concepts));
		el.addAll(convertMeanings(extras));
		return el.toArray(new MeaningElement[el.size()]);
	}

	public List<MeaningElement> convertMeanings(MeaningElement[] concepts) {
		ArrayList<MeaningElement> mm = new ArrayList<MeaningElement>();
		if (concepts != null) {
			for (MeaningElement q : concepts) {
				mm.add(((CompositeWordnet) owner).convertMeaning(q));
			}
		}
		return mm;
	}

	@Override
	public boolean isMultiWord() {
		if (original!=null){
		return original.isMultiWord();
		}
		return extra.isMultiWord();
	}

	@Override
	public TextElement[] getParts() {
		TextElement[] parts = original!=null?original.getParts():extra.getParts();
		TextElement[] newElements = new TextElement[parts.length];
		for (int a = 0; a < parts.length; a++) {
			newElements[a] = convert(parts[a]);
		}
		return newElements;
	}

	private TextElement convert(TextElement textElement) {
		return owner.getWordElement(textElement.getBasicForm());
	}

}
