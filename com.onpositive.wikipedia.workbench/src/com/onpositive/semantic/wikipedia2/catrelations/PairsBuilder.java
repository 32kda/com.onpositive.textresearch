package com.onpositive.semantic.wikipedia2.catrelations;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.onpositive.semantic.wikipedia2.WikiEngine2;
import com.onpositive.semantic.wikipedia2.fulltext.WordIndex;
import com.onpositive.semantic.wordnet.AbstractWordNet;
import com.onpositive.semantic.wordnet.Grammem;
import com.onpositive.semantic.wordnet.Grammem.PartOfSpeech;
import com.onpositive.semantic.wordnet.TextElement;
import com.onpositive.semantic.wordnet.WordNetProvider;

public class PairsBuilder {
	
	public static void main(String[] args) {
		WikiEngine2 eng = new WikiEngine2("D:\\se2\\ruwiki");
		AbstractWordNet instance = WordNetProvider.getInstance();
		int[] notRedirectDocumentIDs = eng.getNotRedirectDocumentIDs();
		int i = 0;
//		HashMap<String, StablePair> pairsMap = new HashMap<String,StablePair>();
		List<Long[]> pairsList = new ArrayList<>();
		for (int q : notRedirectDocumentIDs) {
			i++;
			if (i % 1000 == 0) {
				System.out.println(i);
			}
//			if (i > 100000) {
//				break;
//			}
			String text = eng.getDocument(q).getPlainTextAbstract();
			StringReader rs = new StringReader(text);
			BufferedReader m = new BufferedReader(rs);
			while (true) {
				try {
					String readLine = m.readLine();
					if (readLine == null) {
						break;
					}
					String[] split = readLine.split("[\\.|,|:|\\?|\\!]");
					for (String sentence : split) {
						List<String> wordStrings = new ArrayList<String>();
						ArrayList<HashSet<TextElement>> elements2 = WordIndex
								.toWords(instance, sentence, wordStrings);
						int prevWordId = 0;
						byte prevPartId = 0;
						for (int j = 0; j < elements2.size(); j++) {
//							String wordStr = wordStrings.get(j);
							HashSet<TextElement> wordElements = elements2.get(j);
							PartOfSpeech part = getPartOfSpeech(wordElements);
							if (part != null) {
								byte partId = part.intId;
								TextElement element = wordElements.iterator().next();
								int wordId = element.id();
								if (prevPartId > 0) {
									pairsList.add(new Long[] {(long)prevWordId << 32 | wordId & 0xFFFFFFFFL, (long) prevPartId << 8 | partId & 0xFFFFL});
								}
								prevPartId = partId;
								prevWordId = wordId;
							} else {
								prevPartId = 0;
								prevWordId = 0;
							}
						}
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}
		Map<Long, Short> pairsMap = new HashMap<Long, Short>(pairsList.size());
		for (Long[] data : pairsList) {
			pairsMap.put(data[0], data[1].shortValue());
		}
		
		ObjectOutputStream stream = null;
		try {
			stream = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream("pairs.dat")));
			stream.writeObject(pairsMap);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (stream != null)
					stream.close();
			} catch (IOException e) {}
		}
	}



	private static PartOfSpeech getPartOfSpeech(
			HashSet<TextElement> wordElements) {
		PartOfSpeech result = null;
		for (TextElement textElement : wordElements) {
			HashSet<Grammem> allGrammems = textElement.allGrammems();
			for (Grammem grammem : allGrammems) {
				if (grammem instanceof PartOfSpeech) {
					if (result != null && !result.equals(grammem)) {
						return null;
					} else {
						result = (PartOfSpeech) grammem;
					}
				}
			}
		}
		return result;
	}

}
