package com.onpositive.semantic.categorization.core;

import java.io.Serializable;
import java.util.ArrayList;

import com.onpositive.semantic.search.core.ICategory;
import com.onpositive.semantic.search.core.IDocument;
import com.onpositive.semantic.search.core.RatedCat;
import com.onpositive.semantic.search.core.WordStat;

public class Category implements ICategory,Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected String id;
	protected String title;
	
	
	protected ArrayList<Category>subCats=new ArrayList<Category>();
	
	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public ICategory[] getSubCategories() {
		return subCats.toArray(new ICategory[subCats.size()]);
	}

	@Override
	public IDocument[] getPages() {
		return null;
	}

	@Override
	public ICategory[] getParentCategories() {
		return null;
	}

	@Override
	public RatedCat[] getSameThemeChildren() {
		return null;
	}

	public int getRootDistance() {
		return -1;
	}

	@Override
	public WordStat getStatistics() {
		return null;
	}

	@Override
	public double textDistance(ICategory c) {
		return 0;
	}

	@Override
	public ICategory[] getCategories() {
		return new ICategory[]{};
	}

	@Override
	/**
	 * Made for compatiblity, returns System.identityHashCode() 
	 * for this obj, which is almost always (but not necessry) unique and
	 * can change from start of the program to start 
	 */
	public int getIntId() {
		return System.identityHashCode(this);
	}

}
