package com.onpositive.semantic.wikipedia2.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import com.onpositive.semantic.categorization.core.SearchSystem;
import com.onpositive.semantic.wikipedia2.WikiEngine2;
import com.onpositive.semantic.words3.hds.IntArrayList;
import com.onpositive.wikipedia.dumps.builder.ISqlConsumer;
import com.onpositive.wikipedia.dumps.builder.WikiSqlLoader;

public class ToEnglishIdIndex extends AbstractIntListIndex {

	public ToEnglishIdIndex(WikiEngine2 engine) {
		super(engine);
	}

	@Override
	protected void build(final WikiEngine2 engine) {
		File fl = new File(engine.getLocation());
		final WikiEngine2 enengine = (WikiEngine2) SearchSystem.getEngine("enwiki");
		int length = engine.getDocumentIDs().length;
		final IntArrayList value = new IntArrayList(length + 2);
		for (int a = 0; a < length; a++) {
			value.add(-1);
		}
		final CaseIgnoringSearcher index = engine.getIndex(CaseIgnoringSearcher.class);
		data = value;
		for (File f : fl.listFiles()) {
			if (f.getName().endsWith("langlinks.sql")) {
				try {

					ISqlConsumer consumer = new ISqlConsumer() {
						@Override
						public void consume(Object[] data) {
							if (data[1].equals("ru")) {
								if (data[2] != null
										&& data[2].toString().length() > 0) {
									String string = (String) data[2];
									int i = index.search(
											string.toLowerCase());
									
									if (i > 0) {
										try {
											Integer value2 = (Integer) data[0];
											int i1=enengine.getPageRenumberer().getIndex(value2);
											/*String string = enengine.getPageTitles().get(i1);
											String string0 = engine.getPageTitles().get(i);*/
											//System.out.println(string+"-"+string0);
											if (i1>0){
											value.put(i, i1);
											}
										} catch (Exception e) {
											throw new IllegalStateException(e);
										}
									}
									else{
										System.out.println(data[2]);
									}
								}
							}
						}
					};
					new WikiSqlLoader().parse(new BufferedReader(
							new InputStreamReader(new FileInputStream(f),
									"UTF-8")), consumer);
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public String getFileName() {
		return "toEng.dat";
	}

	@Override
	protected int calc(int a) {
		return 0;
	}

	@Override
	protected int[] getKeySet() {
		return engine.getNotRedirectDocumentIDs();
	}

}