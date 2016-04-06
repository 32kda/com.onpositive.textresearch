package com.onpositive.text.webview;

public class PageComponent extends DemoComponent {

	private String url;
	private String title;
	public PageComponent(String url, String title)
	{
		this.url = url;
		this.title = title;
		
	}
	
	@Override
	public String getDescription() {
		return title;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public String getId() {
		return url;
	}

	@Override
	public String getOutput() {
		// TODO Auto-generated method stub
		return null;
	}

}
