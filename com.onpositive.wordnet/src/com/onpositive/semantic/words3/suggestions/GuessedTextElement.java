package com.onpositive.semantic.words3.suggestions;

import com.onpositive.semantic.wordnet.AbstractWordNet;
import com.onpositive.semantic.wordnet.MeaningElement;
import com.onpositive.semantic.wordnet.TextElement;

public class GuessedTextElement extends TextElement{

	protected String text;
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((text == null) ? 0 : text.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GuessedTextElement other = (GuessedTextElement) obj;
		if (text == null) {
			if (other.text != null)
				return false;
		} else if (!text.equals(other.text))
			return false;
		return true;
	}

	public GuessedTextElement(AbstractWordNet owner,String text) {
		super(owner);
		this.text=text;
	}

	@Override
	public int id() {
		return -1;
	}

	@Override
	public String getBasicForm() {
		return text;
	}

	@Override
	public MeaningElement[] getConcepts() {
		return new MeaningElement[0];
	}

	@Override
	public boolean isMultiWord() {
		return false;
	}
	@Override
	public String toString() {
		return text+"*";
	}
	
	@Override
	public TextElement[] getParts() {
		return new TextElement[]{this};
	}
}
