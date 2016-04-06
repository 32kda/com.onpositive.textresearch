package com.onpositive.semantic.wordnet.edit;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedHashSet;

import com.onpositive.semantic.wordnet.AbstractWordNet;
import com.onpositive.semantic.wordnet.Grammem;
import com.onpositive.semantic.wordnet.MeaningElement;
import com.onpositive.semantic.wordnet.MorphologicalRelation;
import com.onpositive.semantic.wordnet.SemanticRelation;
import com.onpositive.semantic.wordnet.TextElement;

public interface IWordNetEditInterface {

	TextElement registerWord(String word);

	void removeWord(String word);
	
	AbstractWordNet getWordNet();

	void addSemanticRelation(MeaningElement from, SemanticRelation tt);

	void addMorphologicalRelation(MeaningElement to, MorphologicalRelation tt);
	
	void removeSemanticRelation(MeaningElement from, SemanticRelation tt); 
	
	void removeMorphologicalRelation(MeaningElement to, MorphologicalRelation tt);

	void removeGrammarRelation(String from, TextElement to, LinkedHashSet<Grammem> code);
	
	void addGrammarRelation(String from, TextElement to, LinkedHashSet<Grammem> code);
	
	void store() throws IOException;;
	
	void store(File f) throws FileNotFoundException, IOException;
	
	void store(DataOutputStream s) throws IOException;
}
