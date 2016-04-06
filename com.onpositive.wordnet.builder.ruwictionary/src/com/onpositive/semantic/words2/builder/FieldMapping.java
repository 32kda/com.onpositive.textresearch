package com.onpositive.semantic.words2.builder;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface FieldMapping {

	String value();
	int relation();
}
