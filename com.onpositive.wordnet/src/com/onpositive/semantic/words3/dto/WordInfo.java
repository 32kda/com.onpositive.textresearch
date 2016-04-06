package com.onpositive.semantic.words3.dto;

import java.util.Arrays;

public class WordInfo extends SenseElementInfo {

	protected final String basicWord;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(senses);
		result = prime * result + elementId;
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
		WordInfo other = (WordInfo) obj;
		if (!Arrays.equals(senses, other.senses))
			return false;
		if (elementId != other.elementId)
			return false;
		return true;
	}

	public WordInfo(int wordId,String word, ConceptInfo[] wordSenseInfo) {
		super(wordId,wordSenseInfo);
		this.basicWord=word;
	}

	public String getBasicForm() {
		return basicWord;
	}
}
