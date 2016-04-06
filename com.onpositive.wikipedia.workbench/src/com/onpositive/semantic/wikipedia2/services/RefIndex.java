package com.onpositive.semantic.wikipedia2.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import com.carrotsearch.hppc.IntArrayList;
import com.onpositive.semantic.wikipedia2.WikiEngine2;

public class RefIndex extends AbstractIntIntArrayIndex{

	public RefIndex(WikiEngine2 engine) {
		super(engine);
	
	}

	@Override
	protected int[] calcArray(int a) {
		String plainContent = engine.getPlainContent(a);
		if (plainContent==null){
			return new int[0];
		}
		StringReader bufReader=new StringReader(plainContent);
		BufferedReader br=new BufferedReader(bufReader);
		IntArrayList refs=new IntArrayList();
		while (true){
			try {
				String readLine = br.readLine();
				if (readLine==null){
					break;
				}
				analizeLine(refs,readLine);
			} catch (IOException e) {
				
			}
		}
		return refs.toArray();
	}
	public int[] getWithResolvedRedirects(int key){
		RedirectsMap index = engine.getIndex(RedirectsMap.class);
		int[] values = values(key);
		for (int a=0;a<values.length;a++){
			int v=values[a];
			int values2 = index.value(v);
			while (values2!=-1){
				v=values2;
				int values3 = index.value(values2);
				if (values2==values3){
					break;
				}
				values2 = values3;
				
			}
			values[a]=v;
		}
		return values;
	}

	static int correctRefs;
	static int incorrectRefs;
	private void analizeLine(IntArrayList refs, String readLine) {
		int pos=0;
		
		while (true){
			pos=readLine.indexOf("[[", pos);
			if (pos==-1){
				break;
			}
			int indexOf = readLine.indexOf("]]", pos);
			if (indexOf!=-1){
				String reference=readLine.substring(pos+2,indexOf);
				int indexOf2 = reference.indexOf('|');
				if (indexOf2!=-1){
					reference=reference.substring(0,indexOf2);
				}
				reference=reference.trim();
				int pageId = engine.getPageId(reference);
				if (pageId<=0&&reference.length()>0){
					pageId=engine.getPageId(Character.toUpperCase(reference.charAt(0))+reference.substring(1));
				}
				if (pageId>0){
					refs.add(pageId);
					correctRefs++;
				}				
				else{
					incorrectRefs++;
				}
				if (correctRefs%50000==0){
					System.out.println("Correct refs:"+correctRefs+" Incorrect:"+incorrectRefs);
				}
				pos=indexOf+2;
			}
			else{
				break;
			}
		}
	}
	@Override
	protected int[] getKeySet() {
		return engine.getDocumentIDs();
	}
	@Override
	public String getFileName() {
		return "documentRefs.index";
	}
}
