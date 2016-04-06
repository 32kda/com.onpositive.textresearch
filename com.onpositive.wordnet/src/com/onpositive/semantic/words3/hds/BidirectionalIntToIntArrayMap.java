package com.onpositive.semantic.words3.hds;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;


public final class BidirectionalIntToIntArrayMap extends TwoItemCombination<IntToIntArrayMap, IntToIntArrayMap>{

	
	public void add(int key,int[] values){
		first.add(key, values);
	}
	
	public int[] getDirect(int key){
		return first.get(key);
	}
	public int[] getInverse(int key){
		if (second==null){
			throw new IllegalStateException("map is not initialized");
		}
		return second.get(key);
	}
	public BidirectionalIntToIntArrayMap(){
		super(IntToIntArrayMap.class,IntToIntArrayMap.class);
		second=null;
	}
	
	public BidirectionalIntToIntArrayMap(ByteBuffer buffer,int offset){
		super(IntToIntArrayMap.class,IntToIntArrayMap.class,buffer,offset);
	}
	
	public BidirectionalIntToIntArrayMap(File f) throws IOException {
		super(IntToIntArrayMap.class,IntToIntArrayMap.class,f);		
	}
	
	public void init(){
		long l0=System.currentTimeMillis();
		second=new IntToIntArrayMap();
		HashMap<Integer,IntArrayList>mm=new HashMap<Integer,IntArrayList>();
		int[] keys = first.getKeys();
		for (int k:keys){
			if (k==0){
				continue;
			}	
			if  (k==Integer.MIN_VALUE){
				k=0;
			}
			int[] is = first.get(k);
			for (int i:is){
				IntArrayList arrayList = mm.get(i);
				if (arrayList==null){
					arrayList=new IntArrayList(10);
					mm.put(i, arrayList);
				}
				arrayList.add(k);
			}
		}
		for (Integer z:mm.keySet()){
			if (z==1){
				System.out.println("A");
			}
			IntArrayList arrayList = mm.get(z);
			second.add(z, arrayList.toArray());
		}
		long l1=System.currentTimeMillis();
		System.out.println(l1-l0);
	}
}
