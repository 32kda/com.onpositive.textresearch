package com.onpositive.semantic.wikipedia2.fulltext;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.onpositive.wikipedia.dumps.builder.Porter;

public class StemProvider {

	static StemProvider instance;

	private LoadingCache<String, String> cache;
	
	public StemProvider() {
		cache = CacheBuilder.newBuilder()
	       .maximumSize(10000)
	       .expireAfterWrite(10, TimeUnit.MINUTES)
	       //.removalListener(MY_LISTENER)
	       .build(
	           new CacheLoader<String, String>() {
	             public String load(String word) {
	            	 return Porter.stem(word);
	             }
	           });
	}
	
	public static StemProvider getInstance() {
		if(instance == null) {
			instance = new StemProvider();
		}
		return instance;
	}
	
	public String stem(String word) {
		try {
			return cache.get(word);
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return null;
	}
	
}
