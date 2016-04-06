package com.onpositive.semantic.search.core.date;

public class LongPackager {
	
	private int counter = 0;
	
	private long value = 0;
	
	public LongPackager(long initialValue) {
		this.value = initialValue;
	}
	
	public long getValue() {
		return value;
	}
	
	public void packInt(int toPack) {
		packInt(32);
	}
	
	/**
	 * Only positive toPack value supported.
	 * @param toPack
	 * @param maxBits
	 * @throws IllegalArgumentException
	 */
	public void packInt(int toPack, int maxBits) throws IllegalArgumentException {
		if (counter + maxBits >= 64) {
			throw new IllegalArgumentException("Can not pack " + toPack + " because number of bits " + 
					(counter + maxBits) + " exceeds maximum of 64 bits");
		}
		
		value = packToLong(value, counter, maxBits, toPack);
		counter += maxBits;
	}
	
	/**
	 * Only positive toPack supported.
	 * @param toPack
	 * @param maxValue
	 */
	public void packIntByMaxValue(int toPack, int maxValue) {
		packInt(toPack, bitsRequiredForMaxValue(maxValue));
	}
	
	/**
	 * Supports null value. Requires maxValue+1 actual different values. 
	 * @param toPack
	 * @param maxValue
	 */
	public void packIntegerByMaxValue(Integer toPack, int maxValue) {
		int maxBits = bitsRequiredForMaxValue(maxValue + 1); //+1 for null
		
		int intToPack = 0;
		if (toPack == null) {
			intToPack = maxValue + 1;
		} else {
			intToPack = toPack;
		}
		packInt(intToPack, maxBits);
	}
	
	/**
	 * 
	 * @param toPack
	 * @throws IllegalArgumentException
	 */
	public void packEnum(Enum toPack) throws IllegalArgumentException {
		int maxBits = bitsRequiredForEnum(toPack.getClass());
		packInt(toPack.ordinal(), maxBits);
	}
	
	public <T extends Enum> void packEnumNullSupported(T toPack, Class<T> enumClass) throws IllegalArgumentException {
		int maxValues = enumClass.getEnumConstants().length + 1;
		int maxBits = bitsRequiredForMaxValue(maxValues);
		int valueToPack = 0;
		if (toPack == null) {
			valueToPack = maxValues;
		} else {
			valueToPack = toPack.ordinal();
		}
		packInt(valueToPack, maxBits);
	}
	
	public void packBoolean(boolean toPack) {
		int toPackint = toPack?1:0;
		packInt(toPackint, 1);
	}
	
	/**
	 * Reads from right to left. Read should be performed in the same order as the prebious pack.
	 * @param maxBits
	 * @return
	 * @throws IllegalArgumentException
	 */
	public int readInt(int maxBits) throws IllegalArgumentException {
		int result = readFromLong(value, counter, maxBits);
		counter += maxBits;
		
		return result;
	}
	
	public int readIntbyMaxValue(int maxValue)  throws IllegalArgumentException {
		int maxBits = bitsRequiredForMaxValue(maxValue);
		return readInt(maxBits);
	}
	
	public Integer readIntegerByMaxValue(int maxValue) {
		int maxBits = bitsRequiredForMaxValue(maxValue + 1) ;
		int intResult = readInt(maxBits);
		
		if (intResult == maxValue + 1) {
			return null;
		} else {
			return intResult;
		}
	}
	
	public <T extends Enum> T readEnum(Class<T> enumClass) throws IllegalArgumentException {
		int maxBits = bitsRequiredForEnum(enumClass);
		int ordinal = readInt(maxBits);
		
		return enumClass.getEnumConstants()[ordinal];
	}
	
	public boolean readBoolean() {
		int readInt = readInt(1);
		return readInt == 1;
	}
	
	public <T extends Enum> T readEnumNullSupported(Class<T> enumClass) {
		int maxValues = enumClass.getEnumConstants().length + 1;
		int maxBits = bitsRequiredForMaxValue(maxValues);
		int unpackedValue = readInt(maxBits);
		if (unpackedValue == maxValues) {
			return null;
		} else {
			return enumClass.getEnumConstants()[unpackedValue];
		}
	}
	
	private static <T extends Enum> int bitsRequiredForEnum(Class<T> enumClass) {
		int maxValues = enumClass.getEnumConstants().length;
		
		return bitsRequiredForMaxValue(maxValues);
	}

	private static int bitsRequiredForMaxValue(int maxValues) {
		//return (int) Math.ceil(Math.log(maxValues) / Math.log(2)) + 1;
		if (maxValues == 0) return 1;
		int r = 0;
		while (maxValues > 0)
		{
		    ++r;
		    maxValues >>= 1;
		}
		return r;
	}
	
	/**
	 * Only positive integer data supported!
	 * @param toPackTo
	 * @param startBit
	 * @param length
	 * @param data
	 * @return
	 */
	public static long packToLong(long toPackTo, int startBit, int length, int data) {
		long result = toPackTo;
		
		long longData = ((long)data) << startBit;
		
		long maskedData = longData & longMask(startBit, length);
		
		result = result | maskedData;
		return result;
	}
	
	public static int readFromLong(long data, int startBit, int length) {
		long mask = longMask(startBit, length);
		long maskedData = data & mask;
		long shifted = maskedData >>> startBit;
		
		return (int) shifted;
	}
	
	public static long longMask(int start, int length) {
		long result = 0L;
		for (int i = 0; i < length; i++) {
			long bit = 1L << (start + i);
			result = result | bit;
		}
		
		return result;
	}
}
