package com.onpositive.text.analisys.tests.euristics;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;

import junit.framework.TestCase;

import org.junit.Test;

import com.onpositive.semantic.wordnet.Grammem.PartOfSpeech;
import com.onpositive.text.analisys.tests.BasicParsedTokensLoader;
import com.onpositive.text.analisys.tests.CorporaParsedTokensLoader;
import com.onpositive.text.analisys.tests.ParsedTokensLoader;
import com.onpositive.text.analysis.TokenRegistry;
import com.onpositive.text.analysis.utils.Stats;
import com.onpositive.text.morphology.Result;
import com.onpositive.text.morphology.dataset.prepare.DemoEstimationData;
import com.onpositive.text.morphology.dataset.prepare.DemoEstimationData.EstimatedToken;

public class EstimatorTest extends TestCase {
	
	//private static final String CORPORA_PATH = "C:\\WORK\\science\\annot.opcorpora.no_ambig.xml";
	private static final String CORPORA_PATH = "C:\\WORK\\science\\corpora_1M\\texts";
	private static final int FILE_SIZE_THRESHOLD = 10000;	
	private class ComparisonResult {
		int total;
		int totalUnAmbig;
		
		void incTotal() {
			total++;
		}
		
		void incTotalUnAmbig() {
			totalUnAmbig++;
		}
	}
	
	private ComparisonResult neuralResult = new ComparisonResult();
	
	private ComparisonResult heuristicResult = new ComparisonResult();
	
	private int comparedCount = 0;
	
	private int wrongCount = 0;
	
//	private PairsEstimator pairsEstimator = new PairsEstimator();
	
	//F-мера
	private int tp;
	private int fp;
	private int fn;
	
	private int ambigEtalon = 0;
	private int vocabFailCount = 0;
	private int totalPartsCount = 0;
	private int filteredPartsCount = 0;	
//	private int pairsResolvedCnt = 0;
	
//	private int pairsWrong = 0;
	
	private int rightUnAmbigCount = 0;
	private int vocabUnAmbigCount = 0;
	
	private Map<OmonimyPair, Pair> omonimyStat = new HashMap<OmonimyPair, Pair>();
	
	private Map<String, Pair> wordAnalysisMap = new HashMap<String, Pair>();

	private int totalWordCount = 0;
	
	private List<Double> trueNeuralScores = new ArrayList<>();
	private List<Double> trueNeuralDiffs = new ArrayList<>();
	private List<Double> falseTrueNeuralScores = new ArrayList<>();
	private List<Double> falseFalseNeuralScores = new ArrayList<>();
	@Test
	public void test00() throws Exception {
		File folder = new File(CORPORA_PATH);
		if (folder.exists() && folder.isDirectory()) {
			File[] listedFiles = folder.listFiles();
			listedFiles = Arrays.stream(listedFiles).limit(listedFiles.length / 2).filter(file -> file.length() > 50000).toArray(size -> new File[size]);
			
			doTestForFiles(listedFiles);
		    
		} else {
			System.err.println("Папка не найдена: " + folder.getAbsolutePath());
		}
		
	}
	
	/*@Test
	public void test01() throws Exception {
		doTestForFile(new File(CORPORA_PATH + "\\annot.opcorpora.no_ambig.xml"));
	}*/

	private void doTestForFile(File file) {
		testEstimatorWithFile(file);
		calculateTestingResults();
	}

	private void doTestForFiles(File[] listedFiles) {
		for (File file : listedFiles) {
			testEstimatorWithFile(file);
		}
		calculateTestingResults();
	}

