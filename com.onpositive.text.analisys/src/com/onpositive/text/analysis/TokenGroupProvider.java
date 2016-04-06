package com.onpositive.text.analysis;

import java.util.Iterator;
import java.util.List;

public abstract class TokenGroupProvider implements Iterable<TokenGroup> {
	
	public TokenGroupProvider(List<IToken> tokens) {
		super();
		this.tokens = tokens;
	}

	protected List<IToken> tokens;

	@Override
	public Iterator<TokenGroup> iterator() {
		
		return new TokenGroupIterator();
	}
	
	private class TokenGroupIterator implements Iterator<TokenGroup>{

		
		private TokenGroup tg;
		
		@Override
		public boolean hasNext() {
			if(tg==null){
				return true;
			}
			return tg.getLastPosition()<tokens.size();
		}

		@Override
		public TokenGroup next() {
			
			if(!hasNext()){
				return null;
			}
			int start;
			if(tg == null){
				tg = new TokenGroup(tokens);
				start = 0;
			}else{
				start = tg.getLastPosition()+1;
			}
			int last = searchNextBound(start);
			tg.reset(start, last);			
			return tg;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();			
		}
		
	}
	
	protected abstract int searchNextBound(int pos);

}
