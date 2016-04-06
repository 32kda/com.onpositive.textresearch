package com.onpositive.semantic.wikipedia2.properties;

import java.util.ArrayList;
import java.util.List;

import com.onpositive.semantic.wordnet.AbstractWordNet;
import com.onpositive.semantic.wordnet.composite.CompositeWordnet;
import com.onpositive.text.analysis.AbstractParser;
import com.onpositive.text.analysis.IToken;
import com.onpositive.text.analysis.ParserComposition;
import com.onpositive.text.analysis.lexic.NumericsParser;
import com.onpositive.text.analysis.lexic.WordFormParser;
import com.onpositive.text.analysis.lexic.dimension.DimensionParser;
import com.onpositive.text.analysis.lexic.dimension.UnitGroupParser;
import com.onpositive.text.analysis.lexic.dimension.UnitParser;
import com.onpositive.text.analysis.lexic.scalar.ScalarParser;
import com.onpositive.text.analysis.syntax.SentenceToken;
import com.onpositive.words.WikiTokenizerExtension;

public class PropertyTokenizer {

	
	ParserComposition composition;
	
	public PropertyTokenizer() {
		CompositeWordnet wn=new CompositeWordnet();
		wn.add(PropertyTokenizer.class.getResourceAsStream("numerics.xml"));
		wn.add(PropertyTokenizer.class.getResourceAsStream("dimensions.xml"));//FIXME
		wn.prepare();
		AbstractWordNet wordNet = wn;
		WordFormParser wfParser = new WordFormParser(wordNet);
		ScalarParser scalarParser = new ScalarParser();
		UnitParser unitParser = new UnitParser(wordNet);
		UnitGroupParser unitGroupParser = new UnitGroupParser(wordNet);
		DimensionParser dimParser = new DimensionParser();		
		NumericsParser numericsParser = new NumericsParser(wn);
		setParsers(wfParser,scalarParser,numericsParser,unitParser,unitGroupParser,dimParser);
		composition.getTokenizer().add(new WikiTokenizerExtension());
	}
	protected void setParsers(AbstractParser... parsers){
		if(parsers==null||parsers.length==0){
			return;
		}
		else{
			this.composition = new ParserComposition(parsers);
		}
	}
	
	public List<IToken> process(String str){
		ArrayList<IToken> list = new ArrayList<IToken>();
		try{
		List<IToken> processed = composition.parse(str);
		
		for(IToken t : processed){
			if(t instanceof SentenceToken){
				list.addAll(t.getChildren());
			}
			else{
				list.add(t);
			}
		}
		}catch (Exception e){
			e.printStackTrace();
		}
		
		return list;
	}
}
