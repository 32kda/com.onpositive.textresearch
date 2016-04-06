package com.onpositive.wordnet.tests;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import com.onpositive.semantic.words3.hds.StringToByteTrie;
import com.onpositive.semantic.words3.hds.StringTrie;

public class PrefixTest extends TestCase{
	
	private static final String[] SET1 = {"надгрызает",
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
	
	private static final String[] SET2 = { "Ба", "Баба", "Бабай", "Бабахнуть",
			"Бабашка", "Бабёнка", "Бабёшка", "Бабий", "Бабит", "Бабитный",
			"Бабитовый", "Бабища", "Бабка", "Бабник", "Бабничать", "Бабочка",
			"Бабуся", "Бабушка", "Бабьё", "Багаж", "Багажник", "Багажный",
			"Багет", "Багетный", "Багетчик", "Багетчица", "Багор", "Багорный",
			"Багорщик", "Багрение", "Багренный", "Багренье", "Багрец",
			"Багрённый", "Багрить", "Багрить", "Багроветь", "Багровый",
			"Багрянеть", "Багрянец", "Багрянить", "Багряница", "Багряный",
			"Бадеечка", "Бадеечный", "Бадейка", "Бадья", "Бадяга", "База",
			"Базальт", "Базальтовый", "Базар", "Базарить", "Базарный",
			"Базаровщина", "Базедова", "Базилика", "Базировать",
			"Базироваться", "Базис", "Базисный", "Базовый", "Баиньки", "Бай",
			"Байбак", "Байдарка", "Байка", "Байковый", "Байронизм",
			"Байронический", "Бак", "Бакалавр", "Бакалаврский", "Бакалейный",
			"Бакалея", "Бакен", "Бакенбард", "Бакенщик", "Баки", "Баккара",
			"Баклага", "Баклажан", "Баклажанный", "Баклажка", "Баклан",
			"Баклуши", "Баклушничать", "Баковый", "Бактериальный",
			"Бактеризованный", "Баян", "Баянист", "БНР", "Бваке", "Бивуак" };

	public void test01() {
		StringToByteTrie trieGrammarStore = new StringToByteTrie();
		StringTrie<Byte>.TrieBuilder newBuilder = trieGrammarStore.newBuilder();
		newBuilder.append("aa", Byte.valueOf((byte) 34));
		trieGrammarStore.commit(newBuilder);
		Byte found = trieGrammarStore.get("aa");
		assertEquals(34, found.byteValue());
	}
	
	public void test02() {
		findTest(MapBuilder.create().entryOf("aa",(byte) 34).entryOf("ba",(byte) 31).get());
	}
	
	public void test03() {
		StringToByteTrie trieGrammarStore = new StringToByteTrie();
		StringTrie<Byte>.TrieBuilder newBuilder = trieGrammarStore.newBuilder();
		newBuilder.append("aa", Byte.valueOf((byte) 34));
		newBuilder.append("ab", Byte.valueOf((byte) 31));
		trieGrammarStore.commit(newBuilder);
		Byte found = trieGrammarStore.get("aa");
		assertEquals(34, found.byteValue());
		found = trieGrammarStore.get("ab");
		assertEquals(31, found.byteValue());
		found = trieGrammarStore.get("a");
		assertNull(found);
	}
	
	public void test04() {
		findTest(MapBuilder.create().entryOf("aaabb",(byte) 7).entryOf("aaaaa",(byte) 49).get());
	}
	
	public void test05() {
		StringToByteTrie trieGrammarStore = buildTreeSearchTest(SET1);
        int k = 35;
		for (String string : SET1) {
			Byte find = trieGrammarStore.get(string);
			assertEquals(find.byteValue(), (byte)k++);
		}
		
	}
	
	public void testTreeSearch01() {
		StringToByteTrie trieGrammarStore = buildTreeSearchTest(SET1);
		Collection<String> strings = trieGrammarStore.getStrings("на");
		assertEquals(SET1.length, strings.size());
		for (String string : SET1) {
			if (!strings.contains(string.replace('ё','е'))) {
				throw new AssertionFailedError(string + " not found. Contents of source & result data sets should be equal.");
			}
		}
	}
	
	public void testTreeSearch02() {
		StringToByteTrie trieGrammarStore = buildTreeSearchTest(SET1);
		Collection<String> strings = trieGrammarStore.getStrings("подг");
		assertEquals(0, strings.size());
	}
	
	public void testTreeSearch03() {
		StringToByteTrie trieGrammarStore = buildTreeSearchTest(SET1);
		Collection<String> strings = trieGrammarStore.getStrings("надгрызш");
		assertEquals(10, strings.size());
	}
	
	public void testTreeSearch04() {
		StringToByteTrie trieGrammarStore = buildTreeSearchTest(SET2);
		Collection<String> strings = trieGrammarStore.getStrings("бнр");
		assertEquals(1, strings.size());
	}
	
	public void testTreeSearch05() {
		StringToByteTrie trieGrammarStore = buildTreeSearchTest(SET2);
		List<String> strings = trieGrammarStore.getStrings("баб");
		assertEquals(18, strings.size());
		strings = trieGrammarStore.getStrings("бнопня");
		assertEquals(1, strings.size());
	}

	
	protected StringToByteTrie buildTreeSearchTest(String[] set) {
		StringToByteTrie trieGrammarStore = new StringToByteTrie();
		StringTrie<Byte>.TrieBuilder newBuilder = trieGrammarStore.newBuilder();
		int i = 35;
		for (String string : set) {
			newBuilder.append(string.toLowerCase(), Byte.valueOf((byte)i++));
		}
		trieGrammarStore.commit(newBuilder);
		return trieGrammarStore;
	}
	
	private void findTest(Map<String, Byte> data) {
		StringToByteTrie trieGrammarStore = new StringToByteTrie();
		StringTrie<Byte>.TrieBuilder newBuilder = trieGrammarStore.newBuilder();
		for (String key : data.keySet()) {
			newBuilder.append(key, data.get(key));
		}
		trieGrammarStore.commit(newBuilder);
		for (String key : data.keySet()) {
			Byte found = trieGrammarStore.get(key);
			assertEquals(((Byte)data.get(key)), Byte.valueOf(found.byteValue()));
		}
	}
	
	private static class MapBuilder {
		
		private Map<String, Byte> map = new HashMap<String, Byte>();
		
		private MapBuilder() {
			
		}
		
		public static MapBuilder create() {
			return new MapBuilder();
		}
		
		public MapBuilder entryOf(String key, Byte value) {
			map.put(key, value);
			return this;
		}
		
		public Map<String, Byte> get() {
			return map;
		}
		
	}
	
}
