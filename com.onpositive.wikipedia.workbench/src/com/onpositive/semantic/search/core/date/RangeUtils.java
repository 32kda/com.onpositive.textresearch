package com.onpositive.semantic.search.core.date;

public class RangeUtils {
	/**
	 * Null argument means infinite.
	 * @param startDate1
	 * @param endDate1
	 * @param startDate2
	 * @param endDate2
	 * @return
	 */
	public static final boolean intersects(Long startDate1, Long endDate1, Long startDate2, Long endDate2) {
		if (startDate1 != null && endDate1 != null) {
			if (startDate1 > endDate1) {
				//incorrect range
				return false;
			}
		}
		
		if (startDate2 != null && endDate2 != null) {
			if (startDate2 > endDate2) {
				//incorrect range
				return false;
			}
		}
		
		if (endDate1 != null) {
			if (startDate2 != null) {
				if (startDate2 > endDate1) {
					return false;
				}
			}
		}
		
		if (startDate1 != null) {
			if (endDate2 != null) {
				if (startDate1 > endDate2) {
					return false;
				}
			}
		}
		
		return true;
	}
	
	public static final boolean contains (Long outerStart, Long outerEnd, Long innerStart, Long innerEnd) {
		return (outerStart==null || (innerStart != null && outerStart <= innerStart)) && 
				(outerEnd==null || (innerEnd != null && outerEnd >= innerEnd)); 
	}
	
	public static final boolean containsPrimitiveLongs(long outerStart, long outerEnd, long innerStart, long innerEnd) {
		return outerStart <= innerStart && outerEnd >= innerEnd; 
	}
}
