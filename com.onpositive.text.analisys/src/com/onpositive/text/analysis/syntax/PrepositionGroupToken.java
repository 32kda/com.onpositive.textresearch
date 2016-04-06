package com.onpositive.text.analysis.syntax;

import java.util.ArrayList;
import java.util.List;

import com.onpositive.text.analysis.IToken;

public class PrepositionGroupToken extends SyntaxToken {

	private static final ArrayList<GrammemSet> EMPTY_GRAMMEMSET_LIST = new ArrayList<SyntaxToken.GrammemSet>();

	
	public PrepositionGroupToken(SyntaxToken prepToken, SyntaxToken word, int startPosition, int endPosition, boolean isDoubtful) {
		super(IToken.TOKEN_TYPE_PREPOSITION_GROUP, prepToken, null, startPosition, endPosition, isDoubtful);
		this.prepToken = prepToken;
		this.word = word;
	}

	public PrepositionGroupToken(SyntaxToken prepToken, SyntaxToken word, int startPosition, int endPosition) {
		super(IToken.TOKEN_TYPE_PREPOSITION_GROUP, word, null, startPosition, endPosition);
		this.prepToken = prepToken;
		this.word = word;
	}
	
	@Override
	public List<GrammemSet> getGrammemSets() {
		return EMPTY_GRAMMEMSET_LIST;
	}
	
	private SyntaxToken prepToken;
	
	
	private SyntaxToken word;
	

	public SyntaxToken getPrepToken() {
		return prepToken;
	}


	public void setPrepToken(SyntaxToken prepToken) {
		this.prepToken = prepToken;
	}


	public SyntaxToken getWord() {
		return word;
	}


	public void setWord(SyntaxToken word) {
		this.word = word;
	}

}
