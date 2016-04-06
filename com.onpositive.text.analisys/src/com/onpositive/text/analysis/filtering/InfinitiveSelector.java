package com.onpositive.text.analysis.filtering;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.onpositive.semantic.wordnet.Grammem.PartOfSpeech;

public class InfinitiveSelector implements IPartOfSpeechSelector{

	@Override
	public Collection<PartOfSpeech> select(String token,
			Collection<PartOfSpeech> originalParts) {
		if (originalParts.contains(PartOfSpeech.INFN) && !ignore(token) && originalParts.contains(PartOfSpeech.VERB)) {
			List<PartOfSpeech> result = new ArrayList<PartOfSpeech>(originalParts.size());
			result.remove(PartOfSpeech.INFN);
			return result;
		}
		return originalParts;
	}

	private boolean ignore(String token) {
		return "есть".equals(token);
	}

}
