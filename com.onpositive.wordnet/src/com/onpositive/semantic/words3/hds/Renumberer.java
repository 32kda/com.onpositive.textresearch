package com.onpositive.semantic.words3.hds;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

public final class Renumberer extends MapListCombination{

	public Renumberer() {
		super();
		second.add(0);
	}
	public Renumberer(boolean allowZeroId) {
		super();
		if(!allowZeroId){
			second.add(0);
		}
	}

	public Renumberer(ByteBuffer buffer, int offset) {
		super(buffer, offset);
	}

	public Renumberer(File f) throws IOException {
		super(f);
	}
	
	public int add(int number){
		int size = second.size;
//		if (number==75759){
//			System.out.println("a");
//		}
		if (first.containsKey(number)){
			throw new IllegalStateException();
		}
		first.put(number, size);
		second.add(number);
		return size;		
	}

	public int getOrifinalNumber(int index){
		return second.get(index);
	}
	
	public int getIndex(int number){
		return first.get(number);
	}
	public int size() {
		return second.size;
	}
	public int[] getIds() {
		int[] mm=new int[second.size-1];
		for (int a=0;a<mm.length;a++){
			mm[a]=a+1;
		}
		return mm;
	}
}