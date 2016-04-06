package com.onpositive.semantic.words3.prediction;

import com.onpositive.semantic.wordnet.GrammarRelation;

public interface IPredictionHelper {

	public abstract GrammarRelation[] getForms(String word);

}