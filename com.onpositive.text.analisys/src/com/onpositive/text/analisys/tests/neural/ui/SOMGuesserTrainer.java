package com.onpositive.text.analisys.tests.neural.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.encog.mathutil.rbf.RBFEnum;
import org.encog.ml.data.MLData;
import org.encog.ml.data.basic.BasicMLData;
import org.encog.neural.som.SOM;
import org.encog.neural.som.training.basic.BasicTrainSOM;
import org.encog.neural.som.training.basic.neighborhood.NeighborhoodRBF;
import org.encog.persist.EncogDirectoryPersistence;

import com.onpositive.semantic.wordnet.AbstractWordNet;
import com.onpositive.semantic.wordnet.Grammem;
import com.onpositive.semantic.wordnet.Grammem.PartOfSpeech;
import com.onpositive.semantic.wordnet.WordNetProvider;
import com.onpositive.text.analisys.tests.euristics.OmonimyPair;
import com.onpositive.text.analisys.tests.euristics.ValueComparator;
import com.onpositive.text.morphology.dataset.prepare.DataSetPreparation;

public class SOMGuesserTrainer {
	
	private static final int SECOND_PART_THRESHOLD = 5;
	private static final int THIRD_PART_THRESHOLD = 10;
	private static final String PARTIDS_FILE_NAME = "partids.dat";
	private static final String SOM_FILE_NAME = "som.nnet";
	private static final int COUNT_THRESHOLD = 1500000;
	private static final int ANALYZED_LETTERS = 7;
	private static final int OUTPUT_COUNT = 100;
	private static final int LETTER_COUNT = 'я' - 'а' + 1;
	private static final int MIN_LENGTH = 4;
	private static final int VARS_COUNT = 3;
	private static List<PartOfSpeech> SPECIAL_PARTS = Arrays.asList(PartOfSpeech.PREP, PartOfSpeech.CONJ, PartOfSpeech.PRCL, PartOfSpeech.INTJ);
	
	@SuppressWarnings("unchecked")
	private static void trainSOM(String[] words, PartOfSpeech[] parts) {
		SOM network = new SOM(ANALYZED_LETTERS * LETTER_COUNT, OUTPUT_COUNT);
		NeighborhoodRBF gaussian = new NeighborhoodRBF(RBFEnum.Gaussian,MapPanel.WIDTH,
				MapPanel.HEIGHT);
		BasicTrainSOM train = new BasicTrainSOM(network, 0.01, null, gaussian);
		train.setForceWinner(false);
		train.setAutoDecay(1000, 0.8, 0.003, 30, 5);
		
//		int length = Math.min(COUNT_THRESHOLD, words.length);
		int length = words.length;
//		List<MLData> samples = new ArrayList<MLData>();
//		for (int i = 0; i < length; i++) {
//			samples.add(new BasicMLData(getData(words[i])));
//		}
		int prevpercent = 0;
		for (int i = 0; i < words.length; i++) {
			MLData c = new BasicMLData(getData(words[i]));

			train.trainPattern(c);
			train.autoDecay();
			int percent = i * 100 / words.length;
			if (percent > prevpercent) {
				System.out.println(percent + "%");
				prevpercent = percent;
			}
			System.out.println("Iteration " + i + "," + train.toString());
		}
		
		HashMap [] counts = new HashMap [OUTPUT_COUNT];
		for (int i = 0; i < length; i++) {
			int id = network.classify(new BasicMLData(getData(words[i])));
			if (0 <= id && id < OUTPUT_COUNT) {
				HashMap<PartOfSpeech, Integer> map = counts[id];
				if (map == null) {
					map = new HashMap<PartOfSpeech, Integer>();
					counts[id] = map;
				}
				Integer cnt = map.get(parts[i]);
				if (cnt == null) {
					cnt = 1;
				} else {
					cnt += 1;
				}
				map.put(parts[i], cnt);
			}
			
		}
		
		for (int i = 0; i < counts.length; i++) {
			System.out.print("Class " + i + ": ");
			HashMap<PartOfSpeech, Integer> map = counts[i];
			if (map != null) {
				for (PartOfSpeech part : map.keySet()) {
					System.out.print("[" + part + ":" + map.get(part) + "]");					
				}
			}
			System.out.println();
		}
		
		EncogDirectoryPersistence.saveObject(new File(SOM_FILE_NAME), network);
	}

	private static double[] getData(String word) {
		double[] result = new double[ANALYZED_LETTERS * LETTER_COUNT];
		int cnt = Math.min(7,word.length());
		for (int i = 0; i < cnt; i++) {
			char curLetter = word.charAt(word.length() - i - 1);
			if (Character.isLetter(curLetter)) {
				int norm = curLetter - 'а';
				result[LETTER_COUNT * i + norm] = 1.0;
			}
		}
		return result;
	}
	
	public static void main(String[] args) {
		prepareGuesserData();
//		testGuesser();
	}

