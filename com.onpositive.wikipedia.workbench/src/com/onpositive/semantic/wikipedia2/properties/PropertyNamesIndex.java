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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.carrotsearch.hppc.IntOpenHashSetSerializable;
import com.onpositive.semantic.wikipedia2.WikiEngine2;
import com.onpositive.semantic.wikipedia2.WikiEngineService;
import com.onpositive.semantic.wikipedia2.catrelations.TitleModel;
import com.onpositive.semantic.wikipedia2.docclasses.SimplePropertyStorage;
import com.onpositive.semantic.wikipedia2.docclasses.SimplePropertyStorage.PropertyVisitor;

public class PropertyNamesIndex extends WikiEngineService {

	public PropertyNamesIndex(WikiEngine2 engine) {
		super(engine);
	}

	public static class PropertyInfo implements Serializable,Comparable<PropertyInfo> {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		protected String propertyName;
		public PropertyInfo(String propertyName) {
			super();
			this.propertyName = propertyName;
		}
		protected int occurenceCount;
		@Override
		public int compareTo(PropertyInfo o) {
			return this.occurenceCount-o.occurenceCount;
		}
		@Override
		public String toString() {
			return propertyName+":"+occurenceCount;
		}
		
		IntOpenHashSetSerializable documents=new IntOpenHashSetSerializable();
	}

	public static class PropertyGroup implements Serializable,Comparable<PropertyGroup> {
		public PropertyGroup(String core) {
			this.name=core;
		}
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public String name;
		public HashMap<String,PropertyInfo> infos = new HashMap<String,PropertyInfo>();

		public int occurenceCount;

		@Override
		public int compareTo(PropertyGroup o) {
			return this.occurenceCount-o.occurenceCount;
		}
		@Override
		public String toString() {
			return "group "+name+":"+occurenceCount;
		}
	}

	HashMap<String, PropertyGroup> groups;
	
	public Collection<PropertyGroup>getGroups(){
		return groups.values();
	}
	
	public Map<String,PropertyGroup>getGroupsMap(){
		return groups;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void doLoad(File fl) throws IOException {
		ObjectInputStream ss = new ObjectInputStream(new BufferedInputStream(
				new FileInputStream(fl)));
		try {
			groups = (HashMap<String, PropertyGroup>) (ss.readObject());
		} catch (ClassNotFoundException e) {
			throw new IllegalStateException(e);
		}
		ss.close();
	}

	@Override
	protected void build(WikiEngine2 enfine) {
		// TODO Auto-generated method stub
		try {
			groups = new HashMap<String, PropertyNamesIndex.PropertyGroup>();
			SimplePropertyStorage.visitPropertiesFile(new PropertyVisitor() {

				@Override
				public void visit(int document, String pName, String value) {
					TitleModel titleModel = TitleModel.get(pName, false);
					String core = null;
					if (pName.equals("template")){
						pName=pName+"-"+value;
					}
					if (titleModel.hasSingleCore()) {
						core = titleModel.getSingleCore().toString();
					} else {
						core = "<no good core>";
					}
					PropertyGroup propertyGroup = groups.get(core);
					if (propertyGroup==null){
						propertyGroup=new PropertyGroup(core);
						groups.put(core, propertyGroup);
					}
					PropertyInfo propertyInfo = propertyGroup.infos.get(pName);
					if (propertyInfo==null){
						propertyInfo=new PropertyInfo(pName);
						propertyGroup.infos.put(pName, propertyInfo);
					}
					propertyInfo.occurenceCount++;
					propertyInfo.documents.add(document);
					propertyGroup.occurenceCount++;
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
		ss.writeObject(groups);
		ss.close();
	}

	@Override
	public String getFileName() {
		return "propertyStat.dat";
	}
}