package com.onpositive.semantic.words3.hds;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public abstract class TwoItemCombination<A extends ByteBuffered, B extends ByteBuffered>
		extends ByteBuffered {

	protected A first;
	protected B second;

	public TwoItemCombination(Class<A> firstClass, Class<B> secondClass) {
		super();
		try {
			first = createFirst(firstClass);
			second = createSecond(secondClass);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	public TwoItemCombination(Class<A> class1, Class<B> class2, File f)
			throws IOException {
		long length = f.length();
		ByteBuffer allocateDirect = ByteBuffer.allocateDirect((int) length);
		FileInputStream fileImageInputStream = new FileInputStream(f);
		fileImageInputStream.getChannel().read(allocateDirect);
		fileImageInputStream.close();
		init(class1, class2, allocateDirect, 0);
	}

	public TwoItemCombination(Class<A> class1, Class<B> class2,
			ByteBuffer buffer, int offset) {
		init(class1, class2, buffer, offset);
	}

	protected void init(Class<A> class1, Class<B> class2, ByteBuffer buffer,
			int offset) {
		int mapSize = buffer.getInt(offset);
		first = createFirst(class1, buffer, offset+8);
		second = createSecond(class2, buffer, offset + 8 + mapSize);
	}

	protected B createSecond(Class<B> class2, ByteBuffer buffer, int offset) {
		return createInstance(class2, buffer, offset);
	}

	protected <T> T createInstance(Class<T> class2, ByteBuffer buffer,
			int offset) {
		try {
			Constructor<T> constructor = class2.getConstructor(
					ByteBuffer.class, int.class);
			return constructor.newInstance(buffer,offset);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	protected A createFirst(Class<A> class1, ByteBuffer buffer, int offset) {
		return createInstance(class1, buffer, offset);
	}

	protected B createSecond(Class<B> secondClass)
			throws InstantiationException, IllegalAccessException {
		return secondClass.newInstance();
	}

	protected A createFirst(Class<A> firstClass) throws InstantiationException,
			IllegalAccessException {
		return firstClass.newInstance();
	}

	public void store(FileChannel ch) throws IOException {
		ByteBuffer mm = ByteBuffer.allocate(8);
		mm.putInt(0, first().byteSize());
		mm.putInt(4, second().byteSize());
		ch.write(mm);
		first().store(ch);
		second().store(ch);
	}

	public A first() {
		return first;
	}

	public B second() {
		return second;
	}

	public int byteSize() {
		return first().byteSize() + second().byteSize() + 8;
	}

}