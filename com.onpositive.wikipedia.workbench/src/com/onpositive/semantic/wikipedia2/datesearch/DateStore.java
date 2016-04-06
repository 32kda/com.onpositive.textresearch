package com.onpositive.semantic.wikipedia2.datesearch;

import com.onpositive.semantic.search.core.date.IFreeFormDate;

public abstract class DateStore {

	public static class DatesInfo{
		public DatesInfo(int count) {
			this.dates=new IFreeFormDate[count];
			this.positions=new int[count];
		}
		public IFreeFormDate[] dates;
		public int[] positions;
	}

	public abstract DatesInfo getInfo(int d) ;
}
