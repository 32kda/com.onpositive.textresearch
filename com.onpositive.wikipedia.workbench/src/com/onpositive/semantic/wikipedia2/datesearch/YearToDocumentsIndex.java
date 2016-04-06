package com.onpositive.semantic.wikipedia2.datesearch;

import java.util.ArrayList;
import java.util.List;

import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.IntObjectOpenHashMap;
import com.carrotsearch.hppc.IntOpenHashSet;
import com.onpositive.semantic.search.core.ICategory;
import com.onpositive.semantic.search.core.date.FreeFormDate;
import com.onpositive.semantic.search.core.date.FreeFormDateParser;
import com.onpositive.semantic.search.core.date.FreeFormDateParser.FreeFormDateParserConfig;
import com.onpositive.semantic.search.core.date.FreeFormDateParser.FreeFormDateParserConfig.YearAccept;
import com.onpositive.semantic.search.core.date.FreeFormDateRange;
import com.onpositive.semantic.search.core.date.IFreeFormDate;
import com.onpositive.semantic.wikipedia2.WikiDoc;
import com.onpositive.semantic.wikipedia2.WikiEngine2;
import com.onpositive.semantic.wikipedia2.services.AbstractIntIntArrayIndex;
import com.onpositive.semantic.wikipedia2.services.RedirectsMap;

public class YearToDocumentsIndex extends AbstractIntIntArrayIndex {

	public YearToDocumentsIndex(WikiEngine2 engine) {
		super(engine);
	}

	IntObjectOpenHashMap<IntArrayList> tempMap; 

	public int[] filterByDate(IFreeFormDate fdate, int[] array) {
		IntOpenHashSet set = getGoodSet(fdate);
		IntArrayList result=new IntArrayList();
		for (int a:array){
			if (set.contains(a)){
				result.add(a);
			}
		}
		return result.toArray();
	}

	public IntOpenHashSet getGoodSet(IFreeFormDate fdate) {
		int startYear = -1;
		int endYear = -1;
		if (fdate instanceof FreeFormDateRange) {
			FreeFormDateRange date = (FreeFormDateRange) fdate;
			FreeFormDate from = date.getFrom();
			FreeFormDate to = date.getTo();
			Integer year = from.getYear();
			if (year != null) {
				startYear = year;
				// endYear=year;
			}
			year = to.getYear();
			if (year != null) {
				// startYear=year;
				endYear = year;
			}

		} else {
			FreeFormDate date = (FreeFormDate) fdate;
			Integer year = date.getYear();
			if (year != null) {
				startYear = year;
				endYear = year;
			}

		}
		if (endYear-startYear>10){
			return null;
		}
		IntOpenHashSet set = new IntOpenHashSet();
		if (startYear > 0 && endYear > 0) {
			for (int a = startYear; a <= endYear; a++) {
				set.add(getDocumentsForYear(a));
			}
		}
		return set;
	}

	@Override
	protected void prebuild() {
		tempMap= new IntObjectOpenHashMap<IntArrayList>();
		int[] documentIDs = engine.getDocumentIDs();
		RedirectsMap index = engine.getIndex(RedirectsMap.class);
//		DocumentClassService sc = engine.getIndex(DocumentClassService.class);
//		FreeFormDateParserConfig config = new FreeFormDateParserConfig();
//		for (int m : documentIDs) {
//			if (index.isRedirect(m)) {
//				continue;
//			}
//			if (sc.hasClass(m, DocumentClasses.LISTCLASS)) {
//				continue;
//			}
//			if (sc.hasClass(m, DocumentClasses.ASTROCLASS)) {
//				continue;
//			}
//			doProcess(m, config);
//		}
		super.prebuild();
	}

	private void doProcess(int m,FreeFormDateParserConfig config) {
		WikiDoc wikiDoc = new WikiDoc(engine, m);			
		ICategory[] categories = wikiDoc.getCategories();
		ArrayList<IFreeFormDate> dates = new ArrayList<IFreeFormDate>();
		config.setYearAcceptType(YearAccept.SIGNED_AND_TRUSTFUL_YEAR);
		List<IFreeFormDate> parse = FreeFormDateParser.parse(
				wikiDoc.getTitle(), config, new ArrayList<Integer>());
		dates.addAll(parse);
		config.setYearAcceptType(YearAccept.ONLY_SIGNED);
		processCats(config, categories, dates);
		processDates(m, dates);		
	}

	void processCats(FreeFormDateParserConfig config, ICategory[] categories,
			ArrayList<IFreeFormDate> dates) {
		for (ICategory c : categories) {
			if (c.getTitle().contains("Википедия:")){
				continue;
			}
			if (c.getTitle().contains("после")){
				continue;
			}
			if (c.getTitle().contains("до")){
				continue;
			}
			List<IFreeFormDate> parse2 = FreeFormDateParser.parse(
					c.getTitle(), config, new ArrayList<Integer>());
			if (!parse2.isEmpty()){
				dates.addAll(parse2);
			}
		}
	}

	void processDates(int m, ArrayList<IFreeFormDate> dates) {
		for (IFreeFormDate q : dates) {
			if (q instanceof FreeFormDate) {
				FreeFormDate ds = (FreeFormDate) q;
				process(m, ds);
			}
			if (q instanceof FreeFormDateRange) {
				FreeFormDateRange range = (FreeFormDateRange) q;
				FreeFormDate from = range.getFrom();
				if (range.getFrom()!=null&&range.getTo()!=null){
					if (range.getFrom().getYear()!=null&&range.getTo().getYear()!=null){
						if (range.getTo().getYear()-range.getFrom().getYear()>8){
							continue;
						}
					}
				}
				process(m, from);
				process(m, range.getTo());
			}
		}
	}

	void process(int m, FreeFormDate ds) {
		if (ds == null) {
			return;
		}
		Integer year = ds.getYear();
		if (year != null) {
			appendYear(m, year);
		}
	}

	private void appendYear(int m, Integer year) {
		IntArrayList intArrayList = tempMap.get(year);
		if (intArrayList == null) {
			intArrayList = new IntArrayList();
			tempMap.put(year, intArrayList);
		}
		intArrayList.add(m);
	}

	@Override
	protected int[] calcArray(int a) {
		IntArrayList intArrayList = tempMap.get(a);
		if (intArrayList==null){
			return new int[0];
		}
		return intArrayList.toArray();
	}

	public int[] getDocumentsForYear(int year) {
		return values(year);
	}

	@Override
	protected int[] getKeySet() {
		int[] rs = new int[2500];
		for (int a = 0; a < 2500; a++) {
			rs[a] = a;
		}
		return rs;
	}

	@Override
	public String getFileName() {
		return "years.index";
	}

}
