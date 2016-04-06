package com.onpositive.text.analisys.tests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.onpositive.semantic.wordnet.GrammarRelation;
import com.onpositive.semantic.wordnet.Grammem.PartOfSpeech;
import com.onpositive.semantic.wordnet.WordNetProvider;
import com.onpositive.text.analisys.tests.euristics.EuristicTestingUtil;
import com.onpositive.text.analisys.tests.util.TestingUtil;
import com.onpositive.text.analisys.tools.TokenTypeResolver;
import com.onpositive.text.analysis.Euristic;
import com.onpositive.text.analysis.IToken;
import com.onpositive.text.analysis.MorphologicParser;
import com.onpositive.text.analysis.lexic.PrimitiveTokenizer;
import com.onpositive.text.analysis.lexic.WordFormParser;
import com.onpositive.text.analysis.rules.RuleSet;

import junit.framework.TestCase;

public class WordFormParserTest extends TestCase{

	private static final double E = 0.0001;

	public void testWordFormParser() {
		
//		String str = "Сработал автоматический определитель номера. Чудовище село на ковёр-самолёт и полетело.";		
//		String str = "Сработал электрический детонатор. Чудовище село на ковёр-самолёт и полетело.";
//		GrammarRelation[] possibleGrammarForms = instance.getPossibleGrammarForms("автоматический");
		String str = "меж тем";
		List<IToken> processed = TestingUtil.getWordFormTokens(str);
		
		System.out.println("----------------------------------------------------------------------------------------");
		for(IToken t : processed){
			System.out.println(t.getStartPosition() + "-" + t.getEndPosition() + " " + TokenTypeResolver.getResolvedType(t) + " " + t.getStringValue());
		}
		
		List<List<IToken>> possibleChains = calcVariants(processed);
		
		System.out.println("----------------------------------------------------------------------------------------");
		List<Euristic> euristics = Euristic.match(WordFormParser.class);
		for (Euristic euristic : euristics) {
			for (List<IToken> list : possibleChains) {
				boolean match = euristic.match(list.toArray(new IToken[0]));
				if (match) {
					System.out.println(list);
				}
			}
		}
		
		System.out.println();
	}

	
	public void testConflicting() {
		List<Euristic> euristics = new ArrayList<Euristic>();
		Euristic euristic1 = Euristic.concat(
				Euristic.any(PartOfSpeech.INFN),
				Euristic.createConflictChecker(PartOfSpeech.VERB, PartOfSpeech.NOUN)
				);
		euristics.add(euristic1);
		Euristic matched = matched(euristics, "налить белил");
		assertNotNull(matched);
	}
	
	
	
	public void testAnalyzer() {
		List<Euristic> euristics = new ArrayList<Euristic>();
		Euristic euristic1 = Euristic.concat(
				Euristic.any(PartOfSpeech.INFN),
				Euristic.createConflictChecker(PartOfSpeech.NOUN, PartOfSpeech.VERB)
				);
		euristics.add(euristic1);
		MorphologicParser euristicAnalyzingParser = TestingUtil.configureDefaultAnalyzer(euristics);
		List<IToken> processed = euristicAnalyzingParser.process(TestingUtil.getWordFormTokens("отдала мою"));
		assertNotNull(processed);
		System.out.println("//========================================Результат разбора==================================================");
		for(IToken t : processed){
			System.out.print(t.getStartPosition() + "-" + t.getEndPosition() + " " + TokenTypeResolver.getResolvedType(t) + " " + t.getStringValue()+ " ");
		}
		System.out.println();
	}
	
