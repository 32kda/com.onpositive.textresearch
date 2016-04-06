package com.onpositive.semantic.words3.hds;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CollectingVisitor<T> implements IStringTrieVisitor<T> {
	
	private List<String> words = new ArrayList<String>();
	private Map<String, T> wordsToData= new HashMap<String, T>();

	@Override
	public void visit(String word, T data) {
		words.add(word);
		wordsToData.put(word, data);
	}
	
	public List<String> getWords() {
		return words;
	}
	
	public T getData(String word) {
		return wordsToData.get(word);
	}
	
	@Override
	public String toString() {
		return words.toString();
	}

}
