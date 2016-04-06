package com.onpositive.semantic.words3.hds;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

public abstract class MapListCombination extends TwoItemCombination<IntIntMap,IntArrayList> {


	public MapListCombination() {
		super(IntIntMap.class,IntArrayList.class);
	}
	public MapListCombination(File f) throws IOException {
		super(IntIntMap.class,IntArrayList.class,f);
		
	}

	public MapListCombination(ByteBuffer buffer, int offset) {
		super(IntIntMap.class,IntArrayList.class,buffer,offset);
	}
}