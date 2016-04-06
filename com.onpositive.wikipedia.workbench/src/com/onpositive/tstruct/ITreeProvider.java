package com.onpositive.tstruct;

public interface ITreeProvider {

	public String id();
	
	public ChildInfo[] getChildrenElements(Object id);
	
	public String id(Object element);

	public Object[] getParentNodes(ChildInfo element);
}