	public void testAnalyzer1() {
//		List<IToken> processed = euristicAnalyzingParser.process(getWordFormTokens("Он был монтером Ваней, но в духе парижан себе присвоил звание электротехник Жан"));
//		List<IToken> processed = euristicAnalyzingParser.process(getWordFormTokens("Влияет ли обилие белил на сохранность полотен")); сет №25, правило №9
//		List<IToken> processed = euristicAnalyzingParser.process(getWordFormTokens("отдала мою душу")); сет №6, правило №5
//		List<IToken> processed = euristicAnalyzingParser.process(getWordFormTokens("закинул он блесну")); - "блесну" - глагол, сет №25, правило №6
//		List<IToken> processed = euristicAnalyzingParser.process(getWordFormTokens("при головной боли")); - не разбирается, сет №25, правило №2
//		List<IToken> processed = euristicAnalyzingParser.process(getWordFormTokens("на лице гримаса боли")); - не разбирается, сет №25, правило №9
//		List<IToken> processed = euristicAnalyzingParser.process(getWordFormTokens("они обследовали все заводи острова")); - не разбирается, сет №25, правило №6
//		List<IToken> processed = euristicAnalyzingParser.process(getWordFormTokens("необходимость вести борьбу")); "вести борьбу" воспринимает как одно целое, сет №7, правило №7
//		List<IToken> processed = euristicAnalyzingParser.process(getWordFormTokens("ребенок должен учиться вести себя прилично")); - не разбирается, сет №7, правило №6
//		String str = "отдала мою душу";
		String str = "шестьдесят минут прошло";
//		String str = "в обеденный перерыв он приходил домой";
		doBasicAnalyzerTest(str, RuleSet.getFullRulesList());			
	}
	
//	public void testAnalyzer2() {
//		String str ="Что так поздно пришёл";
//		List<Euristic> euristics = RuleSet.getFullRulesList();
//		euristics.addAll(RuleSet.getRulesList30());
//		euristics.addAll(RuleSet.getRulesList31());
//		euristics.addAll(RuleSet.getRulesList32());
//		euristics.addAll(RuleSet.getRulesList33());	
//		euristics.addAll(RuleSet.getRulesList34());
//		euristics.addAll(RuleSet.getRulesList35());
//		EuristicAnalyzingParser euristicAnalyzingParser = TestingUtil.configureDefaultAnalyzer(euristics);
//		euristicAnalyzingParser.process(TestingUtil.getWordFormTokens(str));
//		List<List<IToken>> possibleChains = euristicAnalyzingParser.getPossibleChains();
//		printProcessingResult(str, possibleChains);		
//	}
	
	public void testAnalyzer3() {
		String str = "употреблять белила аккуратно и экономно";
		doBasicAnalyzerTest(str, RuleSet.getFullRulesList());		
	}

	public void testAnalyzer4() {
		doBasicAnalyzerTest("Не умеешь петь - не пей", RuleSet.getFullRulesList());				
	}

	private void doBasicAnalyzerTest(String str, List<Euristic> euristics) {
		MorphologicParser euristicAnalyzingParser = TestingUtil.configureDefaultAnalyzer(euristics);
		List<IToken> processed = euristicAnalyzingParser.process(TestingUtil.getWordFormTokens(str));
		printChain(str, processed);
		matched(RuleSet.getFullRulesList(), str);
	}
	
	
	public void test00() {
		List<Euristic> euristics = new ArrayList<Euristic>();
		euristics.add(Euristic.concat(
				Euristic.any(PartOfSpeech.NOUN),
				Euristic.any(PartOfSpeech.VERB)
				));
		euristics.add(Euristic.concat(
				Euristic.any(PartOfSpeech.VERB),
				Euristic.any(PartOfSpeech.INFN)
				));
		Euristic matched = matched(euristics, "грузовик поехал");
		assertNotNull(matched);
	}
	
	public void test01() {
		List<Euristic> euristics = new ArrayList<Euristic>();
		Euristic euristicNounVerb = Euristic.concat(
				Euristic.any(PartOfSpeech.NOUN),
				Euristic.any(PartOfSpeech.VERB)
				);
		Euristic euristicVerbInfn = Euristic.concat(
				Euristic.any(PartOfSpeech.VERB),
				Euristic.any(PartOfSpeech.INFN)
				);
		
		euristics.add(euristicNounVerb);
		euristics.add(euristicVerbInfn);
		Euristic matched = matched(euristics, "грузовик поехал");
		assertEquals(matched, euristicNounVerb);
	}
	

	
	// глагол - местоимение
	public void test06() {
		
		Euristic matched = matched(RuleSet.getRulesList6(), "тщательно мой");
		assertNotNull(matched);
	}
	
	// инфинитив - существительное
	public void test07() {
		Euristic matched = matched(RuleSet.getRulesList7(), "без вести");
		assertNotNull(matched);
	}
	
	//краткое прилагательное - существительное
	public void test08() {
		Euristic matched = matched(RuleSet.getRulesList8(), "его вещи");
		assertNotNull(matched);
	}
	
	// прилательное - существительное
	public void test09() {
		Euristic matched = matched(RuleSet.getRulesList9(), "сильный красный");
		assertNotNull(matched);
	}	
	
