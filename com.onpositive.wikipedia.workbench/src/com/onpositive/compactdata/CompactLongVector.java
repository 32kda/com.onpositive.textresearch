package com.onpositive.compactdata;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;

import com.carrotsearch.hppc.IntIntOpenHashMapSerialable;
import com.carrotsearch.hppc.IntLongOpenHashMap;
import com.carrotsearch.hppc.LongByteOpenHashMap;

/**
 * 
 * @author kor Simple data structure to keep compact slowly increasing sequence
 *         of longs, append only
 */
public final class CompactLongVector implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected static final int LC = 256;
	protected static final int IC = 8;

	protected char[] deltas = new char[100000];
	protected int[] reperPoints = new int[100000];
	protected long[] longRpoints = new long[100];
	protected IntIntOpenHashMapSerialable omap = new IntIntOpenHashMapSerialable();

	int usedCount;

	public int memUsed() {
		return deltas.length * 2 + reperPoints.length * 4 + longRpoints.length
				* 8 + (omap.keys.length + omap.values.length) * 4
				+ omap.allocated.length + 20;
	}

	public void add(long value) {
		int lp = usedCount / LC;
		int ip = usedCount / IC;
		if (lp >= longRpoints.length) {
			long[] rr = new long[longRpoints.length * 3 / 2];
			System.arraycopy(longRpoints, 0, rr, 0, longRpoints.length);
			longRpoints = rr;
		}
		if (usedCount % LC == 0) {
			longRpoints[lp] = value;
		}
		if (ip >= reperPoints.length) {
			int[] rr = new int[reperPoints.length * 3 / 2];
			System.arraycopy(reperPoints, 0, rr, 0, reperPoints.length);
			reperPoints = rr;
		}
		boolean isR = usedCount % IC == 0;
		if (isR) {
			reperPoints[ip] = (int) (value - longRpoints[lp]);
		}
		if (usedCount >= deltas.length) {
			char[] rr = new char[deltas.length * 3 / 2];
			System.arraycopy(deltas, 0, rr, 0, deltas.length);
			deltas = rr;
		}
		long prev = isR ? reperPoints[ip] + longRpoints[lp]
				: get(usedCount - 1);
		int k = (int) (value - prev);
		if (k > Character.MAX_VALUE - 1) {
			omap.put(usedCount, k);
		} else {
			deltas[usedCount] = (char) (k + 1);
		}
		usedCount++;

	}

	public long get(int position) {
		int lp = position / LC;
		int ip = position / IC;
		int q = position % IC;
		if (q == 0) {
			long l = longRpoints[lp] + reperPoints[ip];
			return l;
		} else {
			long l = longRpoints[lp] + reperPoints[ip];
			int sp = ip * IC;
			for (int a = 1; a <= q; a++) {
				int i = sp + a;
				int c = deltas[i];
				if (c == 0) {
					c = omap.get(i);
				} else {
					c--;
				}
				l += c;
			}
			return l;
		}
	}

	public void trim() {
		long[] rr = new long[usedCount / LC + 1];
		System.arraycopy(longRpoints, 0, rr, 0, usedCount / LC + 1);
		this.longRpoints = rr;
		int[] rr1 = new int[usedCount / IC + 1];
		System.arraycopy(reperPoints, 0, rr1, 0, usedCount / IC + 1);
		this.reperPoints = rr1;
		char[] rr2 = new char[usedCount];
		System.arraycopy(deltas, 0, rr2, 0, usedCount);
		this.deltas = rr2;
	}

	public int size() {
		return usedCount;
	}

	public void write(DataOutputStream str) throws IOException {
		trim();
		str.writeInt(deltas.length);
		for (int a = 0; a < deltas.length; a++) {
			str.writeChar(deltas[a]);
		}
		int[] reperPoints2 = reperPoints;
		writeIntArray(str, reperPoints2);
		str.writeInt(longRpoints.length);
		for (int a = 0; a < longRpoints.length; a++) {
			str.writeLong(longRpoints[a]);
		}

		str.writeInt(usedCount);
		IntIntOpenHashMapSerialable omap2 = omap;
		writeMap(str, omap2);
	}

	public CompactLongVector(DataInputStream di, boolean close)
			throws IOException {
		deltas = readCharArrray(di);
		reperPoints = readIntArrray(di);
		longRpoints = readLongArrray(di);
		usedCount = di.readInt();
		omap = readMap(di);
		if (close) {
			di.close();
		}
	}

	public CompactLongVector(File fl) throws IOException {
		this(new DataInputStream(new BufferedInputStream(
				new FileInputStream(fl))), true);
	}

	public CompactLongVector() {

	}

	public void store(File q) throws IOException {
		DataOutputStream str = new DataOutputStream(new BufferedOutputStream(
				new FileOutputStream(q)));
		try {
			write(str);
		} finally {
			str.close();
		}
	}

	public static void writeMap(DataOutputStream str, IntIntOpenHashMapSerialable omap)
			throws IOException {
		str.writeFloat(omap.loadFactor);
		try{
			Field field = IntIntOpenHashMapSerialable.class.getDeclaredField("perturbation");
			field.setAccessible(true);
			str.writeInt(field.getInt(omap));
			}catch (Exception e) {
				throw new IllegalStateException();
			}
		writeIntArray(str, omap.keys);
		writeIntArray(str, omap.values);
		str.writeInt(omap.assigned);
		boolean[] allocated = omap.allocated;
		str.writeInt(allocated.length);
		for (int a = 0; a < omap.allocated.length; a++) {
			str.writeBoolean(omap.allocated[a]);
		}
	}

	public static IntIntOpenHashMapSerialable readMap(DataInputStream di)
			throws IOException {
		float readFloat = di.readFloat();
		
		IntIntOpenHashMapSerialable intIntOpenHashMap = new IntIntOpenHashMapSerialable(4,
				readFloat);
		try{
			Field field = IntIntOpenHashMapSerialable.class.getDeclaredField("perturbation");
			field.setAccessible(true);
			field.setInt(intIntOpenHashMap,di.readInt());
			}catch (Exception e) {
				throw new IllegalStateException();
			}
		intIntOpenHashMap.keys = readIntArrray(di);
		intIntOpenHashMap.values = readIntArrray(di);
		intIntOpenHashMap.assigned = di.readInt();
		intIntOpenHashMap.allocated = readBoolArrray(di);
		return intIntOpenHashMap;
	}
	
	public static void writeIntLongMap(DataOutputStream str, IntLongOpenHashMap omap)
			throws IOException {
		str.writeFloat(omap.loadFactor);
		try{
			Field field = IntLongOpenHashMap.class.getDeclaredField("perturbation");
			field.setAccessible(true);
			str.writeInt(field.getInt(omap));
			}catch (Exception e) {
				throw new IllegalStateException();
			}
		writeIntArray(str, omap.keys);
		writeLongArray(str, omap.values);
		str.writeInt(omap.assigned);
		boolean[] allocated = omap.allocated;
		str.writeInt(allocated.length);
		for (int a = 0; a < omap.allocated.length; a++) {
			str.writeBoolean(omap.allocated[a]);
		}
	}

	public static IntLongOpenHashMap readIntLongMap(DataInputStream di)
			throws IOException {
		float readFloat = di.readFloat();
		IntLongOpenHashMap intIntOpenHashMap = new IntLongOpenHashMap(4,
				readFloat);
		try{
			Field field = IntLongOpenHashMap.class.getDeclaredField("perturbation");
			field.setAccessible(true);
			field.setInt(intIntOpenHashMap,di.readInt());
			}catch (Exception e) {
				throw new IllegalStateException();
			}
		intIntOpenHashMap.keys = readIntArrray(di);
		intIntOpenHashMap.values = readLongArrray(di);
		intIntOpenHashMap.assigned = di.readInt();
		intIntOpenHashMap.allocated = readBoolArrray(di);
		return intIntOpenHashMap;
	}

	protected static void writeLongMap(DataOutputStream str,
			LongByteOpenHashMap omap) throws IOException {
		str.writeFloat(omap.loadFactor);
		try{
			Field field = LongByteOpenHashMap.class.getDeclaredField("perturbation");
			field.setAccessible(true);
			str.writeInt(field.getInt(omap));
			}catch (Exception e) {
				throw new IllegalStateException();
			}
		writeLongArray(str, omap.keys);
		str.write(omap.values);
		str.writeInt(omap.assigned);
		boolean[] allocated = omap.allocated;
		str.writeInt(allocated.length);
		for (int a = 0; a < omap.allocated.length; a++) {
			str.writeBoolean(omap.allocated[a]);
		}
	}

	protected static LongByteOpenHashMap readLongMap(DataInputStream di)
			throws IOException {
		float readFloat = di.readFloat();
		LongByteOpenHashMap intIntOpenHashMap = new LongByteOpenHashMap(4,
				readFloat);
		try{
			Field field = LongByteOpenHashMap.class.getDeclaredField("perturbation");
			field.setAccessible(true);
			field.setInt(intIntOpenHashMap,di.readInt());
			}catch (Exception e) {
				throw new IllegalStateException();
			}
		intIntOpenHashMap.keys = readLongArrray(di);
		intIntOpenHashMap.values = new byte[intIntOpenHashMap.keys.length];
		di.readFully(intIntOpenHashMap.values);
		intIntOpenHashMap.assigned = di.readInt();
		intIntOpenHashMap.allocated = readBoolArrray(di);
		return intIntOpenHashMap;
	}

	static boolean[] readBoolArrray(DataInputStream di) throws IOException {
		int k = di.readInt();
		boolean[] rs = new boolean[k];
		for (int a = 0; a < k; a++) {
			rs[a] = di.readBoolean();
		}
		return rs;
	}

	char[] readCharArrray(DataInputStream di) throws IOException {
		int k = di.readInt();
		char[] rs = new char[k];
		for (int a = 0; a < k; a++) {
			rs[a] = di.readChar();
		}
		return rs;
	}

	static int[] readIntArrray(DataInputStream di) throws IOException {
		int k = di.readInt();
		int[] rs = new int[k];
		for (int a = 0; a < k; a++) {
			rs[a] = di.readInt();
		}
		return rs;
	}
	
	static byte[] readByteArrray(DataInputStream di) throws IOException {
		int k = di.readInt();
		byte[] rs = new byte[k];
		for (int a = 0; a < k; a++) {
			rs[a] = di.readByte();
		}
		return rs;
	}

	protected static void writeIntArray(DataOutputStream str, int[] reperPoints2)
			throws IOException {
		str.writeInt(reperPoints2.length);
		for (int a = 0; a < reperPoints2.length; a++) {
			str.writeInt(reperPoints2[a]);
		}
	}
	
	protected static void writeByteArray(DataOutputStream str, byte[] reperPoints2)
			throws IOException {
		str.writeInt(reperPoints2.length);
		for (int a = 0; a < reperPoints2.length; a++) {
			str.writeByte(reperPoints2[a]);
		}
	}

	static long[] readLongArrray(DataInputStream di) throws IOException {
		int k = di.readInt();
		long[] rs = new long[k];
		for (int a = 0; a < k; a++) {
			rs[a] = di.readLong();
		}
		return rs;
	}

	protected static void writeLongArray(DataOutputStream str,
			long[] reperPoints2) throws IOException {
		str.writeInt(reperPoints2.length);
		for (int a = 0; a < reperPoints2.length; a++) {
			str.writeLong(reperPoints2[a]);
		}
	}

}
