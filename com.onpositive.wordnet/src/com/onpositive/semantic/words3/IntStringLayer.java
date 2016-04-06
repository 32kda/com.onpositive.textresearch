package com.onpositive.semantic.words3;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.carrotsearch.hppc.IntObjectOpenHashMap;

public class IntStringLayer extends MetaLayer<String>{

	protected IntObjectOpenHashMap<String>data=new IntObjectOpenHashMap<String>();
	
	public IntStringLayer(String id, String caption) {
		super(String.class, id, caption);
	}

	@Override
	public String getValue(int meaningId) {
		return data.get(meaningId);
	}

	@Override
	public void putValue(int meaningId, String value) {
		data.put(meaningId, value);
	}

	@Override
	public void removeValue(int meaningId) {
		data.remove(meaningId);
	}

	@Override
	public boolean hasValue(int id) {
		return data.containsKey(id);
	}

	@Override
	public int[] getAllIds() {
		return data.keys().toArray();
	}

	@Override
	protected void store(DataOutputStream stream) throws IOException {
		stream.writeInt(data.size());
		for (int q: data.keys().toArray()){
			stream.writeInt(q);
			stream.writeUTF(data.get(q));
		}
	}

	@Override
	protected void load(DataInputStream stream) throws IOException {
		int sz=stream.readInt();
		for (int a=0;a<sz;a++){
			data.put(stream.readInt(), stream.readUTF());
		}
	}

}
