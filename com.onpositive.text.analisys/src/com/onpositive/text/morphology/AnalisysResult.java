package com.onpositive.text.morphology;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import com.onpositive.semantic.wordnet.Grammem.PartOfSpeech;

public class AnalisysResult{
	public String content;
	public Set<PartOfSpeech> chooseFrom;
	public ArrayList<Result>results=new ArrayList<>();
	public ArrayList<Result> abandonedResults;
	
	public String toString(){
		return content+results+chooseFrom;
	}

	public static AnalisysResult createConstant(String toEstimate,
			PartOfSpeech part) {
		AnalisysResult result = new AnalisysResult();
		result.content = toEstimate;
		result.results.add(new Result(part, 1.0));
		return result;
	}
	
	public static AnalisysResult createEmpty(String toEstimate) {
		AnalisysResult result = new AnalisysResult();
		result.content = toEstimate;
		return result;
	}
	
	public Result getBestResult() {
		Optional<Result> result = results.stream().max((r1,r2) -> (int)Math.signum(r2.score - r1.score));
		if (!result.isPresent()) {
			if (chooseFrom.size() > 0) {
				for (PartOfSpeech partOfSpeech : chooseFrom) {
					if (partOfSpeech == PartOfSpeech.NOUN) {
						return new Result(partOfSpeech, 1);
					}
					if (partOfSpeech == PartOfSpeech.VERB) {
						return new Result(partOfSpeech, 1);
					}
					if (partOfSpeech == PartOfSpeech.ADJF) {
						return new Result(partOfSpeech, 1);
					}
					if (partOfSpeech == PartOfSpeech.INFN) {
						return new Result(partOfSpeech, 1);
					}
				}
				return new Result(chooseFrom.iterator().next(), 1);
			} else {
				return null;
			}
		}
		return result.get();
	}
}