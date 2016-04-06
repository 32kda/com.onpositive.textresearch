package com.onpositive.semantic.search.core.date;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import com.google.common.collect.Lists;

public class FreeFormDate extends AbstractFreeFormDate implements Serializable {
	
	private static TimeZone STANDARD_TIME_ZONE = TimeZone.getTimeZone("Europe/Moscow");
	
	public enum Month {
		JANUARY("январь", "января", "январе", "январю", "январем"),
		FEBRUARY("февраль", "февраля", "феврале", "февралю", "февралем"),
		MARCH("март", "марта", "марте", "марту", "мартом"),
		APRIL("апрель", "апреля", "апреле", "апрелю", "апрелем"),
		MAY("май", "мая", "мае", "маю", "маем"),
		JUNE("июнь", "июня", "июне", "июню", "июнем"),
		JULY("июль", "июля", "июле", "июлю", "июлем"),
		AUGUST("август", "августа", "августе", "августу", "августом"),
		SEPTEMBER("сентябрь", "сентября", "сентябре", "сентябрю", "сентябрем"),
		OCTOBER("октябрь", "октября", "октябре", "октябрю", "октябрем"),
		NOVEMBER("ноябрь", "ноября", "ноябре", "ноябрю", "ноябрем"),
		DECEMBER("декабрь", "декабря", "декабре", "декабрю", "декабрем");
		
		private List<String> forms;
		
		private Month(String... forms) {
			this.forms = Lists.newArrayList(forms);
		}
		
		public List<String> getForms() {
			return Collections.unmodifiableList(forms);
		}
		
		public String getName() {
			return forms.get(0);
		}
		
		public int getMonthNumber() {
			return this.ordinal();
		}
		
		public static Month fromMonthNumber(int monthNumber) {
			int ordinal = monthNumber;
			Month[] values = Month.values();
			
			if (ordinal < 0 || ordinal > values.length - 1) {
				return null;
			}
			
			return values[ordinal];
		}
		
		public static Month fromNameForm(String nameForm) {
			if (nameForm == null) {
				return null;
			}
			
			String lowercasedNameForm = nameForm.toLowerCase();
			
			for (Month currentValue : Month.values()) {
				List<String> forms = currentValue.getForms();
				
				for (String currentForm : forms) {
					if (lowercasedNameForm.equals(currentForm)) {
						return currentValue;
					}
				}
			}
			
			return null;
		}
		
		public static List<String> getAllForms() {
			List<String> result = new ArrayList<String>();
			
			for (Month currentValue : values()) {
				result.addAll(currentValue.getForms());
			}
			
			return result;
		}
	}
	
	public enum MonthPart {
		BEGINNING("начало", "начала", "начале", "началу", "началом"),
		END("конец", "конца", "конце", "концу", "концом"),
		MIDDLE("середина", "середины", "середине", "серединой", "середину");
		
		private List<String> forms;
		
		private MonthPart(String... forms) {
			this.forms = Lists.newArrayList(forms);
		}
		
		public String getName() {
			return forms.get(0);
		}
		
		public static MonthPart fromNameForm(String nameForm) {
			if (nameForm == null) {
				return null;
			}
			
			String lowercasedNameForm = nameForm.toLowerCase();
			
			for (MonthPart currentValue : MonthPart.values()) {
				List<String> forms = currentValue.getForms();
				
				for (String currentForm : forms) {
					if (lowercasedNameForm.equals(currentForm)) {
						return currentValue;
					}
				}
			}
			
			return null;
		}
		
		public static List<String> getAllForms() {
			List<String> result = new ArrayList<String>();
			
			for (MonthPart currentValue : values()) {
				result.addAll(currentValue.getForms());
			}
			
			return result;
		}
		
		public List<String> getForms() {
			return Collections.unmodifiableList(forms);
		}
	}
	
	public enum YearPart {
		BEGINNING("начало", "начала", "начале", "началу", "началом"),
		END("конец", "конца", "конце", "концу", "концом"),
		MIDDLE("середина", "середины", "середине", "серединой", "середину"),
		SPRING("весна", "весны", "весной", "весне"),
		SUMMER("лето", "лета", "летом", "лету"),
		FALL("осень", "осени", "осенью", "осени"),
		WINTER("зима", "зимы", "зимой", "зиме");
		
		
		private List<String> forms;
		
		private YearPart(String... forms) {
			this.forms = Lists.newArrayList(forms);
		}
		
		public String getName() {
			return forms.get(0);
		}
		
		public static YearPart fromNameForm(String nameForm) {
			if (nameForm == null) {
				return null;
			}
			
			String lowercasedNameForm = nameForm.toLowerCase();
			
			for (YearPart currentValue : YearPart.values()) {
				List<String> forms = currentValue.getForms();
				
				for (String currentForm : forms) {
					if (lowercasedNameForm.equals(currentForm)) {
						return currentValue;
					}
				}
			}
			
			return null;
		}
		
