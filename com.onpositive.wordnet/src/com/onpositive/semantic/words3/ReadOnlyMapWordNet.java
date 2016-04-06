package com.onpositive.semantic.words3;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import com.onpositive.semantic.wordnet.GrammarRelation;
import com.onpositive.semantic.words2.SimpleWordNet;
import com.onpositive.semantic.words3.hds.StringStorage;
import com.onpositive.semantic.words3.hds.StringToDataHashMap;

public class ReadOnlyMapWordNet extends ReadOnlyWordNet {

	public ReadOnlyMapWordNet(DataInputStream is) throws IOException {
		super(is);
	}

	public ReadOnlyMapWordNet(SimpleWordNet original) {
		super(original);
	}

	@Override
	protected StringStorage<GrammarRelation[]> createStorage(int size) {
		return new GrammarRelationsHashMap(size);
	}
	
	public static ReadOnlyWordNet load(String file) throws IOException {
		return load(new File(file));
	}
	
	@Override
	public String[] getAllGrammarKeys() {
		return ((StringToDataHashMap<GrammarRelation[]>) relations).getAllKeys();
	}

	public static ReadOnlyWordNet load(File file) throws FileNotFoundException,
			IOException {
		DataInputStream is = new DataInputStream(new BufferedInputStream(
				new FileInputStream(file)));
		try {
			return new ReadOnlyMapWordNet(is);
		} finally {
			is.close();
		}
	}
	
	public static ReadOnlyWordNet load(InputStream inputStream) throws FileNotFoundException,
			IOException {
		DataInputStream is = (DataInputStream) (inputStream instanceof DataInputStream ? inputStream : new DataInputStream(new BufferedInputStream(inputStream)));
		try {
			return new ReadOnlyMapWordNet(is);
		} finally {
			is.close();
		}
	}


}