	protected static void prepareGuesserData() {
		ArrayList<String> usedWords = new ArrayList<String>();
		ArrayList<PartOfSpeech> wordParts = getWordsWithParts(usedWords);
		trainSOM(usedWords.toArray(new String[0]), wordParts.toArray(new PartOfSpeech[0]));
		try {
			fillNetworkTags(usedWords, wordParts);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	protected static void testGuesser() {
		
		ArrayList<String> usedWords = new ArrayList<String>();
		ArrayList<PartOfSpeech> wordParts = getWordsWithParts(usedWords);
		SOM network = (SOM) EncogDirectoryPersistence.loadObject(new File(SOM_FILE_NAME));
		
		ObjectInputStream stream = null;
		try {
			stream = new ObjectInputStream(new FileInputStream(PARTIDS_FILE_NAME));
			Integer[][] parts = (Integer[][]) stream.readObject();
			int totalCount = 0;
			int rightCount = 0;
			for (int i = COUNT_THRESHOLD; i < usedWords.size(); i++) {
				PartOfSpeech origPart = wordParts.get(i);
				if (!SPECIAL_PARTS.contains(origPart)) {
					totalCount++;
					int clazz = network.classify(new BasicMLData(getData(usedWords.get(i))));
					Integer[] curParts = parts[clazz];
					for (int j = 0; j < curParts.length; j++) {
						Integer part = curParts[j];
						if (part == null) {
							continue;
						}
						Grammem estPart = PartOfSpeech.get(part);
						if (estPart.equals(origPart)) {
							rightCount++;
							break;
						}
					}
				}
			}
			System.out.println("Верно угадано для " + (rightCount * 100 / totalCount) + "% слов");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {}
			}
		}
		
	}

	@SuppressWarnings("unchecked")
	protected static void fillNetworkTags(ArrayList<String> usedWords,
			ArrayList<PartOfSpeech> wordParts) throws IOException,
			FileNotFoundException {
		SOM network = (SOM) EncogDirectoryPersistence.loadObject(new File(SOM_FILE_NAME));
		int clazz = network.classify(new BasicMLData(getData("яблоко")));
		System.out.println(clazz);
		clazz = network.classify(new BasicMLData(getData("ложечка")));
		System.out.println(clazz);
		clazz = network.classify(new BasicMLData(getData("медведь")));
		System.out.println(clazz);
		int length = COUNT_THRESHOLD;
		PartOfSpeech[] parts =  wordParts.toArray(new PartOfSpeech[0]);
		HashMap [] counts = new HashMap [OUTPUT_COUNT];
		for (int i = 0; i < length; i++) {
			int id = network.classify(new BasicMLData(getData(usedWords.get(i))));
			if (0 <= id && id < OUTPUT_COUNT) {
				HashMap<PartOfSpeech, Integer> map = counts[id];
				if (map == null) {
					map = new HashMap<PartOfSpeech, Integer>();
					counts[id] = map;
				}
				Integer cnt = map.get(parts[i]);
				if (cnt == null) {
					cnt = 1;
				} else {
					cnt += 1;
				}
				map.put(parts[i], cnt);
			}
			
		}
		
		Integer[][] partIds = new Integer[100][];
		
		for (int i = 0; i < counts.length; i++) {
			System.out.print("Class " + i + ": ");
			HashMap<PartOfSpeech, Integer> map = counts[i];
			if (map != null) {
				ValueComparator<PartOfSpeech, Integer> comparator = new ValueComparator<PartOfSpeech, Integer> (map);
				Map<PartOfSpeech, Integer> sortedMap = new TreeMap<PartOfSpeech, Integer> (comparator);
				sortedMap.putAll(map);
				int sum = 0;
				for (PartOfSpeech part : sortedMap.keySet()) {
					sum += map.get(part);
				}
				for (PartOfSpeech part : sortedMap.keySet()) {
					System.out.print("[" + part + ":" + getPercent(sortedMap, sum, part) + "%]");
				}
				System.out.println();
				partIds[i] = new Integer[VARS_COUNT];
				int j = 0;
				for (PartOfSpeech part : sortedMap.keySet()) {
					int percent = getPercent(sortedMap, sum, part);
					if (j == 0 ||
						j == 1 && percent >= SECOND_PART_THRESHOLD ||
						j == 2 && percent >= THIRD_PART_THRESHOLD ||
						percent > 30) {
							partIds[i][j] = (int) part.intId;
					}
					j++;
					if (j == partIds[i].length) {
						break;
					}
				}
			}
			System.out.println();
		}
		ObjectOutputStream out = null;
		try {
			out = new ObjectOutputStream(new FileOutputStream(PARTIDS_FILE_NAME));
			out.writeObject(partIds);
		} finally {
			out.close();
		}
	}

	protected static int getPercent(Map<PartOfSpeech, Integer> sortedMap,
			int sum, PartOfSpeech part) {
		return sortedMap.get(part) * 100 / sum;
	}

	protected static ArrayList<PartOfSpeech> getWordsWithParts(
			ArrayList<String> usedWords) {
		AbstractWordNet instance = WordNetProvider.getInstance();
		String[] words = instance.getAllGrammarKeys();
		ArrayList<PartOfSpeech> wordParts = new ArrayList<PartOfSpeech>();
		for (int i = 0; i < words.length; i++) {
			String word = words[i].toLowerCase().trim().replace('ё','е');
			if (word.length() >= MIN_LENGTH) {
				Set<PartOfSpeech> parts = DataSetPreparation.possiblePartsOfSpeech(word);
				if (parts.size() == 1) {
					PartOfSpeech part = parts.iterator().next();
					if (!SPECIAL_PARTS.contains(part)) {
						usedWords.add(word);
						wordParts.add(part);
					}
				}
			}
		}
		return wordParts;
	}


}
