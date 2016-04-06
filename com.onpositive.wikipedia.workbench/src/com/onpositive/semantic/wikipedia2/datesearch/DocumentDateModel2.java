package com.onpositive.semantic.wikipedia2.datesearch;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import com.carrotsearch.hppc.IntOpenHashSetSerializable;
import com.onpositive.semantic.search.core.date.FreeFormDate;
import com.onpositive.semantic.search.core.date.FreeFormDateParser;
import com.onpositive.semantic.search.core.date.FreeFormDateRange;
import com.onpositive.semantic.search.core.date.IFreeFormDate;
import com.onpositive.semantic.wikipedia2.datesearch.DateStore.DatesInfo;
import com.onpositive.semantic.wordnet.AbstractWordNet;
import com.onpositive.semantic.wordnet.GrammarRelation;
import com.onpositive.semantic.wordnet.Grammem;
import com.onpositive.semantic.wordnet.WordNetProvider;

public class DocumentDateModel2 {
	private static final int MATCH_CENTURY_COUNT = 3;
	private static final int RANGE_WITHDAYSMARK = -124;
	private static final int RANGE_MARK = -125;
	private static final int DAYS_MARK = -126;
	protected int minTrustedYear = Integer.MAX_VALUE;
	protected int maxTrustedYear = Integer.MIN_VALUE;
	protected IntOpenHashSetSerializable trustedYears = new IntOpenHashSetSerializable();

	protected ArrayList<IFreeFormDate> dates = new ArrayList<IFreeFormDate>();
	protected HashSet<IFreeFormDate> datesSet = new HashSet<IFreeFormDate>();
	protected boolean hasRanges;
	protected boolean hasRangesWithDays;
	protected boolean hasDays;

	protected int lastYear = -1;

	static ArrayList<FreeFormDate> parseSmallYears(String readLine) {
		int lastIndex = 0;
		ArrayList<FreeFormDate> result = new ArrayList<FreeFormDate>();
		while (true) {
			int indexOf = readLine.indexOf("год", lastIndex);

			boolean hasNotW = false;
			boolean onlyDigit = true;
			if (indexOf != -1) {
				StringBuilder bld = new StringBuilder();
				int count = 0;
				for (int a = indexOf + 3; a < readLine.length(); a++) {
					char c = readLine.charAt(a);
					if (Character.isLetter(c)) {
						count++;
					} else {
						break;
					}
				}
				if (count <= 1) {
					for (int i = indexOf - 1; i >= 0; i--) {
						char c = readLine.charAt(i);
						if (Character.isWhitespace(c) || c == '_') {
							if (hasNotW) {
								break;
							}
						} else if (Character.isDigit(c)) {
							hasNotW = true;
							bld.append(c);
						} else {
							onlyDigit = false;
							break;
						}
					}
				}
				if (bld.length() > 0 && bld.length() < 4 && onlyDigit) {
					int year = Integer.parseInt(bld.reverse().toString());
					FreeFormDate f = new FreeFormDate();
					f.setYear(year);
					result.add(f);
				}
				lastIndex = indexOf + 2;
			} else {
				break;
			}
		}
		return result;
	}

