package com.onpositive.semantic.search.core;

public interface ICategory extends ICategorizable{
	
	String getId();
	
	ICategory[] getSubCategories();
	
	RatedCat[] getSameThemeChildren();
	
	IDocument[] getPages();

	ICategory[] getParentCategories();

	int getRootDistance();
	
	WordStat getStatistics();
	
	double textDistance(ICategory c);
}
