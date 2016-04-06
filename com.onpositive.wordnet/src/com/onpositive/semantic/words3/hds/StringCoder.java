package com.onpositive.semantic.words3.hds;

import com.carrotsearch.hppc.CharByteOpenHashMap;

public class StringCoder {

	protected static final int CHAR_TABLE_SIZE = 126;
	protected CharByteOpenHashMap charToByteMap = new CharByteOpenHashMap();
	protected char[] byteToCharTable = new char[CHAR_TABLE_SIZE];
	
	protected byte[] byteBuffer;
	protected int usedBytes;
	
	protected final boolean equals(String string, int i) {
		i--;
		int a1 = 0;
		int size = usedBytes;
		int len=string.length();
		for (int a = i; a < size; a++) {
			byte b = byteBuffer[a];
			boolean needBreal = false;
			if (b < 0) {
				b = (byte) -b;
				needBreal = true;
			}
			char c = byteToCharTable[b];
			if (a1==len-1&&!needBreal){
				return false;
			}
			if (a1==len){
				return false;
			}
			char charAt = string.charAt(a1++);
			if (c=='ё'){
				c='е';
			}
			if (charAt=='ё'){
				charAt='е';
			}
			if (c != charAt) {
				return false;
			}
			if (needBreal) {
				if (a1 != len) {
					return false;
				}
				else{
					return  true;
				}
			}
		}
		return true;
	}
	
	protected final void encodeString(String string, byte[] rs) {
		int length = string.length();
		for (int a = 0; a < length; a++) {
			char c = string.charAt(a);
			byte b = 0;
			if (charToByteMap.containsKey(c)) {
				b = charToByteMap.get(c);
			} else {
				b = (byte) (charToByteMap.size()+1);
				if (b > 125) {
					throw new IllegalStateException();
				}
				charToByteMap.put(c, b);
				byteToCharTable[b] = c;
			}
			if (a < length - 1) {
				rs[a] = b;
			} else {
				rs[a] = (byte) -b;
			}
		}
	}
	private char[] dds = new char[500];
	public void trim() {
		byte[] dd = new byte[usedBytes];
		System.arraycopy(byteBuffer, 0, dd, 0, usedBytes);
		this.byteBuffer = dd;
	}	
	protected int storeData(byte[] bytes) {
		int p=usedBytes;
		if (usedBytes + bytes.length + 1 >= byteBuffer.length) {
			try {
				byte[] dd = new byte[byteBuffer.length * 3 / 2];
				System.arraycopy(byteBuffer, 0, dd, 0, byteBuffer.length);
				this.byteBuffer = dd;
			} catch (OutOfMemoryError e) {
				byte[] dd = new byte[(int) (((long) byteBuffer.length * 9) / 8)];
				System.arraycopy(byteBuffer, 0, dd, 0, byteBuffer.length);
				this.byteBuffer = dd;
			}
		}
		for (int a = 0; a < bytes.length; a++) {
			byteBuffer[usedBytes] = bytes[a];
			usedBytes++;
		}
		return p;
	}
	
	protected final String decode(int i) {
		i--;
		int a1 = 0;
		int size = usedBytes;
		char[] dds2 = dds;
		for (int a = i; a < size; a++) {
			byte b = byteBuffer[a];
			boolean needBreal = false;
			if (b < 0) {
				b = (byte) -b;
				needBreal = true;
			}
			char c = byteToCharTable[b];
			dds2[a1++] = c;
			if (needBreal) {
				break;
			}
		}
		return new String(dds2, 0, a1);
	}
	
	public byte[] getBytes() {
		return byteBuffer;
	}
	
	public static int makeInt(byte b3, byte b2, byte b1,
			byte b0) {
				return (((b3) << 24) | ((b2 & 0xff) << 16) | ((b1 & 0xff) << 8) | ((b0 & 0xff)));
			}
	public static byte int3(int x) {
		return (byte) (x >> 24);
	}
	
	public static byte int2(int x) {
		return (byte) (x >> 16);
	}

	public static byte int1(int x) {
		return (byte) (x >> 8);
	}

	public static byte int0(int x) {
		return (byte) (x);
	}

	public static int makeInt(byte b2, byte b1, byte b0) {
		return (((b2 & 0xff) << 16) | ((b1 & 0xff) << 8) | ((b0 & 0xff)));
	}

	public static short makeShort(byte b1, byte b0) {
		return (short) (( ((b1 & 0xff) << 8) | ((b0 & 0xff))));
	}
}