	public static DocumentDateModel2 buildModel(String plainTextAbstract,
			String[] categoryTitles) {
		DocumentDateModel2 dm = new DocumentDateModel2();
		StringReader rs = new StringReader(plainTextAbstract);
		BufferedReader reader = new BufferedReader(rs);
		IntOpenHashSetSerializable years = new IntOpenHashSetSerializable();
		for (String cat : categoryTitles) {
			List<Integer> positions = new ArrayList<Integer>();
			List<IFreeFormDate> parse = FreeFormDateParser
					.parse(cat, positions);
			if (cat.startsWith("Википедия:")) {
				continue;
			}
			if (cat.contains("летия")) {
				continue;
			}
			
			// here we are;

			if (!parse.isEmpty()) {
				for (int a = 0; a < parse.size(); a++) {
					IFreeFormDate iFreeFormDate = parse.get(a);

					// TODO TEST YEAR;
					if (iFreeFormDate instanceof FreeFormDate) {
						FreeFormDate mm = (FreeFormDate) iFreeFormDate;
						if (mm.getYear() != null) {
							years.add(mm.getYear());
						}
					}
					if (iFreeFormDate instanceof FreeFormDateRange) {
						FreeFormDateRange range = (FreeFormDateRange) iFreeFormDate;
						FreeFormDate from = range.getFrom();
						if (from != null) {
							if (from.getYear() != null) {
								years.add(from.getYear());
							}
						}
						from = range.getTo();
						if (from != null) {
							if (from.getYear() != null) {
								years.add(from.getYear());
							}
						}
					}
					dm.appendDate(iFreeFormDate, true);
				}
			}
			ArrayList<FreeFormDate> parseSmallYears = parseSmallYears(cat);
			if (parseSmallYears != null) {
				for (FreeFormDate q : parseSmallYears) {
					years.add(q.getYear());
				}
				dm.dates.addAll(parseSmallYears);
			}

		}
		int min = Integer.MAX_VALUE;
		int max = Integer.MIN_VALUE;
		for (int y : years.toArray()) {
			min = Math.min(y, min);
			max = Math.max(y, max);
		}

		while (true) {
			try {
				String readLine = reader.readLine();

				if (readLine == null) {
					break;
				}
				String trim = readLine.trim().toLowerCase();
				if (trim.endsWith("публикации:")){
					break;
				}
				if (readLine.trim().equals("книги:")){
					break;
				}
				if (readLine.trim().equals("литература:")){
					break;
				}
				boolean hasDigit = false;
				for (int a = 0; a < readLine.length(); a++) {
					char c = readLine.charAt(a);
					if (Character.isDigit(c)) {
						hasDigit = true;
						break;
					}
				}
				if (!hasDigit) {
					continue;
				}

				List<Integer> positions = new ArrayList<Integer>();
				List<IFreeFormDate> parse = FreeFormDateParser.parse(readLine,
						positions);
				ArrayList<FreeFormDate> parseSmallYears = parseSmallYears(readLine);
				if (parseSmallYears != null) {
					for (FreeFormDate q : parseSmallYears) {
						int year = q.getYear();
						if (min != Integer.MAX_VALUE
								&& max != Integer.MIN_VALUE) {
							if (year / 200 >= min / 200
									&& year / 200 <= max / 200) {
								dm.appendDate(q, true);
							}
						}
					}
				}
				if (parse != null && !parse.isEmpty()) {
					for (int a = 0; a < parse.size(); a++) {
						IFreeFormDate fd = parse.get(a);
						testBeforeOurAge(readLine, fd, positions.get(a));
						if (!testGrams(readLine, fd, positions.get(a))) {
							continue;
						}
						if (fd instanceof FreeFormDate) {
							FreeFormDate date = (FreeFormDate) fd;
							boolean trust = true;
							if (date.getMonth() == null) {
								boolean trusted = isTrusted(readLine,
										positions.get(a), date, min, max);
								trust = trusted;
							}

							if (!trust) {
								if (!isValid(readLine, positions.get(a), date,
										min, max)) {
									continue;
								}
							}
							dm.appendDate(date, trust);
						}
						if (fd instanceof FreeFormDateRange) {
							if (!isValidRange(readLine, positions.get(a), fd,
									min, max)) {
								continue;
							}
							if (((FreeFormDateRange) fd).getFrom() != null) {
								if (((FreeFormDateRange) fd).getTo() != null) {
									Integer year = ((FreeFormDateRange) fd)
											.getFrom().getYear();
									Integer year1 = ((FreeFormDateRange) fd)
											.getTo().getYear();
									if (year != null && year1 != null) {
										if (Math.abs(year1 - year) > 2000) {
											continue;
										}
									}
								}
							}
							dm.appendDate(fd, true);
						}
					}
				}

			} catch (IOException e) {
				break;
			}
		}
		// dm.optimize();
		return dm;
	}

	private static void testBeforeOurAge(String readLine, IFreeFormDate fd,
			int pos) {
		// char charAt = readLine.charAt(pos+1);
		int indexOf = readLine.indexOf("до н. э.");
		int indexOf1 = readLine.indexOf("до н. э.");
		if (indexOf==-1){
			indexOf = readLine.indexOf("до н.э");
		}
		
		if (indexOf==-1){
			indexOf = readLine.indexOf("до нашей эры");			
		}
		if (indexOf==-1){
			indexOf = readLine.indexOf("до н. э");			
		}
		if (indexOf==-1){
			indexOf = readLine.indexOf("до н. э.");			
		}
		
		if (indexOf != -1 ||indexOf1!=-1) {
			if (fd instanceof FreeFormDate) {
				FreeFormDate from = (FreeFormDate) fd;
				if (from != null && from.getYear() != null) {
					from.setYear(-from.getYear());
				}
			}
			if (fd instanceof FreeFormDateRange) {
				FreeFormDateRange r0 = (FreeFormDateRange) fd;
				FreeFormDate from = r0.getFrom();
				FreeFormDate to = r0.getTo();
				if (from != null && from.getYear() != null) {
					from.setYear(-from.getYear());
				}
				if (to != null && to.getYear() != null) {
					to.setYear(-to.getYear());
				}
			}
		}
	}
	static int yearId=WordNetProvider.getInstance().getWordElement("год").id();

