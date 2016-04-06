package com.onpositive.text.analysis.lexic.disambig;

public class DisambiguatorProvider {

	public static ILexicLevelDisambiguator getInstance(){
		return new NamedStuffDisambiguator();		
	}
}
