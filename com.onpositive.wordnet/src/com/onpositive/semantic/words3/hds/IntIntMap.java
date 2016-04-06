package com.onpositive.semantic.words3.hds;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;



/**
 * Extremly simple int to int map;
 * it is append only, can not store Integer.MIN_VALUE key,
 * internally it is open hash map placed on direct byte buffer;
 * @author kor
 * 
 */
public final class IntIntMap extends SimpleIntBufferBasedStructure {

	public IntIntMap(int capacity){
		super(capacity);		
	}
	public IntIntMap(){
		super(100);		
	}
	
	public IntIntMap(ByteBuffer buffer2, int offset) {
		super(buffer2,offset);		
	}
	
	public IntIntMap(File f) throws FileNotFoundException, IOException {
		super(f);
	}

	public boolean containsKey(int key){
		if (key == 0) {
			key = Integer.MIN_VALUE;
		}
		int pos = (MurmurHash3.hash(key) % capacity) << 1;
		if (pos<0){
			pos=-pos;
		}
		do {
			int value = buffer.get(pos);
			if (value == 0) {
				return false;
			}
			if (value == key) {
				return true;
			}
			pos += 2;
			if (pos>=capacity<<1){
				pos=0;
			}
		} while (true);
	}
	
	public int get(int key) {
		if (key == 0) {
			key = Integer.MIN_VALUE;
		}
		int capacity = this.capacity;
		int pos = (MurmurHash3.hash(key) % capacity) << 1;
		if (pos<0){
			pos=-pos;
		}
		IntBuffer buffer2 = buffer;
		do {
			int value = buffer2.get(pos);
			if (value == 0) {
				return Integer.MIN_VALUE;
			}
			if (value == key) {
				return buffer2.get(pos + 1);
			}
			pos += 2;
			if (pos>=capacity<<1){
				pos=0;
			}
		} while (true);
	}

	public int size() {
		return size;
	}
	
	public void put(int key,int val){
		if (key == 0) {
			key = Integer.MIN_VALUE;
		}
		int capacity = this.capacity;
		int pos = (MurmurHash3.hash(key) % capacity) << 1;
		if (pos<0){
			pos=-pos;
		}
		do {
			int value = buffer.get(pos);
			if (value == 0) {
				buffer.put(pos ,key);
				buffer.put(pos + 1,val);
				size++;
				if (size*4>capacity*3){
					//we need rehash now
					rehash();
					
				}
				return;
				//rehash if needed
			}
			if (value == key) {
				buffer.put(pos + 1,val);
				return;
			}
			pos += 2;
			if (pos>=capacity<<1){
				pos=0;
			}
		} while (true);
	}
	
	private void rehash() {
		IntIntMap newMap=new IntIntMap(capacity*3/2);
		for (int a=0;a<capacity<<1;a+=2){
			int i = buffer.get(a);
			if (i!=0){
				newMap.put(i, buffer.get(a+1));
			}
		}
		this.buffer=newMap.buffer;
		this.original=newMap.original;
		this.capacity=newMap.capacity;
	}

	public int[] keys() {
		int[]result=new int[size];
		int pos=0;
		for (int a=0;a<capacity<<1;a+=2){
			int i = buffer.get(a);
			if (i!=0){
				result[pos]=i;
				pos++;
			}
		}
		return result;
	}

	@Override
	protected int scale() {
		return 8;
	}

	
}