	// сравнительная степень прилагательного - существительное
	public void test10() {
		Euristic matched = matched(RuleSet.getRulesList10(), "этой жиже");
		assertNotNull(matched);
	}
		
	// деепричастие - прилагательное
	public void test11() {
		GrammarRelation[] ddd = WordNetProvider.getInstance().getPossibleGrammarForms("витая");
		System.out.println(Arrays.toString(ddd));
		Euristic matched = matched(RuleSet.getRulesList11(), "ложечка витая");
		assertNotNull(matched);
	}
		
	// глагол - прилагательное
	public void test12() {
		Euristic matched = matched(RuleSet.getRulesList12(), "весело праздную");
		assertNotNull(matched);
	}
		
	// глагол - краткое прилагательное
	public void test13() {
		Euristic matched = matched(RuleSet.getRulesList13(), "солнце росло");
		assertNotNull(matched);
	}
		
	// глагол - наречие	
	public void test14() {
		Euristic matched = matched(RuleSet.getRulesList14(), "показал издали");
		assertNotNull(matched);
	}
	
	// глагол - союз	
	public void test15() {
		Euristic matched = matched(RuleSet.getRulesList15(), "коли уж");
		assertNotNull(matched);
	}
		
	// существительное - наречие
	public void test16() {
		Euristic matched = matched(RuleSet.getRulesList16(), "забывая порой");
		assertNotNull(matched);
	}
		
	// предикат - предлог	
	public void test17() {
		Euristic matched = matched(RuleSet.getRulesList17(), "надо мною");
		assertNotNull(matched);
	}
	
	// существительное - числительное "семью"
	public void test18() {
		Euristic matched = matched(RuleSet.getRulesList18(), "создаст семью");
		assertNotNull(matched);
	}
	
	// существительное - числительное "сорока"
	public void test19() {
		Euristic matched = matched(RuleSet.getRulesList19(), "лет сорока");
		assertNotNull(matched);
	}
	
	// существительное - числительное "сот"
//	public void test20() {
//		Euristic matched = matched(RuleSet.getRulesList20(), "из сот");
//		assertNotNull(matched);
//	}
	
	// существительное - числительное "шестом"
	public void test21() {
		Euristic matched = matched(RuleSet.getRulesList21(), "в шестом");
		assertNotNull(matched);
	}
	
	// глагол - числительное "три"
	public void test22() {
		Euristic matched = matched(RuleSet.getRulesList22(), "три тщательно"); //Ломается, проблема - падеж рд1 вместо просто рд
		assertNotNull(matched);
	}
	
	// прилагательное - наречие
	public void test23() {
		Euristic matched = matched(RuleSet.getRulesList23(), "горько плача");
		assertNotNull(matched);
	}
	
	// существительное - деепричастие
	public void test24() {
		Euristic matched = matched(RuleSet.getRulesList24(), "воя надрывно");
		assertNotNull(matched);
	}
	
	// существительное - глагол
	public void test25() {
		Euristic matched = matched(RuleSet.getRulesList25(), "я баню");
		assertNotNull(matched);
	}
	
	// существительное - местоимение "кому"
	public void test26() {
		Euristic matched = matched(RuleSet.getRulesList26(), "к кому");
		assertNotNull(matched);
	}
	
	// существительное - местоимение "тем"
	public void test27() {
		Euristic matched = matched(RuleSet.getRulesList27(), "к тем кто");
		assertNotNull(matched);
	}
	
	// существительное - местоимение "том"
	public void test28() {
		Euristic matched = matched(RuleSet.getRulesList28(), "о том");
		assertNotNull(matched);
	}
			
	// существительное - местоимение "тому"
	public void test29() {
		Euristic matched = matched(RuleSet.getRulesList29(), "тому подобное");
		assertNotNull(matched);
	}
		
	// предлог - деепричастие "для"
//	public void test30() {
//		Euristic matched = matched(RuleSet.getRulesList30(), "для лису");
//		assertNotNull(matched);
//	}
	
	// предлог - глагол "при"
//	public void test31() {
//		Euristic matched = matched(RuleSet.getRulesList31(), "при лису");
//		assertNotNull(matched);
//	}
	
	// глагол - компаратив
	public void test32() {
		Euristic matched = matched(RuleSet.getRulesList32(), "светлей быстро");
		assertNotNull(matched);
	}
	
