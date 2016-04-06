package com.onpositive.semantic.wikipedia2.fulltext;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import com.carrotsearch.hppc.IntByteOpenHashMap;
import com.carrotsearch.hppc.IntLongOpenHashMap;
import com.carrotsearch.hppc.IntObjectOpenHashMapSerialzable;
import com.carrotsearch.hppc.IntOpenHashSetSerializable;
import com.onpositive.semantic.wikipedia2.WikiEngine2;
import com.onpositive.semantic.wikipedia2.fulltext.WordIndex.WordIndexProvider;

public class TextSearchIndex {

	protected IntLongOpenHashMap headers = new IntLongOpenHashMap();

	protected RandomAccessFile file;

	private WordIndex wordIndex;
	
	public TextSearchIndex(WikiEngine2 engine){
		wordIndex = new WordIndex(engine);
		String location = engine.getLocation();
		File ti = new File(location,"textCat.dat");
		if (!ti.exists()){
			try {
				build(engine);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			DataInputStream si=new DataInputStream(new BufferedInputStream(new FileInputStream(ti)));
			int readInt = si.readInt();
			for (int a=0;a<readInt;a++){
				headers.put(si.readInt(), si.readLong());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			file=new RandomAccessFile(new File(location,"textIndex.dat"), "r");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public final int[] search(String word){
		String stem = StemProvider.getInstance().stem(word);
		Integer wordIndex3 = wordIndex.getWordIndex(stem);
		if(wordIndex3==null){
			return new int[0];
		}
		Integer wordIndex2 = wordIndex3+1;
		if (wordIndex2!=null){
			return getDocuments(wordIndex2);
		}
		return new int[0];
	}

	public final int[] getDocuments(int windex) {
		if (headers.containsKey(windex)) {
			long i = headers.get(windex);
			if (i > 0) {
				try {
					file.seek(i);
					int readInt = file.readInt();
					byte[] b = new byte[readInt*4];
					file.readFully(b);
					IntBuffer asIntBuffer = ByteBuffer.wrap(b).asIntBuffer();
					int[] dta=new int[readInt];
					for (int a=0;a<readInt;a++){
						dta[a]=asIntBuffer.get(a);
						a++;
					}
					return dta;
				} catch (IOException e) {
					throw new IllegalStateException(e);
				}
			}
		}
		return new int[0];
	}
	
	public void build(WikiEngine2 engine) throws FileNotFoundException,IOException{
		String location = engine.getLocation();
		File ti=new File(location,"textIndex.dat");
		WordIndex wordIndex = WordIndexProvider.getInstance().get(engine);
		IntObjectOpenHashMapSerialzable<IntOpenHashSetSerializable>rs=new IntObjectOpenHashMapSerialzable<IntOpenHashSetSerializable>();
		int ppos=0;
		try{
		for (int doc:engine.getDocumentIDs()){
			if (ppos%1000==0){
				System.out.println(ppos);
			}
			ppos++;
			IntByteOpenHashMap documentHistogram = wordIndex.getDocumentHistogram(doc);
			if (documentHistogram==null){
				continue;
			}
			for (int word:documentHistogram.keys().toArray()){
				IntOpenHashSetSerializable set=null;
				if (!rs.containsKey(word)){
					set=new IntOpenHashSetSerializable();
					rs.put(word, set);
				}
				else
				{
					set=rs.get(word);
				}
				set.add(doc);
			}
		}
		}catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);			
		}
		IntLongOpenHashMap locations=new IntLongOpenHashMap();
		BufferedOutputStream bs=new BufferedOutputStream(new FileOutputStream(ti));
		DataOutputStream ds=new DataOutputStream(bs) ;
		long position=0;
		for (int c:rs.keys().toArray()){
			locations.put(c, position);
			IntOpenHashSetSerializable intOpenHashSet = rs.get(c);
			int size = intOpenHashSet.size();
			ds.writeInt(size);
			int[] array = intOpenHashSet.toArray();
			for (int a=0;a<array.length;a++){
				ds.writeInt(array[a]);
			}
			position+=(array.length+1)*4;
		}
		ds.close();
		ti=new File(location,"textCat.dat");
		bs=new BufferedOutputStream(new FileOutputStream(ti));
		ds=new DataOutputStream(bs) ;
		ds.writeInt(locations.size());
		for (int a:locations.keys().toArray()){
			ds.writeInt(a);
			ds.writeLong(locations.get(a));
		}
		ds.close();
	}
}
