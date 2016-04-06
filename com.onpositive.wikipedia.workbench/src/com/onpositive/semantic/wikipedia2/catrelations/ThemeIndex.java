package com.onpositive.semantic.wikipedia2.catrelations;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import com.carrotsearch.hppc.IntIntOpenHashMap;
import com.carrotsearch.hppc.IntIntOpenHashMapSerialable;
import com.onpositive.compactdata.TwoIntToByteMap;
import com.onpositive.semantic.categorization.core.SearchSystem;
import com.onpositive.semantic.search.core.ICategory;
import com.onpositive.semantic.wikipedia2.WikiCat;
import com.onpositive.semantic.wikipedia2.WikiEngine2;
import com.onpositive.tstruct.CategoryNode;
import com.onpositive.tstruct.StructureNode;
import com.onpositive.tstruct.StructureNode.Entry;

public class ThemeIndex {

	private static final int EXTRACTION_LIMIT = 6;
	protected TwoIntToByteMap h = new TwoIntToByteMap();
	private WikiEngine2 engine;

	public ThemeIndex(WikiEngine2 engine) {
		this.engine = engine;
		build(engine, true);
	}

	public IntIntOpenHashMapSerialable getRelevantDocs(String category) {
		IntIntOpenHashMapSerialable mm = new IntIntOpenHashMapSerialable();
		if (category==null){
			return mm;
		}
		CategoryNode schema = SearchSystem.getSchema();
		HashSet<String> mappings = schema != null ? schema.getRootMappings(
				category, engine.id()) : new HashSet<String>();
		mappings.add(category);
		for (String s : mappings) {
			
			int ll = getCategoryId(s.replace(' ', '_'));
			if (ll > 0) {
				IntIntOpenHashMap selectDocs = selectDocs(ll, engine.getKind().equals("vocab")?100:40);
				mm.putAll(selectDocs);
			}
		}
		return mm;
	}
	public IntIntOpenHashMapSerialable getAllDocs(String category) {
		//cleanupTable(engine);
		if (themeToDocument.serializableMap.containsKey(category)){
			return themeToDocument.serializableMap.get(category);
		}
		return getRelevantDocs(category);
	}

	public int getCategoryId(String s) {
		return engine.getCategoryTitles().get(s);
	}

	static class ThemeToDocument implements Serializable{

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		HashMap<String, IntIntOpenHashMapSerialable> serializableMap = new HashMap<String, IntIntOpenHashMapSerialable>();
	}

	protected ThemeToDocument themeToDocument;

