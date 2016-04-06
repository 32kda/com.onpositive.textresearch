package com.onpositive.compactdata;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.IntIntOpenHashMapSerialable;

/**
 * THIS IS ADD-ONLY MAP. REMOVALS AND SUBSEQUENT ADDITIONS TO THE SAME KEY ARE NOT SUPPORTED! 
 * 
 *
 */
public class IntToIntArrayMap {

	protected IntIntOpenHashMapSerialable map = new IntIntOpenHashMapSerialable();
	protected IntArrayList alist = new IntArrayList();

	public IntToIntArrayMap() {

	};

	public void add(int key, int[] values) {
		if (values.length == 0) {
			return;
		}
		
		if (map.containsKey(key)) {
			//subsequent additions not supported. Returning to prevent OOM 
			return;
		}
		
		if (values.length == 1) {
			map.put(key, -values[0] - 1);
			return;
		}
		map.put(key, alist.size());
		alist.add(values.length);
		for (int a = 0; a < values.length; a++) {
			alist.add(values[a]);
		}
	}

	public int[] get(int key) {
		if (!map.containsKey(key)) {
			return new int[0];
		}
		int i = map.get(key);
		if (i < 0) {
			return new int[] { -(i + 1) };
		}
		int j = alist.get(i);
		int[] res = new int[j];
		for (int a = 0; a < j; a++) {
			res[a] = alist.get(a + i + 1);
		}
		return res;
	}
	
	
	public void store(DataOutputStream str) throws IOException {
		CompactLongVector.writeMap(str, map);
		CompactLongVector.writeIntArray(str, alist.toArray());
	}

	public void read(DataInputStream str) throws IOException {
		map = CompactLongVector.readMap(str);
		int[] arr = CompactLongVector.readIntArrray(str);
		alist.buffer = arr;
		alist.elementsCount = arr.length;
	}

	public int[] getKeys() {
		return map.keys;
	}

}