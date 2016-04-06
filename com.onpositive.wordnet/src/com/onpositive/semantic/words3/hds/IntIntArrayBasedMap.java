package com.onpositive.semantic.words3.hds;


/**
 * Extremly simple int to int map;
 * it is append only, can not store Integer.MIN_VALUE key,
 * internally it is open hash map placed on direct byte buffer;
 * @author kor
 * 
 */
public final class IntIntArrayBasedMap  {

	int[]buffer;
	private int capacity;
	private int size;
	
	public IntIntArrayBasedMap(int capacity){
		this.capacity=capacity;
		buffer= new int[capacity*2];//
		
	}
	public IntIntArrayBasedMap(){
		this(100);		
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
			int value = buffer[pos];
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
		int[] buffer2 = buffer;
		do {
			int value = buffer2[pos];
			if (value == 0) {
				return Integer.MIN_VALUE;
			}
			if (value == key) {
				return buffer2[pos + 1];
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
			int value = buffer[pos];
			if (value == 0) {
				buffer[pos]=key;
				buffer[pos + 1]=val;
				size++;
				if (size*4>capacity*3){
					//we need rehash now
					rehash();
					
				}
				return;
				//rehash if needed
			}
			if (value == key) {
				buffer[pos +1 ]=val;
				return;
			}
			pos += 2;
			if (pos>=capacity<<1){
				pos=0;
			}
		} while (true);
	}
	
	

	private void rehash() {
		IntIntArrayBasedMap newMap=new IntIntArrayBasedMap(capacity*3/2);
		for (int a=0;a<capacity<<1;a+=2){
			int i = buffer[a];
			if (i!=0){
				newMap.put(i, buffer[a+1]);
			}
		}
		this.buffer=newMap.buffer;
		this.capacity=newMap.capacity;
	}

	public int[] keys() {
		int[]result=new int[size];
		int pos=0;
		for (int a=0;a<capacity<<1;a+=2){
			int i = buffer[a];
			if (i!=0){
				result[pos]=i;
				pos++;
			}
		}
		return result;
	}

	
}