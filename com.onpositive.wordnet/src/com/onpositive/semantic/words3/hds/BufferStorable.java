package com.onpositive.semantic.words3.hds;

import java.nio.ByteBuffer;

public  abstract class BufferStorable{
	
	protected abstract void init(ByteBuffer buffer,int position);
	
	protected abstract byte[] toByteArray();
}