	private static boolean testGrams(String readLine, IFreeFormDate fd, int pos) {
		if (fd instanceof FreeFormDate) {
			FreeFormDate fq = (FreeFormDate) fd;
			if (fq.getDay() != null && fq.getMonth() == null) {
				return false;
			}
			if (fq.getDay() != null) {
				if (fq.getDay() > 31) {
					return false;
				}
				if (fq.getDay() == 0) {
					return false;
				}
			}
		}
		if (pos + 1 >= readLine.length()) {
			return false;
		}
		for (int a = pos + 1; a >= 0; a--) {
			char c = readLine.charAt(a);
			if (Character.isDigit(c)) {
				continue;
			}
			if (c == '.') {
				continue;
			}
			if (c == ',') {
				return false;
			}
			if (c == '/') {
				return false;
			}
			if (c == '-') {
				return false;
			}
			if (c == '+') {
				return false;
			}
			break;

		}
		int k = 0;
		boolean hasDD = false;
		boolean hasSlash = false;
		for (int a = pos + 1; a < readLine.length(); a++) {
			char c = readLine.charAt(a);
			if (Character.isDigit(c)) {
				continue;
			}
			if (c == '/') {
				if (hasDD) {
					return false;
				}
				hasSlash = true;
			}
			if (c == '.') {
				if (hasSlash) {
					return false;
				}
				hasDD = true;
				continue;
			}
			if (c == '-') {
				continue;
			}
			if (c == '—') {
				continue;
			}
			if (c==' '){
				continue;
			}
			if (c == ',') {
				return false;
			} else {
				k = a;
				break;
			}
		}
		if (k - (pos + 1) > 10) {
			return false;
		}
		if (readLine.contains("жител")){
			return false;//FIXME
		}
		if (readLine.contains("ISSN")){
			return false;//FIXME
		}
		
		for (int a = k; a < readLine.length(); a++) {
			char c = readLine.charAt(a);
			if (Character.isWhitespace(c)) {
				continue;
			}
			if (Character.isDigit(c)) {
				continue;
			}
			if (c == ' ') {
				continue;
			}
			if (c == '-') {
				continue;
			}
			if (c == '—') {
				continue;
			}
			if (c == '/') {
				continue;
			}
			
			String kk = readLine.substring(a).trim();
			
			if (kk.startsWith("%")) {
				return false;
			}
			if (kk.startsWith("л. с.")) {
				return false;
			}
			if (kk.startsWith("об/мин")) {
				return false;
			}
			
			if (kk.startsWith("°")) {
				return false;
			}
			if (kk.startsWith("$")) {
				return false;
			}
			if (kk.startsWith("МГц")) {
				return false;
			}
			if (kk.startsWith("Мбит")) {
				return false;
			}
			if (kk.startsWith("^")) {
				return false;
			}
			
			if (kk.startsWith("кг")) {
				return false;
			}
			
			if (kk.startsWith("км")) {
				return false;
			}
			if (kk.startsWith("м")) {
				return false;
			}
			if (kk.startsWith("K ")) {
				return false;
			}
			if (kk.startsWith("K.")) {
				return false;
			}
			if (kk.startsWith("Па")) {
				return false;
			}
			if (kk.startsWith("ярд")) {
				return false;
			}
			if (kk.startsWith("фут")) {
				return false;
			} 
			
			if (kk.startsWith("см")) {
				return false;
			}
			if (kk.startsWith("гр")) {
				return false;
			}
			if (kk.startsWith("в")) {
				return false;
			}
			for (int i = 0; i < kk.length(); i++) {
				char ca = kk.charAt(i);
				if (!Character.isLetter(ca)) {
					if (Character.isWhitespace(ca)){
						if (i==1){
							if (ca=='г'){
								return true;
							}
							
							kk=kk.substring(1).trim();
							break;
						}
					}
					else{
						break;
					}
				}
			}
			
			for (int i = 0; i < kk.length(); i++) {
				char ca = kk.charAt(i);
				if (!Character.isLetter(ca)) {
					kk = kk.substring(0, i);
					break;
				}
			}
			AbstractWordNet instance = WordNetProvider
					.getInstance();
			boolean isMultipleForm=false;
			if (kk.equals("гг")){
				return true;
			}
			
			GrammarRelation[] possibleGrammarForms = instance.getPossibleGrammarForms(kk);
			if (possibleGrammarForms != null) {
				Grammem.PartOfSpeech.NOUN.mayBeThisPartOfSpech(possibleGrammarForms);
				for (GrammarRelation q : possibleGrammarForms) {
					if(q.conceptId==yearId){
						break;
					}
					HashSet<Grammem> grammemSet = instance.getGrammemSet(q.relation);
					if(grammemSet.contains(Grammem.Case.GENT)){
						isMultipleForm=true;			
					}
					if(grammemSet.contains(Grammem.Case.GEN1)){
						isMultipleForm=true;
					}
					if(grammemSet.contains(Grammem.Case.GEN2)){
						isMultipleForm=true;
					}
				}
			}
			if (isMultipleForm){
				return false;
			}
			break;
		}
		return true;
	}

