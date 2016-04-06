package com.onpositive.semantic.words3.hds;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public abstract class ByteBuffered {

	public ByteBuffered() {
		super();
	}

	public void store(File f) throws IOException {
		FileOutputStream dd=new FileOutputStream(f);
		store(dd.getChannel());
		dd.close();
	}

	public abstract void store(FileChannel channel) throws IOException;

	public abstract int byteSize();

}