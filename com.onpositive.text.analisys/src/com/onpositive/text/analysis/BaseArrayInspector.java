package com.onpositive.text.analysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.carrotsearch.hppc.IntOpenHashSet;
import com.onpositive.text.analysis.IToken.Direction;

public abstract class BaseArrayInspector {
	
	public static class ArrayProcessingReesult{
		
		public ArrayProcessingReesult(boolean accept, boolean stop) {
			super();
			this.stop = stop;
			this.accept = accept;
		}
		
		boolean accept;
		
		boolean stop;
		
		public boolean stopped() {
			return stop;
		}

		public boolean accepted() {
			return accept;
		}
	}
	
	public static ArrayProcessingReesult ACCEPT = new ArrayProcessingReesult(true, false);
	
	public static ArrayProcessingReesult BREAK = new ArrayProcessingReesult(false, true);
	
	public static ArrayProcessingReesult ACCEPT_AND_BREAK = new ArrayProcessingReesult(true, true);
	
	public static ArrayProcessingReesult CONTINUE = new ArrayProcessingReesult(false, false);
	
	
	
	public List<IToken> findToken(IToken startPoint, Direction dir, List<IToken> baseArray, IntOpenHashSet baseTokenIDs){
		
		prepare();
		
		ArrayList<IToken> result = new ArrayList<IToken>();		
		IntOpenHashSet set = new IntOpenHashSet();
		List<IToken> list = getStartList(startPoint, dir, baseArray, baseTokenIDs);
		int bound = dir.absolutBound();
		for(int i = 0 ; i < list.size() ; i++ ){
			IToken t = list.get(i);
			int bp = t.getBoundPosition(dir.opposite());
			ArrayProcessingReesult pr = match(t,dir);
			if(pr.accepted()){				
				if(dir.isBeyondMyBound(bp, bound)){
					if(bp!=bound){
						result.clear();
						bound = bp;
					}
					set.add(t.id());
					result.add(t);
				}
			}
			if(pr.stopped()){
				break;
			}
			else{
				if(dir.isBeyondMyBound(bound,bp)){
					continue;					
				}
				IToken n = t.getNeighbour(dir);
				if(n != null){
					int id = n.id();
					if(!set.contains(id)&&baseTokenIDs.contains(id)){
						set.add(id);
						list.add(n);
					}
				}
				else{
					List<IToken> nbs = t.getNeighbours(dir);
					if(nbs != null){
						for(IToken nb : nbs){
							int id = nb.id();
							if(!set.contains(id)&&baseTokenIDs.contains(id)){
								set.add(id);
								list.add(nb);
							}
						}
					}
				}
			}
		}
		return result;		
	}

	protected abstract  void prepare();

	protected abstract ArrayProcessingReesult match(IToken token, Direction dir);

	private List<IToken> getStartList(IToken startPoint, Direction dir,List<IToken> baseArray, IntOpenHashSet baseTokenIDs) {
		if (baseArray==null){
			return Collections.emptyList();
		}
		IntOpenHashSet set = new IntOpenHashSet();
		ArrayList<IToken> result = new ArrayList<IToken>();
		int bp = startPoint.getBoundPosition(dir);
		for(IToken t : baseArray){
			if(t.getBoundPosition(dir)!=bp){
				continue;
			}
				
			IToken n = t.getNeighbour(dir);			
			if(n != null ){
				int id = n.id();			
				if(!set.contains(id)&&baseTokenIDs.contains(id)){
					set.add(id);
					result.add(n);
				}
			}
			else{
				List<IToken> nbs = t.getNeighbours(dir);
				if(nbs != null){
					for(IToken nb : nbs){
						int id = nb.id();
						if(!set.contains(id)&&baseTokenIDs.contains(id)){
							set.add(id);
							result.add(nb);
						}
					}
				}
			}
		}		
		return result;
	}

	public List<IToken> findToken(IToken token, Direction dir, List<IToken> baseTokens) {
		
		IntOpenHashSet baseTokenIDs = new IntOpenHashSet();
		for(IToken t : baseTokens){
			baseTokenIDs.add(t.id());
		}
		return findToken(token, dir, baseTokens, baseTokenIDs);
	}

}
