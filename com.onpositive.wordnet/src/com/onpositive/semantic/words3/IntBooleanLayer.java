package com.onpositive.semantic.words3;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.carrotsearch.hppc.IntByteOpenHashMap;

public class IntBooleanLayer extends MetaLayer<Boolean>{

	protected IntByteOpenHashMap map=new IntByteOpenHashMap();
	
	public IntBooleanLayer(String id, String caption) {
		super(Boolean.class, id, caption);
	}

	@Override
	public Boolean getValue(int meaningId) {
		if(!map.containsKey(meaningId)){
			return null;
		}
		byte d = map.get(meaningId);
		return d!=0;
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
				stream.writeByte(map.values[k]);
			}
		}
	}

	@Override
	protected void load(DataInputStream stream) throws IOException {
		int sz=stream.readInt();
		map=new IntByteOpenHashMap(sz*3/2);
		for(int a=0;a<sz;a++){
			map.put(stream.readInt(), stream.readByte());
		}
	}
	
	@Override
	public void putValue(int meaningId, Boolean value) {
		map.put(meaningId, (byte) (value?1:0));
	}

	@Override
	public void removeValue(int meaningId) {
		map.remove(meaningId);
	}
}