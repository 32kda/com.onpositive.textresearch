package com.onpositive.compactdata;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.carrotsearch.hppc.ByteArrayList;
import com.carrotsearch.hppc.IntIntOpenHashMapSerialable;
import com.carrotsearch.hppc.LongArrayList;

/**
 * THIS IS ADD-ONLY MAP. REMOVALS AND SUBSEQUENT ADDITIONS TO THE SAME KEY ARE NOT SUPPORTED! 
 * 
 *
 */
public class IntToByteArrayMap {

	protected IntIntOpenHashMapSerialable map = new IntIntOpenHashMapSerialable();
	protected ByteArrayList alist = new ByteArrayList();

	public IntToByteArrayMap() {

	};

	/**
	 * Does not support values array length of more than 256.
	 * If array length is greater then 256, only first 256 values are stored.
	 * 
	 * @param key
	 * @param values
	 */
	public void add(int key, byte[] values) {
		if (values.length == 0) {
			return;
		}
		
		if (map.containsKey(key)) {
			//subsequent additions not supported. Returning to prevent OOM 
			return;
		}
		
		int length = values.length;
		if (length > 256) {
			System.out.println("Cutting array of " + length + " length");
			//cutting long arrays
			length = 256;
		}
		
		byte byteLength = (byte)(length - 129);/*we have length from 1 to 256 inclusive here*/
		
		map.put(key, alist.size());
		alist.add(byteLength);
		for (int a = 0; a < length; a++) {
			alist.add(values[a]);
		}
	}

	public byte[] get(int key) {
		if (!map.containsKey(key)) {
			return new byte[0];
		}
		int i = map.get(key);
		
		byte byteLength = alist.get(i);
		int intLength = ((int)byteLength) + 129;
		
		byte[] res = new byte[intLength];
		for (int a = 0; a < intLength; a++) {
			res[a] = alist.get(a + i + 1);
		}
		return res;
	}
	
	
	public void store(DataOutputStream str) throws IOException {
		CompactLongVector.writeMap(str, map);
		CompactLongVector.writeByteArray(str, alist.toArray());
	}

	public void read(DataInputStream str) throws IOException {
		map = CompactLongVector.readMap(str);
		byte[] arr = CompactLongVector.readByteArrray(str);
		alist.buffer = arr;
		alist.elementsCount = arr.length;
	}

	public int[] getKeys() {
		return map.keys;
	}

}
