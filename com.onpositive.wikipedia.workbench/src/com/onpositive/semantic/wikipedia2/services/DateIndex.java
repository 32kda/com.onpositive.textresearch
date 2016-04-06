package com.onpositive.semantic.wikipedia2.services;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.IntIntOpenHashMap;
import com.carrotsearch.hppc.IntOpenHashSet;
import com.onpositive.semantic.search.core.date.FreeFormDate;
import com.onpositive.semantic.search.core.date.FreeFormDate.Month;
import com.onpositive.semantic.search.core.date.FreeFormDateRange;
import com.onpositive.semantic.search.core.date.IFreeFormDate;
import com.onpositive.semantic.wikipedia2.WikiEngine2;
import com.onpositive.semantic.wikipedia2.WikiEngineService;
import com.onpositive.semantic.wikipedia2.datesearch.DateStore;
import com.onpositive.semantic.wikipedia2.datesearch.DateStore.DatesInfo;
import com.onpositive.semantic.wikipedia2.datesearch.DocumentDateModel;
import com.onpositive.semantic.wikipedia2.datesearch.DocumentDateModel2;

public final class DateIndex extends WikiEngineService {

	int[] positions;
	byte[] dateData;

	public DateIndex(WikiEngine2 engine) {
		super(engine);
	}

	@Override
	protected void doLoad(File fl) throws IOException {
		BufferedInputStream b = new BufferedInputStream(new FileInputStream(fl));
		DataInputStream dd = new DataInputStream(b);
		int len = dd.readInt();
		positions = new int[len];
		for (int a = 0; a < len; a++) {
			positions[a] = dd.readInt();
		}
		dateData = new byte[dd.readInt()];
		dd.readFully(dateData);
		dd.close();
	}

	public boolean testYear(int document, int startYear, int endYear) {
		int start = positions[document];
		int end = positions[document + 1];
		if (start == -1) {
			return false;
		}
		return DocumentDateModel2.testYear(start, end, dateData, startYear,
				endYear);
	}

	public boolean testActualDateYear(int document, int startYear,
			int startMonth, int startDay, int endYear, int endMonth, int endDay) {
		int start = positions[document];
		int end = positions[document + 1];
		if (start == end) {
			return false;
		}
		boolean testYear = DocumentDateModel2.testYear(start, end, dateData,
				startYear, endYear);
		if (testYear) {
			return DocumentDateModel2.testAccurateDate(start, end, dateData,
					startYear, endYear, startMonth, endMonth, startDay, endDay);
		}
		return testYear;
	}

	@Override
	protected void build(WikiEngine2 enfine) {
		int[] documentIDs = enfine.getDocumentIDs();
		positions = new int[documentIDs.length + 2];
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		int pos = 1;
		DateStore dateStore = DocumentDateModel.getDateStore(enfine);
		for (int q : documentIDs) {
			int size = bytes.size();

			DatesInfo info = dateStore.getInfo(q);
			if (info == null) {
				positions[pos] = size;
				pos++;
				continue;
			} else {
				positions[pos] = size;
			}
			DocumentDateModel2 mdl = new DocumentDateModel2();
			mdl.init(info);
			try {
				mdl.write(bytes);
			} catch (IOException e) {
				throw new IllegalStateException();
			}
			pos++;
		}
		positions[pos] = bytes.size();
		try {
			bytes.close();
		} catch (IOException e) {
			throw new IllegalStateException();
		}
		this.dateData = bytes.toByteArray();
	}

	@Override
	protected void doSave(File fl) throws IOException {
		BufferedOutputStream b = new BufferedOutputStream(new FileOutputStream(
				fl));
		DataOutputStream dd = new DataOutputStream(b);
		dd.writeInt(positions.length);
		for (int q : positions) {
			dd.writeInt(q);
		}
		dd.writeInt(dateData.length);
		dd.write(dateData);
		dd.close();
	}

	@Override
	public String getFileName() {
		return "dates.index";
	}

	HashMap<IFreeFormDate, IntOpenHashSet> localMap = new HashMap<IFreeFormDate, IntOpenHashSet>();

