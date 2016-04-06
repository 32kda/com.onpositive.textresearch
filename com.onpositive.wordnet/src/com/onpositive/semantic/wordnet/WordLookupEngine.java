package com.onpositive.semantic.wordnet;

import com.onpositive.semantic.words3.LayersPack;

public abstract class WordLookupEngine {

	
	public abstract LayersPack getMetaLayers();
	
	public abstract GrammarRelation[] getPossibleGrammarForms(String wordForm);
	
	public abstract TextElement getWordElement(String basicForm);
	
	public abstract TextElement[] getPossibleContinuations(TextElement startOfSequence);

	public abstract boolean hasContinuations(TextElement te);

}
