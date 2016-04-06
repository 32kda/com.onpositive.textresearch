package com.onpositive.semantic.words3.hds;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public abstract class ByteStorage<T extends BufferStorable> extends ByteBuffered{

	protected ByteBuffer data=ByteBuffer.wrap(new byte[100]);
	protected int size;
	int offset;
	
	@Override
	public void store(FileChannel channel) throws IOException {
		//trim first
		byte[] array = data.array();
		byte[]newArray=new byte[size];
		System.arraycopy(array, 0, newArray, 0, size);
		this.data=ByteBuffer.wrap(newArray);
		channel.write(data);
	}
	public ByteStorage() {
	}
	public ByteStorage(ByteBuffer buffer,int offset){
		this.data=buffer;
		this.offset=offset;
	}

	@Override
	public int byteSize() {
		return data.capacity();
	}
	
	public T get(int offset){
		T newInstance = newInstance();
		newInstance.init(data, this.offset+offset);
		return newInstance;
	}
	
	protected abstract T newInstance();

	public int add(T data){
		byte[] byteArray = data.toByteArray();
		if (size+byteArray.length>=this.data.capacity()){
			//resize;
			byte[] array = this.data.array();
			byte[]newArray=new byte[byteArray.length+(array.length*4)/3];
			System.arraycopy(array, 0, newArray, 0, array.length);
			this.data=ByteBuffer.wrap(newArray);
		}
		for (int a=size;a<size+byteArray.length;a++){
			this.data.put(a,byteArray[a-size]);
		}
		int k=this.size;
		size+=byteArray.length;
		return k;
	}
}
