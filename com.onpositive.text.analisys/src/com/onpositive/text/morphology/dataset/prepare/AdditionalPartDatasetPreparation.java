package com.onpositive.text.morphology.dataset.prepare;

import java.util.Set;

import com.onpositive.semantic.wordnet.Grammem.PartOfSpeech;

public class AdditionalPartDatasetPreparation extends DataSetPreparation {

	@Override
	protected boolean select(Set<PartOfSpeech> ps) {
		if (ps.contains(PartOfSpeech.PREP) || ps.contains(PartOfSpeech.CONJ)){
			return true;
		}
		return false;
	}


}
