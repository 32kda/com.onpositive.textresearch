package com.onpositive.text.analisys.tests.neural.ui;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashSet;
import java.util.Set;

import org.encog.ml.data.basic.BasicMLData;
import org.encog.neural.som.SOM;
import org.encog.persist.EncogDirectoryPersistence;

import com.onpositive.semantic.wordnet.Grammem;
import com.onpositive.semantic.wordnet.Grammem.PartOfSpeech;

public class SOMGuesser {
	
	private static final String PARTIDS_FILE_NAME = "partids.dat";
	private static final String SOM_FILE_NAME = "som.nnet";
	private static final int ANALYZED_LETTERS = 7;
	private static final int LETTER_COUNT = 'я' - 'а' + 1;
	
	private static SOMGuesser instance;
	private SOM network;
	private Integer[][] parts;
	
	
	public static synchronized SOMGuesser getInstance() {
		if (instance == null) {
			try {
				instance = new SOMGuesser();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				//Shouldn't happen with Integer :)
			}
		}
		return instance;
	}

	private SOMGuesser() throws IOException, ClassNotFoundException {
		ObjectInputStream stream = null;
		try {
			network = (SOM) EncogDirectoryPersistence.loadObject(SOMGuesser.class.getResourceAsStream(SOM_FILE_NAME));
			stream = new ObjectInputStream(SOMGuesser.class.getResourceAsStream(PARTIDS_FILE_NAME));
			parts = (Integer[][]) stream.readObject();
		} finally {
			if (stream != null) 
				stream.close();
		}
	}
	
	public Set<PartOfSpeech> guessPartsOfSpeech(String word) {
		Set<PartOfSpeech> result = new HashSet<>();
		int clazz = network.classify(new BasicMLData(getData(word)));
		Integer[] curParts = parts[clazz];
		for (int j = 0; j < curParts.length; j++) {
			Integer part = curParts[j];
			if (part == null) {
				continue;
			}
			Grammem estPart = PartOfSpeech.get(part);
			result.add((PartOfSpeech) estPart);
		}
		return result;
	}
	
	private static double[] getData(String word) {
		double[] result = new double[ANALYZED_LETTERS * LETTER_COUNT];
		int cnt = Math.min(7,word.length());
		for (int i = 0; i < cnt; i++) {
			char curLetter = word.charAt(word.length() - i - 1);
			if (Character.isLetter(curLetter)) {
				int norm = curLetter - 'а';
				try{
				result[LETTER_COUNT * i + norm] = 1.0;
				}catch (ArrayIndexOutOfBoundsException e){
					
				}
			}
		}
		return result;
	}
}
	