	public void build(WikiEngine2 engine, boolean canReload) {
		this.engine = engine;
		CompositeEstimator compositeEstimator = new CompositeEstimator(engine);
		String location = engine.getLocation();
		File fl = new File(location, "theme.index");
		if (fl.exists() && canReload) {
			try {
				h.read(fl.getAbsolutePath());
				ObjectInputStream streem=new ObjectInputStream(new BufferedInputStream(new FileInputStream(new File(engine.getLocation(),"docByTheme.dat"))));
				themeToDocument=(ThemeToDocument) streem.readObject();
				streem.close();
				return;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		 themeToDocument= new ThemeToDocument();
		h = new TwoIntToByteMap();
		CategoryNode schema = SearchSystem.getSchema();
		HashSet<Entry> gatherExclusions = schema.gatherExclusions();
		HashSet<Entry> gatherInclusions = schema.gatherInclusions();
		int[] categoryKeys = engine.getCategoryRenumberer().getIds();
		int z = 0;
		for (int q : categoryKeys) {
			z++;
			ICategory category = engine.getCategory(q);
			// CombinedCategoryCriteria combinedCategoryCriteria = new
			// CombinedCategoryCriteria(engine, null, null, category);
			// ICategoryStatusCriteria combinedCategoryCriteria = new
			// DefaultCriteriaBuilder().create(category);
			if (z % 1000 == 0) {
				System.out.println(z);
			}
			ICategory[] sameThemeChildren = category.getSubCategories();
			String title = category.getTitle();
			for (ICategory qa : sameThemeChildren) {
				WikiCat c = (WikiCat) qa;
				// here we should block prohibited transfers
				Entry o = new Entry(title, c.getTitle());
				if (gatherExclusions.contains(o)) {
					continue;
				}
				if (gatherInclusions.contains(o)) {
					h.put(q, c.getIntId(), (byte) 1);
					continue;
				}

				if (true) {
					byte rate = (byte) compositeEstimator.relation(q,
							c.getIntId(), null);
					if (rate < 20) {
						h.put(q, c.getIntId(), rate);
					}
					// byte b = h.get(q, c.q);
				}
			}
		}
		try {
			h.write(fl.getAbsolutePath());
		} catch (IOException e) {
			e.printStackTrace();
		}
		ArrayList<StructureNode> elements = schema.getElements();
		for (StructureNode n : elements) {
			String title = n.getTitle();
			IntIntOpenHashMapSerialable relevantDocs = getRelevantDocs(title);
			themeToDocument.serializableMap.put(title, relevantDocs);
		}
		try {
			cleanupTable(engine);
			//store final table
			ObjectOutputStream streem=new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(new File(engine.getLocation(),"docByTheme.dat"))));
			streem.writeObject(themeToDocument);
			streem.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	void cleanupTable(WikiEngine2 engine) {
		for (int doc:engine.getDocumentIDs()){
			int min=Integer.MAX_VALUE;
			/*String string = engine.getPageTitles().get(doc);
			if (string.equals("San Marino RTV")){
				System.out.println("A");
			}*/
			for (String cat:themeToDocument.serializableMap.keySet()){
				IntIntOpenHashMapSerialable intIntOpenHashMapSerialable = themeToDocument.serializableMap.get(cat);
				if (intIntOpenHashMapSerialable.containsKey(doc)){
					min=Math.min(min, intIntOpenHashMapSerialable.get(doc));
				}
			}
			for (String cat:themeToDocument.serializableMap.keySet()){
				IntIntOpenHashMapSerialable intIntOpenHashMapSerialable = themeToDocument.serializableMap.get(cat);
				if (intIntOpenHashMapSerialable.containsKey(doc)){
					int b = intIntOpenHashMapSerialable.get(doc);
					if (b-min>EXTRACTION_LIMIT){
						intIntOpenHashMapSerialable.remove(doc);
					}
				}
			}
			
		}
	}

	public IntIntOpenHashMap select(int word, int maxCost) {
		IntIntOpenHashMap mm = new IntIntOpenHashMap();
		visit(word, 0, maxCost, mm);
		return mm;
	}

	public IntIntOpenHashMap selectDocs(int word, int maxCost) {
		IntIntOpenHashMap mm = new IntIntOpenHashMap();
		visit(word, 0, maxCost, mm);
		// best matches
		IntIntOpenHashMap documentsMap = new IntIntOpenHashMap();
		for (int c : mm.keys().toArray()) {

			int[] pages = engine.getPages(c);
			int rating = mm.get(c);

			for (int p : pages) {
				if (documentsMap.containsKey(p)) {
					int i = documentsMap.get(p);
					if (rating < 20 && i < 20) {
						int min = Math.min(i, rating) - 1;
						documentsMap.put(p, Math.max(min, 0));
					} else {
						int min = Math.min(i, rating);
						documentsMap.put(p, min);
					}
				} else {
					documentsMap.put(p, rating);
				}
			}
		}
		return documentsMap;
	}

	private void visit(int word, int cLevel, int maxCost, IntIntOpenHashMap mm) {
		boolean containsKey = mm.containsKey(word);
		boolean incr = true;
		if (containsKey) {
			int j = mm.get(word);
			if (cLevel < j) {
				mm.put(word, cLevel);
				incr = true;
			}
		} else {
			mm.put(word, cLevel);
			incr = true;
		}
		if (incr && cLevel < maxCost && engine != null) {
			int[] subcategories = engine.getSubCategories(word);
			for (int c : subcategories) {
				// System.out.println(engine.getCategoryTitles().get(word)+":->"+engine.getCategoryTitles().get(c));
				byte b = h.get(word, c);
				if (b != 0) {
					visit(c, cLevel + b, maxCost, mm);
				}
			}
		}
	}

	public void clean() {
		build(engine, false);
	}
}