	public int[] filterByDate(IFreeFormDate fdate, int[] array) {
		int startYear = -1;
		int startMonth = 32;
		int startDay = 32;
		int endYear = -1;
		int endMonth = 32;
		int endDay = 32;
		if (fdate instanceof FreeFormDateRange) {
			FreeFormDateRange date = (FreeFormDateRange) fdate;
			FreeFormDate from = date.getFrom();
			FreeFormDate to = date.getTo();
			Integer year = from.getYear();
			if (year != null) {
				startYear = year;
				// endYear=year;
			}
			Month month = from.getMonth();
			if (month != null) {
				startMonth = month.ordinal();
				// endMonth=month.ordinal();
			}
			Integer day = from.getDay();
			if (day != null) {
				startDay = day;
				// endDay=day;
			}
			year = to.getYear();
			if (year != null) {
				// startYear=year;
				endYear = year;
			}
			month = to.getMonth();
			if (month != null) {
				// startMonth=month.ordinal();
				endMonth = month.ordinal();
			}
			day = to.getDay();
			if (day != null) {
				// startDay=day;
				endDay = day;
			}

		} else {
			FreeFormDate date = (FreeFormDate) fdate;
			Integer year = date.getYear();
			if (year != null) {
				startYear = year;
				endYear = year;
			}
			Month month = date.getMonth();
			if (month != null) {
				startMonth = month.ordinal();
				endMonth = month.ordinal();
			}
			Integer day = date.getDay();
			if (day != null) {
				startDay = day;
				endDay = day;
			}
		}
		boolean isYearOnly = startMonth == 32 && endMonth == 32
				&& startDay == 32 && endDay == 32;
		IntArrayList ll = new IntArrayList(array.length / 2);
		if (isYearOnly) {
			for (int q : array) {
				if (q == 0) {
					continue;
				}
				if (testYear(q, startYear, endYear)) {
					ll.add(q);
				}
			}
		} else {
			if (startYear == endYear && startMonth == endMonth) {
				boolean containsKey = localMap.containsKey(fdate);
				if (containsKey) {
					IntOpenHashSet intOpenHashSet = localMap.get(fdate);
					return result(array, ll, intOpenHashSet);
				} else {
					int[] documentIDs = year(startYear);
					IntOpenHashSet mm = new IntOpenHashSet();
					for (int q : documentIDs) {
						if (q == 0) {
							continue;
						}
						if (testActualDateYear(q, startYear, startMonth,
								startDay, endYear, endMonth, endDay)) {
							mm.add(q);
						}
					}
					localMap.put(fdate, mm);
					return result(array, ll, mm);
				}
			}
			for (int q : array) {
				if (q == 0) {
					continue;
				}
				if (testActualDateYear(q, startYear, startMonth, startDay,
						endYear, endMonth, endDay)) {
					ll.add(q);
				}
			}
		}
		return ll.toArray();
	}
	HashMap<Integer, int[]>yearMap=new HashMap<Integer, int[]>();

	int[] year(int startYear) {
		boolean containsKey = yearMap.containsKey(startYear);
		if (containsKey){
			return yearMap.get(startYear);
		}
		int[] notRedirectDocumentIDs = engine.getNotRedirectDocumentIDs();
		IntArrayList ll=new IntArrayList(); 
				
		for (int q : notRedirectDocumentIDs) {
			if (q == 0) {
				continue;
			}
			if (testYear(q, startYear, startYear)) {
				ll.add(q);
			}
		}
		notRedirectDocumentIDs=ll.toArray();
		yearMap.put(startYear, notRedirectDocumentIDs);
		return notRedirectDocumentIDs;
	}

	int[] result(int[] array, IntArrayList ll, IntOpenHashSet intOpenHashSet) {
		if (!intOpenHashSet.isEmpty()) {
			for (int q : array) {
				if (q == 0) {
					continue;
				}
				if (intOpenHashSet.contains(q)) {
					ll.add(q);
				}
			}
		}
		return ll.toArray();
	}

	public void filterByDate(IFreeFormDate fdate, IntIntOpenHashMap array,
			IntIntOpenHashMap gr) {
		int startYear = -1;
		int startMonth = 32;
		int startDay = 32;
		int endYear = -1;
		int endMonth = 32;
		int endDay = 32;
		if (fdate instanceof FreeFormDateRange) {
			FreeFormDateRange date = (FreeFormDateRange) fdate;
			FreeFormDate from = date.getFrom();
			FreeFormDate to = date.getTo();
			Integer year = from.getYear();
			if (year != null) {
				startYear = year;
				// endYear=year;
			}
			Month month = from.getMonth();
			if (month != null) {
				startMonth = month.ordinal();
				// endMonth=month.ordinal();
			}
			Integer day = from.getDay();
			if (day != null) {
				startDay = day;
				// endDay=day;
			}
			year = to.getYear();
			if (year != null) {
				// startYear=year;
				endYear = year;
			}
			month = to.getMonth();
			if (month != null) {
				// startMonth=month.ordinal();
				endMonth = month.ordinal();
			}
			day = to.getDay();
			if (day != null) {
				// startDay=day;
				endDay = day;
			}

		} else {
			FreeFormDate date = (FreeFormDate) fdate;
			Integer year = date.getYear();
			if (year != null) {
				startYear = year;
				endYear = year;
			}
			Month month = date.getMonth();
			if (month != null) {
				startMonth = month.ordinal();
				endMonth = month.ordinal();
			}
			Integer day = date.getDay();
			if (day != null) {
				startDay = day;
				endDay = day;
			}
		}
		boolean isYearOnly = startMonth == 32 && endMonth == 32
				&& startDay == 32 && endDay == 32;
		if (isYearOnly) {
			for (int q : array.keys) {
				if (q == 0) {
					continue;
				}
				if (array.containsKey(q)) {
					if (gr.containsKey(q)) {
						array.put(q, array.get(q) + 1);
					}
					if (!testYear(q, startYear, endYear)) {
						array.remove(q);
					}
				}
			}
		} else {
			for (int q : array.keys) {
				if (q == 0) {
					continue;
				}
				if (array.containsKey(q)) {
					if (gr.containsKey(q)) {
						array.put(q, array.get(q) + 1);
					}
					if (testActualDateYear(q, startYear, startMonth, startDay,
							endYear, endMonth, endDay)) {
						array.remove(q);
					}
				}
			}
		}
	}

}
