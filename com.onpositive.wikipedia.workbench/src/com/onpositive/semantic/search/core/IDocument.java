package com.onpositive.semantic.search.core;

import java.util.ArrayList;

public interface IDocument extends ICategorizable {
	String getId();
	String getUrl();
	
	String getRichTextAbstract();
	String getPlainTextAbstract();
	String getOriginalMarkup();
	String getImageLink();
	ArrayList<ILocation>getLocations();
	ArrayList<String> getDates();
	
	
	boolean hasCategory(ICategory c);
	
	MatchDescription[] getMatches(SearchRequest request);

	String[] getImages();
	int getPopularity();
	void adjustPopularity(int i);
	
}
