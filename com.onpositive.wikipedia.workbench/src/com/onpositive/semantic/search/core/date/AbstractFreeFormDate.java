package com.onpositive.semantic.search.core.date;

import java.io.Serializable;
import java.util.Date;


public abstract class AbstractFreeFormDate implements IFreeFormDate, Serializable {

	public boolean intersects(IFreeFormDate date) {
		Date currentStartDate = this.getStartDate();
		Date currentEndDate = this.getEndDate();
		
		Date compareStartDate = date.getStartDate();
		Date compareEndDate = date.getEndDate();
		
		return intersects(currentStartDate, currentEndDate, compareStartDate, compareEndDate);
	}
	
	public static boolean intersects(Date startDate1, Date endDate1, Date startDate2, Date endDate2) {
		return RangeUtils.intersects(
				startDate1!=null?startDate1.getTime():null,
				endDate1!=null?endDate1.getTime():null,
				startDate2!=null?startDate2.getTime():null,
				endDate2!=null?endDate2.getTime():null);
	}
	
	public long length() {
		Date startDate = this.getStartDate();
		Date endDate = this.getEndDate();
		
		if (startDate == null || endDate == null) {
			return Long.MAX_VALUE;
		}
		
		return endDate.getTime() - startDate.getTime();
	}
	
	public float lengthInYears() {
		return ((float)length())/((float)31536000000L);
	}
	
}
