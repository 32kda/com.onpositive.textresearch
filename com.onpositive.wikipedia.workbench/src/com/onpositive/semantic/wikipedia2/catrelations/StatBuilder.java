package com.onpositive.semantic.wikipedia2.catrelations;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import com.onpositive.semantic.wikipedia2.WikiEngine2;
import com.onpositive.semantic.wikipedia2.fulltext.WordIndex;
import com.onpositive.semantic.wordnet.AbstractWordNet;
import com.onpositive.semantic.wordnet.TextElement;
import com.onpositive.semantic.wordnet.WordNetProvider;

public class StatBuilder {

	static class Entry implements Comparable<Entry> {
		TextElement element;

		public Entry(TextElement element) {
			super();
			this.element = element;
		}

		int count;

		@Override
		public int compareTo(Entry o) {
			return this.count - o.count;
		}

		@Override
		public String toString() {
			return element.getBasicForm() + ":" + count;
		}
	}

	static class BigramEntry implements Comparable<BigramEntry> {
		TextElement element0;
		TextElement element1;

		public BigramEntry(TextElement element0, TextElement element1) {
			super();
			this.element0 = element0;
			this.element1 = element1;
		}

		int count;

		@Override
		public int compareTo(BigramEntry o) {
			return this.count - o.count;
		}

		@Override
		public String toString() {
			String string = getKey();
			return string + ":" + count;
		}

		String getKey() {
			return element0.getBasicForm()
					+ (element1 != null ? (" " + element1.getBasicForm()) : "");
		}
	}

	static class Trigram implements Comparable<Trigram> {
		String key;

		public Trigram(String key) {
			super();
			this.key=key;
		}

		int count;

		@Override
		public int compareTo(Trigram o) {
			return this.count - o.count;
		}

		@Override
		public String toString() {
			String string = getKey();
			return string + ":" + count;
		}

		String getKey() {
			return key;
		}
	}

	public static void main(String[] args) {
		WikiEngine2 eng = new WikiEngine2("D:\\se2\\ruwiki");
		AbstractWordNet instance = WordNetProvider.getInstance();
		int[] notRedirectDocumentIDs = eng.getNotRedirectDocumentIDs();
		HashMap<String, BigramEntry> entr = new HashMap<String, BigramEntry>();
		HashMap<String, Trigram> entr1 = new HashMap<String, Trigram>();
		int i = 0;

		for (int q : notRedirectDocumentIDs) {
			i++;
			if (i % 1000 == 0) {
				System.out.println(i);
			}
			if(i>100000){
				break;
			}
			String text = eng.getDocument(q).getPlainTextAbstract();
			/*ArrayList<HashSet<TextElement>> elements = WordIndex.toWords(
					instance, text);
			for (int a = 0; a < elements.size(); a++) {
				ArrayList<BigramEntry> e = createEntry(a, elements);
				for (BigramEntry qa : e) {
					BigramEntry bigramEntry = entr.get(qa.getKey());
					if (bigramEntry == null) {
						bigramEntry = qa;
						entr.put(qa.getKey(), qa);
					}
					bigramEntry.count++;
				}
			}*/
			ArrayList<String> elements2 = WordIndex.toWords2(
					instance, text);
			for (int a = 0; a < elements2.size(); a++) {
				String e = createTrigramEntry(a, elements2);
				Trigram bigramEntry = entr1.get(e);
				if (bigramEntry == null) {
						bigramEntry = new Trigram(e);
						entr1.put(e,bigramEntry);
					}
					bigramEntry.count++;
				}
			}
		
		try {
			ArrayList<BigramEntry> es = new ArrayList<StatBuilder.BigramEntry>(
					entr.values());
			Collections.sort(es);
			PrintWriter pw = new PrintWriter(new File(eng.getLocation(),
					"bstat4.txt"));
			for (BigramEntry e : es) {
				if (e.count>1){
				pw.println(e);
				}
			}
			pw.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			ArrayList<Trigram> es1 = new ArrayList<Trigram>(entr1.values());
			Collections.sort(es1);
			PrintWriter pw = new PrintWriter(new File(eng.getLocation(),
					"tstat4.txt"));
			for (Trigram e : es1) {
				if (e.count>1){
					pw.println(e);
				}
			}
			pw.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static String createTrigramEntry(int a, ArrayList<String> elements) {
		// TODO Auto-generated method stub
		String t0 = elements.get(a);
		String t1 = a < elements.size() - 1 ? elements.get(a + 1):null;
		String t2 = a < elements.size() - 2 ? elements.get(a + 2):null;
		StringBuffer bld=new StringBuffer();
		bld.append(t0);
		if (t1!=null){
			bld.append(' ');
			bld.append(t1);
		}
		if (t2!=null){
			bld.append(' ');
			bld.append(t2);
		}
		return bld.toString();
	}

	private static ArrayList<BigramEntry> createEntry(int a,
			ArrayList<HashSet<TextElement>> elements) {
		HashSet<TextElement> t0 = elements.get(a);
		HashSet<TextElement> t1 = a < elements.size() - 1 ? elements.get(a + 1)
				: null;
		ArrayList<BigramEntry> result = new ArrayList<StatBuilder.BigramEntry>();
		if (t1 == null) {
			for (TextElement q : t0) {
				result.add(new BigramEntry(q, null));
			}
		} else {
			for (TextElement q : t0) {
				for (TextElement q1 : t1) {
					result.add(new BigramEntry(q, q1));
				}
			}
		}
		return result;
	}

	/*private static ArrayList<Trigram> createTrigramEntry(int a,
			ArrayList<HashSet<TextElement>> elements) {
		HashSet<TextElement> t0 = elements.get(a);
		HashSet<TextElement> t1 = a < elements.size() - 1 ? elements.get(a + 1)
				: null;
		HashSet<TextElement> t2 = a < elements.size() - 2 ? elements.get(a + 2)
				: null;
		ArrayList<Trigram> result = new ArrayList<Trigram>();
		if (t1 == null) {
			for (TextElement q : t0) {
				result.add(new Trigram(q, null, null));
			}
		} else {
			if (t2 == null) {
				for (TextElement q : t0) {
					for (TextElement q1 : t1) {
						result.add(new Trigram(q, q1, null));
					}
				}
			} else {
				for (TextElement q : t0) {
					for (TextElement q1 : t1) {
						for (TextElement q2 : t2) {
							result.add(new Trigram(q, q1, q2));
						}
					}
				}
			}
		}
		return result;
	}*/

}
