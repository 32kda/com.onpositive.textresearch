package com.onpositive.semantic.words2;

import java.io.Serializable;
import java.util.Set;

import com.onpositive.semantic.wordnet.AbstractWordNet;
import com.onpositive.semantic.wordnet.Grammem;
import com.onpositive.semantic.wordnet.TextElement;

public class WordMeaning extends AbstractRelationTarget implements Serializable{

	private static final long serialVersionUID = 1L;

	protected TextElement parentWord;
	protected short kind;
	
	public WordMeaning(int id,AbstractWordNet owner,TextElement ps){
		super(owner,id);
		this.parentWord=ps;
	}
	
	@Override
	public TextElement getParentTextElement() {
		return parentWord;
	}

	@Override
	public Set<Grammem> getGrammems() {
		return owner.getGrammemSet(kind);
	}

	@Override
	public short getGrammemCode() {
		return kind;
	}
}
