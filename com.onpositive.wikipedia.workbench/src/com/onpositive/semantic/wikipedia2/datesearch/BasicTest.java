package com.onpositive.semantic.wikipedia2.datesearch;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;

import com.carrotsearch.hppc.IntArrayList;
import com.onpositive.semantic.search.core.date.FreeFormDate;
import com.onpositive.semantic.search.core.date.FreeFormDateRange;
import com.onpositive.semantic.search.core.date.IFreeFormDate;
import com.onpositive.semantic.wikipedia2.WikiEngine2;
import com.onpositive.semantic.wikipedia2.datesearch.DateStore.DatesInfo;
import com.onpositive.semantic.wikipedia2.services.DateIndex;

public class BasicTest {

	public static void main(String[] args) {
		DocumentDateModel2 buildModel = DocumentDateModel2
				.buildModel(
						"диапазонах 2530-2565/2650-2685 МГц",
						new String[0]);
		System.out.println(Arrays.asList(buildModel.getDates()));

		WikiEngine2 wikiEngine2 = new WikiEngine2("D:/se2/ruwiki");
		testDocument(wikiEngine2);
		findTwelwe2(wikiEngine2);
		// testDocument(wikiEngine2);
	}

	public static void findTwelwe(WikiEngine2 en) {
		DateStore dateStore = DocumentDateModel.getDateStore(en);
		for (int q : en.getDocumentIDs()) {
			DatesInfo info = dateStore.getInfo(q);
			if (info == null) {
				continue;
			}
			String title = en.getPageTitles().get(q);
			tst2(q, info, en);
		}
	}

	public static void findTwelwe2(WikiEngine2 en) {
		DateIndex index = en.getIndex(DateIndex.class);
		int[] documentIDs = en.getDocumentIDs();
		for (int a = 0; a < 100; a++) {
			long l0 = System.currentTimeMillis();
			IntArrayList ll = doMatch(en, index, documentIDs);
			long l1 = System.currentTimeMillis();
			System.out.println(l1 - l0);
		}
	}

	public static IntArrayList doMatch(WikiEngine2 en, DateIndex index,
			int[] documentIDs) {
		IntArrayList ll = new IntArrayList();
		int count=0;
		for (int q : documentIDs) {
			boolean testYear = index.testYear(q, 2100, 2750);
			if (testYear) {
				ll.add(q);
				DocumentDateModel2 build = DocumentDateModel.build(en, q);
				boolean founf=false;
				for (IFreeFormDate qa:build.getDates()){
					if (qa instanceof FreeFormDate){
						FreeFormDate zz=(FreeFormDate) qa;
						if (zz.getYear()!=null&&zz.getYear()<0){
							continue;
						}
					}
					if (qa instanceof FreeFormDateRange){
						FreeFormDateRange zz=(FreeFormDateRange) qa;
						if (zz.getFrom().getYear()!=null&&zz.getFrom().getYear()<0){
							continue;
						}
					}
					if (qa.getStartDate()!=null&&qa.getEndDate()!=null){
						if (qa.getStartDate().getYear()>=(2100-1900)){
							if (qa.getEndDate().getYear()<=(2750-1900)){
								founf=true;
								count++;
							}	
						}
					}
				}
				if(founf){
				System.out.println(en.getPageTitles().get(q));
				}
			}
			
		}
		System.out.println(count);
		return ll;
	}

	private static void tst(DatesInfo info) {
		DocumentDateModel2 mdl = new DocumentDateModel2();
		mdl.init(info);
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		try {
			mdl.write(stream);
			stream.close();
			byte[] byteArray = stream.toByteArray();
			// dates;
			for (IFreeFormDate qa : mdl.getDates()) {
				if (qa instanceof FreeFormDate) {
					FreeFormDate mm = (FreeFormDate) qa;
					boolean testYear = DocumentDateModel2.testYear(0,
							byteArray.length, byteArray, mm.getYear(),
							mm.getYear());
					if (!testYear) {
						throw new IllegalStateException();
					}
					testYear = DocumentDateModel2.testYear(0, byteArray.length,
							byteArray, mm.getYear() - 1, mm.getYear() + 1);
					if (!testYear) {
						throw new IllegalStateException();
					}
					if (mm.getDay() != null && mm.getMonth() != null) {
						testYear = DocumentDateModel2.testAccurateDate(0,
								byteArray.length, byteArray, mm.getYear(), mm
										.getYear(), mm.getMonth().ordinal(), mm
										.getMonth().ordinal(), mm.getDay(), mm
										.getDay());
						if (!testYear) {
							throw new IllegalStateException();
						}
					}
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void tst2(int doc, DatesInfo info, WikiEngine2 engine) {
		DocumentDateModel2 mdl = new DocumentDateModel2();
		DateIndex index = engine.getIndex(DateIndex.class);
		mdl.init(info);

		// dates;
		for (IFreeFormDate qa : mdl.getDates()) {
			if (qa instanceof FreeFormDate) {
				FreeFormDate mm = (FreeFormDate) qa;
				boolean testYear = index.testYear(doc, mm.getYear(),
						mm.getYear());
				if (!testYear) {
					throw new IllegalStateException();
				}
				testYear = index.testYear(doc, mm.getYear() - 1,
						mm.getYear() + 1);
				if (!testYear) {
					throw new IllegalStateException();
				}
				if (mm.getDay() != null && mm.getMonth() != null) {
					testYear = index.testActualDateYear(doc, mm.getYear(), mm
							.getMonth().ordinal(), mm.getDay(), mm.getYear(),
							mm.getMonth().ordinal(), mm.getDay());
					if (!testYear) {
						throw new IllegalStateException();
					}
				}
			}
		}
	}

	public static void testDocument(WikiEngine2 engine) {
		int i = engine.getPageTitles().get("АМ-42");
		DocumentDateModel2 buildModel = DocumentDateModel.build(engine, i);
		for (IFreeFormDate e : buildModel.getDates()) {
			Date startDate = e.getStartDate();
			if (startDate != null) {
				System.out.println(startDate.getYear());
			}
		}
	}

}
