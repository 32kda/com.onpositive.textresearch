package com.onpositive.text.analysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.carrotsearch.hppc.IntIntOpenHashMap;
import com.carrotsearch.hppc.IntOpenHashSet;

public class BasicCleaner implements ITokenCleaner {
	
	/* (non-Javadoc)
	 * @see com.onpositive.text.analysis.ITokenCleaner#clean(java.util.List)
	 */
	@Override
	public List<IToken> clean(List<IToken> tokens){
		
		final IntIntOpenHashMap depthMap = new IntIntOpenHashMap();
		
		List<IToken> list = new ArrayList<IToken>(tokens);
		while(true){
			int l = list.size();
			List<IToken> list2 = iterate(list, depthMap);
			list=list2;
			if(l==list.size()){
				break;
			}
		}
		return list;
		
	}

	protected List<IToken> iterate(List<IToken> list,
			final IntIntOpenHashMap depthMap) {
		final IntOpenHashSet toRemove = new IntOpenHashSet();
		Collections.sort(list, new Comparator<IToken>() {

			@Override
			public int compare(IToken t1, IToken t2) {
				int sp1 = t1.getStartPosition();
				int ep1 = t1.getEndPosition();
				int sp2 = t2.getStartPosition();				
				int ep2 = t2.getEndPosition();
				if(sp1!=sp2){
					if(sp1<sp2&&ep1>=ep2){
						toRemove.add(t2.id());
					}
					else if(sp2<sp1&&ep2>=ep1){
						toRemove.add(t1.id());
					}
					return sp1-sp2;
				}				
				
				if(ep1!=ep2){
					if(ep2<ep1){
						toRemove.add(t2.id());
					}
					else{
						toRemove.add(t1.id());
					}
					return ep2-ep1;
				}
//				int d1 = computeDepth(t1, depthMap);
//				int d2 = computeDepth(t2, depthMap);
//				if(d1!=d2){
//					if(d2<d1){
//						toRemove.add(t2.id());
//					}
//					else{
//						toRemove.add(t1.id());
//					}
//					return d2-d1;
//				}
				return 0;
			}
		});
		List<IToken> list2 = new ArrayList<IToken>();
		List<IToken> listToRemove = new ArrayList<IToken>();
		for(IToken t : list){
			if(!toRemove.contains(t.id())){
				list2.add(t);
			}
			else{
				listToRemove.add(t);
			}
		}
		TokenBoundsHandler.discardTokens(listToRemove);
		return list2;
	}

	@SuppressWarnings("unused")
	private static int computeDepth(IToken t, IntIntOpenHashMap map){
		int id = t.id();
		if(map.containsKey(id)){
			return map.get(id);
		}
		int d = 0;
		List<IToken> children = t.getChildren();
		if(children!=null){
			for(IToken ch : children){
				d = Math.max(d, computeDepth(ch, map));
			}
		}
		d++;
		map.put(id, d);
		return d;
	}

}
