package com.onpositive.tstruct;

import java.util.List;

public interface ITreeElement {

	public List<ITreeElement>getChildren();
	
	public ITreeElement parent();
	
	public boolean isAutofiltered();
	
	public boolean isExcluded();
	
	public boolean isIncluded();

	public String id();
	
	Object[] getParentNodes();

	public boolean isGood();
	
	public String getSourceId();
	
}
