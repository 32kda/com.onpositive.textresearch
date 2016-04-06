package com.onpositive.wordnet.tests;


import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.onpositive.semantic.wordnet.AbstractWordNet;
import com.onpositive.semantic.wordnet.GrammarRelation;
import com.onpositive.semantic.wordnet.WordNetProvider;
import com.onpositive.semantic.wordnet.edit.WordNetAutoPatcher;
import com.onpositive.semantic.words2.SimpleWordNet;
import com.onpositive.semantic.words3.ReadOnlyMapWordNet;
import com.onpositive.semantic.words3.ReadOnlyTrieWordNet;
import com.onpositive.semantic.words3.ReadOnlyWordNet;
import com.onpositive.semantic.words3.TrieGrammarStore;
import com.onpositive.semantic.words3.TrieZippedProvider;
import com.onpositive.semantic.words3.hds.StringToByteTrie;
import com.onpositive.semantic.words3.hds.StringTrie;
import com.onpositive.semantic.words3.hds.StringTrie.TrieBuilder;
import com.onpositive.semantic.words3.prediction.IPredictionHelper;
import com.onpositive.semantic.words3.prediction.PredictionUtil;
import com.onpositive.semantic.words3.prediction.TriePredictionHelper;


public class Tester {

	private static final String RWNET_PATH = "D:/tmp/rwnet.dat";
	private static final String PREDICTION_TRIE_PATH = "D:/tmp/prediction.dat";
	private static final int COUNT = 2000000;

	public static void main(String[] args) {
//		test1();
//		findBroken(3036596, 3036600);
//		test4();
//		test5();
//		test6();
//		test7();
//		test8();
//		test9();
//		test10();
		
		globalTest();
//		timeTest();
//		testPrefixSearch();
//		testSearch();
//		testPrediction();
//		testTrieSearch();
//		testRebuid();
//		test13();
//		test14();
	}
	
	public static void testPrefixSearch() {
		StringToByteTrie trieGrammarStore = new StringToByteTrie();
		StringTrie<Byte>.TrieBuilder newBuilder = trieGrammarStore.newBuilder();
		int i = 35;
		
		String[] tst = {"надгрызает",
				"надгрызала",
				"надгрызали",
				"надгрызало",
				"надгрызать",
				"надгрызают",
				"надгрызена",
				"надгрызено",
				"надгрызены",
				"надгрызёте",
				"надгрызёшь",
				"надгрызите",
				"надгрызаетесь",
				"надгрызшая",
				"надгрызшее",
				"надгрызшей",
				"надгрызшем",
				"надгрызшею",
				"надгрызшие",
				"надгрызший",
				"надгрызшим",
				"надгрызших",
				"надгрызшую",
				"надгрыз"};
		
		for (String string : tst) {
			newBuilder.append(string, Byte.valueOf((byte)i++));
		}
		
		trieGrammarStore.commit(newBuilder);
        int k = 35;
				
		Collection<String> strings = trieGrammarStore.getStrings("надг");
		
	}

