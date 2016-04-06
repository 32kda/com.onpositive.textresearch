package com.onpositive.semantic.wordnet;

import java.util.HashSet;


public class GrammarRelation extends AbstractRelation<TextElement> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final int UNKNOWN_GRAMMAR_FORM = 99;

	
	public GrammarRelation(AbstractWordNet owner2, int word, int relation2) {
		super(owner2, word, relation2);
	}
	public GrammarRelation(AbstractWordNet owner2, TextElement word, int relation2) {
		super(owner2, word, relation2);
	}

	public TextElement getWord() {
		TextElement result = owner.getWordElement(conceptId);
		return (TextElement) result;
	}

	@Override
	public String toString() {
		return getWord().toString()+" - ("+getGrammems()+")";
	}
	public boolean hasGrammem(Grammem g){
		HashSet<Grammem> grammemSet = getGrammems();
		return grammemSet.contains(g);
	}
	public boolean hasAllGrammems(Grammem...options) {
		HashSet<Grammem> grammemSet = getGrammems();
		for (Grammem q:options){
			if (!grammemSet.contains(q)){
				return false;
			}
		}
		return true;
	}
	public HashSet<Grammem>getGrammems(){
		HashSet<Grammem> grammemSet = owner.getGrammemSet(relation);
		return grammemSet;
	}
	
	public boolean hasArLeastOneOfGrammems(Grammem...options) {
		HashSet<Grammem> grammemSet = getGrammems();
		for (Grammem q:options){
			if (grammemSet.contains(q)){
				return true;
			}
		}
		return false;
	}
}