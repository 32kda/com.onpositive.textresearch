package com.onpositive.text.webview;

import com.onpositive.renderer.wordnet.SiteModel;
import com.onpositive.text.webview.ui.FreeMarkerComponent;
import com.onpositive.text.webview.ui.annotations.View;

@View("navbar.html")
public class NavBarComponent extends FreeMarkerComponent{

	public DemoComponent[] getMenuItems(){
		return SiteModel.getInstance().getComponents();
	}
	
}
