package com.onpositive.semantic.wikipedia2.fulltext;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.carrotsearch.hppc.IntByteOpenHashMap;
import com.carrotsearch.hppc.IntObjectMap;
import com.carrotsearch.hppc.IntObjectOpenHashMapSerialzable;
import com.carrotsearch.hppc.ObjectIntOpenHashMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.primitives.Ints;
import com.onpositive.semantic.wikipedia2.WikiEngine2;
import com.onpositive.semantic.wikipedia2.fulltext.StatFile.PCD;
import com.onpositive.semantic.wikipedia2.fulltext.StatFile.StatFileProvider;
import com.onpositive.semantic.wordnet.AbstractWordNet;
import com.onpositive.semantic.wordnet.GrammarRelation;
import com.onpositive.semantic.wordnet.TextElement;

public class WordIndex {

	private WikiEngine2 engine;
	
	ObjectIntOpenHashMap<String> wordToIndex = new ObjectIntOpenHashMap<String>();
	IntObjectOpenHashMapSerialzable<String> indexToWord = new IntObjectOpenHashMapSerialzable<String>();

	IntObjectMap<IntByteOpenHashMap> docsHistogramMap = new IntObjectOpenHashMapSerialzable<IntByteOpenHashMap>();

	public static class WordIndexProvider {

		Map<WikiEngine2,WordIndex> indexis = Maps.newHashMap();
		
		private static WordIndexProvider instance;
		
		private WordIndexProvider() {
		}

		public static WordIndexProvider getInstance() {
			if(instance == null) {
				instance = new WordIndexProvider();
			}
			return instance;
		}
		
