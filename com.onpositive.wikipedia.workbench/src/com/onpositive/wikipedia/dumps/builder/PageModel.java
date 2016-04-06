package com.onpositive.wikipedia.dumps.builder;

public class PageModel {

	protected int pageId;
	public int getPageId() {
		return pageId;
	}

	public int getNamespace() {
		return namespace;
	}

	public String getText() {
		return text;
	}

	protected int namespace;
	protected String text;
	private String title;
	
	public String getTitle() {
		return title;
	}

	public PageModel(int pageId, int namespace, String title, String text) {
		super();
		this.pageId = pageId;
		this.namespace = namespace;
		this.text = text;
		this.title=title;
	}
	
}
