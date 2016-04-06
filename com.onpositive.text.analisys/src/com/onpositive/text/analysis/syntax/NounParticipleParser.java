package com.onpositive.text.analysis.syntax;

import java.util.Stack;

import com.onpositive.semantic.wordnet.AbstractWordNet;
import com.onpositive.text.analysis.IToken;

public class NounParticipleParser extends AbstractSyntaxParser {

	public NounParticipleParser(AbstractWordNet wordNet) {
		super(wordNet);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void combineTokens(Stack<IToken> sample, ProcessingData processingData) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected ProcessingResult checkToken(IToken newToken) {
		// TODO Auto-generated method stub
		return null;
	}

}
