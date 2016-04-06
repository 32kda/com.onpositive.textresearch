package com.onpositive.text.analysis.syntax;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import com.onpositive.semantic.wordnet.AbstractWordNet;
import com.onpositive.semantic.wordnet.Grammem;
import com.onpositive.semantic.wordnet.Grammem.Case;
import com.onpositive.semantic.wordnet.Grammem.Gender;
import com.onpositive.semantic.wordnet.Grammem.PartOfSpeech;
import com.onpositive.semantic.wordnet.Grammem.SingularPlural;
import com.onpositive.text.analysis.IToken;
import com.onpositive.text.analysis.syntax.SyntaxToken.GrammemSet;
import com.onpositive.text.analysis.syntax.UniformSentencePartsParser;

public class UniformAdjectivesParser extends UniformSentencePartsParser {

	public UniformAdjectivesParser(AbstractWordNet wordNet) {
		super(wordNet, IToken.TOKEN_TYPE_UNIFORM_ADJECTIVE, new PartOfSpeech[]{PartOfSpeech.ADJF});
	}
	
	protected GrammemSet checkGrammemSetCorrespondence(GrammemSet gs0, GrammemSet gs1) {
		
		Set<Gender> matchedGender = matchGender(gs0,gs1);
		if(matchedGender==null||matchedGender.isEmpty()){
			return null;
		}

		Map<SingularPlural,SingularPlural> matchedSp = matchSP(gs0,gs1);
		if(matchedSp==null||matchedSp.isEmpty()){
			return null;
		}
		
		Map<Case, Case> matchedCase = matchCase(gs0,gs1);
		if(matchedCase==null||matchedCase.isEmpty()){
			return null;
		}
		ArrayList<Grammem> list = new ArrayList<Grammem>();
		list.add(PartOfSpeech.ADJF);
		list.addAll(matchedGender);
		list.addAll(matchedSp.values());
		list.addAll(matchedCase.values());
		return new GrammemSet(list);
	}

}
