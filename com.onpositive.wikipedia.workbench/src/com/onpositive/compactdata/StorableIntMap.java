package com.onpositive.compactdata;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import com.carrotsearch.hppc.IntIntOpenHashMapSerialable;

public class StorableIntMap {

	public IntIntOpenHashMapSerialable map = new IntIntOpenHashMapSerialable();

	public StorableIntMap() {

	}

	public StorableIntMap(File fl) throws IOException {
		DataInputStream di = new DataInputStream(new BufferedInputStream(
				new FileInputStream(fl)));
		map = CompactLongVector.readMap(di);
		di.close();
	}
	
	public void put(int n,int k){
		if (map.containsKey(n)){
			throw new RuntimeException();
		}
		map.put(n, k);
	}

	public void write(File fl) throws IOException {
		DataOutputStream str = new DataOutputStream(new BufferedOutputStream(
				new FileOutputStream(fl)));
		try {
			CompactLongVector.writeMap(str, map);
		} finally {
			str.close();
		}
	}
}