		public static List<String> getAllForms() {
			List<String> result = new ArrayList<String>();
			
			for (YearPart currentValue : values()) {
				result.addAll(currentValue.getForms());
			}
			
			return result;
		}
		
		public List<String> getForms() {
			return Collections.unmodifiableList(forms);
		}
	}
	
	private Integer year;
	
	private Month month;
	
	private YearPart yearPart;
	
	private Integer day;
	
	private MonthPart monthPart;
	
	public FreeFormDate() {
		
	}
	
	public static FreeFormDate now() {
		FreeFormDate result = new FreeFormDate();
		
		Calendar calendar = Calendar.getInstance(STANDARD_TIME_ZONE);
		result.year = calendar.get(Calendar.YEAR);
		result.month = Month.fromMonthNumber(calendar.get(Calendar.MONTH));
		result.day = calendar.get(Calendar.DAY_OF_MONTH);
		
		return result;
	}

	public FreeFormDate(Integer year, Month month, YearPart yearPart, Integer day,
			MonthPart monthPart) {
		this.year = year;
		this.month = month;
		this.yearPart = yearPart;
		this.day = day;
		this.monthPart = monthPart;
	}

	public Integer getYear() {
		return year;
	}

	public void setYear(Integer year) {
		this.year = year;
	}

	public Month getMonth() {
		return month;
	}

	public void setMonth(Month month) {
		this.month = month;
	}

	public Integer getDay() {
		return day;
	}

	public void setDay(Integer day) {
		this.day = day;
	}

	public MonthPart getMonthPart() {
		return monthPart;
	}

	public void setMonthPart(MonthPart monthPart) {
		this.monthPart = monthPart;
	}
	
	public YearPart getYearPart() {
		return yearPart;
	}

	public void setYearPart(YearPart yearPart) {
		this.yearPart = yearPart;
	}
	
	public boolean hasMonthOrYearPart() {
		return month != null || yearPart != null;
	}
	
	public boolean hasDayOrMonthPart() {
		return day != null || monthPart != null;
	}
	
	public void copyMonthOrYearPartTo(FreeFormDate toDate) {
		toDate.setMonth(this.month);
		toDate.setYearPart(this.yearPart);
	}
	
	public void copyDayOrMonthPartTo(FreeFormDate toDate) {
		toDate.setDay(this.day);
		toDate.setMonthPart(this.monthPart);
	}

	/**
	 * Gets whether this date represent addressable point in time.
	 * In example, "1980" is addressable, "28 May" is not (because, we do not know the year),
	 * "1980 25" is not addressable too (we do not know the month).
	 * @return
	 */
	public boolean isComplete() {
		Integer[] toCheck = new Integer[3];
		toCheck[0] = year;
		toCheck[1] = (month==null?null:month.getMonthNumber());
		if (toCheck[1] == null && yearPart != null) {
			toCheck[1] = new Integer(0);
		}
		toCheck[2] = day;
		if (toCheck[2] == null && monthPart != null) {
			toCheck[2] = new Integer(0);
		}
		
		if (toCheck[0] == null) {
			return false;
		}
		
		if (toCheck[1] == null && toCheck[2] != null) {
			return false;
		}
		
		return true;
	}

	@Override
	public String toString() {
		String dayName = null;
		if (day != null) {
			dayName = Integer.toString(day);
		} else if (monthPart != null) {
			dayName = monthPart.getName();
		}
		
		String monthName = null;
		if (month != null) {
			if (dayName != null) {
				monthName = month.getForms().get(1);
			} else {
				monthName = month.getName();
			}
		} else if (yearPart != null) {
			monthName = yearPart.getName();
		}
		
		String yearName = null;
		if (year != null) {
			yearName = Integer.toString(year);
		}
		
		return (dayName!=null?(dayName+" "):"") + 
				(monthName!=null?(monthName+" "):"") + 
				(yearName!=null?(yearName + " г."):"");
	}

	public Date getStartDate() {
		
		if (!isComplete()) {
			return null;
		}
		
		Calendar calendar = new GregorianCalendar();
		calendar.clear();
		if (year == null) {
			return null;
		}
		
		int monthNumber = -1;
		if (month != null) {
			monthNumber = month.getMonthNumber();
		} else if (yearPart != null) {
			switch (yearPart) {
			case BEGINNING:
				monthNumber = Month.JANUARY.getMonthNumber();
				break;
			case END:
				monthNumber = Month.NOVEMBER.getMonthNumber();
				break;
			case MIDDLE:
				monthNumber = Month.MAY.getMonthNumber();
				break;
			case SPRING:
				monthNumber = Month.MARCH.getMonthNumber();
				break;
			case SUMMER:
				monthNumber = Month.JUNE.getMonthNumber();
				break;
			case FALL:
				monthNumber = Month.SEPTEMBER.getMonthNumber();
				break;
			case WINTER:
				monthNumber = Month.DECEMBER.getMonthNumber();
				break;
			}
		} else {
			monthNumber = 0;
		}
		
		int dayNumber = -1;
		if (day != null) {
			dayNumber = day;
		} else if (monthPart != null) {
			switch (monthPart) {
			case BEGINNING:
				dayNumber = 1;
				break;
			case MIDDLE:
				dayNumber = 10;
				break;
			case END:
				dayNumber = 20;
				break;
			}
		} else {
			dayNumber = 1;
		}
		if (year<0){
			int year=-this.year;
			calendar.set(year, monthNumber, dayNumber, 0, 0, 0);
			calendar.set(GregorianCalendar.ERA, GregorianCalendar.BC);
		}
		else{
		calendar.set(year, monthNumber, dayNumber, 0, 0, 0);
		}
		return calendar.getTime();
	}