	private void calculateTestingResults() {
		double rightRatio = (comparedCount - wrongCount) * 100.0 / comparedCount;
		double fullRightRatio = (vocabUnAmbigCount + comparedCount - wrongCount) * 100.0 / (vocabUnAmbigCount + vocabFailCount + comparedCount);
		
		double omonimyRatio = totalPartsCount * 100.0 / totalWordCount;
		double filteredOmonimyRatio = filteredPartsCount * 100.0 / totalWordCount;
		System.out.println("** Размер тестового набора " + totalWordCount + " слов");
		
		System.out.println("** Из него c омонимией " + comparedCount + " слов");
		System.out.println(String.format("** Омонимия верно снята для %1$,.2f %% проанализированных слов**", rightRatio));
		System.out.println(String.format("** Всего (словарь + снятие омонимии) верно тегировано %1$,.2f %% проанализированных слов**", fullRightRatio));
				
		System.out.println(String.format("** Изначальная частеречная омонимия %1$,.2f %% **", omonimyRatio));
		System.out.println(String.format("** Частеречная омонимия, после обработки %1$,.2f %% **", filteredOmonimyRatio));
		double precision = tp * 1.0 / (tp + fp);
		double recall = tp * 1.0 / (tp + fn);
		System.out.println(String.format("** Точность %1$,.2f полнота %1$,.2f**", precision, recall));
		System.out.println("Неоднозначность эталона - " + ambigEtalon + " случаев");
		System.out.println(String.format("** Однозначно снято для %1$,.2f %% проанализированных слов**", rightUnAmbigCount * 100.0 / comparedCount));
		double wordSuccessSum = 0;
		for (Pair pair : wordAnalysisMap.values()) {
			wordSuccessSum += (pair.totalCount - pair.wrongCount) * 1.0 / pair.totalCount;
		}
		if (wordAnalysisMap.size() > 0) {
			double wordSuccessRatio = wordSuccessSum / wordAnalysisMap.size();
			System.out.println(String.format("** Верно снято для %1$,.2f %% уникальных слов**", wordSuccessRatio * 100.0));
		}
		
		
		System.out.println("//---------------------------По методам:--------------------------------------------");
		System.out.println(Stats.getInfo());
		
		System.out.println(String.format("** Детерминированный: %1$,.2f всего, %2$,.2f однозначно**", (heuristicResult.total * 100.0 / comparedCount), (heuristicResult.totalUnAmbig * 100.0 / comparedCount)));
		System.out.println(String.format("** Нейросети: %1$,.2f всего, %2$,.2f однозначно**", (neuralResult.total * 100.0 / comparedCount), (neuralResult.totalUnAmbig * 100.0 / comparedCount)));
		
		System.out.println("//---------------------------Нейросети:--------------------------------------------");
		Collections.sort(trueNeuralScores);
		Collections.sort(trueNeuralDiffs);
		Collections.sort(falseTrueNeuralScores);
		Collections.sort(falseFalseNeuralScores);
		System.out.println("** Верные, 70%: [" + trueNeuralScores.get((int) Math.round(0.3 * trueNeuralScores.size())) + ",1.0] **"); 
		int i = 0;
		while (trueNeuralDiffs.get(i) == 0) i++;
		System.out.println(String.format("** Верные с максимальной оценкой: %1$,.2f **", (i * 100.0 / trueNeuralDiffs.size())));
		
		int falseTrueStart = (int) Math.round(falseTrueNeuralScores.size() * 0.15);
		int falseTrueEnd = (int) Math.round(falseTrueNeuralScores.size() * 0.85);
		System.out.println(String.format("** Ошибочно верные, сердцевина (70%%): [%1$,.2f , %2$,.2f]**", falseTrueNeuralScores.get(falseTrueStart).doubleValue(), falseTrueNeuralScores.get(falseTrueEnd).doubleValue()));
		
		int falseFalseStart = (int) Math.round(falseFalseNeuralScores.size() * 0.15);
		int falseFalseEnd = (int) Math.round(falseFalseNeuralScores.size() * 0.85);
		System.out.println(String.format("** Ошибочно ложные, сердцевина (70%%): [%1$,.2f , %2$,.2f]**", falseFalseNeuralScores.get(falseFalseStart).doubleValue(), falseFalseNeuralScores.get(falseFalseEnd).doubleValue()));
		
		ValueComparator<OmonimyPair, Pair> comparator = new ValueComparator<OmonimyPair, Pair> (omonimyStat);
		Map<OmonimyPair, Pair> sortedMap = new TreeMap<OmonimyPair, Pair> (comparator);
		sortedMap.putAll(omonimyStat);
		
		System.out.println("//---------------------------Типы омонимии:------------------------------");
		for (OmonimyPair pair : sortedMap.keySet()) {
			Pair counts = omonimyStat.get(pair);
			System.out.printf("%1$s : всего %2$d раз, верно %3$,.2f %% ", pair.toString(), counts.getTotalCount(), (counts.getTotalCount() - counts.getWrongCount()) * 100.0 / counts.getTotalCount());
		}
		System.out.println("//---------------------------Слова:--------------------------------------");
		ValueComparator<String, Pair> wordStatComparator = new ValueComparator<String, Pair>(wordAnalysisMap) {
			@Override
			public int compare(String o1, String o2) {
				Pair p1 = map.get(o1);
				Pair p2 = map.get(o2);
				return p2.totalCount - p1.totalCount;
			}
		}; 
		Map<String, Pair> sortedWordMap = new TreeMap<> (wordStatComparator);
		sortedWordMap.putAll(wordAnalysisMap);
		for (String pair : sortedWordMap.keySet()) {
			Pair counts = wordAnalysisMap.get(pair);
			double r1 = (counts.totalCount - counts.wrongCount) * 100.0 / counts.totalCount;
			System.out.printf("Слово %1$s : всего %2$d раз, верно %3$,.2f %% ", pair.toString(), counts.getTotalCount(), r1);
		}
		System.out.println("//---------------------------Точность и полнота по методам:--------------------------------------");
		precision = tpCountN * 1.0 / (tpCountN + fpCountN);
		recall = tpCountN * 1.0 / (tpCountN + fnCountN);
		System.out.println(String.format("** Нейронные сети. Точность %1$,.2f полнота %1$,.2f**", precision, recall));
		precision = tpCountR * 1.0 / (tpCountR + fpCountR);
		recall = tpCountR * 1.0 / (tpCountR + fnCountR);
		System.out.println(String.format("** Правила. Точность %1$,.2f полнота %1$,.2f**", precision, recall));
		System.out.println("//---------------------------Дополнительные метрики:--------------------------------------");
		double accuracy = (totalWordCount - missingEtalonCount - notFoundWordsCount - notEqualWordsCount - wrongCount) * 100.0/(totalWordCount-missingEtalonCount);
		System.out.println(String.format("** Accuracy %1$,.2f процентов **", accuracy));
		System.out.println("** Не найдено в словаре " + notFoundWordsCount + " слов");
		System.out.println("** Без омонимии, но эталон не совпал со словарем " + notEqualWordsCount + " слов");
		System.out.println("** Не найдено в эталоне (не с чем сравнить) " + missingEtalonCount + " слов");
		System.out.println("** Правильных тегов true positives " + goodTagsCount + " ");
		System.out.println("** Ошибочных тегов false positives " + badTagsCount + " ");
		System.out.println("** Всего тегов " + allTagsCount + " ");		
		double precisionTags = (goodTagsCount) * 100.0/(badTagsCount+goodTagsCount);
		System.out.println(String.format("** Точность по тегам %1$,.2f процентов **", precisionTags));
		int goodWordCunt = totalWordCount - missingEtalonCount - notFoundWordsCount - notEqualWordsCount - comparedCount;
		int allAnalizedWordCunt = totalWordCount - missingEtalonCount;
		allPresign += goodWordCunt;
		allPresign /= allAnalizedWordCunt;
		System.out.println(String.format("** Средняя точность по словам %1$,.2f процентов **", allPresign*100));
	}
	
