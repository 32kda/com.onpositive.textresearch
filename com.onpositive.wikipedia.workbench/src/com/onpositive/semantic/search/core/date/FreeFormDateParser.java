package com.onpositive.semantic.search.core.date;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.Lists;
import com.onpositive.semantic.search.core.date.FreeFormDate.Month;
import com.onpositive.semantic.search.core.date.FreeFormDate.MonthPart;
import com.onpositive.semantic.search.core.date.FreeFormDate.YearPart;
import com.onpositive.semantic.search.core.date.FreeFormDateParser.FreeFormDateParserConfig.YearAccept;

public class FreeFormDateParser {
	
	public static class FreeFormDateParserConfig {
		
		public static enum YearAccept {
			/**
			 * Accepts any figure having 4 digits
			 */
			ANY_FIGURE,
			
			/**
			 * Accepts only year having a sign like "год" 
			 */
			ONLY_SIGNED,
			
			/**
			 * Accepts signed years and years from "trustful" range of 1800-2030.
			 */
			SIGNED_AND_TRUSTFUL_YEAR;
		}
		
		/**
		 * Whether to accept constructions like "в настоящее время" 
		 */
		boolean acceptPresentTime = false; 
		
		/**
		 * Whether to accept years without year sign like "год"
		 */
		YearAccept yearAcceptType = YearAccept.ONLY_SIGNED;
		
		/**
		 * Whether to accept constructions like "в настоящее время" 
		 */
		public FreeFormDateParserConfig setAcceptPresentTime(boolean acceptPresentTime) {
			this.acceptPresentTime = acceptPresentTime;
			return this;
		}
		
		/**
		 * Whether to accept years without year sign like "год"
		 */
		public FreeFormDateParserConfig setYearAcceptType(YearAccept yearAcceptType) {
			this.yearAcceptType = yearAcceptType;
			return this;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (acceptPresentTime ? 1231 : 1237);
			result = prime
					* result
					+ ((yearAcceptType == null) ? 0 : yearAcceptType.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			FreeFormDateParserConfig other = (FreeFormDateParserConfig) obj;
			if (acceptPresentTime != other.acceptPresentTime)
				return false;
			if (yearAcceptType != other.yearAcceptType)
				return false;
			return true;
		}
	}
	
	private static Map<FreeFormDateParserConfig, Pattern> patterns = new HashMap<FreeFormDateParser.FreeFormDateParserConfig, Pattern>();
	
	private static String NEVER = "zYh_nE_Ha_pz";
	
	private static String whitespace_chars =  "["
            + "\\u0009" // CHARACTER TABULATION
            + "\\u000A" // LINE FEED (LF)
            + "\\u000B" // LINE TABULATION
            + "\\u000C" // FORM FEED (FF)
            + "\\u000D" // CARRIAGE RETURN (CR)
            + "\\u0020" // SPACE
            + "\\u0085" // NEXT LINE (NEL) 
            + "\\u00A0" // NO-BREAK SPACE
            + "\\u1680" // OGHAM SPACE MARK
            + "\\u180E" // MONGOLIAN VOWEL SEPARATOR
            + "\\u2000" // EN QUAD 
            + "\\u2001" // EM QUAD 
            + "\\u2002" // EN SPACE
            + "\\u2003" // EM SPACE
            + "\\u2004" // THREE-PER-EM SPACE
            + "\\u2005" // FOUR-PER-EM SPACE
            + "\\u2006" // SIX-PER-EM SPACE
            + "\\u2007" // FIGURE SPACE
            + "\\u2008" // PUNCTUATION SPACE
            + "\\u2009" // THIN SPACE
            + "\\u200A" // HAIR SPACE
            + "\\u2028" // LINE SEPARATOR
            + "\\u2029" // PARAGRAPH SEPARATOR
            + "\\u202F" // NARROW NO-BREAK SPACE
            + "\\u205F" // MEDIUM MATHEMATICAL SPACE
            + "\\u3000" // IDEOGRAPHIC SPACE
            + "]";
	
	public static List<IFreeFormDate> parse(String text) {
		return parse(text, null);
	}
	
	public static List<IFreeFormDate> parse(String text, List<Integer> positions) {
		return parse(text, new FreeFormDateParserConfig(), positions);
	}
	
