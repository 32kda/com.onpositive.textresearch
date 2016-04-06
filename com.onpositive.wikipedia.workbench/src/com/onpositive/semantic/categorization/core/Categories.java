package com.onpositive.semantic.categorization.core;

import java.io.Serializable;
import java.util.ArrayList;

public class Categories implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected ArrayList<Category>categories=new ArrayList<Category>();

	public ArrayList<Category> getCategories() {
		return categories;
	}
	
}
