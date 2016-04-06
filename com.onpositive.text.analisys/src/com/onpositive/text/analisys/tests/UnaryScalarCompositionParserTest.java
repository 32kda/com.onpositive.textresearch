package com.onpositive.text.analisys.tests;

import java.util.List;

import com.onpositive.text.analysis.IToken;
import com.onpositive.text.analysis.lexic.scalar.ScalarParser;
import com.onpositive.text.analysis.lexic.scalar.UnaryScalarCompositionParser;;

public class UnaryScalarCompositionParserTest extends ParserTest {
	public UnaryScalarCompositionParserTest() {
		super();
		ScalarParser scalarParser = new ScalarParser();
		UnaryScalarCompositionParser unaryScalarCompositionParser = new UnaryScalarCompositionParser();
		setParsers(scalarParser, unaryScalarCompositionParser);
	}

	public void testUSC001(){
		String str = "Каждый день мы продаём 5-7 новых машин.";
		List<IToken> processed = process(str);
		printTokens(processed);
		assertTestScalar(5, processed);
		assertTestScalar(7, processed);
	}
	
	public void testUSC002(){
		String str = "Добрый день, уважаемые пассажиры. Температура за бортом -- -2 градуса Цельсия. Самолёт будет находиться в воздухе 4 часа и приземлится в Москве в 8 часов ровно.";
		List<IToken> processed = process(str);
		printTokens(processed);
		assertTestScalar(-2, processed);
		assertTestScalar(4, processed);
		assertTestScalar(8, processed);
	}
	
	public void testUSC003(){
		String str = "-5, -3, -1, 1, 3, +5, +7, +9, 100-200, 300+400.";
		List<IToken> processed = process(str);
		printTokens(processed);
		int[] scalars = { -5, -3, -1, 1, 3, 5, 7, 9, 100, 200, 300, 400 };
		
		for (int sc: scalars) {
			assertTestScalar(sc, processed);	
		}
	}
	
	
}