	private static boolean isValidRange(String readLine, Integer integer,
			IFreeFormDate fd, int min, int max) {
		int pos = integer - 2;
		FreeFormDateRange r0 = (FreeFormDateRange) fd;
		FreeFormDate from = r0.getFrom();
		if (from == null) {
			return false;
		}
		if (!testYears(from, min, max)) {
			return false;
		}
		FreeFormDate to = r0.getTo();
		if (to == null) {
			return false;
		}
		if (!testYears(to, min, max)) {
			return false;
		}
		for (int a = pos; a >= 0; a--) {
			char c = readLine.charAt(a);
			if (!Character.isWhitespace(c)) {
				if (c == '+') {
					return false;
				}
				if (c == '#') {
					return false;
				}
				if (Character.isDigit(c)) {
					return false;
				}
				if (c == ',') {
					return false;
				}
				if (c == '-') {
					return false;
				}
				if (c == '№') {
					return false;
				}
				if (c == '$') {
					return false;
				}
				if (c == '*') {
					return false;
				}
				if (c == '/') {
					return false;
				}
				break;
			}
		}
		char charAt = readLine.charAt(pos + 1);
		if (charAt == '0') {
			return false;
		}
		if (Character.isLetter(charAt)) {
			return false;
		}
		return true;
	}

	private static boolean testYears(FreeFormDate from, int min, int max) {
		if (from.getYear() != null) {
			int year = from.getYear();
			if (min != Integer.MAX_VALUE && max != Integer.MIN_VALUE) {
				if (year / 1000 >= min / 1000 && year / 1000 <= max / 1000) {
					return true;
				}
			}
		}
		return true;
	}

	private static boolean isValid(String readLine, Integer integer,
			FreeFormDate date, int min, int max) {
		int pos = integer - 2;
		if (date.getDay() != null) {
			return false;
		}
		for (int a = pos; a >= 0; a--) {
			char c = readLine.charAt(a);
			if (!Character.isWhitespace(c)) {
				if (c == '+') {
					return false;
				}
				if (c == ' ') {
					continue;
				}
				if (c == '#') {
					return false;
				}
				if (Character.isDigit(c)) {
					return false;
				}
				if (c == ',') {
					return false;
				}
				if (c == '-') {
					return false;
				}
				if (c == '№') {
					return false;
				}
				if (c == '$') {
					return false;
				}
				if (c == '*') {
					return false;
				}
				if (c == '/') {
					return false;
				}
				break;
			}
		}
		if (readLine.charAt(pos + 1) == '0') {
			return false;
		}
		if (date.getYear() == null) {
			return false;
		}
		int year = date.getYear();
		// allow years in our range
		if (min != Integer.MAX_VALUE && max != Integer.MIN_VALUE) {
			if (year / 200 >= min / 200 && year / 200 <= max / 200) {
				return true;
			}
		}
		if (year > 2014 || date.getYear() < 1700) {
			return false;
		}
		return true;
	}

	private static boolean isTrusted(String readLine, Integer integer,
			FreeFormDate date, int min, int max) {
		int pos = integer - 1;
		// has god sign
		Integer year = date.getYear();
		for (int a = pos; a < readLine.length(); a++) {
			char c = readLine.charAt(a);
			if (Character.isLetter(c)) {
				if (c != 'г') {
					return false;
				} else {
					if (year != null)
						if (min != Integer.MAX_VALUE
								&& max != Integer.MIN_VALUE) {
							if (year / 1000 >= min / 1000
									&& year / 1000 <= max / 1000) {
								return true;
							} else {
								return false;
							}
						}
					return true;
				}
			}
			if (Character.isDigit(c)) {
				continue;
			}
			if (!Character.isWhitespace(c)) {
				return false;
			}
		}

		return false;
	}