	// предлог - наречие "кругом"
	public void test33() {
		Euristic matched = matched(RuleSet.getRulesList33(), "кругом природа");
		assertNotNull(matched);
	}

	// частица - глагол "было"
	public void test34() {
		Euristic matched = matched(RuleSet.getRulesList34(), "было винить");
		assertNotNull(matched);
	}
	
	// союз - деепричастие ("хотя")
	public void test35() {
		Euristic matched = matched(RuleSet.getRulesList35(), "хотя пошел");
		assertNotNull(matched);
	}
	
	public void test36() {
		Euristic matched = matched(RuleSet.getRulesList25(), "обилие белил");
		assertNotNull(matched);
	}
	
	public void test37() {
		Euristic matched = matched(RuleSet.getRulesList14(), "еду домой");
		assertNotNull(matched);
	}
	
	public void test38() {
		Euristic matched = matched(RuleSet.getRulesList14(), "в определённом");
		assertNotNull(matched);
	}
	
	public void test39() {
		String str = "шестьдесят минут прошло";
		Collection<List<IToken>> matched = getAllMatched(RuleSet.getFullRulesList(), str);
		printProcessingResult(str, matched);
		assertTrue(matched.size() > 0);
		MorphologicParser euristicAnalyzingParser = TestingUtil.configureDefaultAnalyzer(RuleSet.getFullRulesList());
		List<IToken> processed = euristicAnalyzingParser.process(TestingUtil.getWordFormTokens(str));
		assertNotNull(processed);
		printChain(str, processed);
	}
	
	public void test40() {
		String str = "шестьдесят минут прошло";
		EuristicTestingUtil util = new EuristicTestingUtil(RuleSet.getFullRulesList());
		List<IToken> processed = util.process(str);
		assertTrue(!util.getMatchedEuristics().isEmpty());
		printChain(str, processed);
		util.printConflictingEuristics();
	}
	
	public void test41() {
		String str = "на скамейке";
		EuristicTestingUtil util = new EuristicTestingUtil(RuleSet.getFullRulesList());
		List<IToken> processed = util.process(str);
		printChain(str, processed);
		util.printConflictingEuristics();
		assertTrue(!util.getMatchedEuristics().isEmpty());
	}
	
	@Test
	public void test42() throws Exception {
		String str = "Но любая задача";
		EuristicTestingUtil util = new EuristicTestingUtil(RuleSet.getFullRulesList());
		List<IToken> processed = util.process(str);
		printChain(str, processed);
		util.printConflictingEuristics();
		assertTrue(!util.getMatchedEuristics().isEmpty());
	}
	
	public void test43() {
		List<Euristic> euristics = new ArrayList<Euristic>();
		euristics.add(Euristic.concat(
				Euristic.any(PartOfSpeech.ADJF),
				Euristic.createConflictChecker(PartOfSpeech.NOUN, PartOfSpeech.VERB)
				));
		Euristic matched = matched(euristics, "первой части пресс-конференции");
				assertNotNull(matched);
	}
	
	public void test44() {
		List<Euristic> euristics = new ArrayList<Euristic>();
		euristics.add(Euristic.concat(
				Euristic.or(Euristic.word("на", PartOfSpeech.PREP), Euristic.word("о", PartOfSpeech.PREP), Euristic.word("в", PartOfSpeech.PREP)), 
				Euristic.createConflictChecker(PartOfSpeech.ADJF, PartOfSpeech.NOUN)
				));
		Euristic matched = matched(euristics, "на пятой пресс-конференции");
				assertNotNull(matched);
	}
	
	public void test45() {
		String str = "к сегодняшней встрече";
		Euristic matched = matched(RuleSet.getRulesList36(), str);
		assertNotNull(matched);
		
		MorphologicParser euristicAnalyzingParser = TestingUtil.configureDefaultAnalyzer(RuleSet.getRulesList36());
		List<IToken> processed = euristicAnalyzingParser.process(TestingUtil.getWordFormTokens(str));
		printChain(str, processed);
	}
	
	public void test46() {
		List<Euristic> euristics = new ArrayList<Euristic>();
		euristics.add(Euristic.concat(
				Euristic.createConflictChecker(PartOfSpeech.ADVB, PartOfSpeech.ADJS),
				Euristic.any(PartOfSpeech.VERB)
				));
		String str = "президент серьёзно встретил";
		
		MorphologicParser euristicAnalyzingParser = TestingUtil.configureDefaultAnalyzer(euristics);
		List<IToken> processed = euristicAnalyzingParser.process(TestingUtil.getWordFormTokens(str));
		printChain(str, processed);
		
		Euristic matched = matched(euristics, str);
		assertNotNull(matched); //без первого слова срабатывает, а так ломается
	}
	
