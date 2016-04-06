package com.onpositive.text.analysis.projection.creators;

import com.onpositive.semantic.wordnet.Grammem.PartOfSpeech;
import com.onpositive.text.analysis.IToken;
import com.onpositive.text.analysis.lexic.WordFormToken;
import com.onpositive.text.analysis.syntax.SyntaxToken;

public class NotPartRemover extends TokenRemover {

	@Override
	protected boolean shouldBeRemoved(IToken token) {
		return (token instanceof WordFormToken) && ((SyntaxToken) token).getBasicForm().equals("не") && (((SyntaxToken) token).hasGrammem(PartOfSpeech.PRCL));
	}

}
