package com.onpositive.semantic.wikipedia2.search;

import java.util.List;

import com.carrotsearch.hppc.IntIntOpenHashMap;
import com.carrotsearch.hppc.IntOpenHashSet;
import com.onpositive.semantic.search.core.date.FreeFormDateParser;
import com.onpositive.semantic.search.core.date.IFreeFormDate;
import com.onpositive.semantic.search.core.date.FreeFormDateParser.FreeFormDateParserConfig;
import com.onpositive.semantic.search.core.date.FreeFormDateParser.FreeFormDateParserConfig.YearAccept;
import com.onpositive.semantic.wikipedia2.WikiEngine2;
import com.onpositive.semantic.wikipedia2.datesearch.YearToDocumentsIndex;
import com.onpositive.semantic.wikipedia2.services.DateIndex;

public class DateSpec  {

	private WikiEngine2 engine;

	static FreeFormDateParserConfig config = new FreeFormDateParserConfig();

	private IFreeFormDate fdate;

	private DateIndex index;

	private boolean allOpen;
	static {
		config.setYearAcceptType(YearAccept.ANY_FIGURE);
	}

	public DateSpec(String fDate, WikiEngine2 engine) {
		this.engine = engine;
		fdate = null;
		index = engine.getIndex(DateIndex.class);
		String date = fDate;
		if (date != null && date.length() > 0) {
			List<IFreeFormDate> parse = FreeFormDateParser.parse(date, config,
					null);
			if (parse.size() > 0) {
				fdate = parse.get(0);
			}
		}
		allOpen = fDate == null;
	}

	protected int size() {
		return 0;
	}

	/*public IntIntOpenHashMap score(IntIntOpenHashMap initialResults) {
		if (fdate==null) {
			return initialResults;
		}
		if (initialResults == null) {
			IntIntOpenHashMap calcResults = getResults();
			int[] documentIDs = engine.getNotRedirectDocumentIDs();
			for (int q : index.filterByDate(fdate, documentIDs)) {
				if (!calcResults.containsKey(q)) {
					calcResults.put(q, 0);
				}
			}
			return calcResults;
		} else {
			index.filterByDate(fdate, initialResults, getResults());
		}
		return initialResults;
	}*/


	protected IntIntOpenHashMap calcResults() {
		YearToDocumentsIndex index = engine
				.getIndex(YearToDocumentsIndex.class);
		IntOpenHashSet goodSet = getGoodSet();
		IntIntOpenHashMap mm = new IntIntOpenHashMap();
		for (int q : goodSet.toArray()) {
			mm.put(q, 1);
		}
		return mm;
	}

	public IntOpenHashSet getGoodSet() {
		YearToDocumentsIndex index = engine
				.getIndex(YearToDocumentsIndex.class);
		IntOpenHashSet goodSet = index.getGoodSet(fdate);
		if (goodSet!=null&&goodSet.isEmpty()){
			return null;
		}
		return goodSet;
	}

	public IFreeFormDate getDate() {
		return fdate;
	}

}