	public void test47() {
		Euristic matched = matched(RuleSet.getRulesList9(), "конце прошлого года");
		assertNotNull(matched); //без первого слова срабатывает, а так ломается
	}
	
	public void test48() {
		Euristic matched = matched(RuleSet.getRulesList23(), "рост значительно выросли");
		assertNotNull(matched); //без первого слова срабатывает, а так ломается
	}
	
	public void test49() {
		Euristic matched = matched(RuleSet.getRulesList23(), "мы регулярно работаем");
		assertNotNull(matched); //без первого слова срабатывает, а так ломается
	}
	
	public void test50() {
		Euristic matched = matched(RuleSet.getRulesList36(), "кафедра всеобщей истории");
		assertNotNull(matched); //без первого слова срабатывает, а так ломается
	}
	
	public void test51() {
		Euristic matched = matched(RuleSet.getRulesList9(), "мир Средние века");
		assertNotNull(matched); //без первого слова срабатывает, а так ломается
	}
	
	public void test52() {
		Euristic matched = matched(RuleSet.getRulesList22(), "Ордер три ордера");
		assertNotNull(matched);
	}
	
	public void test53() {
		Euristic matched = matched(RuleSet.getRulesList25(), "с начала года");
		assertNotNull(matched);
	}
	
	public void test54() {
		Euristic matched = matched(RuleSet.getFullRulesList(), "выслушав его просьбу");
		assertNotNull(matched);
	}
	
	public void test55() {
		Euristic matched = matched(RuleSet.getRulesList9(), "и учёные находившиеся");
		assertNotNull(matched);
	}
	
	public void test56() {
		Euristic matched = matched(RuleSet.getRulesList25(), "соблюдение правил оказывают");
		assertNotNull(matched);
	} //не находятся правила в процентиках
		
	private void printProcessingResult(String str, Collection<List<IToken>> possibleChains) {
		System.out.println("//============Результаты разбора, строка '" + str +  "' ==================================================");
		if (possibleChains == null || possibleChains.isEmpty()) {
			System.out.println("Результатов не найдено");
			return;
		}
		for(List<IToken> chain : possibleChains){
			for (IToken token : chain) {
				System.out.print(token.getStartPosition() + "-" + token.getEndPosition() + " " + TokenTypeResolver.getResolvedType(token) + " " + token.getStringValue()+ " ");
			}
			System.out.println();
		}
	}
	
	private Collection<List<IToken>> getAllMatched(List<Euristic> euristicsToTry, String testString) {
		PrimitiveTokenizer pt = new PrimitiveTokenizer();
		WordFormParser wordFormParser = new WordFormParser(WordNetProvider.getInstance());
		List<IToken> tokens = pt.tokenize(testString);		
		List<IToken> processed = wordFormParser.process(tokens);
		List<List<IToken>> possibleChains = calcVariants(processed);
		return getAllMatched(euristicsToTry, possibleChains);
	}

	private Collection<List<IToken>> getAllMatched(
			List<Euristic> euristicsToTry, List<List<IToken>> possibleChains) {
		Set<List<IToken>> allMatched = new HashSet<List<IToken>>();
		for (Euristic euristic : euristicsToTry) {
			for (List<IToken> list : possibleChains) {
				if (euristic.match(list.toArray(new IToken[0]))) {
					allMatched.add(list);
				}
			}
		}
		return allMatched;
	}

	private Euristic matched(List<Euristic> euristicsToTry, String testString) {
		PrimitiveTokenizer pt = new PrimitiveTokenizer();
		WordFormParser wordFormParser = new WordFormParser(WordNetProvider.getInstance());
		List<IToken> tokens = pt.tokenize(testString);		
		List<IToken> processed = wordFormParser.process(tokens);
		List<List<IToken>> possibleChains = calcVariants(processed);
		System.out.println("-------------------------------------------------------------------------------------------------------");
		if (possibleChains.size() > 1) {
			System.out.println("Строка '" + testString + "' Варианты разбора:");
		}
		for (List<IToken> list : possibleChains) {
			for(IToken t : list){
				System.out.print(t.getStartPosition() + "-" + t.getEndPosition() + " " + TokenTypeResolver.getResolvedType(t) + " " + t.getStringValue()+ " ");
			}
			System.out.println();
		}
		return matched(euristicsToTry, possibleChains);
	}
	
