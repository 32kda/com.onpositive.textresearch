package com.onpositive.semantic.words3.hds;

public class StringToByteTrie extends StringTrie<Byte>{

	@Override
	protected Byte decodeValue(int i) {
		return byteBuffer[i];
	}

	@Override
	protected byte[] encodeValue(Byte associatedData2) {
		return new byte[]{associatedData2};
	}

	@Override
	protected int getDataSize(int i) {
		return 1;
	}

}
