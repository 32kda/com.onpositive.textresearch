package com.onpositive.text.webview;

import com.onpositive.text.webview.ui.annotations.View;

@View("default.html")
public class DefaultDemoComponent extends DemoComponent{

	@Override
	public String getDescription() {
		return "Что нибудь о нас";
	}

	@Override
	public String getTitle() {
		return "О Нас";
	}

	@Override
	public String getId() {
		return "";
	}

	@Override
	public String getOutput() {
		return "";
	}

}
