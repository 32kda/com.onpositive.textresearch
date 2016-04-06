package com.onpositive.semantic.words3.hds;

import com.carrotsearch.hppc.ByteArrayList;
import com.carrotsearch.hppc.CharShortOpenHashMap;

public class StringCoder2 {

	protected CharShortOpenHashMap charToByteMap = new CharShortOpenHashMap();
	
	
	
	protected char[] byteToCharTable = new char[42024];
	
	protected byte[] byteBuffer;
	protected int usedBytes;
	
	protected final int equals(String string, int i) {
		i--;
		int a1 = 0;
		int size = usedBytes;
		int len=string.length();
		int a=0;
		for (a = i; a < size; a++) {
			short b = byteBuffer[a];
			if(b==Byte.MAX_VALUE){
				a++;
				byte b0 = byteBuffer[a];
				a++;
				byte b1 = byteBuffer[a];
				short c0=makeShort(b1, b0);
				b=c0;
			}
			boolean needBreal = false;
			if(b==Byte.MIN_VALUE){
				a++;
				byte b0 = byteBuffer[a];
				a++;
				byte b1 = byteBuffer[a];
				short c0=makeShort(b1, b0);
				b=c0;
				needBreal = true;
			}
			else 
			if (b < 0) {
				b = (byte) -b;
				needBreal = true;
			}
			char c = byteToCharTable[b];
			if (a1==len-1&&!needBreal){
				return -1;
			}
			if (a1==len){
				return -1;
			}
			char charAt = string.charAt(a1++);
			
			if (c != charAt) {
				return -1;
			}
			if (needBreal) {
				if (a1 != len) {
					return -1;
				}
				else{
					return  a+1;
				}
			}
		}
		return a;
	}
	int used;
	
	protected final void encodeString(String string, ByteArrayList rs) {
		int length = string.length();
		for (int a = 0; a < length; a++) {
			char c = string.charAt(a);
			short b = 0;
			if (charToByteMap.containsKey(c)) {
				b = charToByteMap.get(c);
			} else {
				b =  (short) (used+1);
				used++;
				charToByteMap.put(c,b);
				byteToCharTable[b] = c;				
			}
			if (a < length - 1) {
				if (b>=Byte.MAX_VALUE){
					rs.add(Byte.MAX_VALUE);
					rs.add(int0(b));
					rs.add(int1(b));
				}
				else rs.add((byte) b);
			} else {
				if (b>=Byte.MAX_VALUE){
					rs.add(Byte.MIN_VALUE);
					rs.add(int0(b));
					rs.add(int1(b));
				}
				else rs.add((byte) -b);				
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
			short b = byteBuffer[a];
			boolean needBreal = false;
			if (b == Byte.MAX_VALUE) {
				a++;
				byte b0 = byteBuffer[a];
				a++;
				byte b1 = byteBuffer[a];
				short c0=makeShort(b1, b0);
				b=c0;
				
			}
			if (b == Byte.MIN_VALUE) {
				a++;
				byte b0 = byteBuffer[a];
				a++;
				byte b1 = byteBuffer[a];
				short c0=makeShort(b1, b0);
				b=c0;
				needBreal = true;
			}
			else if (b < 0) {
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