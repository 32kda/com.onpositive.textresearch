package com.onpositive.wordnet.tests;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import com.onpositive.semantic.wordnet.WordNetProvider;
import com.onpositive.text.analysis.IToken;
import com.onpositive.text.analysis.lexic.PrimitiveTokenizer;
import com.onpositive.text.analysis.lexic.SentenceSplitter;
import com.onpositive.text.analysis.lexic.WordFormParser;

import junit.framework.TestCase;

public class SentenceSplitterTest extends TestCase {
	
	@Test
	public void test01() throws Exception {
		String line = "Ан-2 (по кодификации НАТО: Colt — «Жеребёнок», разг. — «Аннушка», «Кукурузник» (получил в наследство от По-2)) — советский лёгкий многоцелевой самолёт.";
		final List<IToken> process = tokens(line);
		final List<IToken> split = new SentenceSplitter().split(process);
		assertEquals(split.size(), 1);
	}
	
	@Test
	public void test02() throws Exception {
		String line = "Всего было построено более 18 тыс. Ан-2.";
		final List<IToken> process = tokens(line);
		final List<IToken> split = new SentenceSplitter().split(process);
		assertEquals(split.size(), 1);
	}
	
	@Test
	public void test03() throws Exception {
		String line = "Оборудован двигателем АШ-62ИР конструкции А. Д. Швецова.";
		final List<IToken> process = tokens(line);
		final List<IToken> split = new SentenceSplitter().split(process);
		assertEquals(split.size(), 1);
	}
	
	public void test04() throws Exception {
		String line = "Ан-2 используется как сельскохозяйственный, спортивный, транспортный, пассажирский самолёт и состоит на вооружении ВВС многих стран. Многие самолёты летают более 40 лет и налёт некоторых из них достигает 20 тыс. часов";
		final List<IToken> process = tokens(line);
		final List<IToken> split = new SentenceSplitter().split(process);
		assertEquals(split.size(), 2);
	}

	
	private List<IToken> tokens(String line) {
		line = line.replace("" + (char) 769, "");
		line = line.replace("" + (char) 160, "");
		final List<IToken> tokenize = new PrimitiveTokenizer().tokenize(line);
		final WordFormParser wordFormParser = new WordFormParser(WordNetProvider.getInstance());
		wordFormParser.setIgnoreCombinations(true);
		final List<IToken> process = wordFormParser.process(tokenize);
		return process;
	}
}
