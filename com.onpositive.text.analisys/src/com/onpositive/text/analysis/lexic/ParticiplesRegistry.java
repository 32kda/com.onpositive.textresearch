package com.onpositive.text.analysis.lexic;

import java.util.HashMap;
import java.util.HashSet;

import com.onpositive.semantic.wordnet.AbstractWordNet;
import com.onpositive.semantic.wordnet.MeaningElement;
import com.onpositive.semantic.words3.MetaLayer;

public class ParticiplesRegistry {

	AbstractWordNet wordNet;
	
	public ParticiplesRegistry(AbstractWordNet wordNet) {
		super();
		this.wordNet = wordNet;
		init();
	}
	
	private HashMap<String, MeaningElement> map;
	private HashSet<MeaningElement> verbparset;
	
	public boolean isVerbParticiple(MeaningElement me) {
		return verbparset.contains(me);
	}
	
	public boolean isVerbParticiple(String bForm) {
		MeaningElement me = getParticiple(bForm);
		if (me == null) return false;
		return isVerbParticiple(me);
	}
	
	public MeaningElement getParticiple(String bForm) { 
		if (map.containsKey(bForm)) return map.get(bForm);
		else return null;
	}
	
	private void init() {
		String layerId = "participles";
		map = new HashMap<String, MeaningElement>();
		verbparset = new HashSet<MeaningElement>();
		
		MetaLayer<Object> layer = wordNet.getMetaLayers().getLayer(layerId);
		if (layer==null){
			return;
		}
		int[] ids = layer.getAllIds();
		
		for (int id : ids) {			
			MeaningElement me = wordNet.getConceptInfo(id);
			map.put(me.getParentTextElement().getBasicForm(), me);
			if (layer.getValue(id).equals("vrb")) verbparset.add(me);
		}
		return;
	}	
}
