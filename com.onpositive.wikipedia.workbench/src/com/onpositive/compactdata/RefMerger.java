package com.onpositive.compactdata;

import com.carrotsearch.hppc.LongArrayList;

public class RefMerger {

	LongArrayList lq=new LongArrayList();
	
	protected static class HiComparator implements LongComparator{

		@Override
		public int compare(long l1, long l2) {
			return trimHi(l1)-trimHi(l2);			
		}

		private int trimHi(long l1) {
			return (int) (l1&0xFFFFFFFFL);
		}
		
	}
	
	public void addRef(int a,int b){
		long al=a;
		lq.add((al<<32)+b);
		int i = to(lq.size()-1);
		int w = what(lq.size()-1);
		if (i!=b||w!=a){
			throw new RuntimeException();
		}
	}
	
	public void sort(){
		TimLongSort.sort(lq.buffer,0,lq.elementsCount, new HiComparator());		
	}
	public void sortInverse(){
		TimLongSort.sort(lq.buffer,0,lq.elementsCount, null);		
	}
	
	public static void main(String[] args) {
		RefMerger refMerger = new RefMerger();
		for (int a=0;a<100;a++){
			int b = (int) (Math.random()*Integer.MAX_VALUE);
			System.out.println(a+":"+b);
			refMerger.addRef(a, b);
			refMerger.sortInverse();
		}
		for (int a=0;a<100;a++){
			System.out.println(refMerger.what(a)+":"+refMerger.to(a));
		}
	}

	public int to(int a) {
		return (int) lq.get(a);
	}

	public int what(int a) {
		return (int) ((lq.get(a)&0xFFFFFFFF00000000L)>>32);
	}

	public int size() {
		return lq.size();
	}
}
