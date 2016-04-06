package com.onpositive.text.morphology.neural;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import com.onpositive.semantic.wordnet.Grammem.PartOfSpeech;
import com.onpositive.text.morphology.dataset.prepare.DataSetTestSample;

public class SinglePartPOSSelector extends AbstractPOSSelector {
	
	protected PartOfSpeech partOfSpeech;
	
	public SinglePartPOSSelector(PartOfSpeech partOfSpeech) {
		this.partOfSpeech = partOfSpeech;
	}

	@Override
	public Stream<DataSetTestSample> selectSamples(List<DataSetTestSample> list) {
		return list.stream().filter(x -> x.isFullSet() && x.getOptions().contains(partOfSpeech));
	}
	
	@Override
	public boolean match(Set<PartOfSpeech>set) {
		if (set.contains(partOfSpeech)){
			return true;
		}
		return false;
	}

}
