package com.onpositive.semantic.words3.dto;


public class SequenceInfo extends SenseElementInfo{
	
	public final int[] words;
	public final String basicForm;
	
	public SequenceInfo(int wordId2,String basicForm, ConceptInfo[] wordSenseInfo,int[]words) {
		super(wordId2, wordSenseInfo);
		this.words=words;
		this.basicForm=basicForm;
	}
}
