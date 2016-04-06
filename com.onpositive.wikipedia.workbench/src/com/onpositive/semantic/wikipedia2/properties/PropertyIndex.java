package com.onpositive.semantic.wikipedia2.properties;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
import com.onpositive.semantic.search.core.ISearchEngine;
import com.onpositive.semantic.wikipedia.properties.PropertyInfo;
import com.onpositive.semantic.wikipedia.properties.SwcPropertyExtractor;
import com.onpositive.semantic.wikipedia2.WikiEngine2;
import com.onpositive.semantic.wikipedia2.services.RedirectsMap;

public class PropertyIndex {

	private WikiEngine2 engine;

	private IntLongOpenHashMap catalog;
	private RandomAccessFile abstractsFile;

	private LoadingCache<Integer, ArrayList<PropertyInfo>> cache;

	private SwcPropertyExtractor extractor;

	public static class PropertyModel {
		String name;

		public PropertyModel(String name) {
			super();
			this.name = name;
		}

		protected ArrayList<String> values = new ArrayList<String>();
		protected ArrayList<Boolean> isSimple = new ArrayList<Boolean>();
	}

	public static class TemplateModel {
		protected String templateName;

		public TemplateModel(String templateName) {
			super();
			this.templateName = templateName;
		}

		protected HashMap<String, PropertyModel> vls = new HashMap<String, PropertyModel>();

		public void append(PropertyInfo i) {
			PropertyModel propertyModel = vls.get(i.propertyName);
			if (propertyModel == null) {
				propertyModel = new PropertyModel(i.propertyName);
				vls.put(i.propertyName, propertyModel);
			}
			propertyModel.values.add(i.propertyValue);
			propertyModel.isSimple.add(i.isSimple);
		}
	}

	private PropertyIndex(WikiEngine2 engine) {
		this.engine = engine;
		if (engine.getRole().equals("vocab")){
			ISearchEngine engine2 = SearchSystem.getEngine("enwiki");
			if (engine2!=null){
				WikiEngine2 eg=(WikiEngine2) engine2;
				PropertyIndex propertyIndex = PropertyIndex.AbstractIndexProvider.getInstance().get(eg);
				System.out.println(propertyIndex);
			}
		}
		extractor = new SwcPropertyExtractor();
		cache = CacheBuilder.newBuilder().maximumSize(50000)
				.expireAfterAccess(10, TimeUnit.MINUTES)
				.build(new CacheLoader<Integer, ArrayList<PropertyInfo>>() {
					public ArrayList<PropertyInfo> load(Integer id) {
						return readAbstract(id);
					}
				});
	}

