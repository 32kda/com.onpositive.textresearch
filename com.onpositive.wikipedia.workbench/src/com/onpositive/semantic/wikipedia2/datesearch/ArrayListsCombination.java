package com.onpositive.semantic.wikipedia2.datesearch;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import com.onpositive.semantic.words3.hds.IntArrayList;
import com.onpositive.semantic.words3.hds.TwoItemCombination;

public class ArrayListsCombination extends
		TwoItemCombination<IntArrayList, IntArrayList> {

	public ArrayListsCombination() {
		super(IntArrayList.class, IntArrayList.class);
	}

	public ArrayListsCombination(ByteBuffer buffer, int offset) {
		super(IntArrayList.class, IntArrayList.class, buffer, offset);
	}

	public ArrayListsCombination(File f) throws IOException {
		super(IntArrayList.class, IntArrayList.class, f);
	}
}