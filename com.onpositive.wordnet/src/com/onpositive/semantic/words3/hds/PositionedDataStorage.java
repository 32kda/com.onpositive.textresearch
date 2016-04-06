package com.onpositive.semantic.words3.hds;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

public abstract class PositionedDataStorage<T extends BufferStorable> extends TwoItemCombination<IntArrayList, ByteStorage<T>> {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public PositionedDataStorage(ByteBuffer buffer, int offset) {
		super(IntArrayList.class,(Class) ByteStorage.class, buffer, offset);
	}
	@Override
	protected ByteStorage<T> createSecond(Class<ByteStorage<T>> class2,
			ByteBuffer buffer, int offset) {
		return new ByteStorage<T>(buffer,offset) {

			@Override
			protected T newInstance() {
				return createInstance();
			}
		};
	}
	protected abstract T createInstance();
	@Override
	protected ByteStorage<T> createSecond(Class<ByteStorage<T>> secondClass)
			throws InstantiationException, IllegalAccessException {
		return new ByteStorage<T>() {

			@Override
			protected T newInstance() {
				return createInstance();
			}
		};
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public PositionedDataStorage(File f) throws IOException {
		super(IntArrayList.class,(Class) ByteStorage.class, f);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public PositionedDataStorage() {
		super(IntArrayList.class,(Class) ByteStorage.class);
		first.add(-1);
	}

	public void add(T data){
		if (data==null){
			first.add(-1);
		}
		else{
			first.add(second.add(data));
		}
	}
	
	public T get(int position){
		int i = first.get(position);
		if(i<0){
			return null;
		}
		return second.get(i);
	}
}
