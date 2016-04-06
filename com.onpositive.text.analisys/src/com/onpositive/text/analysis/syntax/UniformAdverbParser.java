package com.onpositive.text.analysis.syntax;

import java.util.ArrayList;
import java.util.Arrays;

import com.onpositive.semantic.wordnet.AbstractWordNet;
import com.onpositive.semantic.wordnet.Grammem;
import com.onpositive.semantic.wordnet.Grammem.PartOfSpeech;
import com.onpositive.text.analysis.IToken;
import com.onpositive.text.analysis.syntax.SyntaxToken.GrammemSet;

public class UniformAdverbParser extends UniformSentencePartsParser {

	public UniformAdverbParser(AbstractWordNet wordNet){
		super(wordNet, IToken.TOKEN_TYPE_UNIFORM_ADVERB, new PartOfSpeech[]{PartOfSpeech.ADVB});
	}

	ArrayList<Grammem> adverbCase = new ArrayList<Grammem>(Arrays.asList(PartOfSpeech.ADVB)); 
	
	@Override
	protected GrammemSet checkGrammemSetCorrespondence(GrammemSet gs0,GrammemSet gs1) {
		return new GrammemSet(adverbCase);
	}

}
