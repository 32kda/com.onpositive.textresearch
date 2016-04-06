package com.onpositive.semantic.wikipedia2.services;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.IntOpenHashSet;
import com.onpositive.semantic.wikipedia2.WikiEngine2;
import com.onpositive.semantic.wikipedia2.WikiEngineService;
import com.onpositive.semantic.words3.hds.StringToDataHashMap2;
import com.onpositive.semantic.words3.hds.StringVocabulary;

public class CaseIgnoringSearcher extends WikiEngineService {

	RedirectsMap index;

	class DocStore extends StringToDataHashMap2<int[]> {

		public DocStore(int sz) {
			super(sz);
		}

		@Override
		protected int[] decodeValue(byte[] buffer, int addr) {
			int c = buffer[addr];
			int[] dd = new int[c];
			addr++;
			for (int a = 0; a < c; a++) {
				byte b0 = buffer[addr++];
				byte b1 = buffer[addr++];
				byte b2 = buffer[addr++];
				byte b3 = buffer[addr++];
				dd[a] = makeInt(b3, b2, b1, b0);
			}
			return dd;
		}

		@Override
		protected byte[] encodeValue(int[] data) {
			ByteArrayOutputStream bs = new ByteArrayOutputStream();
			if (data.length > 255) {
				data=new int[0];
				//throw new IllegalStateException();
			}
			bs.write(data.length);
			for (int q : data) {
				bs.write(DocStore.int0(q));
				bs.write(DocStore.int1(q));
				bs.write(DocStore.int2(q));
				bs.write(DocStore.int3(q));
			}
			try {
				bs.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return bs.toByteArray();
		}

	}

	protected DocStore store;

	public CaseIgnoringSearcher(WikiEngine2 engine) {
		super(engine);
		index = engine.getIndex(RedirectsMap.class);
	}

	@Override
	protected void doLoad(File fl) throws IOException {
		DocStore ds = new DocStore(100000);
		BufferedInputStream stream = new BufferedInputStream(
				new FileInputStream(fl));
		ds.read(stream);
		stream.close();
		this.store=ds;
	}

	@Override
	protected void build(WikiEngine2 enfine) {
		HashMap<String, IntArrayList> lls = new HashMap<String, IntArrayList>();
		int[] notRedirectDocumentIDs = enfine.getDocumentIDs();
		StringVocabulary pageTitles = enfine.getPageTitles();
		for (int m : notRedirectDocumentIDs) {
			String string = pageTitles.get(m);
			string = string.toLowerCase().replace('ё', 'е');
			IntArrayList intArrayList = lls.get(string);
			if (intArrayList == null) {
				intArrayList = new IntArrayList();
				lls.put(string, intArrayList);
			}
			intArrayList.add(m);
		}
		store = new DocStore(lls.size() * 3 / 2);
		for (String m : lls.keySet()) {
			store.store(m, lls.get(m).toArray());
		}
	}

	public int[] searchAll(String title) {
		title=title.toLowerCase();
		int[] is = store.get(title.replace('ё', 'е'));
		return is;
	}

	public int search(String title) {
		title=title.toLowerCase();
		int[] is = store.get(title.replace('ё', 'е'));
		if (is == null||is.length==0) {
			return -1;
		}
		if (is.length > 1) {
			IntOpenHashSet mmm = new IntOpenHashSet();
			for (int c : is) {
				if (index.isRedirect(c)) {
					mmm.add(index.value(c));
				} else {
					mmm.add(c);
				}
			}
			if (mmm.size() > 1) {
			}
			return mmm.toArray()[0];
		}
		int i = is[0];
		if (index.isRedirect(i)) {
			return index.value(i);
		}
		return i;
	}

	@Override
	protected void doSave(File fl) throws IOException {
		DataOutputStream stream = new DataOutputStream(
				new BufferedOutputStream(new FileOutputStream(fl)));
		try {
			store.write(stream);
		} finally {
			stream.close();
		}
	}

	@Override
	public String getFileName() {
		return "docIgnoreTitles.dat";
	}
}