package com.onpositive.semantic.words3;

import java.util.Arrays;

import com.onpositive.semantic.wordnet.GrammarRelation;
import com.onpositive.semantic.words3.hds.StringToDataHashMap;

public class GrammarRelationsHashMap extends StringToDataHashMap<GrammarRelation[]> {

	public GrammarRelationsHashMap(int sz) {
		super(sz);
	}

	@Override
	protected final GrammarRelation[] decodeValue(byte[] buffer, int addr) {
		return CodingUtils.decodeGrammarRelations(addr, buffer);
	}

	@Override
	protected byte[] encodeValue(GrammarRelation[] value) {
		byte[] encodeRelations = CodingUtils.encodeRelations(value);
		GrammarRelation[] decodeGrammarRelations = CodingUtils.decodeGrammarRelations(0, encodeRelations);
		if (!Arrays.equals(decodeGrammarRelations, value)){
			throw new IllegalStateException();
		}
		return encodeRelations;
	}

	public int size() {
		return usedCount;
	}

}
