package com.onpositive.semantic.search.core;

public interface ICategorizable {

	public abstract ICategory[] getCategories();

	public abstract String getTitle();
	
	public abstract int getIntId();
}