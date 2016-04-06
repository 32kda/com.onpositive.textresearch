package com.onpositive.text.analysis.filtering;

import java.util.Collection;

import com.onpositive.semantic.wordnet.Grammem.PartOfSpeech;

public interface IPartOfSpeechSelector {

	public Collection<PartOfSpeech> select(String token, Collection<PartOfSpeech> originalParts);
}	
