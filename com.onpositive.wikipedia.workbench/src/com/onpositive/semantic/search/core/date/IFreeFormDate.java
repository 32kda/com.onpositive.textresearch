package com.onpositive.semantic.search.core.date;

import java.io.IOException;
import java.util.Date;

public interface IFreeFormDate {
	/**
	 * Checks if specified date is inside the rage set by the current date.
	 * In example, returns true for current date being "1980" and specified date being "18 may 1980".
	 * @param date
	 * @return
	 */
	public boolean intersects(IFreeFormDate date);
	
	public Date getStartDate();
	
	public Date getEndDate();
	
	public boolean isComplete();
	
	public long pack() throws IOException;

	public long length();
	
	public float lengthInYears();
}