	public synchronized ArrayList<PropertyInfo> readAbstract(int pageId) {
		if (!catalog.containsKey(pageId)) {
			return new ArrayList<PropertyInfo>();
		}
		long pos = catalog.get(pageId);
		try {
			abstractsFile.seek(pos);
			int len = abstractsFile.readInt();
			byte[] bs = new byte[len];
			abstractsFile.readFully(bs);
			return fromBytes(bs,engine);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static ArrayList<PropertyInfo> fromBytes(byte[] bs,WikiEngine2 eng) {
		try{
		DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(bs));
		short readShort = dataInputStream.readShort();
		ArrayList<TemplateModel>mm=new ArrayList<PropertyIndex.TemplateModel>();
		ArrayList<PropertyInfo>inf=new ArrayList<PropertyInfo>();
		for (int t=0;t<readShort;t++){
			String nm=dataInputStream.readUTF();
			TemplateModel tm=new TemplateModel(nm);
			int pc=dataInputStream.readShort();
			for (int p=0;p<pc;p++){
				String readUTF = dataInputStream.readUTF();
				PropertyModel pm=new PropertyModel(readUTF);
				tm.vls.put(pm.name, pm);
				int vc=dataInputStream.readShort();
				for (int v=0;v<vc;v++){
					String vl=dataInputStream.readUTF();
					boolean isSimple=dataInputStream.readBoolean();
					pm.values.add(vl);
					pm.isSimple.add(isSimple);
					inf.add(new PropertyInfo(nm, readUTF, vl, isSimple,eng));
				}
			}
			mm.add(tm);
		}
		return inf;
		}catch (Exception e) {
			e.printStackTrace();
			
		}
		return null;
	}

	private static byte[] toBytes(ArrayList<PropertyInfo> text) {
		HashMap<String, TemplateModel> mdls = new HashMap<String, PropertyIndex.TemplateModel>();
		for (PropertyInfo i : text) {
			if (i.templateName.length()>1000){
				continue;
			}
			if (i.propertyName.length()>1000){
				continue;
			}
			if (i.propertyValue.length()>30000){
				continue;
			}
			TemplateModel templateModel = mdls.get(i.templateName);
			if (templateModel == null) {
				templateModel = new TemplateModel(i.templateName);
				mdls.put(i.templateName, templateModel);
			}
			templateModel.append(i);
		}
		try {
			ByteArrayOutputStream bs = new ByteArrayOutputStream();
			DataOutputStream ss = new DataOutputStream(bs);
			if (mdls.size() > Short.MAX_VALUE) {
				throw new IllegalStateException();
			}
			ss.writeShort(mdls.size());
			for (TemplateModel m : mdls.values()) {
				ss.writeUTF(m.templateName);
				int size = m.vls.size();
				if (size>Short.MAX_VALUE){
					throw new IllegalStateException();
				}
				ss.writeShort(size);
				for (PropertyModel ma:m.vls.values()){
					ss.writeUTF(ma.name);
					int size2 = ma.values.size();
					if (size2>Short.MAX_VALUE){
						throw new IllegalStateException();
					}
					ss.writeShort(size2);
					for (int a=0;a<size2;a++){
						ss.writeUTF(ma.values.get(a));
						ss.writeBoolean(ma.isSimple.get(a));
					}
				}
			}
			ss.close();
			bs.close();
			return bs.toByteArray();
		} catch (Exception e) {
			return null;
		}
	}

	boolean calc = SearchSystem.isRecalcHtml();

	public ArrayList<PropertyInfo> getProperties(int pageId) {
		try {
			if (calc) {
				return calcProperties(pageId);
			}
			return cache.get(pageId);
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static PropertyIndex build(WikiEngine2 engine) throws IOException {
		long time = System.currentTimeMillis();
		PropertyIndex index = new PropertyIndex(engine);
		System.out.println("Building abstract index for " + engine + "...");
		int count = 0;

		IntLongOpenHashMap catalog = new IntLongOpenHashMap();
		RandomAccessFile abstractsFile = new RandomAccessFile(getFile(engine),
				"rw");
		RedirectsMap rm = engine.getIndex(RedirectsMap.class);
		SwcPropertyExtractor ex = new SwcPropertyExtractor();
		for (int pageid : engine.getDocumentIDs()) {
			boolean redirect = rm.isRedirect(pageid);
			ArrayList<PropertyInfo> text = new ArrayList<PropertyInfo>();
			if (!redirect) {
				String plainContent = engine.getPlainContent(pageid);
				try {
					text = ex.extractProperties(
							engine.getPageTitles().get(pageid), plainContent,
							engine.getUrl() + "/wiki/");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (text.isEmpty()) {
				continue;
			}
			byte[] bytes = toBytes(text);
			ArrayList<PropertyInfo> fromBytes = fromBytes(bytes,engine);
			
			HashSet l1 = new HashSet(text);
			HashSet l2 = new HashSet(fromBytes);
			if (!l1.equals(l2)){
				l1.removeAll(l2);
				System.err.println(engine.getPageTitles().get(pageid));
				continue;
			}
			if (bytes==null){
				continue;
			}
			catalog.put(pageid, abstractsFile.getFilePointer());
			
			
			abstractsFile.writeInt(bytes.length);
			abstractsFile.write(bytes);
			count++;
			if (count % 1000 == 0) {
				System.out.println("Processed:" + count);
			}
		}
		time = System.currentTimeMillis() - time;
		System.out.println("Abstract index for " + engine + " built in " + time
				+ "ms [" + count + " pages]");

		DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(
				new FileOutputStream(getMapFile(engine))));
		CompactLongVector.writeIntLongMap(dos, catalog);
		dos.close();
		abstractsFile.close();
		index.catalog = catalog;
		index.abstractsFile = new RandomAccessFile(getFile(engine), "r");
		return index;
	}

	public void load() throws IOException {
		catalog = CompactLongVector.readIntLongMap(new DataInputStream(
				new BufferedInputStream(new FileInputStream(getMapFile()))));
		abstractsFile = new RandomAccessFile(getFile(engine), "r");
	}

	public static File getFile(WikiEngine2 engine) {
		return new File(engine.getLocation(), "pbd.dat");
	}

	public static File getMapFile(WikiEngine2 engine) {
		return new File(engine.getLocation(), "pbd.map");
	}

	public File getMapFile() {
		return getMapFile(engine);
	}

	public File getFile() {
		return getFile(engine);
	}

	public static class AbstractIndexProvider {

		Map<WikiEngine2, PropertyIndex> indexis = Maps.newHashMap();

		private static AbstractIndexProvider instance;

		private AbstractIndexProvider() {
		}

		public static AbstractIndexProvider getInstance() {
			if (instance == null) {
				instance = new AbstractIndexProvider();
			}
			return instance;
		}

		public synchronized PropertyIndex get(WikiEngine2 engine) {
			PropertyIndex abstractIndex = indexis.get(engine);
			if (abstractIndex == null) {
				if (getFile(engine).exists()) {
					abstractIndex = new PropertyIndex(engine);
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

	public ArrayList<PropertyInfo> calcProperties(int id) {
		ArrayList<PropertyInfo> text = new ArrayList<PropertyInfo>();
		try {
			text = extractor.extractProperties(engine.getPageTitles().get(id),
					engine.getPlainContent(id), engine.getUrl() + "/wiki/");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return text;
	}

}
