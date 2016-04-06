package com.onpositive.semantic.wikipedia2.properties.parsing;

import java.util.ArrayList;

import com.onpositive.semantic.wikipedia2.properties.PropertyPackager.AllPropertiesStorage.DecodedProperty;

public class SourceInformation {

	public final Object source;
	public final ArrayList<DecodedProperty>properties;
	
	public SourceInformation(Object source,
			ArrayList<DecodedProperty> properties) {
		super();
		this.source = source;
		this.properties = properties;
	}
	protected ArrayList<PropertyValue<?>>vals=new ArrayList<PropertyValue<?>>();
}
