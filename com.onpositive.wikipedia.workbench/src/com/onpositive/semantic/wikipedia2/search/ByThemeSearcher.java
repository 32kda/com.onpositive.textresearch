package com.onpositive.semantic.wikipedia2.search;

import java.util.HashMap;
import java.util.LinkedHashMap;

import com.carrotsearch.hppc.IntIntOpenHashMapSerialable;
import com.carrotsearch.hppc.IntOpenHashSet;
import com.carrotsearch.hppc.IntOpenHashSetSerializable;
import com.onpositive.compactdata.IntComparator;
import com.onpositive.compactdata.TimIntSort;
import com.onpositive.semantic.search.core.ICategory;
import com.onpositive.semantic.search.core.ISearchResultCallback;
import com.onpositive.semantic.search.core.SearchMatch;
import com.onpositive.semantic.search.core.SearchRequest;
import com.onpositive.semantic.search.core.date.FreeFormDate;
import com.onpositive.semantic.search.core.date.FreeFormDateRange;
import com.onpositive.semantic.search.core.date.IFreeFormDate;
import com.onpositive.semantic.wikipedia2.WikiDoc;
import com.onpositive.semantic.wikipedia2.WikiEngine2;
import com.onpositive.semantic.wikipedia2.docclasses.DocumentClassService;
import com.onpositive.semantic.wikipedia2.docclasses.DocumentClasses;
import com.onpositive.semantic.wikipedia2.locations.GeoSearchService;
import com.onpositive.semantic.wikipedia2.popularity.ArticlePopularityService;
import com.onpositive.semantic.wikipedia2.search.ThemeProvider.SortedDocs;
import com.onpositive.semantic.wikipedia2.services.DateIndex;

public class ByThemeSearcher {

	private WikiEngine2 engine;
	private ThemeProvider themeProvider;
	private DocumentClassService index;

	public ByThemeSearcher(WikiEngine2 engine) {
		this.engine = engine;
		this.themeProvider = new ThemeProvider(engine);
		index = engine.getIndex(DocumentClassService.class);
		isNews = !engine.getRole().endsWith("vocab");
	}

	LinkedHashMap<String, IntOpenHashSet> keywords = new LinkedHashMap<String, IntOpenHashSet>();

	IntOpenHashSet keyWordFilter(WikiEngine2 engine, SearchRequest request) {
		IntOpenHashSet tsSet = null;

		String keyword = request.getKeyword();
		if (keyword != null && request.getKeyword().length() > 0) {
			IntOpenHashSet intOpenHashSet = keywords.get(keyword);
			if (intOpenHashSet != null) {
				return intOpenHashSet;
			}
			String[] split = request.getKeyword().trim().split(",");
			for (String qa : split) {
				int[] search = engine.getTextSearchIndex().search(
						qa.toLowerCase().trim());
				if (tsSet == null) {
					tsSet = new IntOpenHashSet();
					tsSet.add(search);
				} else {
					IntOpenHashSet c = new IntOpenHashSet();
					c.add(search);
					tsSet.retainAll(c);
				}
			}
			if (keywords.size() > 100) {
				keywords.remove(keywords.keySet().iterator().next());
			}
			keywords.put(keyword, intOpenHashSet);
		}
		return tsSet;
	}

	protected LinkedHashMap<String, int[]> filtered = new LinkedHashMap<String, int[]>();
	protected LinkedHashMap<String, IntOpenHashSet> gs = new LinkedHashMap<String, IntOpenHashSet>();
	protected LinkedHashMap<String, SortedDocs> sd = new LinkedHashMap<String, SortedDocs>();

	String lastMode;

	protected IntOpenHashSet blackListed;
	private boolean isNews;

