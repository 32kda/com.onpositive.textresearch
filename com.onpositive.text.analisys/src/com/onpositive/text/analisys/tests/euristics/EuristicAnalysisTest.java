package com.onpositive.text.analisys.tests.euristics;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.TreeMap;
import java.util.stream.Collectors;

import junit.framework.TestCase;

import com.onpositive.semantic.wordnet.Grammem;
import com.onpositive.semantic.wordnet.Grammem.PartOfSpeech;
import com.onpositive.text.analisys.tests.ParsedTokensLoader;
import com.onpositive.text.analisys.tests.util.TestingUtil;
import com.onpositive.text.analysis.Euristic;
import com.onpositive.text.analysis.EuristicAnalyzingParser;
import com.onpositive.text.analysis.IToken;
import com.onpositive.text.analysis.MorphologicParser;
import com.onpositive.text.analysis.filtering.AbbreviationsFilter;
import com.onpositive.text.analysis.filtering.AdditionalPartsPresetFilter;
import com.onpositive.text.analysis.lexic.SentenceSplitter;
import com.onpositive.text.analysis.lexic.WordFormToken;
import com.onpositive.text.analysis.neural.NeuralParser;
import com.onpositive.text.analysis.rules.RuleSet;
import com.onpositive.text.analysis.syntax.SyntaxToken;

public class EuristicAnalysisTest extends TestCase{
	
	private static final int MAX_NEUTRALIZATION_LOOKAHEAD = 10;
	private static final double E = 0.001;
	private boolean PRINT_ALL_CONFLICTS = true;
	
	private Map<String, Integer> mistakesMap = new HashMap<String, Integer>();
	
	private Map<String, Pair> wordAnalysisMap = new HashMap<String, Pair>();
	
	private PrintWriter logger;
	
	ArrayList<Double> percents = new ArrayList<Double>();
	
//	public void test01() {
//		testNeuralWithFile("3344.xml");
//		testNeuralWithFile("2176.xml");
//		testNeuralWithFile("2241.xml");
//	}
	
//	public void test02() {
//		testWithFile("1551.xml");
//	}
	
