package com.onpositive.semantic.wikipedia2.catrelations;

import com.onpositive.semantic.search.core.ICategorizable;
import com.onpositive.semantic.search.core.ICategory;

public interface IConnectionFactory {
	
	public Connection createConnection(ICategorizable object, ICategory possibleParent);
	
}
