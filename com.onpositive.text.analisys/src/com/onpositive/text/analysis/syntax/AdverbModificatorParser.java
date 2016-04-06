package com.onpositive.text.analysis.syntax;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import com.onpositive.semantic.wordnet.AbstractWordNet;
import com.onpositive.semantic.wordnet.MeaningElement;
import com.onpositive.semantic.wordnet.Grammem.PartOfSpeech;
import com.onpositive.semantic.words3.MetaLayer;
import com.onpositive.text.analysis.IToken;

public class AdverbModificatorParser extends AbstractSyntaxParser {

	
	public AdverbModificatorParser(AbstractWordNet wordNet) {
		super(wordNet);
		this.metaLookUp = new WordNetMetaAccessor<Boolean>("modificator_adverb", wordNet, Boolean.class);
	}


	private WordNetMetaAccessor<Boolean> metaLookUp;
	
	
	@Override
	protected void combineTokens(Stack<IToken> sample, ProcessingData processingData)
	{
		List<IToken> rawTokens = matchTokens(sample);
		ArrayList<IToken> tokens = new ArrayList<IToken>();
		for(IToken token : rawTokens){
			if(checkParents(token, sample)){
				tokens.add(token);
			}
		}
		if(tokens.size()==1){
			processingData.addReliableTokens(tokens);
		}
		else{
			processingData.addDoubtfulTokens(tokens);
		}
	}

	private List<IToken> matchTokens(List<IToken> sample) {
		
		int tokenType = IToken.TOKEN_TYPE_ADVERB_WITH_MODIFICATOR;
		
		ArrayList<IToken> result = new ArrayList<IToken>();
		int size = sample.size();
		SyntaxToken mainWord=null;
		int seqStart = -1;
		for(int i = 0 ; i < size ; i++){
			SyntaxToken token = (SyntaxToken) sample.get(i);
			String basicForm = token.getBasicForm();
			Boolean value = metaLookUp.getValue(basicForm);
			if(value!=null&&value){
				if(seqStart < 0 ){
					seqStart = i;
				}
			}
			else{
				if(mainWord==null){
					mainWord = token;
				}
				if(seqStart>=0){
					
					int startPosition = Math.min(mainWord.getStartPosition(), sample.get(seqStart).getStartPosition());
					int endPosition = Math.max(mainWord.getEndPosition(), sample.get(i-1).getEndPosition());
					seqStart = -1;
					
					
					SyntaxToken st = new SyntaxToken(tokenType, mainWord, null, startPosition, endPosition);
					result.add(st);
					if(mainWord==token){
						mainWord=null;
					}
					else{
						mainWord=token;
					}
				}
			}
		}
		return result;
	}

	@Override
	protected ProcessingResult checkToken(IToken newToken) {
		
		if(has(PartOfSpeech.ADVB).match(newToken)){
			return CONTINUE_PUSH;
		}		
		return DO_NOT_ACCEPT_AND_BREAK;
	}
	
	
	private static class WordNetMetaAccessor<T>{
		
		public WordNetMetaAccessor(String layerId, AbstractWordNet wordNet, Class<T> clazz) {
			super();
			this.layerId = layerId;
			this.wordNet = wordNet;
			this.clazz = clazz;
		}

		private String layerId;
		
		private AbstractWordNet wordNet;
		
		private Class<T> clazz;
		
		private HashMap<String,T> map = null; 
		
		private T getValue(String str){
			if(str==null){
				return null;
			}
			initMap();
			return map.get(str);
		}

		@SuppressWarnings("unchecked")
		private void initMap() {
			if(map!=null){
				return;
			}
			map = new HashMap<String,T>();
			MetaLayer<Object> layer = wordNet.getMetaLayers().getLayer(layerId);
			if (layer==null){
				return;
			}
			int[] allIds = layer.getAllIds();
			for(int id : allIds){
				Object value = layer.getValue(id);
				if(!clazz.isInstance(value)){
					continue;
				}
				MeaningElement me = wordNet.getConceptInfo(id);
				String basicForm = me.getParentTextElement().getBasicForm();
				map.put(basicForm, (T) value);
			}
		}
		
		
				
	}

}
