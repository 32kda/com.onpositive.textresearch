package com.onpositive.semantic.words3;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import com.carrotsearch.hppc.ByteArrayList;
import com.onpositive.semantic.wordnet.GrammarRelation;
import com.onpositive.semantic.words2.SimpleWordNet;
import com.onpositive.semantic.words3.hds.StringCoder;
import com.onpositive.semantic.words3.hds.StringStorage;
import com.onpositive.semantic.words3.hds.StringTrie;

public class ReadOnlyTrieWordNet extends ReadOnlyWordNet {
	
	public ReadOnlyTrieWordNet(DataInputStream is) throws IOException {
		super(is);
	}
	
	public ReadOnlyTrieWordNet(SimpleWordNet original) {
		super(original);
	}

	@Override
	protected void initRelations(SimpleWordNet original) {
		super.initRelations(original);
		((StringTrie<GrammarRelation[]>) relations).commit();
	}

	@Override
	protected StringStorage<GrammarRelation[]> createStorage(int size) {
		return new TrieGrammarStore();
	}

	public void useWordInfo(SimpleWordNet simpleWordNet) {
		relations = new TrieGrammarStore();
		initRelations(simpleWordNet);
	}

	public void initSequences(HashMap<Integer, ArrayList<Integer>> sequenceMap) {
		sequences.clear();
		ByteArrayList mm = new ByteArrayList();

		for (Integer q : sequenceMap.keySet()) {
			sequences.put(q, mm.size());
			ArrayList<Integer> intArrayList = sequenceMap.get(q);
			int size = intArrayList.size();
			if (size > 120) {
				if (size > Short.MAX_VALUE) {
					throw new IllegalStateException();
				}
				mm.add((byte) -1);
				mm.add(StringCoder.int0(size));
				mm.add(StringCoder.int1(size));
			} else {
				mm.add((byte) size);
			}
			for (Integer v : intArrayList) {
				mm.add(StringCoder.int0(v));
				mm.add(StringCoder.int1(v));
				mm.add(StringCoder.int2(v));
			}

		}
		
		this.store = mm.toArray();
	}
	
}