	private Euristic matched(List<Euristic> euristicsToTry, List<List<IToken>> possibleChains) {
		for (Euristic euristic : euristicsToTry) {
			for (List<IToken> list : possibleChains) {
				boolean match = euristic.match(list.toArray(new IToken[0]));
				if (match) {
					return euristic;
				}
			}
		}
		return null;
	}

	private List<List<IToken>> calcVariants(List<IToken> processed) {
		List<IToken> workingCopy = new ArrayList<IToken>(processed);
		List<List<IToken>> result = new ArrayList<List<IToken>>();
		result.add(new ArrayList<IToken>());
		for (int i = 0; i < workingCopy.size(); i++) {
			if (workingCopy.get(i).getConflicts() == null || workingCopy.get(i).getConflicts().isEmpty()) {
				addItem(result, workingCopy.get(i));
			} else {
				List<IToken> conflicts = new ArrayList<IToken>(workingCopy.get(i).getConflicts());
				conflicts.add(0, workingCopy.get(i));
				while (i+1 < workingCopy.size() && conflicts.contains(workingCopy.get(i+1))) {
					workingCopy.remove(i+1);
				}
				result = generateVariants(result,conflicts);
			}
		}
		return result;
	}

	private List<List<IToken>> generateVariants(List<List<IToken>> prevResult, List<IToken> conflicts) {
		List<List<IToken>> result = new ArrayList<List<IToken>>();
		for (List<IToken> list : prevResult) {
			for (IToken curToken : conflicts) {
				List<IToken> newList = new ArrayList<IToken>(list);
				newList.add(curToken);
				result.add(newList);
			}
		}
		return result;
	}

	private void addItem(List<List<IToken>> result, IToken token) {
		for (List<IToken> list : result) {
			list.add(token);
		}
	}
	
	private void printChain(String str, List<IToken> chain) {
		System.out.println("//============Результаты разбора, строка '" + str +  "' ==================================================");
		for (IToken token : chain) {
			if (token.hasConflicts() && Math.abs(token.getCorrelation()) < E) {
				boolean matchedAny = false;
				List<IToken> conflicts = token.getConflicts();
				for (IToken conflictToken : conflicts) {
					if (conflictToken.getCorrelation() > E) {
						matchedAny = true;
						break;
					}
				}
				if (!matchedAny) {
					System.out.println("Результатов не найдено");
					return;
				}
			}
			
		}
		for (int i = 0; i < chain.size(); i++) {
			IToken token = chain.get(i);
			if (hasConflicts(token)) {
				System.out.print(" Конфликт[ ");
				if (token.getCorrelation() > E) {
					printToken(token);
				}
				List<IToken> conflicts = token.getConflicts();
				conflicts.stream().filter(conflictToken -> conflictToken.getCorrelation() > E).forEach(curToken -> {
					System.out.print(" "); printToken(curToken);
				});
				System.out.print("]");
			} else {
				printToken(getValidToken(token));
			}
			i += getConflictingCount(token);
		}
		System.out.println();
	}

	protected int getConflictingCount(IToken token) {
		if (token.getConflicts() == null) {
			return 0;
		}
		return token.getConflicts().size();
	}

	private IToken getValidToken(IToken token) {
		if (token.getCorrelation() > E || !token.hasConflicts()) {
			return token;
		}
		List<IToken> conflicts = token.getConflicts();
		for (IToken conflictToken : conflicts) {
			if (conflictToken.getCorrelation() > E) {
				return conflictToken;
			}
		}
		return null;
	}

	private boolean hasConflicts(IToken token) {
		if (!token.hasConflicts())
			return false;
		int initialCount = token.getCorrelation() > E ? 1 : 0;
		List<IToken> conflicts = token.getConflicts();
		long count = conflicts.stream().filter(conflictToken -> conflictToken.getCorrelation() > E).count();
		return initialCount + count > 1;
	}

	protected void printToken(IToken token) {
		System.out.print(token.getStartPosition() + "-" + token.getEndPosition() + " " + TokenTypeResolver.getResolvedType(token) + " " + token.getStringValue()+ " ");
	}
}
