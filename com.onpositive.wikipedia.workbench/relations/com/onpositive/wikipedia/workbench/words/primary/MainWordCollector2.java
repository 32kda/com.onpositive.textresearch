package com.onpositive.wikipedia.workbench.words.primary;

import java.util.HashSet;
import java.util.LinkedHashSet;

import com.onpositive.semantic.wordnet.Grammem;
import com.onpositive.text.analysis.IToken;
import com.onpositive.text.analysis.lexic.WordFormToken;
import com.onpositive.text.analysis.syntax.ClauseToken;
import com.onpositive.text.analysis.syntax.SentenceToken;
import com.onpositive.text.analysis.syntax.SyntaxToken;

public class MainWordCollector2 {

	protected LinkedHashSet<String> tks = new LinkedHashSet<>();

	public void visit(IToken t) {
		if (t instanceof SentenceToken || t instanceof ClauseToken) {
			if (t.getChildren() != null) {
				t.getChildren().stream().forEach(x -> visit(x));
			}
		}
		if (t instanceof SyntaxToken) {
			SyntaxToken tr = (SyntaxToken) t;
			tr.getChildren().forEach(x -> visit(x));
		}
		if (t instanceof WordFormToken) {
			final WordFormToken wordFormToken = (WordFormToken) t;
			if (!wordFormToken.getGrammemSets().isEmpty()) {
				if (wordFormToken.hasGrammem(Grammem.PartOfSpeech.NOUN)) {
					if ((wordFormToken.hasGrammem(Grammem.Case.NOMN)||wordFormToken.hasGrammem(Grammem.Case.GENT))&&wordFormToken.getBasicForm().length()>1){
					tks.add(wordFormToken.getBasicForm());
					}
				}
			}
		}
	}

	public HashSet<String> getResult() {
		return tks;
	}
}