	public void appendDate(IFreeFormDate date, boolean trusted) {
		if (date instanceof FreeFormDateRange) {
			FreeFormDateRange range = (FreeFormDateRange) date;
			if (range.getFrom() == null) {
				return;
			}
			if (range.getTo() == null) {
				return;
			}
			if (!isOk(range.getFrom())) {
				return;
			}
			if (!isOk(range.getTo())) {
				return;
			}
			Integer year = range.getFrom().getYear();
			if (year == null) {
				return;
			}
			
			if (trusted) {
				appendTrustedYear(year);
			} else {
				appendUntrustedYear(year);
			}
			year = range.getTo().getYear();
			if (year == null) {
				return;
			}
			if (trusted) {
				appendTrustedYear(year);
			} else {
				appendUntrustedYear(year);
			}
			if (range.getFrom().getMonth() != null
					|| range.getTo().getMonth() != null) {
				hasRangesWithDays = true;
			} else {
				hasRanges = true;
			}
		} else {
			FreeFormDate dat = (FreeFormDate) date;

			if (!isOk(dat)) {
				return;
			}
			if (dat.getYear() == null) {
				if (lastYear != -1) {
					dat.setYear(lastYear);
				} else {
					return;
				}
			} else {
				
				if (trusted) {
					appendTrustedYear(dat.getYear());
				} else {
					appendUntrustedYear(dat.getYear());
				}
			}
			if (dat.getMonth() != null) {
				hasDays = true;
			}
		}
		if (datesSet.add(date)) {
			dates.add(date);
		}
	}

	private boolean isOk(FreeFormDate from) {
		if (from == null) {
			return false;
		}
		if (from.getDay() != null && from.getMonth() == null) {
			return false;
		}
		Integer year = from.getYear();
		if (year != null && year > 2600) {
			return false;
		}
		return true;
	}

	// protected LinkedHashSet<FreeFormDateRange>rangesWithoutDays;

	void appendTrustedYear(int year) {
		if (minTrustedYear > year) {
			minTrustedYear = year;
		}
		if (maxTrustedYear < year) {
			maxTrustedYear = year;
		}
		trustedYears.add(year);
		lastYear = year;
	}

	void appendUntrustedYear(int year) {
		untrustedYears.add(year);
		lastYear = -1;
	}

	public void write(ByteArrayOutputStream stream) throws IOException {
		validateYears();
		writeYears(stream);
		if (hasRanges()) {
			stream.write(RANGE_MARK);
			writeRanges(stream);
		}
		if (hasRangesWithDays()) {
			stream.write(RANGE_WITHDAYSMARK);
			writeRangesWithDays(stream);
		}
		if (hasDays()) {
			stream.write(DAYS_MARK);
			writeDays(stream);
		}
	}

	public void read(ByteArrayInputStream stream) {

	}

	private void writeRangesWithDays(ByteArrayOutputStream stream)
			throws IOException {
		for (IFreeFormDate d : dates) {
			if (d instanceof FreeFormDateRange) {
				FreeFormDate from = ((FreeFormDateRange) d).getFrom();
				if (from.getMonth() != null) {
					FreeFormDate to = ((FreeFormDateRange) d).getTo();
					if (to.getMonth() != null) {
						writeYear(from.getYear(), stream);
						writeMonthAndDay(from, stream);
						writeYear(to.getYear(), stream);
						writeMonthAndDay(to, stream);
					}
				}
			}
		}
	}

	private void writeMonthAndDay(FreeFormDate from,
			ByteArrayOutputStream stream) throws IOException {
		stream.write(from.getMonth().ordinal());
		if (from.getDay() == null) {
			stream.write(32);
		} else {
			stream.write(from.getDay());
		}
	}

	private boolean hasRangesWithDays() {
		return hasRangesWithDays;
	}

	private boolean hasDays() {
		return hasDays;
	}

	private void writeRanges(ByteArrayOutputStream stream) throws IOException {
		for (IFreeFormDate d : dates) {
			if (d instanceof FreeFormDateRange) {
				FreeFormDate from = ((FreeFormDateRange) d).getFrom();
				if (from.getMonth() == null) {
					FreeFormDate to = ((FreeFormDateRange) d).getTo();
					if (to.getMonth() == null) {
						writeYear(from.getYear(), stream);
						writeYear(to.getYear(), stream);
					}
				}
			}
		}
	}

	public static final int TWO_BYTE_MIN = 900 * 1000;
	public static final int TWO_BYTES = 1000 * 1000;
	public static final int THREE_BYTE_MIN = 900 * 10000;
	public static final int THREE_BYTES = 1000 * 10000;

