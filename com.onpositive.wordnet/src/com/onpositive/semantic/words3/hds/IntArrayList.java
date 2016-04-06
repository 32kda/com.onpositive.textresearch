package com.onpositive.semantic.words3.hds;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;

public final class IntArrayList extends SimpleIntBufferBasedStructure{
	
	public IntArrayList(){
		super(100);
	}
	
	public IntArrayList(int capacity){
		super(capacity);		
	}
	
	public IntArrayList(ByteBuffer buffer2, int offset) {
		super(buffer2,offset);		
	}


	public IntArrayList(File f) throws FileNotFoundException, IOException {
		super(f);
	}

	public final int get(int position){
		return buffer.get(position);
	}
	public void put(int position,int value){
		buffer.put(position, value);
	}
	
	public int size(){
		return size;
	}
	
	public void add(int value){
		if (size<capacity){
			buffer.put(size,value);
			size++;
		}
		else{
			IntArrayList intArrayList = new IntArrayList(capacity*3/2);
			for (int a=0;a<size;a++){
				intArrayList.add(get(a));
			}
			this.buffer=intArrayList.buffer;
			this.original=intArrayList.original;
			this.capacity=intArrayList.capacity;
			buffer.put(size,value);
			size++;
		}
	}

	public int[] toArray() {
		int[]result=new int[size];
		for (int a=0;a<size;a++){
			result[a]=get(a);
		}
		return result;
	}

	@Override
	protected int scale() {
		return 4;
	}
}
