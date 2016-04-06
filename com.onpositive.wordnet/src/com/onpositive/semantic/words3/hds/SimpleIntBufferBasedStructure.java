package com.onpositive.semantic.words3.hds;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.FileChannel;

public abstract class SimpleIntBufferBasedStructure extends ByteBuffered {

	public IntBuffer buffer;
	protected ByteBuffer original;
	protected int capacity;
	protected int size;

	public void store(FileChannel channel) throws IOException {
		original.putInt(0, capacity);
		original.putInt(4, size);
		channel.write(original);
	}
	
	public int byteSize() {
		return original.capacity();
	}
	
	public SimpleIntBufferBasedStructure(File f) throws FileNotFoundException, IOException{
		long length = f.length();
		ByteBuffer allocateDirect = ByteBuffer.allocateDirect((int) length);
		FileInputStream fileImageInputStream = new FileInputStream(f);
		fileImageInputStream.getChannel().read(allocateDirect);
		fileImageInputStream.close();
		init(allocateDirect, 0);
	}
	
	public SimpleIntBufferBasedStructure(int capacity){
		this.capacity=capacity;
		original = ByteBuffer.allocateDirect(capacity*scale()+8);
		original.position(8);
		buffer= original.asIntBuffer();//
		original.position(0);
	}

	protected abstract int scale() ;
	
	public SimpleIntBufferBasedStructure(ByteBuffer buffer2, int offset) {
		init(buffer2, offset);
	}

	protected void init(ByteBuffer buffer2, int offset) {
		capacity=buffer2.getInt(offset);
		size=buffer2.getInt(offset+4);
		buffer2.position(offset+8);
		this.original=buffer2;
		buffer=original.asIntBuffer();
		original.position(0);
	}
}