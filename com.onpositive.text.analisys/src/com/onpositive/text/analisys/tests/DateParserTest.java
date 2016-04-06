package com.onpositive.text.analisys.tests;

import java.util.List;

import com.onpositive.semantic.wordnet.WordNetProvider;
import com.onpositive.text.analysis.IToken;
import com.onpositive.text.analysis.lexic.WordFormParser;
import com.onpositive.text.analysis.lexic.dates.DateCombineParser;
import com.onpositive.text.analysis.lexic.dates.DateParser;
import com.onpositive.text.analysis.lexic.scalar.ScalarParser;

public class DateParserTest extends ParserTest{

	public DateParserTest() {
		super();
		ScalarParser scalarParser = new ScalarParser();
		setParsers(new WordFormParser(WordNetProvider.getInstance()),scalarParser,new DateParser(),new DateCombineParser());
	}

	public void testS001(){
		String str = "1982 года";
		List<IToken> processed = process(str);
		printTokens(processed);
		assertTestDate((int) 1982.0, null,null,processed);
	}
	public void testS002(){
		String str = "15 апреля 1982 года";
		List<IToken> processed = process(str);
		printTokens(processed);
		assertTestDate((int) 1982.0, 3,15,processed);
	}
	public void testS003(){
		String str = "апреля 1982 года";
		List<IToken> processed = process(str);
		printTokens(processed);
		assertTestDate((int) 1982.0, 3,null,processed);
	}
	
}
