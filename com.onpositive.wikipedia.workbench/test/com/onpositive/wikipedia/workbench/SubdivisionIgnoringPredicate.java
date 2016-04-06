package com.onpositive.wikipedia.workbench;

import java.util.List;

import com.onpositive.semantic.wikipedia2.catrelations.ITokenChainPredicate;
import com.onpositive.semantic.wordnet.Grammem.Case;
import com.onpositive.text.analisys.tests.euristics.SimplifiedToken;

public class SubdivisionIgnoringPredicate implements ITokenChainPredicate {

	@Override
	public boolean matches(List<SimplifiedToken> tokens) {
		int prepIdx = -1;
		for (int i = 0; i < tokens.size(); i++) {
			SimplifiedToken simplifiedToken = tokens.get(i);
			if ("по".equalsIgnoreCase(simplifiedToken.getWord())) {
				prepIdx = i;
			}
			if (simplifiedToken.hasGrammem(Case.DATV) && prepIdx > -1) {
				return true;
			}
		}
		return false;
	}

}
