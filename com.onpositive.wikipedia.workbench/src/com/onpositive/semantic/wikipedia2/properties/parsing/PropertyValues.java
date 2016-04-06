package com.onpositive.semantic.wikipedia2.properties.parsing;

import java.util.ArrayList;
import java.util.LinkedHashSet;

import com.onpositive.semantic.wikipedia2.properties.PropertyPackager.AllPropertiesStorage.DecodedProperty;

public class PropertyValues {
	protected Object source;
	LinkedHashSet<PropertyValue<?>> values;
	ArrayList<DecodedProperty>original;

	public PropertyValues(Object source, ArrayList<PropertyValue<?>> values, ArrayList<DecodedProperty> properties) {
		super();
		this.source = source;
		this.values = new LinkedHashSet<PropertyValue<?>>(values);
		this.original=properties;
	}
	public String toString(){
		return values.toString();
	}
	public String getOriginal(){
		if (original==null||original.isEmpty()){
			return "";
		}
		return original.toString();
	}
}
