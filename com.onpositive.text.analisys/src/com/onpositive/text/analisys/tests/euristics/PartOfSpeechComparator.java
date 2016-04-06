package com.onpositive.text.analisys.tests.euristics;

import com.onpositive.semantic.wordnet.Grammem;
import com.onpositive.semantic.wordnet.Grammem.PartOfSpeech;

public class PartOfSpeechComparator extends DefaultComparator {

	@Override
	protected boolean isWrong(SimplifiedToken etalonToken, Grammem grammem) {
		return grammem instanceof PartOfSpeech && super.isWrong(etalonToken, grammem);
	}

}
