package com.onpositive.text.analisys.tests;


//import org.junit.Test;

import com.onpositive.semantic.wordnet.GrammarRelation;
import com.onpositive.semantic.wordnet.WordNetProvider;

import junit.framework.TestCase;

public class SequenceSeparatorTests extends TestCase {

//	@Test
	public void test() {
		GrammarRelation[] posibleWords = WordNetProvider.getInstance().getPossibleGrammarForms("политический деятель");
		System.out.println(posibleWords);
	}
}
