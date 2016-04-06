package com.onpositive.semantic.categorization.core;

import java.util.Enumeration;
import java.util.Properties;

public class SubSystemConfiguration {
	protected String id;

	public String getId() {
		return id;
	}

	public SubSystemConfiguration(String id) {
		super();
		this.id = id;
	}

	protected Properties properties = new Properties();

	public String getProperty(String key, String defaultValue) {
		return properties.getProperty(key, defaultValue);
	}

	public String getProperty(String key) {
		return properties.getProperty(key);
	}

	public Enumeration<Object> keys() {
		return properties.keys();
	}
}
