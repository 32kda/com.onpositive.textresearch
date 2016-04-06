package com.onpositive.semantic.wikipedia2.properties;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.List;

import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.IntObjectOpenHashMapSerialzable;
import com.carrotsearch.hppc.ObjectIntOpenHashMap;
import com.onpositive.semantic.wikipedia2.WikiDoc;
import com.onpositive.semantic.wikipedia2.WikiEngine2;
import com.onpositive.semantic.wikipedia2.docclasses.SimplePropertyStorage;
import com.onpositive.semantic.wikipedia2.properties.PropertyPackager.AllPropertiesStorage.DecodedProperty;
import com.onpositive.text.analysis.BasicCleaner;
import com.onpositive.text.analysis.IToken;
import com.onpositive.words.WikiTokenizerExtension;

public class PropertyPackager {

	protected AllPropertiesStorage storage;

	public PropertyPackager(WikiEngine2 engine) {
		try {
			storage = packageProps(engine);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static class PropertyValue {
		public final int documentId;
		public final String value;

		public PropertyValue(int documentId, String value) {
			super();
			this.documentId = documentId;
			this.value = value;
		}
	}

	public static class AllPropertiesStorage {

		ObjectIntOpenHashMap<String> propertyNames = new ObjectIntOpenHashMap<String>();
		ObjectIntOpenHashMap<String> propertyValues = new ObjectIntOpenHashMap<String>();

		String[] names;
		String[] values;

		/**
		 * All Properties by object;
		 */
		protected IntObjectOpenHashMapSerialzable<IntArrayList> propsByObject = new IntObjectOpenHashMapSerialzable<IntArrayList>();

		protected IntObjectOpenHashMapSerialzable<IntArrayList> propsByValue = new IntObjectOpenHashMapSerialzable<IntArrayList>();

		public static class DecodedProperty {
			public String propName;
			public String propValue;
			public int documentId;
			public WikiEngine2 engine;
			private List<IToken> clean;
			
			public DecodedProperty(int object, WikiEngine2 wikiEngine2){
				this.documentId=object;
				this.engine=wikiEngine2;
			}
			
			@Override
			public int hashCode() {
				final int prime = 31;
				int result = 1;
				result = prime * result
						+ ((propName == null) ? 0 : propName.hashCode());
				result = prime * result
						+ ((propValue == null) ? 0 : propValue.hashCode());
				return result;
			}
			@Override
			public boolean equals(Object obj) {
				if (this == obj)
					return true;
				if (obj == null)
					return false;
				if (getClass() != obj.getClass())
					return false;
				DecodedProperty other = (DecodedProperty) obj;
				if (propName == null) {
					if (other.propName != null)
						return false;
				} else if (!propName.equals(other.propName))
					return false;
				if (propValue == null) {
					if (other.propValue != null)
						return false;
				} else if (!propValue.equals(other.propValue))
					return false;
				return true;
			}
			@Override
			public String toString() {
				return propName+":"+propValue;
			}
			
			
			public List<IToken> tokenize(){
				if (clean!=null){
					return clean;
				}
				List<IToken> process = tokenizer.process(WikiTokenizerExtension.preprocess(propValue));
				clean = new BasicCleaner().clean(process);
				return clean;				
			}
		}
		static PropertyTokenizer tokenizer=new PropertyTokenizer();

		public DecodedProperty[] getProperties(int object, WikiEngine2 wikiEngine2) {
			IntArrayList intArrayList = propsByObject.get(object);
			if (intArrayList == null) {
				return new DecodedProperty[0];
			}
			DecodedProperty[] res = new DecodedProperty[intArrayList.size() / 2];
			int i = 0;
			for (int a = 0; a < res.length; a++) {
				int key = intArrayList.get(i++);
				int value = intArrayList.get(i++);
				DecodedProperty cc = new DecodedProperty(object,wikiEngine2);
				if (names == null) {
					initInverseArrays();
				}
				cc.propName = names[key];
				cc.propValue = values[value];
				res[a] = cc;
			}
			return res;
		}

		private void initInverseArrays() {
			ObjectIntOpenHashMap<String> propertyNames2 = propertyNames;
			String[] names = toArray(propertyNames2);
			this.names = names;
			this.values = toArray(propertyValues);

		}

		public String[] toArray(ObjectIntOpenHashMap<String> propertyNames2) {
			String[] names = new String[propertyNames2.size()];
			for (String s : propertyNames2.keys().toArray(String.class)) {
				names[propertyNames2.get(s)] = s;
			}
			return names;
		}

		static HashSet<String> ignore = new HashSet<String>();
		static {
			ignore.add("rowspan");
			ignore.add("archiveurl");
			ignore.add("archivedate");
		}

		/**
		 * 
		 */

		public void appendTriple(int objectId, String pName, String pValue) {
			pValue = cleanup(pValue);
			if (pValue.trim().length() == 0) {
				return;
			}
			int pId = -1;
			if (pName.length() > 200) {
				return;
			}
			if (ignore.contains(pName)) {
				return;
			}
			if (pName.equals("width")) {
				if (pValue.startsWith("\"")) {
					if (pValue.contains("%")) {
						return;
					}
				}
			}
			pName = pName.toLowerCase();
			if (propertyNames.containsKey(pName)) {
				pId = propertyNames.get(pName);
			} else {
				pId = propertyNames.size();
				propertyNames.put(pName, pId);
			}
			int pVal = -1;
			if (propertyValues.containsKey(pValue)) {
				pVal = propertyValues.get(pValue);
			} else {
				pVal = propertyValues.size();
				propertyValues.put(pValue, pVal);
			}
			// now lets fill indexes;
			// adding property to object to prop index;
			IntArrayList intArrayList = propsByObject.get(objectId);
			if (intArrayList == null) {
				intArrayList = new IntArrayList();
				propsByObject.put(objectId, intArrayList);
			}
			intArrayList.add(pId);
			intArrayList.add(pVal);
			// adding property to prop to object index
			intArrayList = propsByValue.get(pId);
			if (intArrayList == null) {
				intArrayList = new IntArrayList();
				propsByValue.put(pId, intArrayList);
			}
			intArrayList.add(objectId);
			intArrayList.add(pVal);
		}

		private String cleanup(String pValue) {
			int indexOf = pValue.indexOf("<!--");
			while (indexOf > -1) {
				int indexOf2 = pValue.indexOf("-->");
				if (indexOf2 == -1 || indexOf2 < indexOf) {
					return pValue;
				}
				pValue = pValue.substring(0, indexOf)
						+ pValue.substring(indexOf2 + 3);
				indexOf = pValue.indexOf("<!--");
			}
			indexOf = pValue.indexOf("<ref");
			while (indexOf > -1) {
				int indexOf2 = pValue.indexOf("</ref>");
				if (indexOf2 == -1 || indexOf2 < indexOf) {
					int indexOf3 = pValue.indexOf("/>");
					if (indexOf3 > indexOf) {
						pValue = pValue.substring(0, indexOf)
								+ pValue.substring(indexOf3 + 2);
						indexOf = pValue.indexOf("<ref");
						continue;
					} else {
						return pValue.substring(0, indexOf);
					}
				}
				pValue = pValue.substring(0, indexOf)
						+ pValue.substring(indexOf2 + 6);
				indexOf = pValue.indexOf("<ref");
			}
			return pValue;
		}

		public void write(File file) throws IOException {
			DataOutputStream ds = new DataOutputStream(
					new BufferedOutputStream(new FileOutputStream(file)));
			writeStringIntMap(propertyNames, ds);
			writeStringIntMap(propertyValues, ds);
			writeIntToIntList(propsByObject, ds);
			writeIntToIntList(propsByValue, ds);
			ds.close();
		}

		public void read(File file) throws IOException {
			DataInputStream ds = new DataInputStream(new BufferedInputStream(
					new FileInputStream(file)));
			propertyNames = readStringIntMap(ds);
			propertyValues = readStringIntMap(ds);
			propsByObject = readIntToIntList(ds);
			propsByValue = readIntToIntList(ds);
		}

		private IntObjectOpenHashMapSerialzable<IntArrayList> readIntToIntList(
				DataInputStream ds) throws IOException {
			IntObjectOpenHashMapSerialzable<IntArrayList> ls = new IntObjectOpenHashMapSerialzable<IntArrayList>();
			int size = ds.readInt();
			for (int a = 0; a < size; a++) {
				int key = ds.readInt();
				int sz = ds.readInt();
				IntArrayList rr = new IntArrayList(sz);
				for (int b = 0; b < sz; b++) {
					rr.add(ds.readInt());
				}
				ls.put(key, rr);
			}
			return ls;
		}

		private void writeIntToIntList(
				IntObjectOpenHashMapSerialzable<IntArrayList> propsByObject2,
				DataOutputStream ds) throws IOException {
			ds.writeInt(propsByObject2.size());
			for (int key : propsByObject2.keys().toArray()) {
				ds.writeInt(key);
				IntArrayList intArrayList = propsByObject2.get(key);
				ds.writeInt(intArrayList.size());
				for (int m : intArrayList.toArray()) {
					ds.writeInt(m);
				}
			}
		}

		private void writeStringIntMap(
				ObjectIntOpenHashMap<String> propertyNames2, DataOutputStream ds)
				throws IOException {
			ds.writeInt(propertyNames2.size());
			for (String key : propertyNames2.keys().toArray(String.class)) {
				ds.writeUTF(key);
				ds.writeInt(propertyNames2.get(key));
			}
		}

		private ObjectIntOpenHashMap<String> readStringIntMap(DataInputStream ds)
				throws IOException {
			int size = ds.readInt();
			ObjectIntOpenHashMap<String> sm = new ObjectIntOpenHashMap<String>(
					size * 3 / 2);
			for (int a = 0; a < size; a++) {
				String key = ds.readUTF();
				int vl = ds.readInt();
				sm.put(key, vl);
			}
			return sm;
		}

		public PropertyValue[] getValues(String string) {
			if (names == null) {
				initInverseArrays();
			}
			if (propertyNames.containsKey(string)) {
				int i = propertyNames.get(string);
				IntArrayList intArrayList = propsByValue.get(i);
				PropertyValue[] vl = new PropertyValue[intArrayList.size() / 2];
				int a = 0;
				for (int j = 0; j < intArrayList.size(); j += 2) {
					PropertyValue v = new PropertyValue(intArrayList.get(j),
							values[intArrayList.get(j + 1)]);
					vl[a] = v;
					a++;
				}
				return vl;
			}
			return null;
		}

	}

	private AllPropertiesStorage packageProps(WikiEngine2 engine2)
			throws IOException {
		String location = engine2.getLocation();
		File file = new File(location, "props.all");
		if (file.exists()) {
			//
			AllPropertiesStorage ps = new AllPropertiesStorage();
			ps.read(file);
			return ps;
		}
		AllPropertiesStorage allP = new AllPropertiesStorage();
		File fl0 = SimplePropertyStorage.getPropertiesFile(engine2);
		BufferedReader rr = getReader(fl0);
		fill(allP, rr);
		File fl1 = new File(location, "en-props.dat");
		if (fl1.exists()) {
			fill(allP, getReader(fl1));
		}
		allP.write(file);
		return allP;
	}

	public void fill(AllPropertiesStorage allP, BufferedReader rr) {
		while (true) {
			try {
				String line = rr.readLine();
				if (line == null) {
					break;
				}
				int p0 = line.indexOf(',');
				int objectId = Integer.parseInt(line.substring(0, p0));
				int pe = line.indexOf(',', p0 + 1);
				String pName = line.substring(p0 + 1, pe);
				String pValue = line.substring(pe + 1);
				if (pValue.trim().length() == 0) {
					continue;
				}

				if (pValue.length() > 1000) {
					pValue = pValue.substring(0, 1000);
				}
				allP.appendTriple(objectId, pName, pValue);
			} catch (IOException e) {
				return;
			}
		}
	}

	private BufferedReader getReader(File fl0) {
		try {
			return new BufferedReader(new InputStreamReader(
					new FileInputStream(fl0), "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public DecodedProperty[] getProperties(WikiDoc d) {
		return storage.getProperties(d.getIntId(),d.getEngine());
	}

	public PropertyValue[] getPropertyInfo(String string) {
		return storage.getValues(string);
	}
}
