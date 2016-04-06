package com.onpositive.semantic.words3.hds;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public abstract class StringStorage<T> extends StringCoder {
	
	public abstract T get(String string);
	
	public abstract int store(String string, T data);
	
	public abstract void read(InputStream is) throws IOException;

	public abstract void write(OutputStream ds) throws IOException;
	
	public abstract int size();
}
