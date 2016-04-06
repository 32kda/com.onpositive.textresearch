package com.onpositive.text.morphology.pairs;

import com.onpositive.semantic.wordnet.Grammem.PartOfSpeech;

public class StablePair {
	public final String first;
	public final String second;
	
	public final PartOfSpeech firstPart;
	public final PartOfSpeech secondPart;
	
	public StablePair(String first, String second, PartOfSpeech firstPart, PartOfSpeech secondPart) {
		super();
		this.first = first;
		this.second = second;
		this.firstPart = firstPart;
		this.secondPart = secondPart;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((first == null) ? 0 : first.hashCode());
		result = prime * result
				+ ((second == null) ? 0 : second.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StablePair other = (StablePair) obj;
		if (first == null) {
			if (other.first != null)
				return false;
		} else if (!first.equals(other.first))
			return false;
		if (second == null) {
			if (other.second != null)
				return false;
		} else if (!second.equals(other.second))
			return false;
		return true;
	}
	
	public static String getKey(String prevWord, String form) {
		return prevWord + "|" + form;
	}

	
}