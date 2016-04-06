package com.onpositive.wikipedia.workbench.words.primary;

import com.onpositive.semantic.wikipedia2.WikiEngine2;

public class WikiEngineProvider {

	
	static WikiEngine2 engine=new WikiEngine2("D:/se2/ruwiki");
	
	public static WikiEngine2 getInstance(){
		return engine;
	}
}
