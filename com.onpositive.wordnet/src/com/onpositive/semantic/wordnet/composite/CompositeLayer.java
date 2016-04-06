package com.onpositive.semantic.wordnet.composite;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.carrotsearch.hppc.IntOpenHashSet;
import com.onpositive.semantic.wordnet.MeaningElement;
import com.onpositive.semantic.words2.AbstractRelationTarget;
import com.onpositive.semantic.words3.MetaLayer;

public class CompositeLayer<T> extends MetaLayer<T>{

	MetaLayer<T>first;
	public CompositeLayer(Class<T> type, String id, String caption,
			MetaLayer<T> first, MetaLayer<T> second) {
		super(type, id, caption);
		this.first = first;
		this.second = second;
	}

	MetaLayer<T>second;
	private CompositeWordnet qq;
	
	public CompositeLayer(MetaLayer<T>first,MetaLayer<T>second,CompositeWordnet qq) {
		super(first!=null?first.getType():second.getType(), first!=null?first.getId():second.getId(),
				first!=null?first.getCaption():second.getCaption());
		this.first=first;
		this.qq=qq;
		this.second=second;
	}

	@Override
	public T getValue(int meaningId) {
		CompositeMeaning conceptInfo = (CompositeMeaning) qq.getConceptInfo(meaningId);
		if(conceptInfo==null){
			return null;
		}
		if(conceptInfo.original instanceof AbstractRelationTarget){
			return second!=null?second.getValue(conceptInfo.original.id()):null;
		}
		T value = first!=null?first.getValue(conceptInfo.original.id()):null;
		return value;
		
	}

	@Override
	public void putValue(int meaningId, T value) {
		if(first!=null){
			first.putValue(meaningId, value);
		}
		if(second!=null){
			second.putValue(meaningId, value);
		}
	}

	@Override
	public void removeValue(int meaningId) {
		if(first!=null){
			first.removeValue(meaningId);
		}
		if(second!=null){
			second.removeValue(meaningId);
		}
	}

	@Override
	public boolean hasValue(int id) {
		CompositeMeaning conceptInfo = (CompositeMeaning) qq.getConceptInfo(id);
		if(conceptInfo==null){
			return false;
		}
		if(conceptInfo.original instanceof AbstractRelationTarget){
			return second!=null && second.hasValue(id);
		}
		return first != null && first.hasValue(id);
//		MeaningElement meaningElement = qq.idTomeaning.get(id);
//		return (first!=null&&first.hasValue(id))||(second!=null&&second.hasValue(id));
	}

	@Override
	public int[] getAllIds() {
		IntOpenHashSet ss=new IntOpenHashSet();
		if(first!=null){
			int[] allIds = first.getAllIds();
			for(int id : allIds){
				MeaningElement me = qq.original.getConceptInfo(id);
				int newId = qq.convertMeaning(me).id();
				ss.add(newId);
			}
		}
		if(second!=null){
			int[] allIds = second.getAllIds();
			for(int id : allIds){
				MeaningElement me = qq.additions.getConceptInfo(id);
				int newId = qq.convertMeaning(me).id();
				ss.add(newId);
			}
		}
		return ss.toArray();
	}

	@Override
	protected void store(DataOutputStream stream) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	protected void load(DataInputStream stream) throws IOException {
		throw new UnsupportedOperationException();
	}

}
