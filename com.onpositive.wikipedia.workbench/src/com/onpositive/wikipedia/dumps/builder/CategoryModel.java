package com.onpositive.wikipedia.dumps.builder;

public class CategoryModel {

	public CategoryModel(int id, String title, int pageCount, int catCount,
			int fileCount) {
		super();
		this.id = id;
		this.title = title;
		this.pageCount = pageCount;
		this.catCount = catCount;
		this.fileCount = fileCount;
	}
	public CategoryModel(Object[] data) {
		super();
		this.id = (Integer) data[0];
		this.title = (String) data[1];
		this.pageCount =(Integer) data[3];
		this.catCount = (Integer) data[2];
		this.fileCount = (Integer) data[4];
	}

	protected int id;
	public int getId() {
		return id;
	}
	public String getTitle() {
		return title;
	}
	public int getPageCount() {
		return pageCount;
	}
	public int getCatCount() {
		return catCount;
	}
	public int getFileCount() {
		return fileCount;
	}

	protected String title;
	protected int pageCount;
	protected int catCount;
	protected int fileCount;
	
}