	@SuppressWarnings("unchecked")
	private static void globalTest() {
		AbstractWordNet loaded;
		try {
			loaded = WordNetProvider.getInstance();
//			ReadOnlyTrieWordNet trieWordNet = ReadOnlyTrieWordNet.load(new ZipFile("russian.dict"));
			String[] keys = loaded.getAllGrammarKeys();
//			Map<String, Byte> dataMap = new HashMap<String, Byte>();
			StringTrie<GrammarRelation[]> trieGrammarStore = new TrieGrammarStore();
			TrieBuilder newBuilder = trieGrammarStore.newBuilder();
			int n = keys.length;
			for (int i = 0; i < n; i++) {
//				Byte b = (byte)(Math.random() * 100);
//				dataMap.put(keys[i], b);
				newBuilder.append(keys[i], loaded.getPossibleGrammarForms(keys[i]));
			}
			trieGrammarStore.commit(newBuilder);
			
			ZipOutputStream zos = new ZipOutputStream(new FileOutputStream("dict.dict"));
			ZipEntry entry = new ZipEntry("trie");
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			trieGrammarStore.write(baos);
			byte[] input = baos.toByteArray();
			entry.setSize(input.length);
			zos.putNextEntry(entry);
			zos.write(input);
			zos.closeEntry();
			zos.close();
			
			for (int i = 0; i < n; i++) {
				GrammarRelation[] found = trieGrammarStore.get(keys[i]);
				if (!Arrays.equals(found, loaded.getPossibleGrammarForms(keys[i])) ) {
					System.out.println("Error for str " + keys[i] + " idx = " + i);
				}
				found = trieGrammarStore.get(keys[i] + "aaa");
				if (found != null) {
					System.out.println("Non-null for str " + keys[i] + "aaa" + " idx = " + i);
				}
				if (keys[i].length() > 2) {
					String str = keys[i].substring(0,2) + "a" + keys[i].substring(2);
					found = trieGrammarStore.get(str);
					if (found != null) {
						System.out.println("Non-null for str " + str + " idx = " + i);
					}
				}
//				Byte data = dataMap.get(keys[i]);
//				if (!data.equals(found)) {
//				}
				
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	private static void testTrieSearch() {
		ReadOnlyWordNet loaded;
		try {
			loaded = ReadOnlyMapWordNet.load(RWNET_PATH);
//			ReadOnlyTrieWordNet trieWordNet = ReadOnlyTrieWordNet.load(new ZipFile("russian.dict"));
			String[] keys = loaded.getAllGrammarKeys();
			
			GrammarRelation[] forms = loaded.getPossibleGrammarForms("бнр");
			forms = loaded.getPossibleGrammarForms("бваке");
//			Map<String, Byte> dataMap = new HashMap<String, Byte>();
			StringTrie<GrammarRelation[]> trieGrammarStore = new TrieGrammarStore();
			TrieBuilder newBuilder = trieGrammarStore.newBuilder();
			int n = keys.length;
			for (int i = 0; i < n; i++) {
//				Byte b = (byte)(Math.random() * 100);
//				dataMap.put(keys[i], b);
				newBuilder.append(keys[i], loaded.getPossibleGrammarForms(keys[i]));
				if (keys[i].startsWith("бнр")) {
					System.out.println("word" + keys[i]);
				}
			}
			trieGrammarStore.commit(newBuilder);
			
			List<String> strings = trieGrammarStore.getStrings("анфилоф");
			System.out.println(strings);
			strings = trieGrammarStore.getStrings("фолифна");
			System.out.println(strings);
			strings = trieGrammarStore.getStrings("бнр");
			System.out.println(strings);
			strings = trieGrammarStore.getStrings("б");
			System.out.println(strings);
			strings = trieGrammarStore.getStrings("бнопня");
			System.out.println(strings);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void test4() {
		AbstractWordNet loaded;
		String[] keys;
		loaded = WordNetProvider.getInstance();
		keys = loaded.getAllGrammarKeys();
		int lowBound = 0;
		int highBound = keys.length;
		int step = (highBound - lowBound) / 2;
		while (step > 2) {
			if (broken(keys, lowBound, highBound, loaded)) {
				lowBound += step;
			} else {
				lowBound -= step;
			}
			step /= 2;
		}
		while (!broken(keys, lowBound, highBound, loaded)) {
			lowBound--;
		}
		step = (highBound - lowBound) / 2;
		while (step > 2) {
			if (broken(keys, lowBound, highBound, loaded)) {
				highBound -= step;
			} else {
				highBound += step;
			}
			step /= 2;
		}
		while (!broken(keys, lowBound, highBound, loaded)) {
			highBound++;
		}

		System.out.println(String.format("[ %d, %d ]", lowBound, highBound));
		if (highBound - lowBound < 20) {
			System.out.println("Reproduce dataset:");
			for (int i = lowBound; i < highBound; i++) {
				System.out.println(keys[i]);
			}
		}
	}
	
	private static void findBroken(int start, int end) {
		AbstractWordNet loaded;
		loaded = WordNetProvider.getInstance();
		String[] keys = loaded.getAllGrammarKeys();
		ArrayList<Integer> included = new ArrayList<Integer>();
		for (int i = start; i < end; i++) {
			included.add(i);
		}
		for (int i = 0; i < included.size(); i++) {
			Integer removed = included.remove(i);
			if (!broken(keys, included, loaded)) {
				included.add(i,removed);
			} else {
				i--;
			}
		}
		
		boolean a = broken(keys, included, loaded);
		System.out.println("test3() is broken: " + a);
		
		for (int i = 0; i < included.size(); i++) {
			Integer removed = included.remove(i);
			if (!broken(keys, included, loaded)) {
				included.add(i,removed);
			} else {
				i--;
			}
		}
		
//			while (broken(keys, start, COUNT)) {
//				start++;
//			}
		for (Integer integer : included) {
			System.out.println(keys[integer]);
		}
	}
	
	private static boolean broken(String[] keys, int start, int end, AbstractWordNet loaded) {
//		Map<String, Byte> dataMap = new HashMap<String, Byte>();
		StringTrie<GrammarRelation[]> trieGrammarStore = new TrieGrammarStore();
		TrieBuilder newBuilder = trieGrammarStore.newBuilder();
		for (int i = start; i < end; i++) {
			Byte b = (byte)(Math.random() * 100);
//			dataMap.put(keys[i], b);
			newBuilder.append(keys[i], loaded.getPossibleGrammarForms(keys[i]));
		}
		trieGrammarStore.commit(newBuilder);
		try {
			for (int i = start; i < end; i++) {
				GrammarRelation[] found = trieGrammarStore.get(keys[i]);
				if (!Arrays.equals(found, loaded.getPossibleGrammarForms(keys[i]))) {
					return true;
				}
				found = trieGrammarStore.get(keys[i] + "aaa");
				if (found != null) {
					return true;
				}
			}
		}
		catch (Throwable e) {
			return true;
		}
		return false;
	}
	
	private static boolean broken(String[] keys, ArrayList<Integer> included, AbstractWordNet loaded) {
		try {
			StringTrie<GrammarRelation[]> trieGrammarStore = new TrieGrammarStore();
			TrieBuilder newBuilder = trieGrammarStore.newBuilder();
			for (int i = 0; i < included.size(); i++) {
				int index = included.get(i);
				newBuilder.append(keys[index], loaded.getPossibleGrammarForms(keys[index]));
			}
			trieGrammarStore.commit(newBuilder);
			for (int i = 0; i < included.size(); i++) {
				int index = included.get(i);
				GrammarRelation[] found = trieGrammarStore.get(keys[index]);
				if (!Arrays.equals(found, loaded.getPossibleGrammarForms(keys[index]))) {
					return true;
				}
				found = trieGrammarStore.get(keys[index] + "aaa");
				if (found != null) {
					return true;
				}
			}
		} catch (Throwable e) {
			return true;
		}
		return false;
	}

	private static void test1() {
		StringToByteTrie trieGrammarStore = new StringToByteTrie();
		StringTrie<Byte>.TrieBuilder newBuilder = trieGrammarStore.newBuilder();
		int i = 35;
		
		String[] tst = {"надгрызает",
						"надгрызала",
						"надгрызали",
						"надгрызало",
						"надгрызать",
						"надгрызают",
						"надгрызена",
						"надгрызено",
						"надгрызены",
						"надгрызёте",
						"надгрызёшь",
						"надгрызите",
						"надгрызаетесь",
						"надгрызшая",
						"надгрызшее",
						"надгрызшей",
						"надгрызшем",
						"надгрызшею",
						"надгрызшие",
						"надгрызший",
						"надгрызшим",
						"надгрызших",
						"надгрызшую",
						"надгрыз"};
		
		for (String string : tst) {
			newBuilder.append(string, Byte.valueOf((byte)i++));
		}
		
		trieGrammarStore.commit(newBuilder);
        
		for (String string : tst) {
			Byte find = trieGrammarStore.get(string);
			System.out.println(string + " = " + find);
		}
	}
	
	private static void test5() {
		StringTrie<GrammarRelation[]> trieGrammarStore = new TrieGrammarStore();
		TrieBuilder newBuilder = trieGrammarStore.newBuilder();
		int i = 35;
		
//		String[] tst = {"с",
//						"станичного"};
		String[] tst = {"{{-}}"};

		
		try {
			ReadOnlyWordNet loaded = ReadOnlyMapWordNet.load(RWNET_PATH);
			GrammarRelation[] forms = loaded.getPossibleGrammarForms(tst[0]);
			forms = loaded.getPossibleGrammarForms(tst[0]);
			for (String string : tst) {
				newBuilder.append(string, forms);
			}
			
			trieGrammarStore.commit(newBuilder);
			
			for (String string : tst) {
				forms = loaded.getPossibleGrammarForms(string);
				GrammarRelation[] found = trieGrammarStore.get(string);
				System.out.println(string + " = " + found);
				found = trieGrammarStore.get(string + "aaa");
			}
		} catch (Exception e) {
			e.printStackTrace();
			// TODO: handle exception
		}
		
	}
	
	private static void test6() {
		StringToByteTrie trieGrammarStore = new StringToByteTrie();
		StringTrie<Byte>.TrieBuilder newBuilder = trieGrammarStore.newBuilder();
		int i = 35;
		
		String[] tst = {"{{-}}"};
		
		for (String string : tst) {
			newBuilder.append(string, Byte.valueOf((byte)i++));
		}
		
		trieGrammarStore.commit(newBuilder);
        
		Byte fond = trieGrammarStore.get("{{-}}aaa");
		System.out.println("PrefixRemover.test6() " + fond);
	}
	
	private static void test8() {
		ReadOnlyTrieWordNet instance = TrieZippedProvider.getInstance();
		GrammarRelation[] possibleGrammarForms = instance.getPossibleGrammarForms("самолет");
		possibleGrammarForms = instance.getPossibleGrammarForms("самолёт");
		possibleGrammarForms = instance.getPossibleGrammarForms("фвфаыфуууыауыауы");
		possibleGrammarForms = instance.getPossibleGrammarForms("вертолет");
		GrammarRelation[] forms1 = instance.getPossibleGrammarForms("вертолёт");
		System.setProperty(WordNetProvider.ENGINE_CONFIG_DIR_PROP,"D:/tmp");
		possibleGrammarForms = WordNetProvider.getInstance().getPossibleGrammarForms("самолет");
		possibleGrammarForms = WordNetProvider.getInstance().getPossibleGrammarForms("самолёт");
		System.out.println("PrefixRemover.test8()");
	}
	
	private static void timeTest() {
		ReadOnlyWordNet loaded;
		try {
			loaded = ReadOnlyMapWordNet.load(RWNET_PATH);
			String[] keys = loaded.getAllGrammarKeys();
//			Map<String, Byte> dataMap = new HashMap<String, Byte>();
			List<String> testList = new ArrayList<String>();
			
			for (int i = 0; i < 500000; i++) {
				testList.add(keys[(int) (Math.random() * (keys.length - 1))]);
			}
			
			
			StringTrie<GrammarRelation[]> trieGrammarStore = new TrieGrammarStore();
			TrieBuilder newBuilder = trieGrammarStore.newBuilder();
			Map<String, GrammarRelation[]> relationsMap = new HashMap<String, GrammarRelation[]>();
			int n = testList.size();
			for (int i = 0; i < n; i++) {
//				Byte b = (byte)(Math.random() * 100);
//				dataMap.put(keys[i], b);
				String word = testList.get(i);
				GrammarRelation[] forms = loaded.getPossibleGrammarForms(word);
				relationsMap.put(word, forms);
				newBuilder.append(word, forms);
			}
			trieGrammarStore.commit(newBuilder);
			long n1 = System.currentTimeMillis();
			for (int i = 0; i < n; i++) {
				GrammarRelation[] found = trieGrammarStore.get(testList.get(i));
			}
			System.out.println("Trie: " + (System.currentTimeMillis() - n1));
			
			long n2 = System.currentTimeMillis();
			for (int i = 0; i < n; i++) {
				GrammarRelation[] found = relationsMap.get(testList.get(i));
			}
			System.out.println("HashMap: " + (System.currentTimeMillis() - n2));

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void test9() {

		try {
			ReadOnlyTrieWordNet instance = TrieZippedProvider.getInstance();
			ReadOnlyWordNet readOnlyWordNet = new ReadOnlyMapWordNet(new DataInputStream(new BufferedInputStream(new FileInputStream("D:/tmp/rwnet.dat"))));
			SimpleWordNet simpleWordNet = new SimpleWordNet(readOnlyWordNet);
			instance.useWordInfo(simpleWordNet);
			instance.store(new File("newdict.dat"));
		} catch (FileNotFoundException e) {
			System.err.println("Wordnet is corrupted rebuilding...");
		} catch (IOException e) {
			System.err.println("Wordnet is corrupted rebuilding...");
		}
	}
	
	private static void test10() {
		try {
			ReadOnlyTrieWordNet instance = TrieZippedProvider.getInstance();
			ReadOnlyWordNet mapWordNet = ReadOnlyMapWordNet.load(RWNET_PATH);
			SimpleWordNet simpleWordNet = new SimpleWordNet(mapWordNet);
			instance.initSequences(simpleWordNet.getSequenceMap());
			instance.store(new File("newdict.dat"));
		} catch (FileNotFoundException e) {
			System.err.println("Wordnet is corrupted rebuilding...");
		} catch (IOException e) {
			System.err.println("Wordnet is corrupted rebuilding...");
		}
	}
	
	private static void testSearch() {
		GrammarRelation[] forms1 = WordNetProvider.getInstance().getPossibleGrammarForms("вертолёт");
		System.out.println(Arrays.toString(forms1));
//		GrammarRelation[] forms2 = WordNetProvider.getInstance().getPossibleGrammarForms("километр");
//		System.out.println(forms2);
		forms1 = WordNetProvider.getInstance().getPossibleGrammarForms("законфузиться");
		System.out.println(Arrays.toString(forms1));
	}

	private static void testPrediction() {
//		InputStream inputStream = null;
//		try {
//			ReadOnlyWordNet mapWordNet = ReadOnlyMapWordNet.load(RWNET_PATH);
//			inputStream = new BufferedInputStream(new FileInputStream(PREDICTION_TRIE_PATH));
//			StringToByteTrie basesTrie = new StringToByteTrie();
//			StringToByteTrie endingsTrie = new StringToByteTrie();
//			basesTrie.read(inputStream);
//			endingsTrie.read(inputStream);
//			IPredictionHelper helper = new TriePredictionHelper(mapWordNet, basesTrie, endingsTrie);
//			System.setProperty(WordNetProvider.ENGINE_CONFIG_DIR_PROP,"D:/tmp");
			IPredictionHelper helper = PredictionUtil.getPredictionHelper();
		
			String word = "пейсатому";
			GrammarRelation[] forms = helper.getForms(word);
			System.out.println(word + ":" + Arrays.toString(forms));
			word = "криворогий";
			forms = helper.getForms(word);
			System.out.println(word + ":" + Arrays.toString(forms));
			word = "косоухого";
			forms = helper.getForms(word);
			System.out.println(word + ":" + Arrays.toString(forms));
			word = "рукозадым";
			forms = helper.getForms(word);
			System.out.println(word + ":" + Arrays.toString(forms));
			word = "фут";
			forms = helper.getForms(word);
			System.out.println(word + ":" + Arrays.toString(forms));
			word = "килофут";
			forms = helper.getForms(word);
			System.out.println(word + ":" + Arrays.toString(forms));
//		} catch (IOException e) {
//			e.printStackTrace();
//		} finally {
//			if (inputStream != null)
//				try {inputStream.close();} catch (IOException e) {}
//		}
	}
	
	private static void test11() {
		int lowBound = 0;
		int highBound = 1000000;
		int start = lowBound;
//		ReadOnlyWordNet instance;
		String search = "сурьезнейшая";
		String[] keys;
		AbstractWordNet instance = WordNetProvider.getInstance();
		keys = instance.getAllGrammarKeys();
		highBound = keys.length;
		int step = (highBound - lowBound) / 2;
		while (step > 2) {
			if (searchBroken(search, keys, lowBound, highBound, (ReadOnlyWordNet) instance)) {
				lowBound += step;
			} else {
				lowBound -= step;
			}
			step /= 2;
		}
		while (!searchBroken(search, keys, lowBound, highBound, (ReadOnlyWordNet) instance)) {
			lowBound--;
		}
		step = (highBound - lowBound) / 2;
		while (step > 2) {
			if (searchBroken(search, keys, lowBound, highBound, (ReadOnlyWordNet) instance)) {
				highBound -= step;
			} else {
				highBound += step;
			}
			step /= 2;
		}
		while (!searchBroken(search, keys, lowBound, highBound, (ReadOnlyWordNet) instance)) {
			highBound++;
		}

		System.out.println(String.format("[ %d, %d ]", lowBound, highBound));
	}
	
	private static void test12() {
		String search = "сурьезнейшая";
		AbstractWordNet instance = WordNetProvider.getInstance();
		findSearchBroken(search, 47538, 2122392, (ReadOnlyWordNet) instance);

	}
	
	private static void test13() {
		ReadOnlyTrieWordNet instance = TrieZippedProvider.getInstance();
		GrammarRelation[] possibleGrammarForms = instance.getPossibleGrammarForms("пеaревалившемся");
		possibleGrammarForms = instance.getPossibleGrammarForms("сурьезнейшая");
		System.out.println("Forms: " + Arrays.toString(possibleGrammarForms));
	}
	
	private static void test14() {
		StringToByteTrie trieGrammarStore = new StringToByteTrie();
		StringTrie<Byte>.TrieBuilder newBuilder = trieGrammarStore.newBuilder();
		int i = 35;
		
		String[] tst = {"захламливающемся",
						"подседлаю",
						};
		
		for (String string : tst) {
			newBuilder.append(string, Byte.valueOf((byte)i++));
		}
		
		trieGrammarStore.commit(newBuilder);
        
		for (String string : tst) {
			Byte find = trieGrammarStore.get(string);
			System.out.println(string + " = " + find);
		}

	}
	
	private static boolean searchBroken(String search, String[] keys, ArrayList<Integer> included, ReadOnlyWordNet loaded) {
		try {
			StringTrie<GrammarRelation[]> trieGrammarStore = new TrieGrammarStore();
			TrieBuilder newBuilder = trieGrammarStore.newBuilder();
			for (int i = 0; i < included.size(); i++) {
				int index = included.get(i);
				newBuilder.append(keys[index], loaded.getPossibleGrammarForms(keys[index]));
			}
			trieGrammarStore.commit(newBuilder);
			for (int i = 0; i < included.size(); i++) {
				int index = included.get(i);
				GrammarRelation[] found = trieGrammarStore.get(keys[index]);
				return false;
//				if (!Arrays.equals(found, loaded.getPossibleGrammarForms(keys[index]))) {
//					return true;
//				}
//				found = trieGrammarStore.get(keys[index] + "aaa");
//				if (found != null) {
//					return true;
//				}
			}
		} catch (Throwable e) {
			return true;
		}
		return false;
	}
	
	private static void findSearchBroken(String search, int start, int end, ReadOnlyWordNet loaded) {
		String[] keys = loaded.getAllGrammarKeys();
		ArrayList<Integer> included = new ArrayList<Integer>();
		for (int i = start; i < end; i++) {
			included.add(i);
		}
		for (int i = 0; i < included.size(); i++) {
			Integer removed = included.remove(i);
			if (!searchBroken(search, keys, included, loaded)) {
				included.add(i,removed);
			} else {
				System.out.println(removed + "is not necessary to reproduce");
				i--;
			}
		}
		
		boolean a = searchBroken(search, keys, included, loaded);
		System.out.println("Search is broken: " + a);
		
		for (int i = 0; i < included.size(); i++) {
			Integer removed = included.remove(i);
			if (!searchBroken(search, keys, included, loaded)) {
				included.add(i,removed);
			} else {
				i--;
			}
		}
		
//			while (broken(keys, start, COUNT)) {
//				start++;
//			}
		for (Integer integer : included) {
			System.out.println(keys[integer]);
		}
	}
	
	private static boolean searchBroken(String search, String[] keys, int start, int end, ReadOnlyWordNet loaded) {
		try {
			StringTrie<GrammarRelation[]> trieGrammarStore = new TrieGrammarStore();
			TrieBuilder newBuilder = trieGrammarStore.newBuilder();
			for (int i = start; i < end; i++) {
				Byte b = (byte)(Math.random() * 100);
	//			dataMap.put(keys[i], b);
				newBuilder.append(keys[i], loaded.getPossibleGrammarForms(keys[i]));
			}
			trieGrammarStore.commit(newBuilder);
			GrammarRelation[] relations = trieGrammarStore.get(search);
			return false;
		} catch (Throwable e) {
			return true;
		}
	}
	
	private static void testRebuid() {
		ReadOnlyWordNet wnet = WordNetAutoPatcher.obtainWordNet("D:/tmp/vocab/rwnet.dat");
		GrammarRelation[] forms1 = wnet.getPossibleGrammarForms("вертолёт");
		System.out.println(Arrays.toString(forms1));
	}
}
