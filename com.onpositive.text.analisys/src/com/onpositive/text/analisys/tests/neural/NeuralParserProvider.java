package com.onpositive.text.analisys.tests.neural;

import com.onpositive.text.analysis.MorphologicParser;
import com.onpositive.text.analysis.neural.NeuralParser;

public class NeuralParserProvider {
	
	private static MorphologicParser parserInstance;
	
	private NeuralParserProvider() {
	}
	
	public static MorphologicParser getParser() {
		if (parserInstance == null) {
			parserInstance = new NeuralParser();
		}
		return parserInstance;
	}
}
