package com.onpositive.semantic.wikipedia2.properties;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import com.carrotsearch.hppc.IntObjectOpenHashMapSerialzable;
import com.carrotsearch.hppc.IntOpenHashSetSerializable;
import com.onpositive.semantic.wikipedia2.WikiEngine2;
import com.onpositive.semantic.wikipedia2.WikiEngineService;
import com.onpositive.semantic.wikipedia2.docclasses.SimplePropertyStorage;
import com.onpositive.semantic.wikipedia2.docclasses.SimplePropertyStorage.PropertyVisitor;
import com.onpositive.semantic.wordnet.Grammem;

public class DocToPropIndex extends WikiEngineService {

	public DocToPropIndex(WikiEngine2 engine) {
		super(engine);
	}

	public static class DocToPropData implements Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public HashMap<String, Integer> voc = new HashMap<String, Integer>();
		public ArrayList<String> all = new ArrayList<String>();
		public IntObjectOpenHashMapSerialzable<IntOpenHashSetSerializable> map = new IntObjectOpenHashMapSerialzable<IntOpenHashSetSerializable>();

		public void add(String pName, int document) {

		}
	}
	DocToPropData data;
	
	@Override
	protected void doLoad(File fl) throws IOException {
		ObjectInputStream ss = new ObjectInputStream(new BufferedInputStream(
				new FileInputStream(fl)));
		try {
			data = (DocToPropData) (ss.readObject());
		} catch (ClassNotFoundException e) {
			throw new IllegalStateException(e);
		}
		ss.close();
	}

	@Override
	protected void build(WikiEngine2 enfine) {
		// TODO Auto-generated method stub
		try {
			final DocToPropData pm=new DocToPropData();
			data=pm;
			SimplePropertyStorage.visitPropertiesFile(new PropertyVisitor() {

				@Override
				public void visit(int document, String pName, String value) {
					if (pName.equals("template")) {
						pName = pName + "-" + value;
					}
					IntOpenHashSetSerializable arrayList = pm.map.get(document);
					if (arrayList == null) {
						arrayList = new IntOpenHashSetSerializable();
						pm.map.put(document, arrayList);
					}
					Integer integer = pm.voc.get(pName);
					int q=-1;
					if (integer==null){
						int pos=pm.all.size();
						pm.all.add(pName);
						pm.voc.put(pName, pos);
						q=pos;
					}
					else{
						q=integer;
					}
					arrayList.add(q);
				}
			}, enfine);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	protected void doSave(File fl) throws IOException {
		ObjectOutputStream ss = new ObjectOutputStream(
				new BufferedOutputStream(new FileOutputStream(fl)));
		ss.writeObject(data);
		ss.close();
	}

	@Override
	public String getFileName() {
		return "propertyToDoc.dat";
	}

	public DocToPropData getData() {
		return data;
	}
}