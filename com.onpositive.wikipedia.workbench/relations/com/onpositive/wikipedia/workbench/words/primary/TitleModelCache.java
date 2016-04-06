package com.onpositive.wikipedia.workbench.words.primary;

import java.util.LinkedHashMap;

public class TitleModelCache {

	
	private static final int TRESHOLD = 100000;
	LinkedHashMap<String, SimpleTitleModel>cache=new LinkedHashMap<>();
	
	
	public SimpleTitleModel getModel(String title){
		if (cache.containsKey(title)){
			return cache.get(title);
		}
		SimpleTitleModel m=new SimpleTitleModel(title);
		cache.put(title, m);
		if (cache.size()>TRESHOLD){
			cache.remove(cache.keySet().iterator().next());
		}
		return m;
	}
	
	private static final TitleModelCache instance=new TitleModelCache();
	
	public static TitleModelCache getInstance(){
		return instance;
	}
}
