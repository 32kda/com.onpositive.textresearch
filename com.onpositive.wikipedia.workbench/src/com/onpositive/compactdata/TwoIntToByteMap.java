package com.onpositive.compactdata;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import com.carrotsearch.hppc.LongByteOpenHashMap;

public class TwoIntToByteMap {

	private LongByteOpenHashMap map = new LongByteOpenHashMap();

	private static byte int3(int x) {
		return (byte) (x >> 24);
	}

	private static byte int2(int x) {
		return (byte) (x >> 16);
	}

	private static byte int1(int x) {
		return (byte) (x >> 8);
	}

	private static byte int0(int x) {
		return (byte) (x);
	}

	static int makeInt(byte b3, byte b2, byte b1, byte b0) {
		return (((b3) << 24) | ((b2 & 0xff) << 16) | ((b1 & 0xff) << 8) | ((b0 & 0xff)));
	}

	static long makeLong(byte b7, byte b6, byte b5, byte b4, byte b3, byte b2,
			byte b1, byte b0) {
		return ((((long) b7) << 56) | (((long) b6 & 0xff) << 48)
				| (((long) b5 & 0xff) << 40) | (((long) b4 & 0xff) << 32)
				| (((long) b3 & 0xff) << 24) | (((long) b2 & 0xff) << 16)
				| (((long) b1 & 0xff) << 8) | (((long) b0 & 0xff)));
	}

	public static long fromInts(int i0, int i1) {
		return makeLong(int0(i0), int1(i0), int2(i0), int3(i0), int0(i1),
				int1(i1), int2(i1), int3(i1));
	}

	public void put(int i0, int i1, byte v) {
		map.put(fromInts(i0, i1), v);
	}

	public byte get(int i0, int i1) {
		return map.get(fromInts(i0, i1));
	}
	public boolean hasKey(int i0, int i1) {
		return map.containsKey(fromInts(i0, i1));
	}
	
	public void write(String str) throws IOException{
		DataOutputStream str2 = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(str)));
		CompactLongVector.writeLongMap(str2, map);
		str2.close();
	}
	
	public void read(String str) throws IOException{
		DataInputStream str2 = new DataInputStream(new BufferedInputStream(new FileInputStream(str)));
		map=CompactLongVector.readLongMap(str2);
		str2.close();
	}

	public int size() {
		return map.size();
	}

	public void write(DataOutputStream str) throws IOException {
		CompactLongVector.writeLongMap(str, map);
	}
	public void read(DataInputStream str) throws IOException {
		map=CompactLongVector.readLongMap(str);
	}

	public void remove(int i0, int i1) {
		map.remove(fromInts(i0, i1));
	}
}