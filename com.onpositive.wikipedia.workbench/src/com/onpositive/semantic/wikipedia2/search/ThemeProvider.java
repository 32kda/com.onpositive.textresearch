package com.onpositive.semantic.wikipedia2.search;

import java.util.LinkedHashMap;

import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.IntIntOpenHashMapSerialable;
import com.carrotsearch.hppc.IntOpenHashSet;
import com.onpositive.compactdata.IntComparator;
import com.onpositive.compactdata.TimIntSort;
import com.onpositive.semantic.search.core.SearchRequest;
import com.onpositive.semantic.wikipedia2.WikiEngine2;
import com.onpositive.semantic.wikipedia2.docclasses.DocumentClassService;
import com.onpositive.semantic.wikipedia2.docclasses.DocumentClasses;

public class ThemeProvider {

	public static class SortedDocs {
		public IntIntOpenHashMapSerialable documentIDsToRelevance;

		public IntIntOpenHashMapSerialable documentIDsRerated;
		public int[] documentIDs;
		long timeStamp;
	}

	private static final int MAX_TIME = 10;

	private static final int MAX_CACHE_SIZE = 20;
	protected LinkedHashMap<String, SortedDocs> docs = new LinkedHashMap<String, SortedDocs>();
	private SortedDocs sortedDocs;

	WikiEngine2 engine;

	DocumentClassService index;

	public ThemeProvider(WikiEngine2 engine) {
		this.engine=engine;
		index = engine.getIndex(DocumentClassService.class);
	}
	
	public synchronized SortedDocs getDocs(WikiEngine2 engine, String category,
			long currentTimeMillis, SearchRequest request) {
		long currentTimeMillisZZ = System.currentTimeMillis();
		if (docs.containsKey(getCacheKey(category, request))) {
			SortedDocs sortedDocs = docs.get(getCacheKey(category, request));
			sortedDocs.timeStamp = currentTimeMillisZZ;
			return sortedDocs;
		}
		SortedDocs sortedDocs = getSortedDocs(engine, category,
				currentTimeMillis, request.getMaxRelevancy(),request.isOnlyEvents(),request.isShowBlackList());

		long l1 = System.currentTimeMillis();
		sortedDocs.timeStamp = l1;
		long delta = l1 - currentTimeMillisZZ;
		if (delta > MAX_TIME) {
			if (docs.size() > MAX_CACHE_SIZE) {
				long min = Long.MAX_VALUE;
				String toRemove = null;
				for (String s : docs.keySet()) {
					SortedDocs sortedDocs2 = docs.get(s);
					if (min < sortedDocs2.timeStamp) {
						min = sortedDocs2.timeStamp;
						toRemove = s;
					}
				}
				if (toRemove != null) {
					docs.remove(toRemove);
				}
			}
			docs.put(getCacheKey(category, request), sortedDocs);
		}
		System.out.println("Sort time:" + delta);
		return sortedDocs;
	}

	String getCacheKey(String category, SearchRequest request) {
		return category + request.getMaxRelevancy() + request.isOnlyEvents()
				+ request.isShowBlackList();
	}

	public SortedDocs getSortedDocs(WikiEngine2 engine, final String category,
			long currentTimeMillis, int mr, boolean onlyEvents, boolean showBlackList) {
		final IntIntOpenHashMapSerialable allDocs = engine
				.getRelevantDocs(category);

		int[] array = allDocs.keys().toArray();
		sort(engine, allDocs, array);
		if (onlyEvents||!showBlackList){
			IntArrayList newList=new IntArrayList();
			for (int q:array){
				if (!filterOut(q,onlyEvents,showBlackList)){
					newList.add(q);
				}
			}
			array=newList.toArray();
		}
		int max = array.length;
		for (int i = 0; i < array.length; i++) {
			if (allDocs.get(array[i]) > mr) {
				max = i;
				break;
			}
		}
		if (max != array.length) {
			int[] rs = new int[max];
			System.arraycopy(array, 0, rs, 0, max);
			array = rs;
		}
		SortedDocs result = new SortedDocs();
		result.documentIDs = array;
		result.documentIDsToRelevance = allDocs;
		return result;
	}
	

	private boolean filterOut(int q, boolean onlyEvents, boolean showBlackList) {
		if (!showBlackList){
			if (engine.isBlackListed(q)){
				return true;
			}
		}
		if (onlyEvents&&engine.getRole().endsWith("vocab")){
			if (!index.hasClass(q, DocumentClasses.EVENTCLASS)){
				return true;
			}
		}
		return false;
	}

	public void sort(WikiEngine2 engine,
			final IntIntOpenHashMapSerialable allDocs, int[] array) {
		IntComparator c = new IntComparator() {

			@Override
			public int compare(int l1, int l2) {
				int i = allDocs.get(l1);
				int j = allDocs.get(l2);
				return i - j;
			}

		};
		TimIntSort.sort(array, c);
	}

	public final int rate(int l1) {
		return sortedDocs.documentIDsToRelevance.get(l1);
	}

	public void clear() {
		docs.clear();
		sortedDocs = null;
	}

	public void clearSort() {
		for (SortedDocs d : docs.values()) {
			d.documentIDsRerated = null;
		}
	}
}