	public Date getEndDate() {
		if (!isComplete()) {
			return null;
		}
		
		Calendar calendar = Calendar.getInstance(STANDARD_TIME_ZONE);
		calendar.clear();
		if (year == null) {
			return null;
		}
		
		int monthNumber = -1;
		if (month != null) {
			monthNumber = month.getMonthNumber();
		} else if (yearPart != null) {
			switch (yearPart) {
			case BEGINNING:
				monthNumber = Month.MARCH.getMonthNumber();
				break;
			case END:
				monthNumber = Month.DECEMBER.getMonthNumber();
				break;
			case MIDDLE:
				monthNumber = Month.AUGUST.getMonthNumber();
				break;
			case SPRING:
				monthNumber = Month.MAY.getMonthNumber();
				break;
			case SUMMER:
				monthNumber = Month.AUGUST.getMonthNumber();
				break;
			case FALL:
				monthNumber = Month.NOVEMBER.getMonthNumber();
				break;
			case WINTER:
				monthNumber = Month.DECEMBER.getMonthNumber();
				break;
			}
		} else {
			monthNumber = 11;
		}
		
		int dayNumber = -1;
		if (day != null) {
			dayNumber = day;
		} else if (monthPart != null) {
			switch (monthPart) {
			case BEGINNING:
				dayNumber = 9;
				break;
			case MIDDLE:
				dayNumber = 19;
				break;
			case END:
				dayNumber = -1; //last day of the month
				break;
			}
		} else {
			dayNumber = -1; //last day of the month
		}
		
		if (dayNumber != -1) {
			//day is known
			calendar.set(year, monthNumber, dayNumber, 23, 59, 59);
		} else {
			//day is unknown / last day of the month
			calendar.set(Calendar.YEAR, year);
			calendar.set(Calendar.MONTH, monthNumber);
			calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
			calendar.set(Calendar.HOUR_OF_DAY, 23);
			calendar.set(Calendar.MINUTE, 59);
			calendar.set(Calendar.SECOND, 59);
		}
		
		return calendar.getTime();
	}
	
	/**
	 * Packs this date into a single long value for affective storage.
	 * @return 
	 * @throws IOException 
	 */
	public long pack() throws IOException {
		return packInt();
	}
	
	/**
	 * Unpacks this date from a long. All internal values are overwritten from the data inside that long.
	 * @param packed
	 */
	public void unpackFrom(long packed) {
		unpackInt((int) packed);
	}
	
	public int packInt() {
		LongPackager packer = new LongPackager(0);
		packer.packBoolean(monthPart != null);
		if (monthPart != null) {
			packer.packEnumNullSupported(monthPart, MonthPart.class);
		} else {
			packer.packIntegerByMaxValue(day, 31);
		}
		
		packer.packBoolean(yearPart != null);
		if (yearPart != null) {
			packer.packEnumNullSupported(yearPart, YearPart.class);
		} else {
			packer.packEnumNullSupported(month, Month.class);
		}
		
		packer.packIntegerByMaxValue(year, 262144-1);
		
		long value = packer.getValue();
		return (int) value;
	}
	
	void unpackInt(int packed) {
		LongPackager packer = new LongPackager(packed);
		
		if (packer.readBoolean()) {
			monthPart = packer.readEnumNullSupported(MonthPart.class);
		} else {
			day = packer.readIntegerByMaxValue(31);
		}
		
		if (packer.readBoolean()) {
			yearPart = packer.readEnumNullSupported(YearPart.class);
		} else {
			month = packer.readEnumNullSupported(Month.class);
		}

		year = packer.readIntegerByMaxValue(262144-1);
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((day == null) ? 0 : day.hashCode());
		result = prime * result + ((month == null) ? 0 : month.hashCode());
		result = prime * result
				+ ((monthPart == null) ? 0 : monthPart.hashCode());
		result = prime * result + ((year == null) ? 0 : year.hashCode());
		result = prime * result
				+ ((yearPart == null) ? 0 : yearPart.hashCode());
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
		FreeFormDate other = (FreeFormDate) obj;
		if (day == null) {
			if (other.day != null)
				return false;
		} else if (!day.equals(other.day))
			return false;
		if (month != other.month)
			return false;
		if (monthPart != other.monthPart)
			return false;
		if (year == null) {
			if (other.year != null)
				return false;
		} else if (!year.equals(other.year))
			return false;
		if (yearPart != other.yearPart)
			return false;
		return true;
	}

}
