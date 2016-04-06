package com.onpositive.semantic.wikipedia2.catrelations;

import java.util.ArrayList;
import java.util.Collections;

import com.onpositive.semantic.wikipedia2.WikiEngine2;
import com.onpositive.semantic.wikipedia2.properties.PropertyNamesIndex;
import com.onpositive.semantic.wikipedia2.properties.PropertyNamesIndex.PropertyGroup;

public class Categorizer {

	static class Entry implements Comparable<Entry> {
		String s;

		public Entry(String s, int k) {
			super();
			this.s = s;
			this.k = k;
		}

		int k;

		@Override
		public int compareTo(Entry o) {
			return k - o.k;
		}
	}

	public static void main(String[] args) {
		final WikiEngine2 wikiEngine2 = new WikiEngine2("D:/se2/ruwiki");
		PropertyNamesIndex index = wikiEngine2.getIndex(PropertyNamesIndex.class);
		ArrayList<PropertyGroup> arrayList = new ArrayList<PropertyGroup>(index.getGroups());
		Collections.sort(arrayList);
		Collections.reverse(arrayList);
		for (PropertyGroup g:arrayList){
			if (g.toString().startsWith("group длина")){
				System.out.println(g);
			}
			if (g.occurenceCount<1000){
				break;
			}
			System.out.println(g);
		}
	}
}