	public void test03() {
		File folder = new File("D:\\tmp\\corpora");
		if (folder.exists() && folder.isDirectory()) {
			try {
				logger = new PrintWriter(Files.newBufferedWriter(Paths.get("D:\\tmp","words.txt")));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			File[] listedFiles = folder.listFiles();
			for (int i = listedFiles.length / 2; i < listedFiles.length; i++) {
				if (listedFiles[i].length() > 100000) {
					testWithFile(listedFiles[i]);
				}
			}
			if (logger != null) {
				logger.close();
			}
			int percentSum = 0;
			for (Double percent : percents) {
				percentSum += Math.round(percent);
			}
			
			System.out.println(String.format("** Суммарно верно снято для %1$,.2f процентов лексем", percentSum * 1.0 / percents.size()));
			
			ValueComparator<String, Integer> comparator = new ValueComparator<String, Integer> (mistakesMap);
		    Map<String, Integer> sortedMap = new TreeMap<String, Integer> (comparator);
		    sortedMap.putAll(mistakesMap);
		    
		    System.out.println("Частые ошибки:");
		    for (String word : sortedMap.keySet()) {
				int count = sortedMap.get(word);
				if (count > 100) {
					System.out.println(word + " - " + count + " ошибок, всего встречено " + wordAnalysisMap.get(word).totalCount);
				} else {
					break;
				}
			}
		}
	}
		
	private void testNeuralWithFile(File file) {
		ParsedTokensLoader loader;
		try {
			loader = createLoader(file);
			testNeural(loader);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	private ParsedTokensLoader createLoader(File file)
			throws FileNotFoundException {
		return new ParsedTokensLoader(new BufferedInputStream(new FileInputStream(file)));
	}
	
	private void testNeuralWithFile(String filename) {
		ParsedTokensLoader loader = new ParsedTokensLoader(EuristicAnalysisTest.class.getResourceAsStream(filename));
		testNeural(loader);
	}

	protected void testNeural(ParsedTokensLoader loader) {
		List<SimplifiedToken> etalonTokens = loader.getTokens();
		String text = loader.getInitialText();
		NeuralParser neuralParser = new NeuralParser();
//		neuralParser.addTokenFilter(new AdditionalPartsPresetFilter());
		neuralParser.addTokenFilter(new AbbreviationsFilter());
		List<IToken> wordTokens = TestingUtil.getWordFormTokens(text);
		neuralParser.process(new SentenceSplitter().split(wordTokens));
		compare(etalonTokens,wordTokens, neuralParser);
		System.out.println("//--------------------------------------------------------------------------------------------------");
	}

	
	private void testWithFile(ParsedTokensLoader loader) {
		
		List<SimplifiedToken> etalonTokens = loader.getTokens();
		String text = loader.getInitialText();
		MorphologicParser euristicAnalyzingParser = TestingUtil.configureDefaultAnalyzer(createRulesList());
		List<IToken> processed = euristicAnalyzingParser.process(TestingUtil.getWordFormTokens(text));
		compare(etalonTokens,processed, (EuristicAnalyzingParser) euristicAnalyzingParser);
//		System.out.println("//---------------------------------With sentences---------------------------------------------------");
//		List<IToken> tokens1 = TestingUtil.getWordFormTokens(text);
//		List<IToken> sentences = new SentenceSplitter().split(tokens1);
//		processed = euristicAnalyzingParser.process(sentences);
//		compare(etalonTokens,tokens1);
		System.out.println("//--------------------------------------------------------------------------------------------------");
	}
	
	private void testWithFile(File file) {
		ParsedTokensLoader loader;
		try {
			loader = createLoader(file);
			testWithFile(loader);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	private void testWithFile(String filename) {
		ParsedTokensLoader loader = new ParsedTokensLoader(EuristicAnalysisTest.class.getResourceAsStream(filename));
		testWithFile(loader);
	}
	
	
	private List<Euristic> createRulesList() {
		List<Euristic> euristics = new ArrayList<Euristic>();
//		euristics.addAll(RuleSet.getRulesList5());
		euristics.addAll(RuleSet.getRulesList6());
		euristics.addAll(RuleSet.getRulesList7());
		euristics.addAll(RuleSet.getRulesList8());
		euristics.addAll(RuleSet.getRulesList9());
		euristics.addAll(RuleSet.getRulesList10());
		euristics.addAll(RuleSet.getRulesList11());
		euristics.addAll(RuleSet.getRulesList12());
		euristics.addAll(RuleSet.getRulesList13());
		euristics.addAll(RuleSet.getRulesList14());
		euristics.addAll(RuleSet.getRulesList15());
		euristics.addAll(RuleSet.getRulesList16());
		euristics.addAll(RuleSet.getRulesList17());
		euristics.addAll(RuleSet.getRulesList18());
		euristics.addAll(RuleSet.getRulesList19());
		euristics.addAll(RuleSet.getRulesList20());
		euristics.addAll(RuleSet.getRulesList21());
		euristics.addAll(RuleSet.getRulesList22());
		euristics.addAll(RuleSet.getRulesList23());
		euristics.addAll(RuleSet.getRulesList24());
		euristics.addAll(RuleSet.getRulesList25());
		euristics.addAll(RuleSet.getRulesList26());
		euristics.addAll(RuleSet.getRulesList27());
		euristics.addAll(RuleSet.getRulesList28());
		euristics.addAll(RuleSet.getRulesList29());
//		euristics.addAll(RuleSet.getRulesList30());
//		euristics.addAll(RuleSet.getRulesList31());
		euristics.addAll(RuleSet.getRulesList32());
		euristics.addAll(RuleSet.getRulesList33());
		euristics.addAll(RuleSet.getRulesList34());
		euristics.addAll(RuleSet.getRulesList35());
		euristics.addAll(RuleSet.getRulesList36());
		euristics.addAll(RuleSet.getRulesList37());
		euristics.addAll(RuleSet.getRulesList38());
		euristics.addAll(RuleSet.getRulesList39());
		euristics.addAll(RuleSet.getRulesList40());
		euristics.addAll(RuleSet.getRulesList41());
		return euristics;
	}

	@SuppressWarnings("unchecked")
	private void compare(List<SimplifiedToken> etalonTokens, List<IToken> tokens, MorphologicParser parser) {
		EuristicAnalyzingParser euristicParser = parser instanceof EuristicAnalyzingParser?(EuristicAnalyzingParser)parser:null;
		int i = 0;
		int j = 0;
		int filteredByFilters = parser.getFilteredCount();
		ITokenComparator tokenComparator = new PartOfSpeechComparator();
		Map<PartOfSpeech, Integer> comparedCounts = new HashMap<PartOfSpeech, Integer>();
		int conflictingCount = 0;
		int wrongCount = 0;
		long filteredOut = 0;
		while (i < etalonTokens.size() && j < tokens.size()) {
			SimplifiedToken etalonToken = etalonTokens.get(i);
			List<WordFormToken> comparedTokens = new ArrayList<WordFormToken>();
			j = getNextWordTokenIdx(tokens, j, etalonToken);
			if (j == -1) {
				printComparisonResults(comparedCounts, i, conflictingCount, wrongCount, filteredOut, filteredByFilters);
				return;
			}
			WordFormToken wordFormToken = (WordFormToken) tokens.get(j);
			if (!etalonToken.wordEquals(wordFormToken)) {
				i = tryEtalonNeutralization(i, etalonTokens, wordFormToken);
				if (i == -1) {
					printComparisonResults(comparedCounts, i, conflictingCount, wrongCount, filteredOut, filteredByFilters);
					return;
				} else {
					etalonToken = etalonTokens.get(i);
				}
			}
			
			comparedTokens.add(wordFormToken);
			List<IToken> conflicts = wordFormToken.getConflicts();
			if (conflicts != null) {
				comparedTokens.addAll((Collection) conflicts);
				j += conflicts.size();
			}
			if (comparedTokens.size() == 1) {
				boolean wordEquals = etalonToken.wordEquals(comparedTokens.get(0));
				if (!wordEquals) {
					System.out.println("Несовпадение слова: нужно " + etalonToken.getWord() + " найдено " + comparedTokens.get(0).getStringValue());
				}
			} else {
				String word = normalize(wordFormToken.getShortStringValue().toLowerCase().trim());
				if (PRINT_ALL_CONFLICTS) {
					printConflict(etalonTokens, i, word);
				}
				if (comparedTokens.size() > 1) {
					conflictingCount++;
				}			
				handleOmonymicWord(word);
				filteredOut += comparedTokens.stream().filter(token -> token.getCorrelation() <= E).count();
				comparedTokens = comparedTokens.stream().filter(token -> token.getCorrelation() > E).collect(Collectors.toList());
				if (comparedTokens.size() > 1 && !isSamePart(comparedTokens)) {
					wrongCount++;
					StringJoiner joiner = new StringJoiner(", ");
					comparedTokens.stream().forEach(token -> joiner.add(token.toString()));
					System.out.println("Осталось несколько вариантов: " + joiner.toString());
					handleMistake(etalonToken);
					if (euristicParser != null) {
						if (euristicParser.getMatchedEuristic(comparedTokens.get(0)) == null) {
							System.out.println("Правила не найдено");
							handleNoRule();
						} else {
							StringJoiner joiner1 = new StringJoiner(", ");
							comparedTokens.stream().map(token -> euristicParser.getMatchedEuristic(token)).forEach(euristic ->joiner1.add(euristic != null ? euristic.toString():"ОШИБКА")); 
							System.out.println("Правила: " + joiner1.toString());
							handleMultipleRules(joiner1.toString());
						}
					}
				} else if (comparedTokens.size() == 1) {
					WordFormToken token = comparedTokens.get(0);
					boolean wordEquals = etalonToken.wordEquals(token);
					if (!wordEquals) {
						System.out.println("Несовпадение слова: нужно " + etalonToken.getWord() + " найдено " + token.getShortStringValue());
					}
					incrementCount(comparedCounts, token.getPartOfSpeech());
					Collection<Grammem> wrongGrammems = tokenComparator.calculateWrong(etalonToken, token);
					if (!wrongGrammems.isEmpty()) {
						System.out.println("Ошибочное определение для слова " + etalonToken + " во фразе " + getPhrase(etalonTokens, i));
						handleMistake(etalonToken);
						String wrongTxt = "Неверные граммемы: " + wrongGrammems;
						Euristic matchedEuristic = euristicParser.getMatchedEuristic(token);
						if (euristicParser != null && matchedEuristic != null) {
							wrongTxt += " Эвристика: " + matchedEuristic;
							handleWrongRule(matchedEuristic);
						}
						System.out.println(wrongTxt);
						wrongCount++;
					} else {
						handleGoodRule(euristicParser.getMatchedEuristic(token));
					}
				}
			}
			i++;
			j++;
			
		}
		printComparisonResults(comparedCounts, i, conflictingCount, wrongCount, filteredOut, filteredByFilters);
	}

	private void handleGoodRule(Euristic matchedEuristic) {
		if (logger != null && matchedEuristic != null) {
			logger.println("Good:" + matchedEuristic);
		}
	}

	private void handleWrongRule(Euristic matchedEuristic) {
		if (logger != null && matchedEuristic != null) {
			logger.println("Wrong:" + matchedEuristic);
		}
	}

	private void handleMultipleRules(String string) {
		if (logger != null) {
			logger.println("Several:" + string);
		}
	}

	private void handleNoRule() {
		if (logger != null) {
			logger.println("No_rule");
		}
		
	}

	protected void printConflict(List<SimplifiedToken> etalonTokens, int idx,
			String word) {
		String phrase = getPhrase(etalonTokens, idx);
		System.out.println("Неоднозначность: слово " + word + " фраза " + phrase);
		if (logger != null) {
			logger.printf("%s:%s",word,phrase);
			logger.println();
		}
	}

	private String normalize(String word) {
		word = word.replaceAll("\\s","");
		if (word.endsWith(".")) {
			word = word.substring(0, word.length() - 1);
		}
		return word;
	}

	private void handleOmonymicWord(String word) {
		Pair pair = wordAnalysisMap.get(word);
		if (pair == null) {
			pair = new Pair();
			wordAnalysisMap.put(word, pair);
		}
		pair.incTotalCount();
		
	}

	private void handleMistake(SimplifiedToken etalonToken) {
		String word = etalonToken.getWord().toLowerCase().trim();
		Integer count = mistakesMap.get(word);
		if (count == null) {
			count = 1;
		} else {
			count++;
		}
		mistakesMap.put(word, count);
		wordAnalysisMap.get(word).incWrongCount();
	}

	private String getPhrase(List<SimplifiedToken> etalonTokens, int j) {
		StringBuilder builder = new StringBuilder();
		if (j > 0) {
			builder.append(etalonTokens.get(j - 1).getWord());
			builder.append(" ");
		}
		builder.append(etalonTokens.get(j).getWord());
		if (j < etalonTokens.size() - 1) {
			builder.append(" ");
			builder.append(etalonTokens.get(j + 1).getWord());
		}
		return builder.toString();
	}

	private boolean isSamePart(List<WordFormToken> comparedTokens) {
		WordFormToken first = comparedTokens.get(0);
		PartOfSpeech partOfSpeech = first.getPartOfSpeech();
		for (int i = 1; i < comparedTokens.size(); i++) {
			if (comparedTokens.get(i).getPartOfSpeech() != partOfSpeech) {
				return false;
			}
		}
		return true;
	}

	protected void printComparisonResults(Map<PartOfSpeech, Integer> comparedCounts, int comparedCount, int conflictingCount, int wrongCount, long filteredOut, int filteredByFilters) {
		System.out.println("** Всего сравнили " + comparedCount + " лексем **");
		System.out.println("** Из них с омонимией: " + conflictingCount + " лексем **");
		if (conflictingCount > 0) {
			double percent = (conflictingCount - wrongCount) * 100.0 / conflictingCount;
			percents.add(percent);
			System.out.println(String.format("** Верно снято для %1$,.2f процентов лексем **", percent));
			System.out.println("** Исключено: " + filteredOut + " лексем **");
			System.out.println("** Из них фльтром: " + filteredByFilters + "шт.**");
			System.out.println("** Части речи: ");
		}
		comparedCounts.keySet().stream().sorted((part1, part2) -> {return part1.intId - part2.intId;}).forEach( part -> {
			System.out.println("** " + part.description + " = " + comparedCounts.get(part));
		});
		double wordSuccessSum = 0;
		for (Pair pair : wordAnalysisMap.values()) {
			wordSuccessSum += (pair.totalCount - pair.wrongCount) * 1.0 / pair.totalCount;
		}
		if (wordAnalysisMap.size() > 0) {
			double wordSuccessRatio = wordSuccessSum / wordAnalysisMap.size();
			System.out.println(String.format("** Верно снято для %1$,.2f процентов уникальных слов**", wordSuccessRatio * 100.0));
		}
	}

	private void incrementCount(Map<PartOfSpeech, Integer> comparedCounts, PartOfSpeech partOfSpeech) {
		if (partOfSpeech == null) {
			return;
		}
		Integer count = comparedCounts.get(partOfSpeech);
		if (count == null) {
			count = 1;
		} else {
			count++;
		}
		comparedCounts.put(partOfSpeech, count);
	}

	private int tryEtalonNeutralization(int i, List<SimplifiedToken> etalonTokens, SyntaxToken wordFormToken) {
		i++;
		for (int k = i; k < i + MAX_NEUTRALIZATION_LOOKAHEAD && k < etalonTokens.size(); k++) {
			SimplifiedToken curToken = etalonTokens.get(k);
			if (curToken.wordEquals(wordFormToken)) {
				return k;
			}
		}
		return -1;
	}

	protected int getNextWordTokenIdx(List<IToken> tokens, int j, SimplifiedToken etalonToken) {
		j = skipNonWordTokens(tokens, j);
		if (j == -1) {
			return -1;
		}
		int lookahead = 0;
		SyntaxToken token = (SyntaxToken) tokens.get(j);
		while (j > -1 && lookahead < MAX_NEUTRALIZATION_LOOKAHEAD && !etalonToken.wordEquals(token)) {
			j++;
			lookahead++;
			j = skipNonWordTokens(tokens, j);
			if (j == -1) {
				return -1;
			}
			token = (SyntaxToken) tokens.get(j);
		}
		if (lookahead == MAX_NEUTRALIZATION_LOOKAHEAD) {
			return -1;
		}
		return j;
	}

	protected int skipNonWordTokens(List<IToken> tokens, int j) {
		while (j < tokens.size() && !(tokens.get(j) instanceof WordFormToken)) {			
			j++;
		}
		if (j == tokens.size()) {
			return -1;
		}
		return j;
	}
	
}