	public static boolean testYear(int position, int endPosition, byte[] bytes,
			int startYear, int endYear) {
		int fe = Integer.MIN_VALUE;
		int le = Integer.MIN_VALUE;
		while (position < endPosition) {
			int year = year(position, bytes);
			position++;
			if (year < 0 && year <= -THREE_BYTE_MIN) {
				year += THREE_BYTES;
				if (year == RANGE_WITHDAYSMARK) {
					// test range
					return testYearInRangesWithDays(position, endPosition,
							bytes, startYear, endYear);
				}
				if (year == RANGE_MARK) {
					return testYearInRanges(position, endPosition, bytes,
							startYear, endYear);
					// test
				}
				return false;
			}
			if (year > THREE_BYTE_MIN) {
				position += 2;
				year -= THREE_BYTES;
			}
			if (year > TWO_BYTE_MIN) {
				position++;
				year -= TWO_BYTES;
			}
			if (fe == Integer.MIN_VALUE) {
				fe = year;
				if (fe > endYear) {
					return false;
				}

			} else {
				if (le == Integer.MIN_VALUE) {
					le = year;
					if (le < startYear) {
						return false;
					}
				}
			}
			if (year >= startYear && year <= endYear) {
				return true;
			}
		}
		return false;
	}

	public static boolean testAccurateDate(int position, int endPosition,
			byte[] bytes, int startYear, int endYear, int startMonth,
			int endMonth, int startDay, int endDay) {
		while (position < endPosition) {
			int year = year(position, bytes);
			position++;
			if (year < 0 && year <= -THREE_BYTE_MIN) {
				year += THREE_BYTES;
				if (year == RANGE_WITHDAYSMARK) {
					// test range
					return testAccurateDateInRangesWithDays(position,
							endPosition, bytes, startYear, startMonth,
							startDay, endYear, endMonth, endDay);
				}
				if (year == RANGE_MARK) {
					return testAccurateDateInRanges(position, endPosition,
							bytes, startYear, startMonth, startDay, endYear,
							endMonth, endDay);
					// test
				}
				if (year == DAYS_MARK) {
					return testAccurateDateInCompleteDays(position,
							endPosition, bytes, startYear, endYear, startMonth,
							endMonth, startDay, endDay);
				}
				return false;
			}
			if (year > THREE_BYTE_MIN) {
				position += 2;
				year -= THREE_BYTES;
			}
			if (year > TWO_BYTE_MIN) {
				position++;
				year -= TWO_BYTES;
			}
			if (accept(year, 32, 32, startYear, startMonth, startDay, endYear,
					endMonth, endDay)) {
				return true;
			}

		}
		return false;
	}

	private static boolean testAccurateDateInCompleteDays(int position,
			int endPosition, byte[] bytes, int startYear, int endYear,
			int startMonth, int endMonth, int startDay, int endDay) {
		while (position < endPosition) {
			int year = year(position, bytes);
			position++;
			if (year > THREE_BYTE_MIN) {
				position += 2;
				year -= THREE_BYTES;
			}
			if (year > TWO_BYTE_MIN) {
				position++;
				year -= TWO_BYTES;
			}
			int month = bytes[position++];
			int day = bytes[position++];
			if (accept(year, month, day, startYear, startMonth, startDay,
					endYear, endMonth, endDay)) {
				return true;
			}

		}
		return false;
	}

	static boolean accept(int year, int month, int day, int startYear,
			int startMonth, int startDay, int endYear, int endMonth, int endDay) {
		if (year >= startYear && year <= endYear) {
			if ((month >= startMonth && month <= endMonth)
					|| (startMonth == 32 && endMonth == 32)) {
				if ((day >= startDay && day <= endDay)
						|| (startDay == 32 && endDay == 32)) {
					return true;
				}
			}
		}
		return false;
	}

	static boolean testYearInRanges(int position, int endPosition,
			byte[] bytes, int startYear, int endYear) {
		while (position < endPosition) {
			int year = year(position, bytes);
			position++;
			if (year < 0 && year <= -THREE_BYTE_MIN) {
				year += THREE_BYTES;
				if (year == RANGE_WITHDAYSMARK) {
					// test range
					return testYearInRangesWithDays(position, endPosition,
							bytes, startYear, endYear);
				}
				return false;
			}
			if (year > THREE_BYTE_MIN) {
				position += 2;
				year -= THREE_BYTES;
			}
			if (year > TWO_BYTE_MIN) {
				position++;
				year -= TWO_BYTES;
			}
			int year1 = year(position, bytes);
			position++;

			if (year1 > THREE_BYTE_MIN) {
				position += 2;
				year1 -= THREE_BYTES;
			}
			if (year1 > TWO_BYTE_MIN) {
				position++;
				year1 -= TWO_BYTES;
			}
			if (year >= startYear && year <= endYear) {
				if (year1 >= startYear && year1 <= endYear) {
					return true;
				}
			}
		}
		return false;
	}

