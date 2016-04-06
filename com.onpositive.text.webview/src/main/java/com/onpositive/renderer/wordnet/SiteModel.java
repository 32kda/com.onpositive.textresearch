package com.onpositive.renderer.wordnet;

import com.onpositive.text.webview.Context;
import com.onpositive.text.webview.DefaultDemoComponent;
import com.onpositive.text.webview.DemoComponent;
import com.onpositive.text.webview.LinksComponent;

public class SiteModel {

	protected DemoComponent[] components;
	protected DemoComponent active;

	public DemoComponent[] getComponents() {
		return components;
	}

	SiteModel() {
		this.components = init();
	}

	public DemoComponent getActive() {
		return this.active;
	}

	private DemoComponent[] init() {
		DemoComponent[] cm = new DemoComponent[] { new DefaultDemoComponent(), new WordNetComponent(),
				new MorhologyComponent(), new SyntaxParseComponent(), new LinksComponent() };
		cm[0].setActive(true);
		this.active = cm[0];
		
		for (DemoComponent c : cm) {
			boolean matchPath = Context.getContext().localPath.equals("/" + c.getId());
			if (matchPath) {
				this.active.setActive(false);
				this.active = c;
				c.setActive(true);
			}
		}
		
		return cm;
	}

	public static SiteModel getInstance() {
		return new SiteModel();
	}
}
