package com.onpositive.text.analisys.tests.util;

import java.util.List;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import com.onpositive.semantic.wordnet.AbstractWordNet;
import com.onpositive.semantic.wordnet.Grammem;
import com.onpositive.semantic.wordnet.WordNetProvider;
import com.onpositive.text.analysis.Euristic;
import com.onpositive.text.analysis.EuristicAnalyzingParser;
import com.onpositive.text.analysis.IToken;
import com.onpositive.text.analysis.filtering.AbbreviationsFilter;
import com.onpositive.text.analysis.filtering.AdditionalPartsPresetFilter;
import com.onpositive.text.analysis.lexic.PrimitiveTokenizer;
import com.onpositive.text.analysis.lexic.SentenceSplitter;
import com.onpositive.text.analysis.lexic.WordFormParser;
import com.onpositive.text.analysis.syntax.SyntaxToken;
import com.onpositive.text.analysis.utils.MorphologicUtils;

public class TestingUtil {
	
	private static final double CORRELATION_THRESHOLD = 0.1;

	public static void checkHas(List<IToken> result, int i, Grammem wantedGrammem) {
		TestCase.assertTrue(i < result.size() && (result.get(i) instanceof SyntaxToken));
		result = MorphologicUtils.getWithNoConflicts(result);
		SyntaxToken wordFormToken = (SyntaxToken) result.get(i);
		if (wordFormToken.getCorrelation() > CORRELATION_THRESHOLD && wordFormToken.hasGrammem(wantedGrammem)) {
			return;
		}
		List<IToken> conflicts = wordFormToken.getConflicts();
		for (IToken conflictingToken : conflicts) {
			if (conflictingToken.getCorrelation() > CORRELATION_THRESHOLD && 
				conflictingToken instanceof SyntaxToken && 
				((SyntaxToken) conflictingToken).hasGrammem(wantedGrammem)) {
				return;
			}	
		}
		throw new AssertionFailedError("Assertion failed, " + result.get(i).getShortStringValue() + " isn't " + wantedGrammem);
	}
	
	public static List<IToken> getWordFormTokens(String str) {
		PrimitiveTokenizer pt = new PrimitiveTokenizer();
		AbstractWordNet instance = WordNetProvider.getInstance();
		WordFormParser wfParser = new WordFormParser(instance);
		wfParser.setIgnoreCombinations(true);
		List<IToken> tokens = pt.tokenize(str);		
		List<IToken> processed = wfParser.process(tokens);
		return processed;
	}
	
	public static List<IToken> getSentences(String str) {
		return new SentenceSplitter().split(getWordFormTokens(str));
	}
	
	public static EuristicAnalyzingParser configureDefaultAnalyzer(List<Euristic> euristics) {
		EuristicAnalyzingParser euristicAnalyzingParser = new EuristicAnalyzingParser(euristics);
		euristicAnalyzingParser.addTokenFilter(new AbbreviationsFilter());
		euristicAnalyzingParser.addTokenFilter(new AdditionalPartsPresetFilter());
		return euristicAnalyzingParser;
	}
	
}
