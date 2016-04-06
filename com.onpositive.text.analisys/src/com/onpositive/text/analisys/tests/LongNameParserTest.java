package com.onpositive.text.analisys.tests;

import java.util.List;

import junit.framework.TestCase;

import com.onpositive.semantic.wordnet.WordNetProvider;
import com.onpositive.semantic.wordnet.composite.CompositeWordnet;
import com.onpositive.text.analysis.IToken;
import com.onpositive.text.analysis.lexic.LongNameToken;
import com.onpositive.text.analysis.lexic.WordFormParser;
import com.onpositive.text.analysis.lexic.dates.DateCombineParser;
import com.onpositive.text.analysis.lexic.dates.DateParser;
import com.onpositive.text.analysis.lexic.dates.LongNameParser;
import com.onpositive.text.analysis.lexic.scalar.ScalarParser;
import com.onpositive.text.analysis.syntax.SyntaxParser;

public class LongNameParserTest extends ParserTest {

	public LongNameParserTest() {
		super();
		CompositeWordnet wn = new CompositeWordnet();
		wn.addUrl("/numerics.xml");
		wn.addUrl("/dimensions.xml");
		wn.addUrl("/modificator-adverb.xml");
		wn.addUrl("/prepositions.xml");
		wn.addUrl("/conjunctions.xml");
		wn.addUrl("/modalLikeVerbs.xml");
		wn.addUrl("/participles.xml");
		wn.prepare();
		ScalarParser scalarParser = new ScalarParser();
		setParsers(new WordFormParser(WordNetProvider.getInstance()),scalarParser,new DateParser(),new DateCombineParser(),new LongNameParser());		
	}

	public void testS001() {
		List<IToken> parse = composition
				.parse("Президент Украины Петр Порошенко заявил, что вносит в парламент законопроект о снятии полной депутатской неприкосновенности");
		boolean found=false;
		for (IToken c:parse){
			if (c instanceof LongNameToken){
				if (c.getStringValue().contains("петр(")&&c.getStringValue().contains("порошенко(")){
					found=true;
				}
				
			}
		}
		TestCase.assertTrue(found);
	}
	public void testS0066() {
		List<IToken> parse = composition
				.parse("касающихся романа Бориса Пастернака «Доктор Живаго»");
		boolean found=false;
		for (IToken c:parse){
			if (c instanceof LongNameToken){
				if (c.getStringValue().contains("борис(")&&c.getStringValue().contains("пастернак(")){
					found=true;
				}
				
			}
		}
		TestCase.assertTrue(found);
	}
	
	public void testS002() {
		List<IToken> parse = composition
				.parse("Премьер Министр Армении Павел Петроченко");
		boolean found=false;
		for (IToken c:parse){
			if (c instanceof LongNameToken){
				if (c.getStringValue().contains("павел(")&&c.getStringValue().contains("петроченко(")){
					found=true;
				}
				if (c.getStringValue().contains("арме")){
					TestCase.assertTrue(false);			
				}
			}
		}
		TestCase.assertTrue(found);
	}
	public void testS003() {
		List<IToken> parse = composition
				.parse("Премьер Министр Армении Павел Петраченко");
		boolean found=false;
		for (IToken c:parse){
			if (c instanceof LongNameToken){
				if (c.getStringValue().contains("павел(")&&c.getStringValue().contains("Петраченко")){
					found=true;
				}
				if (c.getStringValue().contains("арме")){
					TestCase.assertTrue(false);			
				}
			}
		}
		TestCase.assertTrue(found);
	}
	
	public void testS004() {
		List<IToken> parse = composition
				.parse("Премьер Министр Армении Петраченко Павел ");
		boolean found=false;
		for (IToken c:parse){
			if (c instanceof LongNameToken){
				if (c.getStringValue().contains("павел(")&&c.getStringValue().contains("Петраченко")){
					found=true;
				}
				if (c.getStringValue().contains("арме")){
					TestCase.assertTrue(false);			
				}
			}
		}
		TestCase.assertTrue(found);
	}
	public void testS005() {
		List<IToken> parse = composition
				.parse("Премьер Министр Армении Петроченко П. A. ");
		boolean found=false;
		for (IToken c:parse){
			if (c instanceof LongNameToken){
				if (c.getStringValue().contains("петроченко(")&&c.getStringValue().contains("a(")){
					found=true;
				}
				if (c.getStringValue().contains("арме")){
					TestCase.assertTrue(false);			
				}
			}
		}
		TestCase.assertTrue(found);
	}
	public void testS008() {
		List<IToken> parse = composition
				.parse("Владимир Нестеров");
		boolean found=false;
		for (IToken c:parse){
			if (c instanceof LongNameToken){
				if (c.getStringValue().contains("владимир(")&&c.getStringValue().contains("естер")){
					found=true;
				}
				if (c.getStringValue().contains("арме")){
					TestCase.assertTrue(false);			
				}
			}
		}
		TestCase.assertTrue(found);
	}
	     
}
