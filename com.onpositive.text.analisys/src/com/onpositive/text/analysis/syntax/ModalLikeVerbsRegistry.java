package com.onpositive.text.analysis.syntax;

import java.util.HashSet;

import com.onpositive.semantic.wordnet.AbstractWordNet;
import com.onpositive.semantic.wordnet.MeaningElement;
import com.onpositive.semantic.wordnet.MorphologicalRelation;
import com.onpositive.semantic.wordnet.TextElement;
import com.onpositive.semantic.words3.MetaLayer;

public class ModalLikeVerbsRegistry {
	
	public ModalLikeVerbsRegistry(AbstractWordNet wordNet) {
		this.wordNet = wordNet;
	}
	
	private AbstractWordNet wordNet;
	
	private HashSet<String> set = null;
	
	public boolean isModalLike(String str){
		
		if(set==null){
			init();
		}
		return set.contains(str);		
	}

	private void init() {
		set = new HashSet<String>();
		
		MetaLayer<Object> layer = wordNet.getMetaLayers().getLayer("modalLikeVerbs");
		if(layer==null){
			return;
		}
		int[] allIds = layer.getAllIds();
		for(int id : allIds){
			MeaningElement me = wordNet.getConceptInfo(id);			
			TextElement te = me.getParentTextElement();
			MeaningElement[] concepts = wordNet.getPossibleGrammarForms(te.getBasicForm())[0].getWord().getConcepts();
			for (MeaningElement c : concepts) {
				for (MorphologicalRelation r: c.getMorphologicalRelations()) {
					set.add(r.getWord().getParentTextElement().getBasicForm());					
				}
			}
			set.add(me.getParentTextElement().getBasicForm());
		}		
	}

}
