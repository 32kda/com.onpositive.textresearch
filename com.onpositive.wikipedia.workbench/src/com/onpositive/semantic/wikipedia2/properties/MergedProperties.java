package com.onpositive.semantic.wikipedia2.properties;

import java.util.ArrayList;

import com.onpositive.semantic.categorization.core.SearchSystem;
import com.onpositive.semantic.wikipedia.properties.PropertyInfo;
import com.onpositive.semantic.wikipedia2.WikiDoc;
import com.onpositive.semantic.wikipedia2.WikiEngine2;
import com.onpositive.semantic.wikipedia2.services.ToEnglishIdIndex;

public class MergedProperties {

	static MergedProperties instance;

	ToEnglishIdIndex englishId;
	PropertyIndex englishIndex;
	PropertyIndex russianIndex;
	
	public MergedProperties() {
		WikiEngine2 engine = (WikiEngine2) SearchSystem.getEngine("enwiki");
		if (engine!=null){
		englishIndex=PropertyIndex.AbstractIndexProvider.getInstance().get(engine);
		}
		russianIndex=PropertyIndex.AbstractIndexProvider.getInstance().get(WikiEngine2.getVocabEngine());
		englishId=WikiEngine2.getVocabEngine().getIndex(ToEnglishIdIndex.class);
	}
	
	public ArrayList<PropertyInfo>getProperties(WikiDoc doc){
		ArrayList<PropertyInfo>result=new ArrayList<PropertyInfo>();
		if (englishIndex!=null){
			int value = englishId.value(doc.getIntId());
			if (value!=-1){
				result.addAll(englishIndex.getProperties(value));
			}
		}
		if (russianIndex!=null){
			result.addAll(russianIndex.getProperties(doc.getIntId()));
		}
		return result;
	}
	
	public static MergedProperties getInstance(){
		if(instance==null){
			instance=new MergedProperties();
		}
		return instance;
	}
}
