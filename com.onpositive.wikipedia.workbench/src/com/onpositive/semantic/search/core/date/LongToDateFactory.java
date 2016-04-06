package com.onpositive.semantic.search.core.date;


public class LongToDateFactory {
	public static IFreeFormDate unpack(long packed) {
		
		LongPackager packer = new LongPackager(packed);
		int toInt = packer.readInt(31);
		int fromInt = packer.readInt(31);
		boolean isRange = packer.readBoolean();
		if (isRange) {
			FreeFormDate fromDate = null;
			FreeFormDate toDate = null;
			if (fromInt != 0) {
				fromDate = new FreeFormDate();
				fromDate.unpackFrom(fromInt);
			}
			
			if (toInt != 0) {
				toDate = new FreeFormDate();
				toDate.unpackFrom(toInt);
			}
			
			return new FreeFormDateRange(fromDate, toDate);
		} else {
			FreeFormDate result = new FreeFormDate();
			result.unpackFrom(toInt);
			return result;
		}
//		packer.packInt(to, 31);
//		packer.packInt(from, 31);
//		packer.packBoolean(true);
//		int from = LongPackager.readFromLong(packed, 32, 32);
//		int to = LongPackager.readFromLong(packed, 0, 32);
//		
//		if (from == 0 && to == 0) {
//			return null;
//		} else if (from != 0 && to == 0) {
//			FreeFormDate result = new FreeFormDate();
//			result.unpackInt(from);
//			return result;
//		} else if (from == 0 && to != 0) {
//			FreeFormDate result = new FreeFormDate();
//			result.unpackInt(to);
//			return result;
//		} else {
//			FreeFormDate fromDate = new FreeFormDate();
//			fromDate.unpackInt(from);
//			
//			FreeFormDate toDate = new FreeFormDate();
//			fromDate.unpackInt(to);
//			
//			return new FreeFormDateRange(fromDate, toDate);
//		}
	}
}
