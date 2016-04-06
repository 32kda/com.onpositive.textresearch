package com.onpositive.text.analisys.tests;

import java.util.List;

import junit.framework.TestCase;

import com.onpositive.semantic.wordnet.AbstractWordNet;
import com.onpositive.semantic.wordnet.composite.CompositeWordnet;
import com.onpositive.text.analysis.IToken;
import com.onpositive.text.analysis.ParserComposition;
import com.onpositive.text.analysis.lexic.DimensionToken;
import com.onpositive.text.analysis.lexic.NumericsParser;
import com.onpositive.text.analysis.lexic.ScalarToken;
import com.onpositive.text.analysis.lexic.WordFormParser;
import com.onpositive.text.analysis.lexic.dimension.DimensionParser;
import com.onpositive.text.analysis.lexic.dimension.Unit;
import com.onpositive.text.analysis.lexic.dimension.UnitGroupParser;
import com.onpositive.text.analysis.lexic.dimension.UnitKind;
import com.onpositive.text.analysis.lexic.dimension.UnitParser;
import com.onpositive.text.analysis.lexic.scalar.ScalarParser;

public class NumericParserTest extends ParserTest{

	private Unit kilometerPerHourUnit = new Unit("километр в час",UnitKind.SPEED,1);
	
	public NumericParserTest() {
		super();
		CompositeWordnet wn=new CompositeWordnet();
		wn.addUrl("/numerics.xml");
		wn.addUrl("/dimensions.xml");
		wn.prepare();
		AbstractWordNet wordNet = wn;
		WordFormParser wfParser = new WordFormParser(wordNet);
		ScalarParser scalarParser = new ScalarParser();
		UnitParser unitParser = new UnitParser(wordNet);
		UnitGroupParser unitGroupParser = new UnitGroupParser(wordNet);
		DimensionParser dimParser = new DimensionParser();		
		NumericsParser numericsParser = new NumericsParser(wn);
		setParsers(wfParser,scalarParser,numericsParser,unitParser,unitGroupParser,dimParser);
	}
	
	public void testBasic(){
		String str = "Он ехал со скоростью двадцать два кмч";		
		List<IToken> processed = process(str);
		assertTestDimension(22.0,kilometerPerHourUnit,processed);
	}
	public void testThousand(){
		String str = "Миг-31 способен разгоняться до 3.5 тысяч кмч";		
		List<IToken> processed = process(str);
		assertTestDimension(3500,kilometerPerHourUnit,processed);
	}
	
	public void testMulti(){
		String str = "раз два три";		
		List<IToken> processed = process(str);
		assertTestScalar(1, processed);
		assertTestScalar(2, processed);
		assertTestScalar(3, processed);
	}
	
	public void testM002(){
		String str = "50 тысяч 222 кмч";		
		List<IToken> processed = process(str);
		assertTestDimension(50222, kilometerPerHourUnit, processed);
	}
	
	public void testM007(){
		String str = "двадцать кмч";		
		List<IToken> processed = process(str);
		assertTestDimension(20, kilometerPerHourUnit, processed);
	}
	
	public void testM008(){
		String str = "девяносто кмч";		
		List<IToken> processed = process(str);
		assertTestDimension(90, kilometerPerHourUnit, processed);
	}
	
	public void testM003(){
		String str = "50 два 34 двадцать два";		
		List<IToken> processed = process(str);
		printTokens(processed);
		assertTestScalar(50, processed);
		assertTestScalar(2, processed);
		assertTestScalar(34, processed);
		assertTestScalar(22, processed);
	}
	
	public void testM004(){
		String str = "трёхтысячный отряд";		
		List<IToken> processed = process(str);
		printTokens(processed);
		
	}
	
	public void testM005(){
		String str = "Население новосибирска составляет около полутора миллионов человек";		
		List<IToken> processed = process(str);
		printTokens(processed);
		assertTestScalar(1500000, processed);
	}
	
}