	public void search(SearchRequest request, ISearchResultCallback callback) {
		if (request.getLocation() != null
				&& request.getLocation().equals("Мир")) {
			request.setLocation(null);
		}
		String da = request.getDate();
		if (da != null && da.indexOf('-') != -1) {
			da = da.substring(0, da.indexOf('-'));
		}
		if (da != null && da.indexOf('.') == -1 && da.indexOf(' ') == -1) {
			if (da.length() > 4) {
				callback.done(0);
				return;
			}
		}

		long q0 = System.currentTimeMillis();
		if (lastMode != request.getSearchMode()
				&& request.getSearchMode() != null
				&& !request.getSearchMode().equals(lastMode)) {
			clearPopularity();
		}
		this.lastMode = request.getSearchMode();
		String category = request.getCategory();
		SortedDocs docs = null;
		if (category == null||category.trim().isEmpty()||category.equals("Всё")) {
			if (sd.containsKey(getKey(request, category))) {
				docs = sd.get(getKey(request, category));
			}
			docs=new SortedDocs();
			docs.documentIDs=engine.getNotRedirectDocumentIDs();
			docs.documentIDsRerated=new IntIntOpenHashMapSerialable();
			for (int q:docs.documentIDs){
				docs.documentIDsRerated.put(q, 1);
			}
			sd.put(getKey(request, category), docs);
		} else {
			if (sd.containsKey(getKey(request, category))) {
				docs = sd.get(getKey(request, category));
			} else {
				docs = themeProvider.getDocs(engine, category,
						System.currentTimeMillis(), request);
				if (sd.size() > 20) {
					sd.remove(sd.keySet().iterator().next());
				}
				sd.put(getKey(request, category), docs);
			}
		}
		if (docs.documentIDsRerated == null) {
			docs.documentIDsRerated = new IntIntOpenHashMapSerialable();
			for (int q : docs.documentIDs) {
				docs.documentIDsRerated.put(q,
						rate(q, docs.documentIDsToRelevance.get(q), category));
			}
			themeProvider.sort(engine, docs.documentIDsRerated,
					docs.documentIDs);
			final SortedDocs d1 = docs;
			if (request.getSearchMode() != null
					&& request.getSearchMode().equals(
							SearchRequest.MODE_POPULARITY)) {
				final ArticlePopularityService index2 = engine
						.getIndex(ArticlePopularityService.class);
				IntComparator c = new IntComparator() {

					@Override
					public int compare(int l1, int l2) {
						return index2.getTotalPopularity(l2)
								- index2.getTotalPopularity(l1);
					}
				};
				TimIntSort.sort(docs.documentIDs, c);
			}
			if (request.getSearchMode() != null
					&& request.getSearchMode().equals(SearchRequest.MODE_MIXED)) {
				final ArticlePopularityService index2 = engine
						.getIndex(ArticlePopularityService.class);
				TimIntSort.sort(docs.documentIDs, new IntComparator() {

					@Override
					public int compare(int l1, int l2) {

						int i = d1.documentIDsRerated.get(l1);
						int j = d1.documentIDsRerated.get(l2);
						if (i != j) {
							return i - j;
						}
						return index2.getTotalPopularity(l2)
								- index2.getTotalPopularity(l1);
					}
				});
			}
		}

		int[] results = docs.documentIDs;
		IntOpenHashSet goodSet = null;

		DateSpec ds = new DateSpec(request.getDate(), engine);
		long l0 = System.currentTimeMillis();
		System.out.println("initial prepare time:" + (l0 - q0));
		boolean preferNews = false;
		if (ds.getDate() != null) {
			IFreeFormDate date = ds.getDate();
			if (date.getStartDate() != null) {
				preferNews = date.getStartDate().getYear() > 100;
				if (date instanceof FreeFormDate) {
					FreeFormDate qq = (FreeFormDate) date;
					if (qq.getYear() != null && qq.getYear() >= 2100) {
						callback.done(0);
						return;
					}
				}
				if (date instanceof FreeFormDateRange) {
					FreeFormDate qq = ((FreeFormDateRange) date).getFrom();
					if (qq != null) {
						if (qq.getYear() != null && qq.getYear() >= 2100) {
							callback.done(0);
							return;
						}
					}
				}
				if (date.getStartDate().getYear() >= 200) {
					callback.done(0);
					return;
				}
			}

			String key = "" + request.getCategory() + request.getDate();
			int[] is = filtered.get(key);
			IntOpenHashSet intOpenHashSet = gs.get(request.getDate());
			if (intOpenHashSet != null) {
				goodSet = intOpenHashSet;
			} else {
				goodSet = ds.getGoodSet();
				if (gs.size() > 100) {
					gs.remove(request.getDate());
				}
				gs.put(request.getDate(), goodSet);
			}
			if (is != null) {
				results = is;
			} else if (ds.getDate() != null) {
				DateIndex index2 = engine.getIndex(DateIndex.class);
				results = index2.filterByDate(ds.getDate(), docs.documentIDs);
				if (filtered.size() > 200) {
					filtered.remove(filtered.keySet().iterator().next());
				}
				filtered.put(key, results);
			}
		}
		if (ds.getDate() == null && request.getDate() != null
				&& request.getDate().trim().length() > 0) {
			callback.done(0);
			return;
		}
		long l1 = System.currentTimeMillis();
		System.out.println("Date filter:" + (l1 - l0));
		IntOpenHashSetSerializable lset = null;
		if (request.getLocation() != null
				&& request.getLocation().trim().length() > 0) {
			lset = engine.getIndex(GeoSearchService.class)
					.getAllDocumentsWithLocation(request.getLocation());
		}
		IntOpenHashSet ks = null;
		if (request.getKeyword() != null
				&& request.getKeyword().trim().length() > 0) {
			ks = keyWordFilter(engine, request);
		}
		int limit = request.getCount() + request.getOffset();
		int pos = 0;
		long l2 = System.currentTimeMillis();
		System.out.println("Locations+Keywords time:" + (l2 - l1));

		if (lset == null && ks == null) {
			if (lastMode == null
					|| !lastMode.equals(SearchRequest.MODE_POPULARITY)) {

				if (goodSet != null) {
					pos = reportGood(request, callback, docs, results, goodSet,
							lset, ks, limit, pos, preferNews);
				}
			}
			pos = reportOther(request, callback, docs, results, lset, ks,
					limit, pos, goodSet, preferNews);
			callback.done(results.length);
		} else {
			if (lastMode == null
					|| !lastMode.equals(SearchRequest.MODE_POPULARITY)) {

				if (goodSet != null) {
					pos = reportGood1(request, callback, docs, results,
							goodSet, lset, ks, limit, pos, preferNews);
				}
			}
			pos = reportOther1(request, callback, docs, results, lset, ks,
					limit, pos, goodSet, preferNews);
			callback.done(pos);
		}
		long l3 = System.currentTimeMillis();
		System.out.println("Accept time:" + (l3 - l2));
	}

