package com.onpositive.semantic.words2;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import com.onpositive.semantic.wordnet.AbstractWordNet;
import com.onpositive.semantic.wordnet.Grammem;
import com.onpositive.semantic.wordnet.Grammem.PartOfSpeech;
import com.onpositive.semantic.wordnet.Grammem.SemanGramem;
import com.onpositive.semantic.wordnet.MeaningElement;
import com.onpositive.semantic.wordnet.TextElement;

public class Word extends TextElement implements Serializable {

	private static final long serialVersionUID = 1L;
	
	
	@Override
	public String toString() {
		return basicForm;
	}
	
	/**
	 * 
	 */
	//private static final WordRelation[] NO_RELATIONS = new WordRelation[0];
	protected int id;
	
	protected ArrayList<WordMeaning>meanings=new ArrayList<WordMeaning>();
	
	protected final String basicForm;
	
	public Word(String basicForm,int id,AbstractWordNet owner) {
		super(owner);
		this.basicForm = basicForm;		
		this.id=id;		
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((basicForm == null) ? 0 : basicForm.hashCode());
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
		Word other = (Word) obj;
		if (basicForm == null) {
			if (other.basicForm != null)
				return false;
		} else if (!basicForm.equals(other.basicForm))
			return false;
		return true;
	}

	@Override
	public String getBasicForm() {
		return basicForm;
	}

	boolean hasKind;
	
	static int minimalNumber=-1;
	
	
	
	@Override
	public int id() {
		return id;
	}

	@Override
	public MeaningElement[] getConcepts() {
		return meanings.toArray(new WordMeaning[meanings.size()]);
	}

	

	@Override
	public boolean isMultiWord() {
		return false;
	}

	@Override
	public TextElement[] getParts() {
		return new TextElement[]{this};
	}

	public boolean mayBeUsedAsNoun() {
		MeaningElement[] concepts = getConcepts();
		for (MeaningElement e:concepts){
			if (e.getGrammems().contains(Grammem.PartOfSpeech.NOUN)){
				return true;
			}
		}
		return false;
	}

	
	public void setGrammemCode(int relationCode) {
		if (relationCode>Short.MAX_VALUE){
			throw new IllegalStateException();
		}
		MeaningElement[] concepts = getConcepts();
		WordMeaning e=(WordMeaning) concepts[concepts.length-1];
		HashSet<Grammem> grammemSet = owner.getGrammemSet(relationCode);
		HashSet<Grammem> grammemSet1 = owner.getGrammemSet(e.kind);
		if (sameKindOfSpeech(grammemSet,grammemSet1)){
		e.kind=(short) relationCode;
		}
		else{
				//no we should allocate new concept
				WordMeaning mm=new WordMeaning(minimalNumber, owner, this);
				minimalNumber--;
				meanings.add(mm);
				concepts=getConcepts();
			hasKind=true;
			((WordMeaning)concepts[concepts.length-1]).kind=(short)relationCode;
			tempSet=null;
		}
		}
		
	

	private boolean sameKindOfSpeech(HashSet<Grammem> grammemSet,
			HashSet<Grammem> grammemSet1) {
		PartOfSpeech p=null;
		for (Grammem q:grammemSet){
			if (q instanceof PartOfSpeech){
				p=(PartOfSpeech) q;
			}
		}
		for (Grammem q:grammemSet1){
			if (q instanceof PartOfSpeech){
				if (p!=null&&p!=q){
					return false;
				}
			}
		}
		return true;
	}

	public void setFeature(SemanGramem toponim) {
		if (tempSet==null){
			tempSet=new LinkedHashSet<Grammem>();
		}
		tempSet.add(toponim);
	}
	
	protected LinkedHashSet<Grammem>tempSet=null;
	
	public void setPartOfSpeech(PartOfSpeech noun) {
		if (tempSet==null){
			tempSet=new LinkedHashSet<Grammem>();
		}
		else{
			commitTempSet();
			tempSet=new LinkedHashSet<Grammem>();
		}
		tempSet.add(noun);		
	}

	public void commitTempSet() {
		if (tempSet==null){
			return; 
		}
		if (hasKind&&!canReuse()){	
			//no we should allocate new concept
			WordMeaning mm=new WordMeaning(minimalNumber, owner, this);
			minimalNumber--;
			meanings.add(mm);
		}
		hasKind=true;
		MeaningElement[] concepts = getConcepts();
		((WordMeaning)concepts[concepts.length-1]).kind=(short) ((SimpleWordNet)owner).getGrammemCode(tempSet);
		tempSet=null;
		//here  we should think about what we have
	}

	private boolean canReuse() {
		
		MeaningElement[] concepts = getConcepts();
		int knd=((WordMeaning)concepts[concepts.length-1]).kind;
		Set<Grammem>ms=owner.getGrammemSet(knd);
		for (Grammem g:tempSet){
			if (g instanceof PartOfSpeech){
				if (ms.contains(g)){
					return true;
				}
			}
		}
		return false;
	}

	public AbstractRelationTarget getCurrentRelationTarget() {
		MeaningElement[] concepts = getConcepts();
		return ((WordMeaning)concepts[concepts.length-1]);
	}

}