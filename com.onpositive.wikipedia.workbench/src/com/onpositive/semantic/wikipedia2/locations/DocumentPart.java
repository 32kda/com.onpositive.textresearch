package com.onpositive.semantic.wikipedia2.locations;

public enum DocumentPart{
	TITLE,
	CATEGORY,
	INFOBOX,
	FIRST_PARAGRAPH,
	OTHER;
	
	public final byte toByte() {
		return (byte)(this.ordinal() - 128);
	}
	
	public static DocumentPart fromByte(byte bt) {
		int ordinal = ((int) bt)+128;
		return DocumentPart.values()[ordinal];
	}
}