	private void testEstimatorWithFile(File file) {
		TokenRegistry.clean();
		System.out.println("***** Файл " + file.getName() + " *****");
		try {
			BasicParsedTokensLoader loader = createLoader(file);
			List<List<SimplifiedToken>> sentenceTokens = loader.getChains();
			List<String> sentences = loader.getSentences();
			if (sentences.size() != sentenceTokens.size()) {
				System.err.println("Ошибка: несовпадение количества эталонных и анализируемых предложений в файле " + file.getName());
			}
			int size = Math.min(sentences.size(), sentenceTokens.size());
			for (int i = 0; i < size; i++) {
				List<SimplifiedToken> curTokens = sentenceTokens.get(i);
				String curSentence = sentences.get(i);
				doTest(curSentence, curTokens);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
	}

	private BasicParsedTokensLoader createLoader(File file)
			throws FileNotFoundException {
		return new CorporaParsedTokensLoader(new BufferedInputStream(new FileInputStream(file)));
		//return new ParsedTokensLoader(new BufferedInputStream(new FileInputStream(file)));
	}

	private void doTest(String sentence, List<SimplifiedToken> etalonTokens) {
		DemoEstimationData data = new DemoEstimationData(sentence);
//		AnalysisResultProvider pairsEstimatedProvider = new AnalysisResultProvider(pairsEstimator.analyze(sentence));
		EtalonTokenProvider etalonTokenProvider = new EtalonTokenProvider(etalonTokens);
		for (int i = 0; i < data.results.size(); i++) {
			 EstimatedToken curResult = data.results.get(i);
			 if (isWord(curResult)) {
				 totalWordCount++;
//				 AnalisysResult pairsToken = pairsEstimatedProvider.getToken(curResult.string.toLowerCase().trim());
				 String word = curResult.string;
				SimplifiedToken etalonToken = etalonTokenProvider.getToken(word);
				 if (etalonToken == null) {
					 handleMissingEtalon(word, sentence);
					 missingEtalonCount++;
					 continue;
				 }
				 if (!wordEquals(curResult, etalonToken)) {
					 // System.out.println("Несовпадение по слову: " + word + " , " + etalonToken.getWord());
					 notEqualWordsCount++;
					 continue;
				 }
				 Set<PartOfSpeech> origParts = curResult.originalParts;
				 if (!intersects(origParts,etalonToken.getPartsOfSpeech())) {
				    System.err.println("Слово неверно определено в словаре: " + word + " - " + origParts + " вместо " + etalonToken.getPartOfSpeech());
				    vocabFailCount++;
					 notFoundWordsCount++;
				  }
				 totalWordCount++;
				totalPartsCount += origParts.size();
				 if (origParts.size() > 1) { //Count only for those which have conflicts
//					 boolean pairsResolved = false;
					 HashSet<PartOfSpeech> parts = curResult.doSelect();
					 HashSet<PartOfSpeech> partsNeural = curResult.neural;
					 HashSet<PartOfSpeech> partsHeuristic= curResult.heuristic;
					 filteredPartsCount += parts.size();
//					 if (pairsToken != null && !pairsToken.results.isEmpty() && pairsToken.results.size() == 1) {
//						 parts.clear();
//						 parts.add(pairsToken.results.get(0).speach);
//						 pairsResolved = true;
//						 pairsResolvedCnt++;
//					 }
					 List<PartOfSpeech> etalonParts = etalonToken.getPartsOfSpeech();
					 if (etalonParts.size() > 1) {
						 System.out.println("неоднозначность эталона: " + etalonToken.getWord() + " : " + etalonParts);
						 ambigEtalon++;
						 if (!intersects(parts, etalonParts)) {
							 comparedCount++;
							 goodTagsCount+=intersectSize(parts, etalonParts);
							 badTagsCount+=excludeSize(parts, etalonParts);
							 allTagsCount+=parts.size();
							 allPresign += goodTagsCount/(allTagsCount);
							 handleOmonymicWord(word);
							 handleMistake(sentence, word, etalonParts, parts);
							 handleFMeasure(etalonParts, parts);
						 } 
					 } else if (etalonParts.size() > 0) {
						 comparedCount++;
						 goodTagsCount+=intersectSize(parts, etalonParts);
						 badTagsCount+=excludeSize(parts, etalonParts);
						 allTagsCount+=parts.size();
						 allPresign += goodTagsCount/(allTagsCount);
						 handleOmonymicWord(word);
						 boolean right = parts.contains(etalonParts.get(0));
						 if (!right) {
							 handleMistake(sentence, word, etalonParts, parts);
						 } else if (parts.size() == 1) {
							 rightUnAmbigCount++;
						 }						 
						 boolean neuralRight = partsNeural.contains(etalonParts.get(0));
						 if (neuralRight) {
							 neuralResult.incTotal();
							 if (partsNeural.size() == 1) {
								 neuralResult.incTotalUnAmbig();
							 }
						 }
						 Optional<Result> max = curResult.neuralAnalysisResult.results.stream().max((Result result1, Result result2) -> (int)Math.signum(result1.score - result2.score));
						 if (max.isPresent()) {
							 double maxScore = max.get().score;
							 for (Result result: curResult.neuralAnalysisResult.results) {
								 if (result.speach == etalonParts.get(0)) {
									 trueNeuralScores.add(result.score);
									 trueNeuralDiffs.add(maxScore - result.score);
								 } else {
									 falseTrueNeuralScores.add(result.score);
								 }
							 }
							 for (Result result: curResult.neuralAnalysisResult.abandonedResults) {
								 if (result.speach == etalonParts.get(0)) {
									 falseFalseNeuralScores.add(result.score);
								 }
							 }
						 }
						 boolean heuristicRight = partsHeuristic.contains(etalonParts.get(0));
						 if (heuristicRight) {
							 heuristicResult.incTotal();
							 if (partsHeuristic.size() == 1) {
								 heuristicResult.incTotalUnAmbig();
							 }
						 }
						 handleFMeasure(etalonParts, parts);
						 handleFMeasureCount(etalonParts, partsNeural, partsHeuristic, parts);
						 handleOmonimyStatistics(origParts, right);
					 }
				 } else  {
					 vocabUnAmbigCount++;
					 filteredPartsCount++;
			      }
			 }
		}
			 
	}
	private int notFoundWordsCount = 0;
	private int notEqualWordsCount = 0;
	private int missingEtalonCount = 0;
	private int goodTagsCount = 0;
	private int badTagsCount = 0;
	private int allTagsCount = 0;
	private double allPresign = 0;
	
	private void handleOmonymicWord(String word) {
		word = word.toLowerCase().trim();
		Pair pair = wordAnalysisMap.get(word);
		if (pair == null) {
			pair = new Pair();
			wordAnalysisMap.put(word, pair);
		}
		pair.incTotalCount();
		
	}

	private void handleOmonimyStatistics(Set<PartOfSpeech> originalParts,
			boolean right) {
		PartOfSpeech[] parts = originalParts.toArray(new PartOfSpeech[0]);
		for (int i = 0; i < parts.length; i++) {
			for (int j = i + 1; j < parts.length; j++) {
				OmonimyPair pair = new OmonimyPair(parts[i], parts[j]);
				Pair counts = omonimyStat.get(pair);
				if (counts == null) {
					counts = new Pair();
				}
				counts.incTotalCount();
				if (!right) {
					counts.incWrongCount();
				}
				omonimyStat.put(pair, counts);
			}
			
		}
	}

	private void handleFMeasure(List<PartOfSpeech> etalonParts,
			HashSet<PartOfSpeech> parts) {
		Set<PartOfSpeech> etalonSet = new HashSet<PartOfSpeech>(etalonParts);
		Set<PartOfSpeech> resultSet = new HashSet<PartOfSpeech>(parts);
		resultSet.retainAll(etalonSet);
		tp += resultSet.size();
		
		resultSet.addAll(parts);
		resultSet.removeAll(etalonParts);
		fp += resultSet.size();
		
		resultSet.addAll(parts);
		etalonSet.removeAll(resultSet);
		fn += etalonSet.size();
		
	}

	private void handleFMeasureCount(List<PartOfSpeech> etalonParts,
			HashSet<PartOfSpeech> neyroParts,
			HashSet<PartOfSpeech> rulesParts,
			HashSet<PartOfSpeech> parts) {
		Set<PartOfSpeech> etalonSet = new HashSet<PartOfSpeech>(etalonParts);
		Set<PartOfSpeech> resultSet = new HashSet<PartOfSpeech>(parts);
		Set<PartOfSpeech> resultSetNeyro = new HashSet<PartOfSpeech>(neyroParts);
		Set<PartOfSpeech> resultSetRules = new HashSet<PartOfSpeech>(rulesParts);
		resultSet.retainAll(etalonSet);
		resultSetNeyro.retainAll(etalonSet);
		resultSetRules.retainAll(etalonSet);
		tpCount += resultSet.size();
		tpCountN += resultSetNeyro.size();
		tpCountR += resultSetRules.size();
		
		resultSet.addAll(parts);
		resultSet.removeAll(etalonParts);
		fpCount += resultSet.size();
		resultSetNeyro.addAll(neyroParts);
		resultSetNeyro.removeAll(etalonParts);
		fpCountN += resultSetNeyro.size();
		resultSetRules.addAll(rulesParts);
		resultSetRules.removeAll(etalonParts);
		fpCountR += resultSetRules.size();
		
		resultSet.addAll(parts);
		etalonSet.removeAll(resultSet);
		fnCount += etalonSet.size();
		etalonSet.addAll(etalonParts);
		resultSetNeyro.addAll(neyroParts);
		etalonSet.removeAll(resultSetNeyro);
		fnCountN += etalonSet.size();
		etalonSet.addAll(etalonParts);
		resultSetRules.addAll(rulesParts);
		etalonSet.removeAll(resultSetRules);
		fnCountR += etalonSet.size();
		
	}
	
	private void handleMissingEtalon(String string, String sentence) {
		//System.err.println("не найден эталон для слова: " + string + " в предложении " + sentence);
	}

	private boolean intersects(Set<PartOfSpeech> originalParts,
			List<PartOfSpeech> etalonParts) {
		for (PartOfSpeech partOfSpeech : etalonParts) {
			if (originalParts.contains(partOfSpeech)) {
				return true;
			}
		}
		return false;
	}
	
	//F-мера
	private int tpCount;
	private int fpCount;
	private int fnCount;
	private int tpCountN;
	private int fpCountN;
	private int fnCountN;
	private int tpCountR;
	private int fpCountR;
	private int fnCountR;
	
	/**
	 * Количество элементов в найденном списке частей речи, не содержащихся в эталоне
	 * @param originalParts
	 * @param etalonParts
	 * @return
	 */
	private int excludeSize(Set<PartOfSpeech> originalParts,
			List<PartOfSpeech> etalonParts) {
		int count = 0;
		for (PartOfSpeech partOfSpeech : originalParts) {
			count += etalonParts.contains(partOfSpeech) ? 0 : 1;
		}
		return count;
	}
	/**
	 * Количество элементов в пересечении эталона и найденных
	 * @param originalParts
	 * @param etalonParts
	 * @return
	 */
	private int intersectSize(Set<PartOfSpeech> originalParts,
			List<PartOfSpeech> etalonParts) {
		int count = 0;
		for (PartOfSpeech partOfSpeech : etalonParts) {
			count += originalParts.contains(partOfSpeech) ? 1 : 0;
		}
		return count;
	}	
	
	private boolean isWord(EstimatedToken curResult) {
		String trimmed = curResult.string.toLowerCase().trim();
		if (trimmed.length() > 0 && (Character.isAlphabetic(trimmed.charAt(0)) || Character.isDigit(trimmed.charAt(0)))) {
			return true;
		}
		return false;
	}

	private void handleMistake(String sentence, String word, List<PartOfSpeech> etalonParts,
			HashSet<PartOfSpeech> parts) {
		word = word.toLowerCase().trim();
		wrongCount++;
		 System.out.println("Несовпадение по частям речи слова "+word+": " + parts.toString() + " ,ожидается " + etalonParts.toString() + "("+sentence+")");
		wordAnalysisMap.get(word).incWrongCount();
		
	}

	private boolean wordEquals(EstimatedToken curResult,
			SimplifiedToken etalonToken) {
		String token1 = curResult.string.toLowerCase().trim();
		String token2 = etalonToken.getWord().toLowerCase().trim();
		return token2.equals(token1);
	}
	
}
