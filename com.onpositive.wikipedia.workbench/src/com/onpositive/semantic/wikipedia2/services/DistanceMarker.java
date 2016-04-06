package com.onpositive.semantic.wikipedia2.services;

import com.onpositive.semantic.wikipedia2.WikiCat;
import com.onpositive.semantic.wikipedia2.WikiEngine2;
import com.onpositive.semantic.words3.hds.BidirectionalIntToIntArrayMap;
import com.onpositive.semantic.words3.hds.IntArrayList;

public class DistanceMarker extends AbstractIntListIndex{

	public DistanceMarker(WikiEngine2 engine) {
		super(engine);
	}
	

	@Override
	protected void build(WikiEngine2 engine) {
		data = new IntArrayList(engine.getCategoryRenumberer().getIds().length+2);
		int i =((WikiCat) engine.getRootCategory()).getIntId();
		data.put(i, 1);
		iterate(engine.getCategoryToParentCategories(),getKeySet());
	}
	
	
	public int getDistanceFromRoot(int q){
		int i = data.get(q);
		return i-1;
	}

	private void iterate(BidirectionalIntToIntArrayMap map,int[]ids) {
		int count = 1;
		while (count > 0) {
			count = 0;
			for (int z : ids) {
				if (z != 0) {
					if (data.get(z)!=0) {
						int dist = data.get(z);
						int[] is =map.getDirect(z);
						for (int i : is) {
							int j = data.get(i);
							if (j != 0) {
								if (dist + 1 < j) {
									data.put(i, dist + 1);
									count++;
								}
							} else {
								data.put(i, dist + 1);
								count++;
							}
						}
					}
				}
			}
		}
	}
	@Override
	protected int calc(int a) {
		throw new UnsupportedOperationException();
	}
	@Override
	protected int[] getKeySet() {
		return engine.getCategoryRenumberer().getIds();
	}
	@Override
	public String getFileName() {
		return "rootDistance.index";
	}
}
