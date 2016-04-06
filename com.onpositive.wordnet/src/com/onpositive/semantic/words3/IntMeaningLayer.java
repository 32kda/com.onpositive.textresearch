package com.onpositive.semantic.words3;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.carrotsearch.hppc.IntIntOpenHashMapSerialable;
import com.onpositive.semantic.wordnet.AbstractWordNet;
import com.onpositive.semantic.wordnet.MeaningElement;

public class IntMeaningLayer extends MetaLayer<MeaningElement>{

	protected IntIntOpenHashMapSerialable map=new IntIntOpenHashMapSerialable();
	private AbstractWordNet net;
	
	public IntMeaningLayer(String id, String caption,AbstractWordNet net) {
		super(MeaningElement.class, id, caption);
		this.net=net;
	}

	@Override
	public MeaningElement getValue(int meaningId) {
		if(!map.containsKey(meaningId)){
			return null;
		}
		int d = map.get(meaningId);
		return net.getConceptInfo(d);
	}

	@Override
	public boolean hasValue(int id) {
		return map.containsKey(id);
	}

	@Override
	public int[] getAllIds() {
		return map.keys().toArray();
	}

	@Override
	protected void store(DataOutputStream stream) throws IOException {
		stream.writeInt(map.size());
		for(int k=0;k<map.allocated.length;k++){
			if(map.allocated[k]){
				stream.writeInt(map.keys[k]);
				stream.writeInt(map.values[k]);
			}
		}
	}

	@Override
	protected void load(DataInputStream stream) throws IOException {
		int sz=stream.readInt();
		map=new IntIntOpenHashMapSerialable(sz*3/2);
		for(int a=0;a<sz;a++){
			map.put(stream.readInt(), stream.readInt());
		}
	}

	@Override
	public void putValue(int meaningId, MeaningElement value) {
		map.put(meaningId, value.id());
	}

	@Override
	public void removeValue(int meaningId) {
		map.remove(meaningId);
	}
	
	public void recode(IntIntOpenHashMapSerialable idrecoder) {
		super.recode(idrecoder);
		for (int q:getAllIds()){
			int i = map.get(q);
			if (idrecoder.containsKey(i)){
				idrecoder.put(q, idrecoder.get(i));
			}
		}
	}
}