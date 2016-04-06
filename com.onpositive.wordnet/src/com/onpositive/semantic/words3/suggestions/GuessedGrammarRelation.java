package com.onpositive.semantic.words3.suggestions;

import com.onpositive.semantic.wordnet.AbstractWordNet;
import com.onpositive.semantic.wordnet.GrammarRelation;
import com.onpositive.semantic.wordnet.TextElement;

public class GuessedGrammarRelation extends GrammarRelation{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private GuessedTextElement element;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((element == null) ? 0 : element.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		GuessedGrammarRelation other = (GuessedGrammarRelation) obj;
		if (element == null) {
			if (other.element != null)
				return false;
		} else if (!element.equals(other.element))
			return false;
		if (this.relation!=other.relation){
			return false;
		}
		return true;
	}

	public GuessedGrammarRelation(AbstractWordNet owner2, int word,
			int relation2,GuessedTextElement textElement) {
		super(owner2, -word, relation2);
		this.element=textElement;
	}
	
	@Override
	public TextElement getWord() {
		return element;
	}
	@Override
	public String toString() {
		return super.toString();
	}

}