package com.onpositive.compactdata;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

public class ContainersTest {

	public static void main(String[] args) throws IOException {
		System.out.println("testing int to long");
		
		testIntToLongArrayMap();
		
		System.out.println("testing int to byte");
		
		testIntToByteArrayMap();
		
		System.out.println("done");
	}
	
	private static void testIntToLongArrayMap() throws IOException {
		Random rnd = new Random(System.currentTimeMillis());
		
		Map<Integer, long[]> referenceData = new HashMap<Integer, long[]>();
		IntToLongArrayMap testData = new IntToLongArrayMap();
		
		//generating data
		final int MAX_SAMPLES = 100000;
		
		for (int i = 0; i < MAX_SAMPLES; i++) {
			int key = rnd.nextInt(Integer.MAX_VALUE);
			
			int dataLength = rnd.nextInt(256) + 1;
			long[] data = new long[dataLength];
			for (int j = 0; j < dataLength; j++) {
				data[j] = rnd.nextLong();
			}
			
			referenceData.put(key, data);
			testData.add(key, data);
		}
		
		File file = new File("C:\\temp\\test.data");
		if (file.exists()) {
			file.delete();
		}
		file.createNewFile();
		
		DataOutputStream out = new DataOutputStream(new FileOutputStream(file));
		testData.store(out);
		
		IntToLongArrayMap loadedTestData = new IntToLongArrayMap();
		DataInputStream in = new DataInputStream(new FileInputStream(file));
		loadedTestData.read(in);
		
		//comparing data
		for (Entry<Integer, long[]> entry : referenceData.entrySet()) {
			long[] referenceArray = entry.getValue();
			long[] testArray = loadedTestData.get(entry.getKey());
			
			if (testArray == null || referenceArray.length != testArray.length) {
				throw new RuntimeException("Error found in array size!");
			}
			
			for (int i = 0; i < referenceArray.length; i++) {
				long referenceByte = referenceArray[i];
				long testByte = testArray[i];
				
				if (referenceByte != testByte) {
					throw new RuntimeException("Error found in array data!");
				}
			}
		}
	}

	private static void testIntToByteArrayMap() throws IOException {
		Random rnd = new Random(System.currentTimeMillis());
		
		Map<Integer, byte[]> referenceData = new HashMap<Integer, byte[]>();
		IntToByteArrayMap testData = new IntToByteArrayMap();
		
		//generating data
		final int MAX_SAMPLES = 100000;
		
		for (int i = 0; i < MAX_SAMPLES; i++) {
			int key = rnd.nextInt(Integer.MAX_VALUE);
			
			int dataLength = rnd.nextInt(256) + 1;
			byte[] data = new byte[dataLength];
			rnd.nextBytes(data);
			
			referenceData.put(key, data);
			testData.add(key, data);
		}
		
		File file = new File("C:\\temp\\test.data");
		if (file.exists()) {
			file.delete();
		}
		file.createNewFile();
		
		DataOutputStream out = new DataOutputStream(new FileOutputStream(file));
		testData.store(out);
		
		IntToByteArrayMap loadedTestData = new IntToByteArrayMap();
		DataInputStream in = new DataInputStream(new FileInputStream(file));
		loadedTestData.read(in);
		
		//comparing data
		for (Entry<Integer, byte[]> entry : referenceData.entrySet()) {
			byte[] referenceArray = entry.getValue();
			byte[] testArray = loadedTestData.get(entry.getKey());
			
			if (testArray == null || referenceArray.length != testArray.length) {
				throw new RuntimeException("Error found in array size!");
			}
			
			for (int i = 0; i < referenceArray.length; i++) {
				byte referenceByte = referenceArray[i];
				byte testByte = testArray[i];
				
				if (referenceByte != testByte) {
					throw new RuntimeException("Error found in array data!");
				}
			}
		}
	}
}
