package com.onpositive.semantic.search.core.date;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;


public class FreeFormDateRange extends AbstractFreeFormDate implements Serializable {
	private FreeFormDate from;
	private FreeFormDate to;
	
	public FreeFormDateRange(FreeFormDate from, FreeFormDate to) {
		this.from = from;
		this.to = to;
	}
	
	public FreeFormDate getFrom() {
		return from;
	}
	public void setFrom(FreeFormDate from) {
		this.from = from;
	}
	public FreeFormDate getTo() {
		return to;
	}
	public void setTo(FreeFormDate to) {
		this.to = to;
	}

	@Override
	public String toString() {
		return "" + (from!=null?from:"[]") + " - " + (to!=null?to:"[]");
	}

	@Override
	public Date getStartDate() {
		if (from!=null) {
			return from.getStartDate();
		}
		
		return null;
	}

	@Override
	public Date getEndDate() {
		if (to != null) {
			return to.getEndDate();
		}
		
		return null;
	}

	@Override
	public boolean isComplete() {
		if (from != null && !from.isComplete()) {
			return false;
		}
		
		if (to != null && !to.isComplete()) {
			return false;
		}
		
		return true;
	}

	@Override
	public long pack() throws IOException {
		int from = getFrom() == null ? 0 : getFrom().packInt();
		int to = getTo() == null ? 0 : getTo().packInt();
		
		LongPackager packer = new LongPackager(0);
		packer.packInt(to, 31);
		packer.packInt(from, 31);
		packer.packBoolean(true);
//		long result = 0;
//		result = LongPackager.packToLong(result, 31, 31, from);
//		result = LongPackager.packToLong(result, 0, 31, to);
//		result = LongPackager.packToLong(result, 62, 1, to);
		
		return packer.getValue();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((from == null) ? 0 : from.hashCode());
		result = prime * result + ((to == null) ? 0 : to.hashCode());
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
		FreeFormDateRange other = (FreeFormDateRange) obj;
		if (from == null) {
			if (other.from != null)
				return false;
		} else if (!from.equals(other.from))
			return false;
		if (to == null) {
			if (other.to != null)
				return false;
		} else if (!to.equals(other.to))
			return false;
		return true;
	}
}
