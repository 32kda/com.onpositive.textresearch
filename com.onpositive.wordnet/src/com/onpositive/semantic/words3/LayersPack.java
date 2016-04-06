package com.onpositive.semantic.words3;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;

import com.onpositive.semantic.wordnet.AbstractWordNet;

public class LayersPack {

	protected HashMap<String, MetaLayer<?>>laeyrs=new HashMap<String, MetaLayer<?>>();
	private AbstractWordNet net;
	
	public LayersPack(AbstractWordNet net){
		this.net=net;
	}
	
	public void store(DataOutputStream stream) throws IOException{
		stream.writeInt(laeyrs.size());
		for(MetaLayer<?>l:laeyrs.values()){
			stream.writeUTF(l.id);
			stream.writeUTF(l.caption);
			stream.writeInt(code(l));
			l.store(stream);
		}
	}

	private int code(MetaLayer<?> l) {
		if (l instanceof IntBooleanLayer){
			return 1;
		}
		if (l instanceof IntIntLayer){
			return 2;
		}
		if (l instanceof IntDoubleLayer){
			return 3;
		}
		if (l instanceof IntByteLayer){
			return 4;
		}
		if (l instanceof IntStringLayer){
			return 5;
		}
		if (l instanceof IntMeaningLayer){
			return 6;
		}
		return 0;
	}

	public void load(DataInputStream stream)throws IOException{
		laeyrs.clear();
		int sz=stream.readInt();
		for (int a=0;a<sz;a++){
			String id=stream.readUTF();
			String caption=stream.readUTF();
			int code=stream.readInt();
			MetaLayer<?>l=null;
			switch (code) {
			case 1:
				l=new IntBooleanLayer(id, caption);
				break;
			case 2:
				l=new IntIntLayer(id, caption);
				break;
			case 3:
				l=new IntDoubleLayer(id, caption);
				break;
			case 4:
				l=new IntByteLayer(id, caption);
				break;
			case 5:
				l=new IntStringLayer(id, caption);
				break;	
			case 6:
				l=new IntMeaningLayer(id, caption, net);
				break;
			default:
				throw new IllegalArgumentException("unknown layer kind");
			}
			l.load(stream);
			laeyrs.put(id, l);
		}
	}

	public void removeLayer(String layerId) {
		laeyrs.remove(layerId);
	}
	
	@SuppressWarnings("unchecked")
	public<T> MetaLayer<T>getLayer(String id){
		MetaLayer<?> metaLayer = laeyrs.get(id);
		return (MetaLayer<T>) metaLayer;				
	}
	
	public void registerLayer(MetaLayer<?>layer){
		laeyrs.put(layer.id, layer);
	}

	public Collection<MetaLayer<?>> getAll() {
		return laeyrs.values();
	}
}