	static boolean testAccurateDateInRanges(int position, int endPosition,
			byte[] bytes, int startYear, int startMonth, int startDay,
			int endYear, int endMonth, int endDay) {
		while (position < endPosition) {
			int year = year(position, bytes);
			position++;
			if (year < 0 && year <= -THREE_BYTE_MIN) {
				year += THREE_BYTES;
				if (year == RANGE_WITHDAYSMARK) {
					// test range
					return testAccurateDateInRangesWithDays(position,
							endPosition, bytes, startYear, startMonth,
							startDay, endYear, endMonth, endDay);
				}
				if (year == DAYS_MARK) {
					return testAccurateDateInCompleteDays(position,
							endPosition, bytes, startYear, endYear, startMonth,
							endMonth, startDay, endDay);
				}
				return false;
			}
			if (year > THREE_BYTE_MIN) {
				position += 2;
				year -= THREE_BYTES;
			}
			if (year > TWO_BYTE_MIN) {
				position++;
				year -= TWO_BYTES;
			}
			int year1 = year(position, bytes);
			position++;

			if (year1 > THREE_BYTE_MIN) {
				position += 2;
				year1 -= THREE_BYTES;
			}
			if (year1 > TWO_BYTE_MIN) {
				position++;
				year1 -= TWO_BYTES;
			}
			if (accept(year, 32, 32, startYear, startMonth, startDay, endYear,
					endMonth, endDay)) {
				if (accept(year1, 32, 32, startYear, startMonth, startDay,
						endYear, endMonth, endDay)) {
					return true;
				}
			}
		}
		return false;
	}

	static boolean testAccurateDateInRangesWithDays(int position,
			int endPosition, byte[] bytes, int startYear, int startMonth,
			int startDay, int endYear, int endMonth, int endDay) {
		while (position < endPosition) {
			int year = year(position, bytes);
			position++;
			if (year < 0 && year <= -THREE_BYTE_MIN) {
				year += THREE_BYTES;
				if (year == DAYS_MARK) {
					return testAccurateDateInCompleteDays(position,
							endPosition, bytes, startYear, endYear, startMonth,
							endMonth, startDay, endDay);
				}
				return false;
			}

			if (year > THREE_BYTE_MIN) {
				position += 2;
				year -= THREE_BYTES;
			}
			if (year > TWO_BYTE_MIN) {
				position++;
				year -= TWO_BYTES;
			}
			int m = bytes[position++];
			int d = bytes[position++];
			int year1 = year(position, bytes);
			position++;

			if (year1 > THREE_BYTE_MIN) {
				position += 2;
				year1 -= THREE_BYTES;
			}
			if (year1 > TWO_BYTE_MIN) {
				position++;
				year1 -= TWO_BYTES;
			}
			int m1 = bytes[position++];
			int d1 = bytes[position++];
			// position += 2;
			if (accept(year, m, d, startYear, startMonth, startDay, endYear,
					endMonth, endDay)) {
				if (accept(year1, m1, d1, startYear, startMonth, startDay,
						endYear, endMonth, endDay)) {
					return true;
				}
			}
		}
		return false;
	}

	static boolean testYearInRangesWithDays(int position, int endPosition,
			byte[] bytes, int startYear, int endYear) {
		while (position < endPosition) {
			int year = year(position, bytes);
			position++;
			if (year < 0 && year <= -THREE_BYTE_MIN) {
				year += THREE_BYTE_MIN;
				return false;
			}
			if (year > THREE_BYTE_MIN) {
				position += 2;
				year -= THREE_BYTES;
			}
			if (year > TWO_BYTE_MIN) {
				position++;
				year -= TWO_BYTES;
			}
			position += 2;
			int year1 = year(position, bytes);
			position++;

			if (year1 > THREE_BYTE_MIN) {
				position += 2;
				year1 -= THREE_BYTES;
			}
			if (year1 > TWO_BYTE_MIN) {
				position++;
				year1 -= TWO_BYTES;
			}
			position += 2;
			if (year >= startYear && year <= endYear) {
				if (year1 >= startYear && year1 <= endYear) {
					return true;
				}
			}
		}
		return false;
	}

