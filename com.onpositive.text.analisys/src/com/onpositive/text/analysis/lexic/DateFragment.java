package com.onpositive.text.analysis.lexic;

import java.util.Date;

public class DateFragment {
	
	public final static int DET_MILI = 1;
	
	public final static int DET_SECOND = 1<<1;
	
	public final static int DET_MINUTE = 1<<2;	
	
	public final static int DET_HOUR = 1<<3;
	
	public final static int DET_DAY = 1<<4;
	
	public final static int DET_MONTH = 1<<5;
	
	public final static int DET_YEAR = 1<<6;	
		
	public final static int DET_SIMPLE_DATE = DET_DAY & DET_MONTH & DET_YEAR;
	
	public final static int DET_SIMPLE_TIME = DET_MINUTE & DET_HOUR;
	
	public DateFragment(Date date, int detMask) {
		this.date = date;
		this.detMask = detMask;
	}
	
	private final Date date;
	
	private final int detMask;

	public Date getDate() {
		return date;
	}

	public int getDetMask() {
		return detMask;
	}	
	
}
