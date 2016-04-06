package com.onpositive.text.analysis.syntax;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.onpositive.semantic.wordnet.AbstractWordNet;
import com.onpositive.semantic.wordnet.Grammem;
import com.onpositive.semantic.wordnet.Grammem.Gender;
import com.onpositive.semantic.wordnet.Grammem.PartOfSpeech;
import com.onpositive.semantic.wordnet.Grammem.SingularPlural;
import com.onpositive.text.analysis.IToken;
import com.onpositive.text.analysis.syntax.SyntaxToken.GrammemSet;

public class UniformVerbsParser extends UniformSentencePartsParser {

	
	
	public UniformVerbsParser(AbstractWordNet wordNet) {
		super(wordNet, IToken.TOKEN_TYPE_UNIFORM_VERB, new PartOfSpeech[]{PartOfSpeech.VERB});
	}

	@Override
	protected GrammemSet checkGrammemSetCorrespondence(GrammemSet gs0, GrammemSet gs1) {

		Set<Gender> gnd = matchGender(gs0, gs1);
		if (gnd == null || gnd.size() == 0) return null;
		
		Map<SingularPlural, SingularPlural> sp =  matchSP(gs0, gs1);
		if (sp == null || sp.isEmpty()) return null;
		
		List<Grammem> result = new ArrayList<Grammem>();
		result.add(PartOfSpeech.VERB);
		result.addAll(gnd);
		result.addAll(sp.keySet());
		
		return new GrammemSet(result);
	}

	@Override
	protected boolean noComma() {
		return false;
	}
	
}
