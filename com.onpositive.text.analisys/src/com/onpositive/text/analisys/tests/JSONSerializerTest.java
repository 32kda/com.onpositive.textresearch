package com.onpositive.text.analisys.tests;

import java.util.ArrayList;
import java.util.List;

import com.onpositive.semantic.wordnet.composite.CompositeWordnet;
import com.onpositive.text.analisys.tools.data.TokenSerializer;
import com.onpositive.text.analysis.BasicCleaner;
import com.onpositive.text.analysis.IToken;
import com.onpositive.text.analysis.syntax.SentenceToken;
import com.onpositive.text.analysis.syntax.SyntaxParser;

public class JSONSerializerTest extends ParserTest {
	private TokenSerializer serializer;
	public JSONSerializerTest() {
		super();
		CompositeWordnet wn=new CompositeWordnet();
		wn.addUrl("/numerics.xml");
		wn.addUrl("/dimensions.xml");
		wn.addUrl("/modificator-adverb.xml");
		wn.addUrl("/prepositions.xml");
		wn.addUrl("/conjunctions.xml");
		wn.addUrl("/modalLikeVerbs.xml");
		wn.addUrl("/participles.xml");
		wn.prepare();
		SyntaxParser syntaxParser = new SyntaxParser(wn);
		this.composition = syntaxParser;
		this.serializer = new TokenSerializer();
	}
	
	@Override
	protected List<IToken> process(String str){
		List<IToken> processed = composition.parse(str);
		ArrayList<IToken> list = new ArrayList<IToken>();
		for(IToken t : processed){
			if(t instanceof SentenceToken){
				list.addAll(new BasicCleaner().clean(t.getChildren()));
			}
			else{
				list.add(t);
			}
		}
		
		System.out.println(serializer.serialize());		
		return list;
	}	
	
	public void test001() {
		String str = "Я не знаю";
		List<IToken> processed = process(str);
		
		assertTrue(processed != null);		
	}
	
	public void test003() {
		String str = "Странно будет без очков смотреть на его рукоплескания.";
		List<IToken> processed = process(str);
		
		assertTrue(processed != null);
	}
}