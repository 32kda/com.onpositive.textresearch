package com.onpositive.text.analisys.tests;

import java.util.List;

import com.onpositive.semantic.wordnet.AbstractWordNet;
import com.onpositive.semantic.wordnet.composite.CompositeWordnet;
import com.onpositive.text.analysis.IToken;
import com.onpositive.text.analysis.lexic.NumericsParser;
import com.onpositive.text.analysis.lexic.WordFormParser;
import com.onpositive.text.analysis.lexic.dimension.DimensionParser;
import com.onpositive.text.analysis.lexic.dimension.Unit;
import com.onpositive.text.analysis.lexic.dimension.UnitGroupParser;
import com.onpositive.text.analysis.lexic.dimension.UnitKind;
import com.onpositive.text.analysis.lexic.dimension.UnitParser;
import com.onpositive.text.analysis.lexic.scalar.ScalarParser;


public class DimensionParserTest extends ParserTest{
	
	private Unit squareKilometerUnit = new Unit("километр^2",UnitKind.AREA,1000*1000);
	
	private Unit kilometerUnit = new Unit("километр",UnitKind.SIZE,1000);
	
	private Unit meterUnit = new Unit("метр",UnitKind.SIZE,1);
	
	private Unit squareMeterUnit = new Unit("метр^2",UnitKind.AREA,1);
	
	private Unit nanometerUnit = new Unit("нанометр",UnitKind.SIZE,0.000000001);
	
	private Unit mileUnit = new Unit("сухопутная миля",UnitKind.SIZE,1609.344);
	
	private Unit kilofootUnit = new Unit("килофут",UnitKind.SIZE, 304.8);
	
	private Unit squareKilofootUnit = new Unit("килофут^2",UnitKind.AREA, 304.8*304.8);

	private Unit meterPerSecondUnit = new Unit("метр в секунду",UnitKind.SPEED,1);
	
	public DimensionParserTest() {
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

	public void testD001(){
		String str = "Проехал два км со скоростью 10 метров в секунду";		
		List<IToken> processed = process(str);		
		assertTestDimension(new Double[]{2.0,10.0}, new Unit[]{kilometerUnit,meterPerSecondUnit},processed );
	}
	

	public void testD002(){
		String str = "Вскопал 2 км^2 земли.";		
		List<IToken> processed = process(str);
		assertTestDimension(2.0, squareKilometerUnit,processed);
	}
	
	public void testD003(){
		String str = "Обработал 4 км² асфальта.";		
		List<IToken> processed = process(str);
		assertTestDimension(4.0,squareKilometerUnit,processed);
	}
	
	public void testD004(){
		String str = "Технологический процесс 22 нм.";		
		List<IToken> processed = process(str);
		assertTestDimension(22.0,nanometerUnit,processed);
	}
	
	public void testD005(){
		String str = "Глубина моря в этом месте составляет 50 килофутов.";		
		List<IToken> processed = process(str);
		assertTestDimension(50.0,kilofootUnit,processed);
	}
	
	public void testD006(){
		String str = "Площадь моря составляет 50000 квадратных килофутов.";		
		List<IToken> processed = process(str);
		assertTestDimension(50000.0,squareKilofootUnit ,processed);
	}
	
	public void testD007(){
		String str = "Площадь моря составляет 50000 килофутов квадратных.";		
		List<IToken> processed = process(str);
		assertTestDimension(50000.0,squareKilofootUnit ,processed);
	}
	
	public void testD008(){
		String str = "Мы проехали три сухопутных мили в сторону гор.";		
		List<IToken> processed = process(str);
		assertTestDimension(3.0,mileUnit,processed);
	}
	
	public void testD009(){
		String str = "92,8 м^2";		
		List<IToken> processed = process(str);
		assertTestDimension(92.8,squareMeterUnit,processed);
	}	
	
	public void testD010(){
		String str = "Площадь страны равна 3 млрд. м^2.";		
		List<IToken> processed = process(str);
		assertTestDimension(3000000000.,squareMeterUnit,processed);
	}
	
	public void testD011(){
		String str = "Площадь страны равна 3 101 млн. м^2.";		
		List<IToken> processed = process(str);
		assertTestDimension(3101000000.,squareMeterUnit,processed);
	}
}
