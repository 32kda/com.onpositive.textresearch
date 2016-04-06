package com.onpositive.text.analysis.syntax;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.onpositive.semantic.wordnet.AbstractWordNet;
import com.onpositive.semantic.wordnet.Grammem;
import com.onpositive.semantic.wordnet.Grammem.Case;
import com.onpositive.semantic.wordnet.Grammem.PartOfSpeech;
import com.onpositive.text.analysis.IToken;
import com.onpositive.text.analysis.syntax.SyntaxToken.GrammemSet;

public class UniformNounsParser extends UniformSentencePartsParser {

	
	private final static Set<Case> genCases = new HashSet<Case>(
			Arrays.asList(Case.GENT,Case.GEN1,Case.GEN2));
	
	//private final static Set<Case> nomCases = new HashSet<Case>(Arrays.asList(Case.NOMN));
	
	public UniformNounsParser(AbstractWordNet wordNet) {
		super(wordNet, IToken.TOKEN_TYPE_UNIFORM_NOUN, new PartOfSpeech[]{PartOfSpeech.NOUN});
	}
	
	protected GrammemSet checkGrammemSetCorrespondence(GrammemSet gs0, GrammemSet gs1) {
		
		Map<Case, Case> matchedCase = matchCase(gs0,gs1);
		if(matchedCase==null||matchedCase.isEmpty()){//||nomCases.containsAll(matchedCase.values())){
			return null;
		}
		if(genCases.containsAll(matchedCase.values())){
			return new GrammemSet(Arrays.asList(Case.GENT));
		}
		ArrayList<Grammem> list = new ArrayList<Grammem>(matchedCase.values());
		list.add(PartOfSpeech.NOUN);
		return new GrammemSet(list);
	}
	
	@Override
	protected boolean isReliable(List<GrammemSet> grammemSets, Set<Grammem> occuredGrammems) {
		
		for(Case c : genCases){
			if(occuredGrammems.contains(c)){
				return false;
			}
		}
		return true;
	}
	
	@Override
	protected boolean noComma() {
		return false;
	}

}
