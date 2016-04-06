package com.onpositive.text.webview;

import com.onpositive.renderer.wordnet.SiteModel;
import com.onpositive.renderer.wordnet.WordNetComponent;
import com.onpositive.text.webview.ui.FreeMarkerComponent;
import com.onpositive.text.webview.ui.IComponent;
import com.onpositive.text.webview.ui.annotations.View;

@View("template.html")
public class WebSite extends FreeMarkerComponent{
	
	
	public IComponent getHead(){
		return new HeadComponent();
	}
	
	public IComponent getNavbar(){
		return new NavBarComponent();
	}
	public IComponent getContent(){
		return SiteModel.getInstance().getActive();
	}
}
