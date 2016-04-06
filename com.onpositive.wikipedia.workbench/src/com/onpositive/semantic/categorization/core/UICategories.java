package com.onpositive.semantic.categorization.core;

import java.util.ArrayList;
import java.util.HashSet;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class UICategories {

	ArrayList<UICategory>elements=new ArrayList<UICategory>();

	public ArrayList<UICategory> getElements() {
		return elements;
	}

	public void setElements(ArrayList<UICategory> elements) {
		this.elements = elements;
	}

	public void load(Document d){
		elements.clear();
		Element el=d.getDocumentElement();
		NodeList childNodes = el.getChildNodes();
		for (int a = 0; a < childNodes.getLength(); a++) {
			Node item = childNodes.item(a);
			if (item instanceof Element) {
				Element e = (Element) item;
				elements.add(new UICategory(e));
			}
		}
	}
	
	public void save(Document newDocument) {
		Element createElement = newDocument.createElement("categories");	
		newDocument.appendChild(createElement);
		for (UICategory c:elements){
			c.save(createElement);
		}
	}

	public HashSet<String> getMappings(String category) {
		HashSet<String>r=new HashSet<String>();
		for (UICategory c:elements){			
			c.fill(r,category,false);
		}
		return r;		
	}
}
