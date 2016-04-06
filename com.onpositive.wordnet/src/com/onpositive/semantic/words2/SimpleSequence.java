package com.onpositive.semantic.words2;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

import com.onpositive.semantic.wordnet.AbstractWordNet;
import com.onpositive.semantic.wordnet.MeaningElement;
import com.onpositive.semantic.wordnet.TextElement;

public class SimpleSequence extends TextElement implements
		Serializable {

	protected Word[] words;
	protected String form;

	public SimpleSequence(Word[] words, int id,String form,AbstractWordNet owner) {
		super(owner);
		this.form=form;
		this.words = words;
		this.id = id;
	}
	protected ArrayList<WordMeaning>meanings=new ArrayList<WordMeaning>();
	
	@Override
	public MeaningElement[] getConcepts() {
		return meanings.toArray(new WordMeaning[meanings.size()]);
	}

	protected int id;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	public Word[] getWords() {
		return words;
	}

	@Override
	public int id() {
		return id;
	}
	public String getBasicForm(){
		return form;
	}

	@Override
	public String toString() {
		return Arrays.toString(words);
	}
	public Word get(int i) {
		return words[i];
	}

	public boolean match(ArrayList<Word> w, int a) {
		for (int i = a; i < a + words.length; i++) {
			if (i >= w.size()) {
				return false;
			}
			if (!words[i - a].equals(w.get(i))) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean isMultiWord() {
		return true;
	}

	@Override
	public TextElement[] getParts() {
		return words;
	}
}