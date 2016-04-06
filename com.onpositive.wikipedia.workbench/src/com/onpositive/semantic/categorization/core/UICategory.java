package com.onpositive.semantic.categorization.core;

import java.rmi.server.UID;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.StringTokenizer;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class UICategory {

	String title;
	String id=new UID().toString();

	public ArrayList<UICategory> children = new ArrayList<UICategory>();

	protected String description;
	protected String keywords;
	protected String mappingWiki;
	protected String mappingWikiNews;
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public ArrayList<UICategory> getChildren() {
		return children;
	}

	public void setChildren(ArrayList<UICategory> children) {
		this.children = children;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getKeywords() {
		return keywords;
	}

	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}

	public String getMappingWiki() {
		return mappingWiki;
	}

	public void setMappingWiki(String mappingWiki) {
		this.mappingWiki = mappingWiki;
	}

	public String getMappingWikiNews() {
		return mappingWikiNews;
	}

	public void setMappingWikiNews(String mappingWikiNews) {
		this.mappingWikiNews = mappingWikiNews;
	}

	public String getKeyArticles() {
		return keyArticles;
	}

	public void setKeyArticles(String keyArticles) {
		this.keyArticles = keyArticles;
	}

	protected String keyArticles;

	public UICategory() {

	}

	public UICategory(Element el) {
		this.title = el.getAttribute("title");
		this.id = el.getAttribute("id");
		this.description = el.getAttribute("description");
		this.mappingWiki = el.getAttribute("mappingWiki");
		this.mappingWikiNews = el.getAttribute("mappingWikiNews");
		this.keyArticles = el.getAttribute("keyArticles");
		this.keywords = el.getAttribute("keywords");
		NodeList childNodes = el.getChildNodes();
		for (int a = 0; a < childNodes.getLength(); a++) {
			Node item = childNodes.item(a);
			if (item instanceof Element) {
				Element e = (Element) item;
				children.add(new UICategory(e));
			}
		}
	}

	@Override
	public String toString() {
		return title;
	}

	public void save(Element createElement) {
		Element createElement2 = createElement.getOwnerDocument()
				.createElement("category");
		createElement.appendChild(createElement2);
		createElement2.setAttribute("title", title);
		createElement2.setAttribute("id", id);
		createElement2.setAttribute("keywords", keywords);
		createElement2.setAttribute("description", description);
		createElement2.setAttribute("mappingWiki", mappingWiki);
		createElement2.setAttribute("mappingWikiNews", mappingWikiNews);
		createElement2.setAttribute("keyArticles", keyArticles);
		for (UICategory c : children) {
			c.save(createElement2);
		}
	}

	public void fill(HashSet<String> r, String category, boolean b) {
		b|=this.getTitle().equals(category);
		if (b){
		String mappingWikiNews2 = mappingWikiNews;
		addTokens(r, mappingWikiNews2);
		addTokens(r, mappingWiki);
		addTokens(r, keywords);
		}
		for (UICategory m:children){
			m.fill(r,category,b);
		}
	}

	private void addTokens(HashSet<String> r, String mappingWikiNews2) {
		StringTokenizer t=new StringTokenizer(mappingWikiNews2);
		while (t.hasMoreTokens()){
			String nextToken = t.nextToken();
			r.add(nextToken);
		}
	}

	
}