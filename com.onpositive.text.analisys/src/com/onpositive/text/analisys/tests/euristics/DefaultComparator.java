package com.onpositive.text.analisys.tests.euristics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.onpositive.semantic.wordnet.Grammem;
import com.onpositive.text.analysis.syntax.SyntaxToken;
import com.onpositive.text.analysis.syntax.SyntaxToken.GrammemSet;

public class DefaultComparator implements ITokenComparator {

	@Override
	public Collection<Grammem> calculateWrong(SimplifiedToken etalonToken, SyntaxToken comparedToken) {
		Set<Grammem> wrong = new HashSet<Grammem>();
		List<GrammemSet> grammemSets = comparedToken.getGrammemSets();
		for (GrammemSet grammemSet : grammemSets) {
			for (Grammem grammem : grammemSet.grammems()) {
				if (isWrong(etalonToken, grammem)) {
					wrong.add(grammem);
				}
			}
		}
		return wrong;
	}

	protected boolean isWrong(SimplifiedToken etalonToken, Grammem grammem) {
		return !etalonToken.getGrammems().contains(grammem);
	}

}
