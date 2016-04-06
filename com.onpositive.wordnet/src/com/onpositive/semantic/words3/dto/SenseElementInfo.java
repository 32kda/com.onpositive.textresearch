package com.onpositive.semantic.words3.dto;


public class SenseElementInfo {

	public final int elementId;
	public final ConceptInfo[] senses;

	public SenseElementInfo(int wordId2, ConceptInfo[] wordSenseInfo) {
		super();
		this.elementId=wordId2;
		this.senses=wordSenseInfo;
		for (ConceptInfo i:wordSenseInfo){
			i.parentTextElement=elementId;
		}
	}
	
	
}