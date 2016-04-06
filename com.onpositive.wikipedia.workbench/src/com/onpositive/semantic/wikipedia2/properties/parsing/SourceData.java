package com.onpositive.semantic.wikipedia2.properties.parsing;

import java.util.ArrayList;

import com.onpositive.semantic.wikipedia2.properties.PropertyPackager.AllPropertiesStorage.DecodedProperty;

public class SourceData {

	public SourceData(ArrayList<SourceInformation> vals) {
		informations = vals;
		vals.forEach(v -> allProperty.addAll(v.properties));
	}

	protected ArrayList<SourceInformation> informations;
	protected ArrayList<DecodedProperty> allProperty = new ArrayList<DecodedProperty>();

}
