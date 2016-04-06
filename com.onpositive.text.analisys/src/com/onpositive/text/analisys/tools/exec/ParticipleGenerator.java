package com.onpositive.text.analisys.tools.exec;

import java.util.Iterator;

import com.onpositive.semantic.wordnet.Grammem.PartOfSpeech;
import com.onpositive.semantic.wordnet.MeaningElement;
import com.onpositive.semantic.wordnet.TextElement;
import com.onpositive.semantic.wordnet.composite.CompositeWordnet;
import com.onpositive.semantic.words2.SimpleWordNet;
import com.onpositive.semantic.words3.ReadOnlyMapWordNet;
import com.onpositive.text.analysis.syntax.SyntaxParser;

public class ParticipleGenerator {

	public static void main(String[] args) {
		CompositeWordnet wn=new CompositeWordnet();
		wn.addUrl("/numerics.xml");
		wn.addUrl("/dimensions.xml");
		wn.addUrl("/modificator-adverb.xml");
		wn.addUrl("/prepositions.xml");
		wn.addUrl("/conjunctions.xml");
		wn.addUrl("/modalLikeVerbs.xml");
		wn.addUrl("/participles.xml");
		wn.prepare();		

		ReadOnlyMapWordNet swn = (ReadOnlyMapWordNet) wn.getOriginal();
		
		for (int i = 0; i < swn.conceptCount(); i++) {
			MeaningElement me = swn.getConceptInfo(i);
			if (me == null) continue;
			if (me.getPartOfSpeech() == PartOfSpeech.PRCL) System.out.println(me.toString());
		}		
	}
}