	static int year(int position, byte[] bytes) {
		byte c = bytes[position++];
		int year = -1;
		if (c >= 120) {
			// this is two byte year
			int n = bytes[position++];
			year = (c - 120) * 100 + 1000 + n;
			return year + TWO_BYTES;
		}
		if (c == -122) {
			// this is three byte year
			int n = bytes[position++];
			int n1 = bytes[position++];
			year = n * 100 + n1;
			return year + THREE_BYTES;
		}
		if (c <= RANGE_WITHDAYSMARK) {
			return c - THREE_BYTES;
		}
		year = 1900 + c;
		return year;
	}

	private void writeYear(int year, ByteArrayOutputStream bs)
			throws IOException {
		if (year > 1780 && year < 2020) {
			bs.write(year - 1900);
		} else {
			if (year > 1000 && year < 1800) {
				bs.write(120 + (year - 1000) / 100);
				bs.write(year % 100);
			} else {
				bs.write(-122);
				bs.write(year / 100);
				bs.write(year % 100);
			}
		}
	}

	private void writeDays(ByteArrayOutputStream stream) throws IOException {
		for (IFreeFormDate d : dates) {
			if (d instanceof FreeFormDate) {
				FreeFormDate from = ((FreeFormDate) d);
				if (from.getMonth() != null) {
					writeYear(from.getYear(), stream);
					writeMonthAndDay(from, stream);
				}
			}
		}
	}

	private boolean hasRanges() {
		return hasRanges;
	}

	private void writeYears(ByteArrayOutputStream stream) throws IOException {
		IntOpenHashSetSerializable all = new IntOpenHashSetSerializable(trustedYears);
		all.addAll(untrustedYears);
		int[] array = all.toArray();
		Arrays.sort(array);
		if (array.length > 1) {
			writeYear(array[0], stream);
			writeYear(array[array.length - 1], stream);
			for (int a = 1; a < array.length - 1; a++) {
				writeYear(array[a], stream);
			}
		} else {
			for (int year : all.toArray()) {
				writeYear(year, stream);
			}
		}
		// return null;
	}

	protected void validateYears() {
		IntOpenHashSetSerializable untrusted = new IntOpenHashSetSerializable();
		// int maxDelta=maxTrustedYear-minTrustedYear;
		for (int year : untrustedYears.toArray()) {
			if (validateYear(year)) {
				untrusted.add(year);
			}
		}
		this.untrustedYears = untrusted;
		for (IFreeFormDate q : new ArrayList<IFreeFormDate>(dates)) {
			if (q instanceof FreeFormDateRange) {
				FreeFormDate fd = (FreeFormDate) ((FreeFormDateRange) q)
						.getFrom();
				if (fd == null) {
					dates.remove(q);
					continue;
				}
				Integer year = fd.getYear();
				if (year == null) {
					continue;
				}
				if (!untrustedYears.contains(year)
						&& !trustedYears.contains(fd.getYear())) {
					dates.remove(q);
				}
				fd = (FreeFormDate) ((FreeFormDateRange) q).getTo();
				if (fd == null) {
					dates.remove(q);
					continue;
				}
				year = fd.getYear();
				if (year == null) {
					continue;
				}
				if (!untrustedYears.contains(fd.getYear())
						&& !trustedYears.contains(fd.getYear())) {
					dates.remove(q);
				}
			} else {
				FreeFormDate fd = (FreeFormDate) q;
				Integer year = fd.getYear();
				if (year == null) {
					continue;
				}
				if (!untrustedYears.contains(year)
						&& !trustedYears.contains(fd.getYear())) {
					dates.remove(fd);
				}
			}
		}
	}
	
	

	private boolean validateYear(int year) {
		// примерно тот же промежуток
		if (year / 50 >= minTrustedYear / 50) {
			if (year / 50 <= maxTrustedYear / 50) {
				// okey this year looks to be in ok range
				return true;
			}
		}
		int count = 0;
		for (int q : untrustedYears.toArray()) {
			if (q != year) {
				if (year / 100 == q / 100) {
					count++;
				}
			}
		}
		for (int q : trustedYears.toArray()) {
			if (q != year) {
				if (year / 100 == q / 100) {
					count++;
				}
			}
		}
		if (count >= MATCH_CENTURY_COUNT) {
			return true;
		}
		return false;
	}

	protected IntOpenHashSetSerializable untrustedYears = new IntOpenHashSetSerializable();

	public IFreeFormDate[] getDates() {
		return dates.toArray(new IFreeFormDate[dates.size()]);
	}

	public void init(DatesInfo info) {
		for (IFreeFormDate q : info.dates) {
			appendDate(q, true);
		}
	}
}