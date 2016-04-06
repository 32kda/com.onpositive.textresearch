package com.onpositive.semantic.words2.builder;

import java.util.HashMap;

import com.onpositive.semantic.wordnet.TextElement;
import com.onpositive.semantic.words2.Word;


public class TempExtras {

	protected HashMap<String, WordFormTemplate> wordTemplateMap = new HashMap<String, WordFormTemplate>();
	
	public WordFormTemplate findTemplate(String string) {
		return wordTemplateMap.get(string);
	}

	public void registerTemplate(WordFormTemplate template) {
		wordTemplateMap.put(template.title, template);
	}
	
	protected HashMap<TextElement, TempWordInfo>mp=new HashMap<TextElement, TempWordInfo>();
	
	public void init(){
		for(TextElement q:mp.keySet()){
			TempWordInfo tempWordInfo = mp.get(q);
			if(tempWordInfo.template!=null){
				tempWordInfo.template.register((Word) q, this, tempWordInfo);
			}
		}
	}

	public TempWordInfo getInfo(Word orCreate) {
		if (mp.containsKey(orCreate)){
			return mp.get(orCreate);
		}
		TempWordInfo wi=new TempWordInfo();
		mp.put(orCreate, wi);
		return wi;
	}
}
