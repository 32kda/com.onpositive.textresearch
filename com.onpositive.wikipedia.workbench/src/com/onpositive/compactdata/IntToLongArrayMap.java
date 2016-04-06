package com.onpositive.compactdata;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.carrotsearch.hppc.IntIntOpenHashMapSerialable;
import com.carrotsearch.hppc.IntIntOpenHashMapSerialable.KeysContainer;
import com.carrotsearch.hppc.LongArrayList;

/**
 * THIS IS ADD-ONLY MAP. REMOVALS AND SUBSEQUENT ADDITIONS TO THE SAME KEY ARE NOT SUPPORTED! 
 * 
 *
 */
public class IntToLongArrayMap {

	protected IntIntOpenHashMapSerialable map = new IntIntOpenHashMapSerialable();
	protected LongArrayList alist = new LongArrayList();

	public IntToLongArrayMap() {

	};

	public void add(int key, long[] values) {
		if (values.length == 0) {
			return;
		}
		
		if (map.containsKey(key)) {
			//subsequent additions not supported. Returning to prevent OOM 
			return;
		}
		
		map.put(key, alist.size());
		alist.add(values.length);
		for (int a = 0; a < values.length; a++) {
			alist.add(values[a]);
		}
	}

	public long[] get(int key) {
		if (!map.containsKey(key)) {
			return new long[0];
		}
		int i = map.get(key);
		
		int j = (int) alist.get(i);
		long[] res = new long[j];
		for (int a = 0; a < j; a++) {
			res[a] = alist.get(a + i + 1);
		}
		return res;
	}
	
	
	public void store(DataOutputStream str) throws IOException {
		CompactLongVector.writeMap(str, map);
		CompactLongVector.writeLongArray(str, alist.toArray());
	}

	public void read(DataInputStream str) throws IOException {
		map = CompactLongVector.readMap(str);
		long[] arr = CompactLongVector.readLongArrray(str);
		alist.buffer = arr;
		alist.elementsCount = arr.length;
	}

	public int[] getKeys() {
		return map.keys;
	}

	public KeysContainer getKeyContainer() {
		return map.keys();
	}
}
