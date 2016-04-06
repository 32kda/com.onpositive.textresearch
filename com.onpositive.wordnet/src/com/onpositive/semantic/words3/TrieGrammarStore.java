package com.onpositive.semantic.words3;

import com.onpositive.semantic.wordnet.AbstractRelation;
import com.onpositive.semantic.wordnet.GrammarRelation;
import com.onpositive.semantic.words3.hds.StringTrie;

public class TrieGrammarStore extends StringTrie<GrammarRelation[]> {
	
	@Override
	protected int getDataSize(int i) {
		return CodingUtils.estimateRelationsLength(i, byteBuffer);
	}

	@Override
	protected GrammarRelation[] decodeValue(int i) {
		return CodingUtils.decodeGrammarRelations(i, byteBuffer);
	}

	
	@Override
	public byte[] encodeValue(GrammarRelation[] associatedData2) {
		return CodingUtils.encodeRelations((AbstractRelation<?>[]) associatedData2);
	}

}
