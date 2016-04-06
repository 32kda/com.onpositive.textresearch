package com.onpositive.text.analisys.tests;

import java.util.List;

import com.onpositive.text.analysis.IToken;
import com.onpositive.text.analysis.lexic.scalar.ScalarParser;

public class ScalarParserTest extends ParserTest{

	public ScalarParserTest() {
		super();
		ScalarParser scalarParser = new ScalarParser();
		setParsers(scalarParser);
	}

	public void testS001(){
		String str = "У меня есть 25 яблок";
		List<IToken> processed = process(str);
		printTokens(processed);
		assertTestScalar(25.0, processed);
	}
	
	public void testS002(){
		String str = "Прилетело 1 000 000 попугаев";		
		List<IToken> processed = process(str);
		printTokens(processed);
		assertTestScalar(1000000.0, processed);
	}
	
	public void testS0022(){
		String str = "1 223 232 ";		
		List<IToken> processed = process(str);
		printTokens(processed);
		assertTestScalar(1223232.0, processed);
	}
	public void testS0023(){
		String str = "1 223";		
		List<IToken> processed = process(str);
		printTokens(processed);
		assertTestScalar(1223.0, processed);
	}
	
	public void testS003(){
		String str = "Прилетело попугаев 1,000,000.2,000,000 -- это прилетело ворон.";		
		List<IToken> processed = process(str);
		printTokens(processed);
		assertTestScalar(1000000, processed);
		assertTestScalar(2000000, processed);
	}
	
	public void testS004(){
		String str = "У меня есть " + '\u2159' + " яблока";		
		List<IToken> processed = process(str);
		printTokens(processed);
		assertTestScalar(1.0/6.0, processed);
	}
	
	public void testS005(){
		String str = "Ширина картинки -- 18px";		
		List<IToken> processed = process(str);
		printTokens(processed);
		assertTestScalar(18, processed);
	}
	
	public void testS006(){
		String str = "Каждый день мы продаём 5-7 новых машин.";
		List<IToken> processed = process(str);
		printTokens(processed);
		assertTestScalar(5, processed);
		assertTestScalar(7, processed);
	}
}
