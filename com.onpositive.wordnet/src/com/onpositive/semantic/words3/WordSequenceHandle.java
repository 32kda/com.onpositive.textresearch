package com.onpositive.semantic.words3;

import com.onpositive.semantic.wordnet.TextElement;
import com.onpositive.semantic.words3.ReadOnlyWordNet.WordStore;
import com.onpositive.semantic.words3.dto.SequenceInfo;

public class WordSequenceHandle extends SenseElementHandle{

	public WordSequenceHandle(int address, WordStore store) {
		super(address, store);
	}

	public WordHandle[] getWords(){
		SequenceInfo info = (SequenceInfo) store.getInfo(id());
		int[] words = info.words;
		WordHandle[] hh=new WordHandle[words.length];
		for (int a=0;a<words.length;a++){
			TextElement wordElement = store.getWordNet().getWordElement(words[a]);
			hh[a]=(WordHandle) wordElement;
		}
		return hh;		
	}

	@Override
	public boolean isMultiWord() {
		return true;
	}
	
	@Override
	public TextElement[] getParts() {
		return getWords();
	}
}