	public static List<IFreeFormDate> parse(String text, FreeFormDateParserConfig config, List<Integer> positions) {
		if (patterns.get(config) == null) {
			initializePattern(config);
		}
		
		List<IFreeFormDate> result = new ArrayList<IFreeFormDate>();
		
		text = " " + text + " ";
		
		Matcher matcher = patterns.get(config).matcher(text);
		while (matcher.find()) {
			
			//textual range
			FreeFormDate textualStartDate = createDateFromResults(matcher, 0, config);
			FreeFormDate textualEndDate = createDateFromResults(matcher, 1, config);
			if (textualStartDate != null || textualEndDate != null) {
				FreeFormDateRange range = createRange(textualStartDate, textualEndDate);
				if (range != null) {
					if (positions != null) {
						positions.add(matcher.start());
					}
					result.add(range);
				}
				
				continue;
			}
			
			//dash range
			FreeFormDate dashStartDate = createDateFromResults(matcher, 2, config);
			FreeFormDate dashEndDate = createDateFromResults(matcher, 3, config);
			
			if (dashStartDate != null && dashEndDate == null) {
				if (positions != null) {
					positions.add(matcher.start());
				}
				result.add(dashStartDate);
				continue;
			}
			
			if (dashStartDate != null && dashEndDate != null) {
				FreeFormDateRange range = createRange(dashStartDate, dashEndDate);
				if (range != null) {
					if (positions != null) {
						positions.add(matcher.start());
					}
					result.add(range);
				}
				
				continue;
			}
		}
		
		return result;
	}
	
	public static List<IFreeFormDate> parse(String text, FreeFormDateParserConfig config, List<Integer> positions,List<Integer> length) {
		if (patterns.get(config) == null) {
			initializePattern(config);
		}
		
		List<IFreeFormDate> result = new ArrayList<IFreeFormDate>();
		
		text = " " + text + " ";
		
		Matcher matcher = patterns.get(config).matcher(text);
		while (matcher.find()) {
			
			//textual range
			FreeFormDate textualStartDate = createDateFromResults(matcher, 0, config);
			FreeFormDate textualEndDate = createDateFromResults(matcher, 1, config);
			if (textualStartDate != null || textualEndDate != null) {
				FreeFormDateRange range = createRange(textualStartDate, textualEndDate);
				if (range != null) {
					if (positions != null) {
						positions.add(matcher.start());
					}
					if (length!=null){
						length.add(matcher.end()-matcher.start());
					}
					result.add(range);
				}
				
				continue;
			}
			
			//dash range
			FreeFormDate dashStartDate = createDateFromResults(matcher, 2, config);
			FreeFormDate dashEndDate = createDateFromResults(matcher, 3, config);
			
			if (dashStartDate != null && dashEndDate == null) {
				if (positions != null) {
					positions.add(matcher.start());
				}
				if (length!=null){
					length.add(matcher.end()-matcher.start());
				}
				result.add(dashStartDate);
				continue;
			}
			
			if (dashStartDate != null && dashEndDate != null) {
				FreeFormDateRange range = createRange(dashStartDate, dashEndDate);
				if (range != null) {
					if (positions != null) {
						positions.add(matcher.start());
					}
					if (length!=null){
						length.add(matcher.end()-matcher.start());
					}
					result.add(range);
				}
				
				continue;
			}
		}
		
		return result;
	}
	
	private static FreeFormDateRange createRange(FreeFormDate startDate,
			FreeFormDate endDate) {
		if (startDate != null && endDate != null) {
			//adding missing values to the start date for the cases like the following: "5-10 июня 2013 г"
			if (endDate.getYear() != null && startDate.getYear() == null) {
				startDate.setYear(endDate.getYear());
			}
			
			if (endDate.getMonth() != null && startDate.getMonth() == null && startDate.getYearPart() == null) {
				startDate.setMonth(endDate.getMonth());
			}
			
			if (endDate.getYearPart() != null && startDate.getMonth() == null && startDate.getYearPart() == null) {
				startDate.setYearPart(endDate.getYearPart());
			}
		}
		
		return new FreeFormDateRange(startDate, endDate);
	}

