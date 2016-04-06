package com.onpositive.wordnet.tests;

import com.onpositive.semantic.wordnet.GrammarRelation;
import com.onpositive.semantic.words3.ReadOnlyTrieWordNet;
import com.onpositive.semantic.words3.TrieZippedProvider;

import junit.framework.TestCase;

public class ZippedTrieTest extends TestCase{

	/**
	 * Tests fix for <a>https://github.com/OnPositive/wordnet/issues/18</a>
	 */
	public void test01() {
		ReadOnlyTrieWordNet instance = TrieZippedProvider.getInstance();
		GrammarRelation[] possibleGrammarForms = instance.getPossibleGrammarForms("сурьёзнейшая");
		assertNull(possibleGrammarForms);
		possibleGrammarForms = instance.getPossibleGrammarForms("сурьезнейшая");
		assertNull(possibleGrammarForms);
		possibleGrammarForms = instance.getPossibleGrammarForms("пеaревалившемся");
		assertNull(possibleGrammarForms);
	}
	
}
