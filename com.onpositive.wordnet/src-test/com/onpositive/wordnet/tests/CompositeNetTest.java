package com.onpositive.wordnet.tests;


import java.util.HashSet;

import com.onpositive.semantic.wordnet.GrammarRelation;
import com.onpositive.semantic.wordnet.Grammem;
import com.onpositive.semantic.wordnet.composite.CompositeWordnet;

import junit.framework.TestCase;

public class CompositeNetTest extends TestCase{

	public void test0(){
		CompositeWordnet wn=new CompositeWordnet();
//		wn.addUrl("numerics.xml");
		wn.add(getClass().getResourceAsStream("dimensions.xml"));
		GrammarRelation[] possibleGrammarForms = wn.getPossibleGrammarForms("км");
		HashSet<Grammem>all=new HashSet<Grammem>();
		for (GrammarRelation z:possibleGrammarForms){
			all.addAll(z.getGrammems());
			System.out.println(z);
		}
		TestCase.assertTrue(all.contains(Grammem.SemanGramem.ABBR));
	}
}