	private static FreeFormDate createDateFromResults(Matcher matcher, int dateNumber, FreeFormDateParserConfig config) {
		final int CAPTURE_GROUPS_IN_SINGLE_DATE = 8;
		
		int startingIndex = dateNumber*CAPTURE_GROUPS_IN_SINGLE_DATE + 1;
		
		//checking year
		String yearSign = matcher.group(startingIndex + 7);
		String year = matcher.group(startingIndex + 6);
		if (dateNumber==2||dateNumber==3){
			yearSign="гг";
		}
		Integer yearInt = getYear(year, yearSign, config);
		if (yearInt == null) {
			year = matcher.group(startingIndex + 3);
			yearInt = getYear(year, "", config);
		}
		
		//month
		String monthOrPartOfYear = matcher.group(startingIndex + 5);
		if (monthOrPartOfYear == null) {
			monthOrPartOfYear = matcher.group(startingIndex + 2);
		}
		
		//day
		String dayOrPartOfMonth = matcher.group(startingIndex + 4);
		if (dayOrPartOfMonth == null) {
			dayOrPartOfMonth = matcher.group(startingIndex + 1);
		}
		
		Month month = Month.fromNameForm(monthOrPartOfYear);
		if (month == null && monthOrPartOfYear != null) {
			try {
				int monthNumber = Integer.parseInt(monthOrPartOfYear);
				month = Month.fromMonthNumber(monthNumber - 1);
			} catch (Throwable th){}
			
		}
		YearPart yearPart = null;
		if (month == null) {
			yearPart = YearPart.fromNameForm(monthOrPartOfYear);
		}
		
		Integer dayInt = getDay(dayOrPartOfMonth);
		MonthPart monthPart = null;
		if (dayInt == null) {
			monthPart = MonthPart.fromNameForm(dayOrPartOfMonth);
		}
		
		String currentTime = matcher.group(startingIndex + 0);
		
		FreeFormDate result = null;
		if (yearInt != null || month != null || dayInt != null || yearPart != null || monthPart != null) {
			result = new FreeFormDate(yearInt, month, yearPart, dayInt, monthPart);
		} else if (currentTime != null) {
			result = FreeFormDate.now();
		}
		
		if (result == null) {
			return null;
		}
		
		patchNewDate(result);
		
		return result;
	}
	
	private static void patchNewDate(FreeFormDate result) {
		if (result == null) {
			return;
		}
		
		if (result.getYear() != null && result.getYearPart() == null && result.getMonth() == null 
				&& result.getMonthPart() != null && result.getDay() == null) {
			
			//only year part + year. In such a case parser incorrectly puts year part as a month part (due to them being called in the same way).
			//so moving month part to year part
			switch (result.getMonthPart()) {
			case BEGINNING:
				result.setYearPart(YearPart.BEGINNING);
				break;
			case MIDDLE:
				result.setYearPart(YearPart.MIDDLE);
				break;
			case END:
				result.setYearPart(YearPart.END);
				break;
			default:
				return;
			}
			
			result.setMonthPart(null);
		}
	}

	private static Integer getDay(String day) {
		if (day == null) {
			return null;
		}
		
		try {
			return Integer.parseInt(day);
		} catch (Throwable th) {
		}
		
		return null;
	}

	private static Integer getYear(String year, String yearSign, FreeFormDateParserConfig config) {
		if (year == null) {
			return null;
		}
		
		try {
			Integer result = Integer.parseInt(year);
			
			if (config.yearAcceptType == YearAccept.ANY_FIGURE) {
				if (result <= 0 || result > 2100) {
					return null;
				}
			} else if (config.yearAcceptType == YearAccept.ONLY_SIGNED) {
				if (yearSign == null) {
					return null;
				}
			} else if (config.yearAcceptType == YearAccept.SIGNED_AND_TRUSTFUL_YEAR) {
				if (yearSign == null && (result < 1800 || result > 2030)) {
					return null;
				}
			}
			
			return result;
		} catch (Throwable th) {
		}
		
		return null;
	}

	private static void initializePattern(FreeFormDateParserConfig config) {
		Pattern pattern = Pattern.compile(createMainRegexp(config),
		        Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE);
		patterns.put(config, pattern);
	}


	private static String createMainRegexp(FreeFormDateParserConfig config) {
		//"^"
		//String validStart = /*oneOf("^",*/oneOrMore(oneOf("\\s", new String(Character.toChars(160)), "\\p{Punct}"))/*)*/; //160 is a magic "NO-BREAK SPACE" character, which is used by Wikipedia but not recognized by \\s
		//String validStart = "(?:\\s|\\p{Punct})+";
		//String validStart = "[ \\,\\.]+";
		String validStart = "(?<=" + whitespace_chars +"|\\p{Punct})";
		
		//String validEnd = oneOrMore(oneOf("\\s", new String(Character.toChars(160)), "\\p{Punct}", "$"));
		String validEnd = "(?=" + whitespace_chars +"|\\p{Punct})";
		
		String rangePatternRegexp = createRangePatternRegexp(config);
		
		return validStart + 
				rangePatternRegexp + 
				validEnd;
	}


