package com.onpositive.semantic.search.core;

public interface ILearningCallback {

	void log(String string);
	void error(Exception e);
	void done();
}
