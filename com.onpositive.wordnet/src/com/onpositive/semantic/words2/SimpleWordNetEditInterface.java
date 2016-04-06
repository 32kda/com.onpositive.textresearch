package com.onpositive.semantic.words2;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;

import com.onpositive.semantic.wordnet.AbstractWordNet;
import com.onpositive.semantic.wordnet.GrammarRelation;
import com.onpositive.semantic.wordnet.Grammem;
import com.onpositive.semantic.wordnet.MeaningElement;
import com.onpositive.semantic.wordnet.MorphologicalRelation;
import com.onpositive.semantic.wordnet.SemanticRelation;
import com.onpositive.semantic.wordnet.TextElement;
import com.onpositive.semantic.wordnet.edit.IWordNetEditInterface;
import com.onpositive.semantic.words3.ReadOnlyMapWordNet;

public class SimpleWordNetEditInterface implements IWordNetEditInterface {

	SimpleWordNet original;
	File toStore;

	public SimpleWordNetEditInterface(SimpleWordNet original,File toStore) {
		super();
		this.original = original;
		this.toStore=toStore;
	}

	@Override
	public TextElement registerWord(String word) {
		Word orCreateWord = original.getOrCreateWord(word.toLowerCase());
		if (orCreateWord.getBasicForm().indexOf(' ')!=-1){
			original.toSequenceIfNeeded(orCreateWord);
		}
		return orCreateWord;
	}

	@Override
	public void removeWord(String word) {
		throw new IllegalStateException();
	}

	@Override
	public AbstractWordNet getWordNet() {
		return original;
	}

	@Override
	public void addSemanticRelation(MeaningElement from, SemanticRelation tt) {
		AbstractRelationTarget t = (AbstractRelationTarget) from;
		t.registerRelation(tt.relation, tt.conceptId);
	}

	@Override
	public void removeSemanticRelation(MeaningElement from, SemanticRelation tt) {
		AbstractRelationTarget t =(AbstractRelationTarget) from;
		t.unregisterRelation(tt.relation, tt.conceptId);
	}

	@Override
	public void addMorphologicalRelation(MeaningElement to,
			MorphologicalRelation tt) {
		AbstractRelationTarget t = (AbstractRelationTarget)to;
		t.registerRelation(tt.relation, tt.conceptId);
	}

	@Override
	public void removeMorphologicalRelation(MeaningElement to,
			MorphologicalRelation tt) {
		AbstractRelationTarget t = (AbstractRelationTarget) to;
		t.unregisterRelation(tt.relation, tt.conceptId);
	}

	@Override
	public void removeGrammarRelation(String from, TextElement to, LinkedHashSet<Grammem> codes) {
		ArrayList<GrammarRelation> arrayList = original.wordforms.get(from);
		int code = original.getGrammemCode(codes);
		if (arrayList != null) {
			for (GrammarRelation a : arrayList) {
				if (a.conceptId == to.id() && a.relation == code) {
					arrayList.remove(a);
					break;
				}
			}
			if (arrayList.isEmpty()) {
				original.wordforms.remove(from);
			}
		}
	}

	@Override
	public void addGrammarRelation(String from, TextElement to,LinkedHashSet<Grammem> codes) {
		int code = original.getGrammemCode(codes);
		ArrayList<GrammarRelation> arrayList = original.wordforms.get(from);
		if (arrayList != null) {
			for (GrammarRelation a : arrayList) {
				if (a.relation == code && a.conceptId == to.id()) {
					return;
				}
			}
			
			
		}
		if (arrayList==null){
			arrayList=new ArrayList<GrammarRelation>();
			original.wordforms.put(from, arrayList);
		}
		arrayList.add(new GrammarRelation(original, to.id(), code));
	}

	@Override
	public void store() throws FileNotFoundException, IOException {
		store(toStore);
	}

	@Override
	public void store(File f) throws FileNotFoundException, IOException {
		new ReadOnlyMapWordNet(original).store(f);
	}

	@Override
	public void store(DataOutputStream s) throws IOException {
		new ReadOnlyMapWordNet(original).store(s);
	}
}