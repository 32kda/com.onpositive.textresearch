package com.onpositive.tstruct;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class CategoryNode extends StructureNode{

	public CategoryNode(String engine, String name) {
		super(engine, name);	
	}
	public CategoryNode(String name) {
		super(null,name);	
	}
	public CategoryNode() {
		super(null,null);	
	}

	@XmlAttribute(name="description")
	protected String description;

}
