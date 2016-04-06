package com.onpositive.text.analysis.filtering;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.onpositive.semantic.wordnet.Grammem.PartOfSpeech;

public class CompositeSelector implements IPartOfSpeechSelector {
	
	protected List<IPartOfSpeechSelector> selectors = new ArrayList<IPartOfSpeechSelector>();
	
	public CompositeSelector(IPartOfSpeechSelector... newSelectors) {
		for (IPartOfSpeechSelector selector : newSelectors) {
			selectors.add(selector);
		}
	}
	
	public CompositeSelector() {
	}
	
	public void addSelector(IPartOfSpeechSelector selector) {
		selectors.add(selector);
	}

	@Override
	public Collection<PartOfSpeech> select(String token,
			Collection<PartOfSpeech> originalParts) {
		Collection<PartOfSpeech> result = originalParts;
		for (IPartOfSpeechSelector selector : selectors) {
			result = selector.select(token, result);
		}
		return result;
	}

}
