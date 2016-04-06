package com.onpositive.wikipedia.workbench.words.primary;

import java.util.HashSet;
import java.util.LinkedHashSet;

import com.onpositive.text.analysis.IToken;
import com.onpositive.text.analysis.lexic.WordFormToken;
import com.onpositive.text.analysis.syntax.ClauseToken;
import com.onpositive.text.analysis.syntax.SentenceToken;
import com.onpositive.text.analysis.syntax.SyntaxToken;

public class MainWordCollector {

	protected LinkedHashSet<WordFormToken> tks = new LinkedHashSet<>();
	
	public void visit(IToken t) {
		if (t instanceof SentenceToken || t instanceof ClauseToken) {
			if (t.getChildren() != null) {
				t.getChildren().stream().forEach(x -> visit(x));
			}
		}
		if (t instanceof SyntaxToken) {
			SyntaxToken tr = (SyntaxToken) t;
			if (tr.getMainWord() != null) {
				if (!tr.getMainWord().getGrammemSets().isEmpty()) {
					tks.add(tr.getMainWord());
				}
			}
		}
		if (t instanceof WordFormToken) {
			if (!((WordFormToken) t).getGrammemSets().isEmpty()) {
				tks.add((WordFormToken) t);
			}
		}
	}
	
	public HashSet<WordFormToken> getResult(){
		return tks;
	}
}
