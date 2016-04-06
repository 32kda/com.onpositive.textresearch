package com.onpositive.semantic.wordnet.composite;

import com.onpositive.semantic.wordnet.AbstractRelation;
import com.onpositive.semantic.wordnet.AbstractWordNet;
import com.onpositive.semantic.wordnet.MeaningElement;
import com.onpositive.semantic.wordnet.MorphologicalRelation;
import com.onpositive.semantic.wordnet.SemanticRelation;
import com.onpositive.semantic.wordnet.TextElement;

public class CompositeMeaning extends MeaningElement{

	private TextElement parent;
	 MeaningElement original;
	private int id;

	public CompositeMeaning(CompositeWordnet owner,TextElement parent,MeaningElement original,int id) {
		super(owner);
		this.parent=parent;
		this.original=original;
		this.id=id;
	}

	@Override
	public TextElement getParentTextElement() {
		return parent;
	}

	@Override
	public AbstractRelation<MeaningElement>[] getAllRelations() {
		AbstractRelation<MeaningElement>[] allRelations = original.getAllRelations();
		for(int a=0;a<allRelations.length;a++){
			 allRelations[a]=patch(allRelations[a]);
		}
		return allRelations;
	}

	private AbstractRelation<MeaningElement> patch(AbstractRelation<MeaningElement> r) {
		int neId=((CompositeWordnet)owner).remapMeaning(r.getWord());
		if (r instanceof SemanticRelation){
			return new SemanticRelation(owner, neId, r.relation);
		}
		if (r instanceof MorphologicalRelation){
			return new MorphologicalRelation(owner, neId, r.relation);
		}
		throw new IllegalArgumentException();
	}

	@Override
	public short getGrammemCode() {
		return original.getGrammemCode();
	}

	@Override
	public int id() {
		return id;
	}

}
