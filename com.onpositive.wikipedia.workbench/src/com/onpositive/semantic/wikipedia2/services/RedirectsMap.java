package com.onpositive.semantic.wikipedia2.services;

import com.onpositive.semantic.wikipedia2.WikiEngine2;



public class RedirectsMap extends AbstractIntListIndex{

	private static final String REDIRECT2 = "#перенаправление";
	private static final String REDIRECT = "#redirect";
	public RedirectsMap(WikiEngine2 engine) {
		super(engine);
	}

	@Override
	protected int calc(int key) {
		String plainText = engine.getPlainContent(key);
		if (plainText==null){
			return -1;
		}
		String lowerCase = plainText.toLowerCase().trim();
		if (lowerCase.startsWith(REDIRECT)) {
			return extractRedirect(key,
					plainText.trim().substring(REDIRECT.length()));
		}
		if (lowerCase.startsWith(REDIRECT2)) {
			return extractRedirect(key,
					plainText.trim().substring(REDIRECT2.length()));
		}
		return -1;
	}
	public boolean isRedirect(int k){
		return data.get(k)>0;
	}
	

	private int extractRedirect(int key, String string) {
		int lastIndexOf = string.indexOf("[[");
		if (lastIndexOf != -1) {
			int indexOf = string.indexOf("]]");
			String tm = string.substring(lastIndexOf + 2, indexOf);
			int pageId = engine.getPageId(tm);
			if (pageId > 0) {
				return pageId;				
			}
			if (Character.isLowerCase(tm.charAt(0))){
				pageId = engine.getPageId(Character.toUpperCase(tm.charAt(0))+tm.substring(1));
				if (pageId>0){
					return pageId;
				}
			}
		}
		return -1;
	}

	@Override
	protected int[] getKeySet() {
		return engine.getDocumentIDs();
	}

	@Override
	public String getFileName() {
		return "redirects.dat";
	}
}
