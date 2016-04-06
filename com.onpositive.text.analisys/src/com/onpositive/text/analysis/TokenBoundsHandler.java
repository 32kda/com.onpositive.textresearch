package com.onpositive.text.analysis;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.carrotsearch.hppc.IntObjectOpenHashMap;
import com.onpositive.text.analysis.IToken.Direction;

public class TokenBoundsHandler {
	
	protected IntObjectOpenHashMap<IToken> resultTokens = new IntObjectOpenHashMap<IToken>();
	
	protected IntObjectOpenHashMap<IToken> newTokens = new IntObjectOpenHashMap<IToken>();
	
	public void setResultTokens(IntObjectOpenHashMap<IToken> resultTokens) {
		this.resultTokens = resultTokens;
	}

	public void setNewTokens(IntObjectOpenHashMap<IToken> newTokens) {
		this.newTokens = newTokens;
	}

	public void handleBounds(List<IToken> tokens, boolean append){
		List<IToken> nextTokens = append ? addBounds(tokens) : new ArrayList<IToken>();
		
		for(IToken t : tokens){
			boolean isNew = newTokens.containsKey(t.id());
			processToken(t, isNew);
		}
		for(IToken t : nextTokens){
			boolean isNew = newTokens.containsKey(t.id());
			processToken(t, isNew);
		}
	}

	public void handleConflicts(List<IToken> tokens) {
		for (IToken token : tokens) processConflict(token);
	}
	
	protected List<IToken> addBounds(List<IToken> tokens) {
		
		int startPosition = Integer.MAX_VALUE;
		int endPosition = Integer.MIN_VALUE;
		for(IToken t : tokens){
			startPosition = Math.min(startPosition, t.getStartPosition());
			endPosition = Math.max(endPosition, t.getEndPosition());
		}
		
		HashSet<IToken> nextTokens = new HashSet<IToken>();
		for(IToken t : tokens){
			
			if(t.getStartPosition()==startPosition){
				IToken t0 = resultTokens.containsKey(t.id()) ? t.getFirstChild(Direction.START) : t;
				if(t0!=null){
					IToken prev = t0.getPrevious();
					if(prev!=null){
						resultTokens.put(prev.id(), prev);
					}
					else{
						List<IToken> prevTokens = t0.getPreviousTokens();
						if(prevTokens!=null){
							for(IToken n : prevTokens){
								resultTokens.put(n.id(), n);
							}
						}
					}
				}
			}
			if(t.getEndPosition()==endPosition){
				IToken t0 = resultTokens.containsKey(t.id()) ? t.getFirstChild(Direction.END) : t;
				if(t0!=null){
					IToken next = t0.getNext();
					if(next!=null){
						resultTokens.put(next.id(), next);
						nextTokens.add(next);
					}
					else{
						List<IToken> nts = t0.getNextTokens();			
						if(nts!=null){
							nextTokens.addAll(nts);
							for(IToken n : nts){
								resultTokens.put(n.id(), n);
							}
						}
					}
				}
			}
		}
		ArrayList<IToken> result = new ArrayList<IToken>(nextTokens);
		return result;
	}
	
	private void processConflict(IToken token) {
		if (token.childrenCount() == 0) return;
		for (IToken ch : token.getChildren()) {
			if (ch.getParents() == null) continue;
			for (IToken p : ch.getParents())
				if (p != token) token.addConflict(p);
		}
	}
	
	private void processToken(IToken token, boolean isNew) {
		
		if(isNew){
			List<IToken> children = token.getChildren();
			if(children!=null){
				for(IToken ch : children) {
					ch.addParent(token);
				}
			}
		}
		
		IToken boundToken = null;
		if(isNew){
			boundToken = token.getFirstChild(Direction.START);
		}
		else{
			boundToken = token;
		}
		
		if(boundToken==null){
			return;
		}

		IToken neighbour = boundToken.getNeighbour(Direction.START);
		if(neighbour!=null){
			processNeighbour(neighbour,boundToken,token);
		}
		else{
			List<IToken> neighbours = boundToken.getNeighbours(Direction.START);
			if(neighbours!=null){
				neighbours = new ArrayList<IToken>(neighbours);
				for(IToken n : neighbours){
					processNeighbour(n,boundToken,token);
				}
			}
		}
	}

	private void processNeighbour(IToken neighbour, IToken boundToken,IToken token) {
		
		if(resultTokens.containsKey(neighbour.id())){
			neighbour.addNeighbour(token, Direction.END);
			token.addNeighbour(neighbour, Direction.START);
		}
		List<IToken> parents = neighbour.getParents();
		if(parents==null){
			return;
		}
		for(IToken parent : parents){
			if(!resultTokens.containsKey(parent.id())){
				continue;
			}
			IToken lastChildOfParent = parent.getChild(0, Direction.END);
			if(lastChildOfParent!=neighbour){
				continue;
			}
			parent.addNeighbour(token, Direction.END);
			token.addNeighbour(parent, Direction.START);
		}
	}
	
	public static void discardTokens(List<IToken> toDiscard) {
		for(IToken t : toDiscard){
			discardToken(t);
		}
	}

	public static void discardToken(IToken t) {
		discardNeighbours(t, Direction.START);
		discardNeighbours(t, Direction.END);
	}


	protected static void discardNeighbours(IToken token, Direction dir) {
		IToken neighbour = token.getNeighbour(dir);
		if(neighbour!=null){
			neighbour.removeNeighbour(dir.opposite(), token);
		}
		
		List<IToken> neighbours = token.getNeighbours(dir);
		if(neighbours!=null){
			for(IToken n : neighbours){
				n.removeNeighbour(dir.opposite(), token);
			}
		}
	}

	public void handleBounds(IToken predicateToken) {
	
		ArrayList<IToken> list = new ArrayList<IToken>();
		list.add(predicateToken);
		
		IntObjectOpenHashMap<IToken> newMap = new IntObjectOpenHashMap<IToken>();
		newMap.put(predicateToken.id(), predicateToken);
		
		IntObjectOpenHashMap<IToken> resultMap = new IntObjectOpenHashMap<IToken>();
		resultMap.put(predicateToken.id(), predicateToken);

		setResultTokens(resultMap);
		setNewTokens(newMap);
		handleBounds(list,true);
	}



}