	private static String createRangePatternRegexp(FreeFormDateParserConfig config) {
		String date = createDateRegexp(config);
		
		String rangeStart = oneOf("с", "после", "со", "начиная");
		String rangeEnd = oneOf("по", "до", "к", "заканчивая", "и заканчивая");
		String dashe = oneOf("-", "—");
		String delimiter = oneOf("\\s","_");
		
		//22 июня 1941 — 18 ноября 1942 ИЛИ 15 мая 2011
		String dashRange = 
				date + 
				optional(some(delimiter) + dashe + some(delimiter) + date);
		
		//с 7 сентября по 22 октября 1941	
		String textualRange =
				optional(rangeStart + some(delimiter) + date) +
				optional(some(delimiter) + rangeEnd + some(delimiter) + date);
		
		return oneOf(textualRange, dashRange);
	}


	private static String createDateRegexp(FreeFormDateParserConfig config) {
		String textDateRegexp = createTextDateRegexp(config);
		String numericDateRegexp = createNumericDateRegexp();
		String currentDateRegexp = createCurrentDateRegexp(config);
		

		return oneOf(currentDateRegexp, numericDateRegexp, textDateRegexp);
	}
	
	/**
	 * Creates date regexp for numeric dates like "15.05.1980"
	 * @return
	 */
	private static String createNumericDateRegexp() {
		String delimiter = oneOf("\\.","/","\\-");
		return "(\\d{1,2})" + delimiter + "(\\d{1,2})" + delimiter + "(\\d{4})";
	}
	
	/**
	 * Creates regexp for current date like "текущий момент" or "настоящее время"
	 * @return
	 */
	private static String createCurrentDateRegexp(FreeFormDateParserConfig config) {
		if (!config.acceptPresentTime) {
			return oneOf(Lists.newArrayList(NEVER), true);
		}
		
		List<String> currentDateKeywords = Lists.newArrayList(
				"текущий момент",
				"текущему моменту",
				"текущего момента",
				"настоящее время",
				"настоящему времени",
				"настоящего времени",
				"сегодня",
				"сегодняшний день",
				"сегодняшнему дню",
				"сегодняшнего дня"
				);
		return oneOf(currentDateKeywords, true);
	}

	/**
	 * Creates regexp for "text" dates like "18 ноября 1980 года"
	 * @return
	 */
	private static String createTextDateRegexp(FreeFormDateParserConfig config) {
		String delimiter = oneOf("\\s","_");
		
		List<String> monthOrPartOfYear = new ArrayList<String>();
		monthOrPartOfYear.addAll(Month.getAllForms());
		monthOrPartOfYear.addAll(YearPart.getAllForms());
		
		List<String> dayOrPartOfMonth = new ArrayList<String>();
		dayOrPartOfMonth.add("\\d{1,2}");
		dayOrPartOfMonth.addAll(MonthPart.getAllForms());
		
		String day_sign = oneOf(dayOrPartOfMonth, true);
		String month_sign = oneOf(monthOrPartOfYear, true);
		String year_sign = oneOf(Lists.newArrayList("год","года","годы","гг", "г"), true);
		
		String day = day_sign + some(delimiter);
		String month = month_sign + some(delimiter);
		String year = "(\\d{4})" + optional(some(delimiter) + year_sign);
				
		String result =
				  optional(day)
				+ optional(month)
				+ optional(year);
		return result;
	}
	
	private static String oneOf(String... args) {
		List<String> vals = new ArrayList<String>();
		for (String arg : args) {
			vals.add(arg);
		}
		
		return oneOf(vals);
	}
	
	private static String oneOf(List<String> potentialValues) {
		return oneOf(potentialValues, false);
	}
	
	private static String oneOf(List<String> potentialValues, boolean capturing) {
		StringBuilder result = new StringBuilder();
		result.append("(");
		if (!capturing) {
			result.append("?:");
		}
		
		for (int i =0; i < potentialValues.size(); i++) {
			String value = potentialValues.get(i);
			result.append(value);
			if (i < potentialValues.size() - 1) {
				result.append("|");
			}
		}
		
		result.append(")");
		
		return result.toString();
	}
	
	private static String optional(String regexp) {
		return "(?:" + regexp + ")?";
	}
	
	private static String some(String regexp) {
		return "(?:" + regexp + ")*";
	}
	
	private static String oneOrMore(String regexp) {
		return "(?:" + regexp + ")+";
	}
}
