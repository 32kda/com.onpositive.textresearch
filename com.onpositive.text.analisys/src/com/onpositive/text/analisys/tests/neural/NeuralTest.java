package com.onpositive.text.analisys.tests.neural;

import java.io.FileNotFoundException;
import java.util.List;

import org.junit.Test;

import com.onpositive.semantic.wordnet.Grammem.PartOfSpeech;
import com.onpositive.text.analisys.tests.util.TestingUtil;
import com.onpositive.text.analysis.IToken;
import com.onpositive.text.analysis.MorphologicParser;
import com.onpositive.text.analysis.filtering.AbbreviationsFilter;
import com.onpositive.text.analysis.filtering.AdditionalPartsPresetFilter;
import com.onpositive.text.analysis.neural.NeuralParser;

import junit.framework.TestCase;

public class NeuralTest extends TestCase {
	
//	@Test
//	public void test00() throws Exception {
//		new Trainer().trainEncog();
//	}
	
	@Test
	public void test01() throws FileNotFoundException {
		basicNeuralTest("ложечка витая");
	}
	
	@Test
	public void test02() throws FileNotFoundException {
		List<IToken> result = basicNeuralTest("шестьдесят минут прошло");
		TestingUtil.checkHas(result, 1, PartOfSpeech.NOUN);
		TestingUtil.checkHas(result, 2, PartOfSpeech.VERB);
	}
	
	@Test
	public void test03() throws FileNotFoundException {
		List<IToken> result = basicNeuralTest("не покупать белил совсем");
		TestingUtil.checkHas(result, 2, PartOfSpeech.NOUN);
	}
	
	@Test
	public void test04() throws FileNotFoundException {
		List<IToken> result = basicNeuralTest("Маша белила стену");
		TestingUtil.checkHas(result, 1, PartOfSpeech.VERB);
	}
	
	// существительное - предлог
	public void test05a() throws FileNotFoundException {
		List<IToken> result = basicNeuralTest("перед яблока");
		TestingUtil.checkHas(result, 0, PartOfSpeech.NOUN);
	}
	
	public void test05b() throws FileNotFoundException {
		List<IToken> result = basicNeuralTest("стоял перед строем");
		TestingUtil.checkHas(result, 1, PartOfSpeech.PREP);
	}
		
	// глагол - местоимение
	public void test06a() throws FileNotFoundException {
		List<IToken> result = basicNeuralTest("тщательно мой");
		TestingUtil.checkHas(result, 1, PartOfSpeech.VERB);
	}
	
	public void test06b() throws FileNotFoundException {
		List<IToken> result = basicNeuralTest("мой друг приехал");
		TestingUtil.checkHas(result, 0, PartOfSpeech.ADJF);
	}
	
	// инфинитив - существительное
	public void test07a() throws FileNotFoundException {
		List<IToken> result = basicNeuralTest("без вести");
		TestingUtil.checkHas(result, 1, PartOfSpeech.NOUN);
	}
	
	public void test07b() throws FileNotFoundException {
		List<IToken> result = basicNeuralTest("вести исследования");
		TestingUtil.checkHas(result, 0, PartOfSpeech.INFN);
	}
	
	//глагол - наречие
	public void test08a() throws FileNotFoundException {
		List<IToken> result = basicNeuralTest("показал издали");
		TestingUtil.checkHas(result, 1, PartOfSpeech.ADVB);
	}
	
	public void test08b() throws FileNotFoundException {
		List<IToken> result = basicNeuralTest("императоры издали закон"); //XXX
		TestingUtil.checkHas(result, 1, PartOfSpeech.VERB);
	}

	//существительное - числительное 
	public void test09a() throws FileNotFoundException {
		List<IToken> result = basicNeuralTest("создал семью");
		TestingUtil.checkHas(result, 1, PartOfSpeech.NOUN);
	}

	public void test09b() throws FileNotFoundException {
		List<IToken> result = basicNeuralTest("с семью нулями"); //XXX
		TestingUtil.checkHas(result, 1, PartOfSpeech.NUMR);
	}
	
	//числительное - существительное
	public void test10a() throws FileNotFoundException {
		List<IToken> result = basicNeuralTest("сорока студентам");
		TestingUtil.checkHas(result, 0, PartOfSpeech.NUMR);
	}
	
	public void test10b() throws FileNotFoundException {
		List<IToken> result = basicNeuralTest("большая сорока прилетела");
		TestingUtil.checkHas(result, 1, PartOfSpeech.NOUN);
	}

	private List<IToken> basicNeuralTest(String str) throws FileNotFoundException {
		MorphologicParser neuralParser = new NeuralParser();
		neuralParser.addTokenFilter(new AdditionalPartsPresetFilter());
		neuralParser.addTokenFilter(new AbbreviationsFilter());
		return neuralParser.process(TestingUtil.getWordFormTokens(str));
	}
	


}
