package com.onpositive.semantic.words3.hds;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;


/**
 * THIS IS ADD-ONLY MAP. REMOVALS AND SUBSEQUENT ADDITIONS TO THE SAME KEY ARE NOT SUPPORTED! 
 * only positive values may be stored;
 *
 */
public final class IntToIntArrayMap extends MapListCombination{

	public IntToIntArrayMap() {

	};
	
	public IntToIntArrayMap(ByteBuffer buffer,int offset){
		super(buffer,offset);
	}

	public IntToIntArrayMap(File f) throws IOException {
		super(f);
	}

	public void add(int key, int[] values) {
		if (values.length == 0) {
			return;
		}
		if (first.containsKey(key)) {
			//subsequent additions not supported. Returning to prevent OOM 
			return;
		}		
		if (values.length == 1) {
			first.put(key, -values[0] - 1);
			return;
		}
		first.put(key, second.size());
		second.add(values.length);
		for (int a = 0; a < values.length; a++) {
			second.add(values[a]);
		}
	}

	public int[] get(int key) {
		if (!first.containsKey(key)) {
			return new int[0];
		}
		int i = first.get(key);
		if (i < 0) {
			return new int[] { -(i + 1) };
		}
		int j = second.get(i);
		int[] res = new int[j];
		for (int a = 0; a < j; a++) {
			res[a] = second.get(a + i + 1);
		}
		return res;
	}

	public int[] getKeys() {
		return first.keys();
	}
}