	String getKey(SearchRequest request, String category) {
		return category + request.getMaxRelevancy();
	}

	void clearPopularity() {
		sd.clear();
		themeProvider.clear();
		lastMode = null;
	}

	int reportOther(SearchRequest request, ISearchResultCallback callback,
			SortedDocs docs, int[] results, IntOpenHashSetSerializable lset,
			IntOpenHashSet ks, int limit, int pos, IntOpenHashSet gs,
			boolean preferNews) {
		for (int a : results) {
			if (pos >= limit) {
				break;
			}
			if (gs != null) {
				if (gs.contains(a)) {
					continue;
				}
			}
			if (lset != null) {
				if (!lset.contains(a)) {
					continue;
				}
			}
			if (ks != null) {
				if (!ks.contains(a)) {
					continue;
				}
			}
			createMatch(request, callback, docs, a, preferNews);
			pos++;
			if (pos >= limit) {
				break;
			}
		}
		return pos;
	}

	int reportOther1(SearchRequest request, ISearchResultCallback callback,
			SortedDocs docs, int[] results, IntOpenHashSetSerializable lset,
			IntOpenHashSet ks, int limit, int pos, IntOpenHashSet gs,
			boolean preferNews) {
		for (int a : results) {
			if (gs != null) {
				if (gs.contains(a)) {
					continue;
				}
			}
			if (lset != null) {
				if (!lset.contains(a)) {
					continue;
				}
			}
			if (ks != null) {
				if (!ks.contains(a)) {
					continue;
				}
			}
			if (pos < limit) {
				createMatch(request, callback, docs, a, preferNews);
			}
			pos++;
		}
		return pos;
	}

