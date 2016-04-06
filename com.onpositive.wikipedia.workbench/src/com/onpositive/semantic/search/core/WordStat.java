package com.onpositive.semantic.search.core;

import java.util.ArrayList;
import java.util.List;


public class WordStat {
	
	public static class StatEntry{
		String word;
		int count;
		int totalCount;
		
		double ratio;
	}
	
	protected ArrayList<StatEntry>entries=new ArrayList<StatEntry>();

	public void add(String word, int i, int wordCount) {
		StatEntry statEntry = new StatEntry();
		statEntry.word=word;
		statEntry.count=i;
		statEntry.totalCount=wordCount;
		statEntry.ratio=((double)i)/wordCount;
		entries.add(statEntry);
	}

	public List<StatEntry> getEntries() {
		return entries;
	}
}