		public synchronized WordIndex get(WikiEngine2 engine) {
			WordIndex wordIndex = indexis.get(engine);
			if(wordIndex == null) {
				if(getFile(engine).exists()) {
					wordIndex = new WordIndex(engine);
					try {
						wordIndex.load();
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else {
					try {
						wordIndex = build(engine);
					} catch (IOException e) {
						e.printStackTrace();
					}		
				}
				indexis.put(engine, wordIndex);
			}
			return wordIndex;
		}
		
	}
	StatFile statFile =null;
	
	public int getWordCount(int windex){
		if (statFile==null)
			 statFile=StatFileProvider.getInstance().get(engine);
		PCD pcd = statFile.getStatistics().get(windex-1);
		return pcd.count;	
	}
	
	WordIndex(WikiEngine2 engine) {
		this.engine = engine;
		StatFile statFile = StatFileProvider.getInstance().get(engine);
		int index = 1;
		for(PCD c: statFile.getStatistics()) {
			wordToIndex.put(c.str, index);
			indexToWord.put(index, c.str);
			index++;
		}
	}
	
	public Integer getWordCount(String stem) {
		PCD pcd = getEntry(stem);
		return pcd == null? null : pcd.count;
	}

	public Integer getDocumentCount(String stem) {
		PCD pcd = getEntry(stem);
		return pcd == null? null : pcd.documentCount;
	}

	private PCD getEntry(String stem) {
		Integer index = getWordIndex(stem);
		StatFile statFile = StatFileProvider.getInstance().get(engine);
		return (index == null)? null : statFile.getStatistics().get(index); 
	}

	public Integer getWordIndex(String stem) {
		if(!wordToIndex.containsKey(stem)) return null;
		return wordToIndex.get(stem)-1;
	}

	public String getWord(int index) {
		return indexToWord.get(index);
	}
	
	public static File getFile(WikiEngine2 engine) {
		return new File(engine.getLocation(),"wi.dat");
	}

	public File getFile() {
		return getFile(engine);
	}
		
	public void load() throws IOException {
		DataInputStream di = new DataInputStream(new BufferedInputStream(new FileInputStream(getFile())));
		int no = 0;
		System.out.println("Reading word index");
		HashSet<Integer> docIds = Sets.newHashSet(Ints.asList(engine.getDocumentIDs()));
		while(di.available() > 0) {
			int pageId = di.readInt();
			if(!docIds.contains(pageId)) {
				System.out.println("ERROR: no document id" + pageId);
			}
			IntByteOpenHashMap docHistorgam = new IntByteOpenHashMap();			
			int size = di.readInt();
			for (int i=0; i<size; i++) {
				int wordIndex = di.readInt();
				String stem = indexToWord.get(wordIndex);
				if(stem == null) System.out.println("ERROR: no stem for index " + wordIndex);
				int count = di.read();
				docHistorgam.put(wordIndex, (byte)count);
			}
			docsHistogramMap.put(pageId, docHistorgam);
			//System.out.println("Page Id: " + pageId);
			no++;
		}
		di.close();
		System.out.println("Count: " + no);
	}
	
	

	public IntByteOpenHashMap getDocumentHistogram(int pageId) {
		return docsHistogramMap.get(pageId);
	}
	
	

	private static IntByteOpenHashMap makeDocumentHistogram(ObjectIntOpenHashMap<String> wm, String text, StringBuilder bld) {
		IntByteOpenHashMap his = new IntByteOpenHashMap();

		for (int a = 0; a < text.length(); a++) {
			char c = text.charAt(a);
			if (Character.isLetter(c)||Character.isDigit(c)||c=='-') {
				bld.append(Character.toLowerCase(c));
			} else {
				String string = bld.toString();
				if (string.length() > 3) {
					boolean allRussian = true;
					
					if (allRussian) {
						String stem = StemProvider.getInstance().stem(string);
						if (wm.containsKey(stem)) {
							int index = wm.get(stem);
							if (his.containsKey(index)) {
								byte b = his.get(index);
								if (b < 127) {
									his.put(index, (byte)(b+1));
								}
							} else {
								his.put(index, (byte) 1);
							}
						}
					}
				}
				bld.delete(0, bld.length());
			}
		}
		return his;
	}
	

	public static ArrayList<HashSet<TextElement>> toWords(AbstractWordNet instance,
			String text, List<String> wordStrings) {
		ArrayList<HashSet<TextElement>>elements=new ArrayList<HashSet<TextElement>>();
		StringBuilder bld = new StringBuilder();
		
		
		for (int a = 0; a < text.length(); a++) {
			char c = text.charAt(a);
			if (Character.isLetter(c) || Character.isDigit(c) || c == '-') {
				bld.append(Character.toLowerCase(c));
			} else {
				String string = bld.toString();
				if (string.length() > 0) {
					// boolean allRussian = true;
					GrammarRelation[] possibleGrammarForms = instance
							.getPossibleGrammarForms(string.toLowerCase());
					HashSet<TextElement>te=new HashSet<TextElement>();
					if (possibleGrammarForms!=null&&possibleGrammarForms.length > 0) {
						for (GrammarRelation rel : possibleGrammarForms) {
							te.add(rel.getWord());
						}
					}
					elements.add(te);
				}
				wordStrings.add(bld.toString());
				bld.delete(0, bld.length());
			}
		}
		return elements;
	}
	
	public static ArrayList<String> toWords2(AbstractWordNet instance,
			String text) {
		ArrayList<String>elements=new ArrayList<String>();
		StringBuilder bld = new StringBuilder();
		
		
		for (int a = 0; a < text.length(); a++) {
			char c = text.charAt(a);
			if (Character.isLetter(c) || Character.isDigit(c) || c == '-') {
				bld.append(Character.toLowerCase(c));
			} else {
				String string = bld.toString();
				if (string.length() > 0) {
					// boolean allRussian = true;
					elements.add(string);
				}
				bld.delete(0, bld.length());
			}
		}
		return elements;
	}
	
	private static WordIndex build(WikiEngine2 engine) throws IOException {
		DataOutputStream ds = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(getFile(engine))));
		int[] articleKeys = engine.getDocumentIDs();
		long time = System.currentTimeMillis();
	
		WordIndex wordIndex = new WordIndex(engine);
		System.out.println("Starting index writing");
		int count=0;
		for (int pageId: articleKeys) {
			count++;
			if (count%1000==0){
				System.out.println("Written:"+count);
			}
			String text = engine.getDocumentAbstract(pageId);
			if (text == null) {
				text = "";
			}
			StringBuilder bld = new StringBuilder();
			IntByteOpenHashMap his = makeDocumentHistogram(wordIndex.wordToIndex, text, bld);
			wordIndex.docsHistogramMap.put(pageId, his);
			if (!his.isEmpty()) {
				ds.writeInt(pageId);
				ds.writeInt(his.size());
				for (int wi : his.keys().toArray()) {
					ds.writeInt(wi);
					ds.write(his.get(wi));
				}
			}
		}
		ds.close();
		time = System.currentTimeMillis() - time;
		System.out.println("Indexing completed: " + time/1000 + " sec");
		return wordIndex;
	}
	
	public IntByteOpenHashMap build(String documentContent){
		
		return makeDocumentHistogram(wordToIndex, documentContent, new StringBuilder());		
	}
	
}