	int reportGood(SearchRequest request, ISearchResultCallback callback,
			SortedDocs docs, int[] results, IntOpenHashSet goodSet,
			IntOpenHashSetSerializable lset, IntOpenHashSet ks, int limit,
			int pos, boolean preferNews) {
		for (int a : results) {
			if (goodSet != null) {
				if (!goodSet.contains(a)) {
					continue;
				}
			}
			if (lset != null) {
				if (!lset.contains(a)) {
					continue;
				}
			}
			if (ks != null) {
				if (!ks.contains(a)) {
					continue;
				}
			}
			createMatch(request, callback, docs, a, preferNews);
			pos++;
			if (pos >= limit) {
				break;
			}
		}
		return pos;
	}

	void createMatch(SearchRequest request, ISearchResultCallback callback,
			SortedDocs docs, int a, boolean preferNews) {
		int i = docs.documentIDsRerated.get(a);
		if (preferNews) {
			if (isNews) {
				// i+=3;
			} else {
				i += 5;
			}
		} else {
			if (isNews) {
				i += 10;
			} else {
				// i-=3;
			}
		}

		SearchMatch d = new SearchMatch(new WikiDoc(engine, a), i, null,
				request);
		callback.acceptDocument(d);
	}

	int reportGood1(SearchRequest request, ISearchResultCallback callback,
			SortedDocs docs, int[] results, IntOpenHashSet goodSet,
			IntOpenHashSetSerializable lset, IntOpenHashSet ks, int limit,
			int pos, boolean preferNews) {
		for (int a : results) {
			if (goodSet != null) {
				if (!goodSet.contains(a)) {
					continue;
				}
			}
			if (lset != null) {
				if (!lset.contains(a)) {
					continue;
				}
			}
			if (ks != null) {
				if (!ks.contains(a)) {
					continue;
				}
			}
			if (pos < limit) {
				createMatch(request, callback, docs, a, preferNews);
			}
			pos++;

		}
		return pos;
	}

	private int rate(int q, int i, String category) {
		i += 2;
		String string = engine.getPageTitles().get(q).toLowerCase();
		if (string.contains(category.toLowerCase())) {
			i -= 2;
		} else {
			WikiDoc wikiDoc = new WikiDoc(engine, q);
			ICategory[] categories = wikiDoc.getCategories();
			for (ICategory c : categories) {
				if (c.getTitle().toLowerCase().contains(category)) {
					i--;
					break;
				}
			}
		}
		boolean hasClass = index.hasClass(q, DocumentClasses.PERSONCLASS);
		if (hasClass) {
			return i + 10;
		}
		hasClass = index.hasClass(q, DocumentClasses.LOCATIONCLASS);
		if (hasClass) {
			return i + 10;
		}
		hasClass = index.hasClass(q, DocumentClasses.LISTCLASS);
		if (hasClass) {
			return i + 15;
		}
		hasClass = index.hasClass(q, DocumentClasses.FILMCLASS);
		if (hasClass) {
			return i + 2;
		}
		hasClass = index.hasClass(q, DocumentClasses.METACLASS_TECH);
		if (hasClass) {
			return i + 3;
		}
		hasClass = index.hasClass(q, DocumentClasses.ASTROCLASS);
		if (hasClass) {
			return i + 3;
		}
		hasClass = index.hasClass(q, DocumentClasses.EVENTCLASS);
		if (hasClass) {
			return i - 1;
		}
		return i;
	}

	public void clear() {
		clearPopularity();
		themeProvider.clear();
	}
}
