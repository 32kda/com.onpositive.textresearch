package com.onpositive.text.analysis;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.carrotsearch.hppc.IntOpenHashSet;
import com.onpositive.text.analysis.IToken.Direction;

public abstract class TokenAttacher {
	
	public static class AttachmentPlace{
		
		public AttachmentPlace(IToken parent, IToken token) {
			super();
			this.parent = parent;
			this.token = token;
		}
		
		private IToken parent;
		
		private IToken token;

		public IToken getParent() {
			return parent;
		}

		public IToken getToken() {
			return token;
		}
		
		@Override
		public int hashCode() {
			return parent.id() + 91 * token.id();
		}
		
		@Override
		public boolean equals(Object obj) {
			if(!(obj instanceof AttachmentPlace)){
				return false;
			}
			AttachmentPlace other = (AttachmentPlace) obj;
			if(other.parent.id() != this.parent.id()){
				return false;
			}			
			if(other.token.id() != this.token.id()){
				return false;
			}
			return true;
		}
		
		@Override
		public String toString() {			
			return token.toString() +" -> " + parent.toString();
		}
	}
	
	public void attachToken(IToken attached, IToken mainWord, int type){
		
		Set<AttachmentPlace> attachmentPlaces = findAttachmentPlaces(mainWord);
		for(AttachmentPlace place : attachmentPlaces){
			attachToken(place, attached, type);
		}		
	}

	private void attachToken(AttachmentPlace place, IToken attached, int type) {
		
		IToken parent = place.getParent();
		Direction dir = null;
		if(parent.getEndPosition()<=attached.getStartPosition()){
			dir = Direction.END;
		}
		else if(parent.getStartPosition() >= attached.getEndPosition()){
			dir = Direction.START;
		}
		else{
			return;
		}
		List<IToken> neighbours = getNeighbours(attached, dir);
		IntOpenHashSet neighboursToRemove = completeIdSet(attached);		
		IToken token = place.getToken();
		IToken newToken = createNewToken(token,attached,type);
		parent.replaceChild(token, newToken);
		
		int startPosition = Math.min(parent.getStartPosition(), newToken.getStartPosition());
		int endPosition = Math.max(parent.getEndPosition(), newToken.getEndPosition());
		
		correctNeighbours(parent,dir,neighbours,neighboursToRemove, startPosition, endPosition);
	}

	private void correctNeighbours(
			IToken parent,
			Direction dir,
			List<IToken> neighbours,
			IntOpenHashSet neighboursToRemove,
			int startPosition,
			int endPosition)
	{
		ArrayList<IToken> nbsToRemove = new ArrayList<IToken>();
		ArrayList<IToken> list = new ArrayList<IToken>();
		list.add(parent);
		for(int i = 0 ; i < list.size() ; i++)
		{
			IToken t = list.get(i);
			List<IToken> parents = t.getParents();
			if(parents != null){
				list.addAll(parents);
			}
			
			t.adjustStartPosition(startPosition);
			t.adjustEndPosition(endPosition);
			
			nbsToRemove.clear();
			IToken nb = t.getNeighbour(dir);
			if(nb!=null&&neighboursToRemove.contains(nb.id())){
				nbsToRemove.add(nb);
			}
			else{
				List<IToken> nbs = t.getNeighbours(dir);
				if(nbs != null){
					for(IToken n : nbs){
						if(neighboursToRemove.contains(n.id())){
							nbsToRemove.add(n);
						}					
					}
				}
			}
			for(IToken n : nbsToRemove){
				t.removeNeighbour(dir, n);
			}
			
			int pos1 = t.getBoundPosition(dir);
			Direction opp = dir.opposite();
			for(IToken n : neighbours){
				int pos2 = n.getBoundPosition(opp);
				if(dir.isBeyondMyBound(pos1, pos2)){
					t.addNeighbour(n, dir);
				}
			}
		}
	}

	protected abstract IToken createNewToken(IToken token, IToken attached, int type);
		

	private IntOpenHashSet completeIdSet(IToken token) {
		
		IntOpenHashSet set = new IntOpenHashSet();
		ArrayList<IToken> list = new ArrayList<IToken>();
		list.add(token);
		for(int i = 0 ; i < list.size() ; i++){
			IToken t = list.get(i);
			set.add(t.id());
			List<IToken> children = t.getChildren();
			if(children !=  null){
				list.addAll(children);
			}
		}
		return set;
	}

	protected List<IToken> getNeighbours(IToken token, Direction dir)
	{
		List<IToken> neighbours = new ArrayList<IToken>();
		IToken nb = token.getNeighbour(dir);
		if(nb != null){
			neighbours.add(nb);
		}
		else{
			List<IToken> nbs = token.getNeighbours(dir);
			if(nbs != null){
				neighbours.addAll(nbs);
			}
		}
		return neighbours;
	}

	protected Set<AttachmentPlace> findAttachmentPlaces(IToken token) {
		Set<AttachmentPlace> set = new HashSet<TokenAttacher.AttachmentPlace>();
		List<IToken> parents = token.getParents();
		for(IToken p : parents){
			set.add(new AttachmentPlace(p, token));
		}
		return set;
	}

}
