package com.onpositive.wikipedia.dumps.builder;

public class CategoryLinkModel {
	
	public static final int TYPE_CATEGORY=1;
	public static final int TYPE_PAGE=2;
	public static final int TYPE_FILE=3;
	
	protected int from;
	protected int type;
	protected String target;
	
	public int getFrom() {
		return from;
	}

	public int getType() {
		return type;
	}

	public String getTarget() {
		return target;
	}

	public CategoryLinkModel(Object[] items) {
		from=(Integer) items[0];
		target=(String) items[1];
		if (items[6].equals("page")){
			type=TYPE_PAGE;
		}
		else if (items[6].equals("subcat")){
			type=TYPE_CATEGORY;
		}
		else if (items[6].equals("file")){
			type=TYPE_FILE;
		}
	}
}