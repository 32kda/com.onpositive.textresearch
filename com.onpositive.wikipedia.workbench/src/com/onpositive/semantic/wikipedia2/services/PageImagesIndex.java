package com.onpositive.semantic.wikipedia2.services;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

import com.onpositive.semantic.wikipedia.abstracts.TextAbstractExtractor;
import com.onpositive.semantic.wikipedia.properties.PropertyExtractor;
import com.onpositive.semantic.wikipedia.properties.PropertyExtractor.RawProperty;
import com.onpositive.semantic.wikipedia2.WikiEngine2;
import com.onpositive.semantic.wikipedia2.WikiEngineService;
import com.onpositive.semantic.words3.hds.StringArray;
import com.onpositive.semantic.words3.hds.StringArrayStorage;

public class PageImagesIndex extends WikiEngineService{

	public PageImagesIndex(WikiEngine2 engine) {
		super(engine);
	}
	StringArrayStorage storage;

	@Override
	protected void doLoad(File fl) throws IOException {
		storage=new StringArrayStorage(fl);
	}

	@Override
	protected void build(WikiEngine2 enfine) {
		storage=new StringArrayStorage();
		int[] documentIDs = enfine.getDocumentIDs();
		int count=0;
		int icount=0;
		int dicount=0;		
		for(int i:documentIDs){
			if (count%10000==0){
				System.out.println("processed:"+count+" image count:"+icount+" docs with image:"+dicount);
			}
			count++;
			String[] images=extractImages(i);
			if(images==null||images.length==0){
				storage.add(null);
			}
			else{
				StringArray ar=new StringArray();
				dicount+=1;
				icount+=images.length;
				ar.data=images;
				storage.add(ar);
			}
		}
	}

	public String[] extractImages(int i) {
		RedirectsMap index = engine.getIndex(RedirectsMap.class);
		if(index.isRedirect(i)){
			return null;
		}
		String plainContent = engine.getPlainContent(i);
		if (plainContent==null||plainContent.length()==0){
			return null;
		}
		String[] doExtractImages = new TextAbstractExtractor().doExtractImages(plainContent);
		if (doExtractImages==null){
			doExtractImages=new String[0];
		}
		PropertyExtractor ps=new PropertyExtractor();
		LinkedHashSet<String>str=new LinkedHashSet<String>(Arrays.asList(doExtractImages));
		try{
		List<RawProperty> extract = ps.extract(i, plainContent);
		for(RawProperty p:extract){
			if(p.name.toLowerCase().contains("image")||p.name.toLowerCase().contains("изображение")){
				String v=p.value.toLowerCase();
				if (v.endsWith(".jpg")||v.endsWith(".png")||v.endsWith(".gif")||v.endsWith(".svg")){
					if (!v.contains("|")&&!v.contains("[")&&!v.contains("{")){
						if (p.value.startsWith("Файл:")){
							p.value=p.value.substring("файл:".length()).trim();
						}
						str.add(p.value);
					}
				}
			}
		}
		}catch (Exception e){
			e.printStackTrace();
		}
		return str.toArray(new String[str.size()]);
	}

	@Override
	protected void doSave(File fl) throws IOException {
		storage.store(fl);
	}

	@Override
	public String getFileName() {
		return "images.dat";
	}

	public String[] getImages(int i) {
		StringArray stringArray = storage.get(i);
		if (stringArray!=null){
			return stringArray.data;
		}
		//return extractImages(i);
		return new String[0];
	}
}