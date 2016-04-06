package com.onpositive.semantic.words2;

import com.onpositive.semantic.wordnet.AbstractWordNet;
import com.onpositive.semantic.wordnet.GrammarRelation;
import com.onpositive.semantic.wordnet.TextElement;

public abstract class WordNet extends AbstractWordNet implements
		Iterable<TextElement> {

	int fullyCorrectNouns;
	int fullyCorrectAdj;
	int fullyCorrectVerbs;

	int incorrectNouns;
	int incorrectAdj;
	int incorrectVerbs;
	public WordNet(){
		
	}

	public WordNet(AbstractWordNet net) {
		super(net);
	}

	public abstract TextElement getOrCreateRelationTarget(String s);
	
	protected abstract void registerWord(Word word);
	
	public abstract void registerWordForm(String wf,GrammarRelation form);
	
	public abstract Word getOrCreateWord(String lowerCase);
	
	public abstract void init();


	public abstract void markRedirect(String from, String to);
	
}
