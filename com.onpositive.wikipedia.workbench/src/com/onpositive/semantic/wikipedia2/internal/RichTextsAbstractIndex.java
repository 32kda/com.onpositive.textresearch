package com.onpositive.semantic.wikipedia2.internal;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.carrotsearch.hppc.IntLongOpenHashMap;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Maps;
import com.onpositive.compactdata.CompactLongVector;
import com.onpositive.semantic.categorization.core.SearchSystem;
import com.onpositive.semantic.wikipedia.abstracts.TextAbstractExtractor;
import com.onpositive.semantic.wikipedia.abstracts.svc.SwcAbstractExtractor;
import com.onpositive.semantic.wikipedia2.WikiEngine2;
import com.onpositive.semantic.wikipedia2.services.RedirectsMap;

public class RichTextsAbstractIndex {

	private WikiEngine2 engine;

	TextAbstractExtractor extractor;

	private IntLongOpenHashMap catalog;
	private RandomAccessFile abstractsFile;

	private LoadingCache<Integer, String> cache; 
	
	private RichTextsAbstractIndex(WikiEngine2 engine) {
		this.engine = engine;
		extractor = new TextAbstractExtractor();
		cache = CacheBuilder.newBuilder()
	       .maximumSize(50000)
	       .expireAfterAccess(10, TimeUnit.MINUTES)
	       .build(
	           new CacheLoader<Integer, String>() {
	             public String load(Integer id) {
	               return readAbstract(id);
	             }
	           });
	}

	public synchronized String readAbstract(int pageId) {
		if (!catalog.containsKey(pageId)){
			return "";
		}
		long pos = catalog.get(pageId);
		try {
			abstractsFile.seek(pos);
			int len = abstractsFile.readInt();
			byte[] bs = new byte[len];
			abstractsFile.readFully(bs);
			return new String(bs, "UTF-8");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	boolean calc=SearchSystem.isRecalcHtml();
	
	public String getAbstract(int pageId) {
		try {
			if (calc){
				return calcAbstract(pageId);
			}
			return cache.get(pageId);
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	private static RichTextsAbstractIndex build(WikiEngine2 engine) throws IOException {
		long time = System.currentTimeMillis();
		RichTextsAbstractIndex index = new RichTextsAbstractIndex(engine);
		System.out.println("Building abstract index for " + engine + "...");
		int count = 0;
	
		
		IntLongOpenHashMap catalog = new IntLongOpenHashMap();
		RandomAccessFile abstractsFile = new RandomAccessFile(getFile(engine), "rw");
		SwcAbstractExtractor ae=new SwcAbstractExtractor();
		RedirectsMap rm= engine.getIndex(RedirectsMap.class);
		for(int pageid: engine.getDocumentIDs()) {
			boolean redirect = rm.isRedirect(pageid);
			String text="";
			if(!redirect){
			String plainContent = engine.getPlainContent(pageid);			
			try {
				text = ae.extractHTML(engine.getPageTitles().get(pageid), plainContent,engine.getUrl()+"/wiki/");
			} catch (Exception e) {
				e.printStackTrace();
			}
			}
			if(text.length()==0){
				continue;
			}
			catalog.put(pageid, abstractsFile.getFilePointer());
			byte[] bytes = text.getBytes("UTF-8");
			abstractsFile.writeInt(bytes.length);
			abstractsFile.write(bytes);
			count++;
			if (count%1000==0){
				System.out.println("Processed:"+count);
			}
		}
		time = System.currentTimeMillis() - time;
		System.out.println("Abstract index for " + engine + " built in " + time + "ms [" + count + " pages]");
		
		DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(getMapFile(engine))));
		CompactLongVector.writeIntLongMap(dos, catalog);
		dos.close();
		abstractsFile.close();
		index.catalog = catalog;
		index.abstractsFile = new RandomAccessFile(getFile(engine), "r");
		return index;
	}
	
	public void load() throws IOException {
		catalog = CompactLongVector.readIntLongMap(new DataInputStream(new BufferedInputStream(new FileInputStream(getMapFile()))));			
		abstractsFile = new RandomAccessFile(getFile(engine), "r");
	}

	public static File getFile(WikiEngine2 engine) {
		return new File(engine.getLocation(),"rabstracts.dat");
	} 

	public static File getMapFile(WikiEngine2 engine) {
		return new File(engine.getLocation(),"rabstracts.map");
	}

	public File getMapFile() {
		return getMapFile(engine);
	}

	public File getFile() {
		return getFile(engine);
	}

	public static class AbstractIndexProvider {

		Map<WikiEngine2,RichTextsAbstractIndex> indexis = Maps.newHashMap();
		
		private static AbstractIndexProvider instance;
		
		private AbstractIndexProvider() {
		}

		public static AbstractIndexProvider getInstance() {
			if(instance == null) {
				instance = new AbstractIndexProvider();
			}
			return instance;
		}
		
		public synchronized RichTextsAbstractIndex get(WikiEngine2 engine) {
			RichTextsAbstractIndex abstractIndex = indexis.get(engine);
			if(abstractIndex == null) {
				if(getFile(engine).exists()) {
					abstractIndex= new RichTextsAbstractIndex(engine);
					try {
						abstractIndex.load();
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else {
					try {
						abstractIndex = build(engine);
					} catch (IOException e) {
						e.printStackTrace();
					}		
				}
				indexis.put(engine, abstractIndex);
			}
			return abstractIndex;
		}
	}

	public String calcAbstract(int id) {
		SwcAbstractExtractor ae=new SwcAbstractExtractor();
		String text="";
		try {
			text = ae.extractHTML(engine.getPageTitles().get(id),engine.getPlainContent(id),engine.getUrl()+"/wiki/");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return text;
	}
	
}
