package com.onpositive.semantic.wikipedia2.locations;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


import com.carrotsearch.hppc.IntObjectOpenHashMapSerialzable;
import com.carrotsearch.hppc.IntOpenHashSetSerializable;
import com.google.common.collect.Lists;
import com.onpositive.semantic.categorization.core.SearchSystem;
import com.onpositive.wikipedia.dumps.builder.Porter;

@SuppressWarnings("unchecked")
public class GeoIndex implements Serializable{
	
	private static final int[] INT_ARRAY_LIST = new int[0];

	static GeoIndex geoIndex;
	
	public static GeoIndex getIndex(){
		if (geoIndex==null){
			loadGeoIndex();
		}
		return geoIndex;
	}
	private HashMap<String, Object> namesToNodes = new HashMap<String, Object>();
	private IntObjectOpenHashMapSerialzable<GeoPlace> idsToNodes = new IntObjectOpenHashMapSerialzable<GeoPlace>();
	
	
	
	private static void loadGeoIndex() {
		Path rootIndexPath = SearchSystem.getRootIndexPath();
		//rootIndexPath = rootIndexPath.append("geodata");
//			String osString = rootIndexPath.append("geoIndex_v4.dat").toOSString();
//			if (new File(osString).exists()){
//				try {
//					ObjectInputStream is=new ObjectInputStream(new BufferedInputStream(new FileInputStream(osString)));
//					geoIndex=(GeoIndex) is.readObject();
//					is.close();
//					return;
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				} catch (ClassNotFoundException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//			geoIndex = GeoIndexBuilder.build(
//					new File(rootIndexPath.append("alternateNames.txt").toOSString()),
//					new File(rootIndexPath.append("hierarchy.txt").toOSString()),
//					new File(rootIndexPath.append("cities1000.txt").toOSString()),
//					new File(rootIndexPath.append("allCountries.txt").toOSString()));
		try {
			
//				ObjectOutputStream os=new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(osString)));
//				os.writeObject(geoIndex);
//				os.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static enum FeatureClass {
		ADM,
		PPL;
		
		//there are other classes, but those are excluded from the index at the moment
		
		public static FeatureClass fromString(String classStr) {
			if ("A".equals(classStr)) {
				return ADM;
			} else if ("P".equals(classStr)) {
				return PPL;
			}
			
			return null;
		}
	}
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static class GeoPlace implements Serializable{
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private Object names;
		private Object stemmedNames;
		private Collection<GeoPlace> children;
		private int id; //actually, no use for this at the moment.
		private int population = -1;
		private FeatureClass featureClass;
		//private GeoPlace parent;
		private GeoPlace parent;
		
		public GeoPlace(int id) {
			this.id = id;
		}
		
		public Collection<String> getNames() {
			if (names == null) {
				return Collections.emptyList();
			} else if (names instanceof String) {
				ArrayList<String> arrayList = new ArrayList<String>();
				arrayList.add((String) names);
				return arrayList;
			} else {
				return ((Collection<String>) names);
			}
		}
		
		public Collection<String> getStemmedNames() {
			if (stemmedNames == null) {
				return Collections.emptyList();
			} else if (stemmedNames instanceof String) {
				ArrayList<String> arrayList = new ArrayList<String>();
				arrayList.add((String) stemmedNames);
				return arrayList;				
			} else {
				return ((List<String>) stemmedNames);
			}
		}
		
		public void addName(String name) {
			if (this.names == null) {
				names = name;
			} else if (this.names instanceof String) {
				String oldName = (String) this.names;
				if (!oldName.equals(name)) {
					List<String> namesList = new ArrayList<String>(2);
					namesList.add(oldName);
					namesList.add(name);
					this.names = namesList;
				}
			} else if (this.names instanceof List) {
				List<String> list = ((List<String>) names);
				if (!list.contains(name)) {
					list.add(name);
				}
			}
		}
		
		public void addStemmedName(String name) {
			if (this.stemmedNames == null) {
				stemmedNames = name;
			} else if (this.stemmedNames instanceof String) {
				String oldName = (String) this.stemmedNames;
				if (!oldName.equals(name)) {
					List<String> namesList = new ArrayList<String>(2);
					namesList.add(oldName);
					namesList.add(name);
					this.stemmedNames = namesList;
				}
			} else if (this.stemmedNames instanceof List) {
				List<String> list = ((List<String>) stemmedNames);
				if (!list.contains(name)) {
					list.add(name);
				}
			}
		}
		
		public Collection<GeoPlace> getUnmodifiableChildren() {
			if (children == null) {
				return Collections.emptyList();
			} else {
				return (Collection<GeoPlace>) children; 
			}
		}
		
		public void addChild(GeoPlace child) {
			if (children == null) {
				children = new ArrayList<GeoIndex.GeoPlace>();
			}
			
			children.add(child);
		}

		public int getPopulation() {
			return population;
		}

		void setPopulation(int population) {
			this.population = population;
		}

		public FeatureClass getFeatureClass() {
			return featureClass;
		}

		void setFeatureClass(FeatureClass featureClass) {
			this.featureClass = featureClass;
		}

		public GeoPlace getParent() {
			return parent;
		}

		void setParent(GeoPlace parent) {
			this.parent = parent;
		}
	}
	
	
	
	public void registerGeoName(int id, String name, FeatureClass featureClass, int population) {
		GeoPlace info = getOrRegisterInfo(id);
		info.addName(name);
		
		if (featureClass != null) {
			info.setFeatureClass(featureClass);
		}
		
		if (population != 0) {
			info.setPopulation(population);
		}
		
		String nameRoot = Porter.stem(name);
		if (nameRoot != null && nameRoot.length() > 3) {
			info.addStemmedName(nameRoot);
			registerNameInNamesToNodes(nameRoot, info);
		}
		
		registerNameInNamesToNodes(name, info);
	}

	private void registerNameInNamesToNodes(String name, GeoPlace info) {
		Object currentInfos = namesToNodes.get(name);
		if (currentInfos == null) {
			namesToNodes.put(name, info);
		} else if (currentInfos instanceof GeoPlace) {
			List<GeoPlace> list = new ArrayList<GeoIndex.GeoPlace>(2);
			list.add((GeoPlace) currentInfos);
			list.add(info);
			namesToNodes.put(name, list);
		} else if (currentInfos instanceof List) {
			List<GeoPlace> list = (List<GeoPlace>) currentInfos;
			list.add(info);
		}
	}
	
	public void registerHierarchy(int parent, int child) {
		GeoPlace parentInfo = getOrRegisterInfo(parent);
		GeoPlace childInfo = getOrRegisterInfo(child);
		
		parentInfo.addChild(childInfo);
		childInfo.setParent(parentInfo);
	}
	
	public boolean contains(String geoName) {
		return namesToNodes.get(geoName) != null; 
	}
	
	/**
	 * Gets id by name. Returns null if no id was found. Returns the first id found if several match.
	 * @param geoName
	 * @return
	 */
	
	public int[] getIdsByName(String geoName) {
		Object mappedInfos = namesToNodes.get(geoName);
		
		if (mappedInfos == null) {
			return INT_ARRAY_LIST;
		} else if (mappedInfos instanceof GeoPlace) {
			return new int[]{(((GeoPlace) mappedInfos).id)};
		} else if (mappedInfos instanceof List) {
			List<GeoPlace> list = ((List<GeoPlace>) mappedInfos);
			IntOpenHashSetSerializable set=new IntOpenHashSetSerializable();
			for (GeoPlace geoInfo : list) {
				set.add(geoInfo.id);								
			}
			return set.toArray();
		}
		
		return INT_ARRAY_LIST;
	}
	
	public Collection<String> getNamesById(int id) {
		GeoPlace node = idsToNodes.get(id);
		if (node == null) {
			return Collections.emptyList();
		}
		
		return node.getNames();
	}
	
	public Collection<String> getStemmedNamesById(int id) {
		GeoPlace node = idsToNodes.get(id);
		if (node == null) {
			return Collections.emptyList();
		}
		
		return node.getStemmedNames();
	}
	
	/**
	 * If current id is registered, returns a list containing this id and all of its children ids recursivelly
	 * @param id
	 * @return
	 */
	public List<Integer> getAllChildren(int id) {
		List<Integer> result = new ArrayList<Integer>();
		
		GeoPlace node = idsToNodes.get(id);
		if (node != null) {
			addAllIdsrecursivelly(node, result);
		}
		
		return result;
	}
	
	public Collection<String> getAllAlternativeAndChildNames(String geoName) {
		Object mappedInfos = namesToNodes.get(geoName);
		
		List<GeoPlace> infosList = null;
		if (mappedInfos == null) {
			return Collections.emptyList();
		} else if (mappedInfos instanceof GeoPlace) {
			infosList = Lists.newArrayList((GeoPlace) mappedInfos);
		} else if (mappedInfos instanceof List) {
			infosList = (List<GeoPlace>) mappedInfos;
		}
		
		
		Set<String> result = new HashSet<String>();
		Set<GeoPlace> visited = new HashSet<GeoIndex.GeoPlace>();
		
		for (GeoPlace info : infosList) {
			addAllNamesRecursivelly(info, result, visited);
		}
		
		return result;
	}
	
	private final void addAllNamesRecursivelly(GeoPlace info, Collection<String> namesToCollect, Collection<GeoPlace> visited) {
		if (visited.contains(info)) {
			return;
		}
		visited.add(info);
		
		namesToCollect.addAll(info.getNames());
		
		for (GeoPlace child : info.getUnmodifiableChildren()) {
			addAllNamesRecursivelly(child, namesToCollect, visited);
		}
	}
	
	/**
	 * 
	 * @param geoName
	 * @param minChildPopulation - null means no population limit 
	 * @return
	 */
	public IntOpenHashSetSerializable getAllAlternativeAndChildGeoIds(String geoName, Integer minChildPopulation) {
		Object mappedInfos = namesToNodes.get(geoName);
		
		List<GeoPlace> infosList = null;
		if (mappedInfos == null) {
			return new IntOpenHashSetSerializable();
		} else if (mappedInfos instanceof GeoPlace) {
			infosList = Lists.newArrayList((GeoPlace) mappedInfos);
		} else if (mappedInfos instanceof List) {
			infosList = (List<GeoPlace>) mappedInfos;
		}
		
		
		IntOpenHashSetSerializable result = new IntOpenHashSetSerializable();
		Set<GeoPlace> visited = new HashSet<GeoIndex.GeoPlace>();
		
		for (GeoPlace info : infosList) {
			addAllGeoIdsRecursivelly(info, result, visited, minChildPopulation, 0);
		}
		
		return result;
	}
	
	private final void addAllGeoIdsRecursivelly(GeoPlace info, IntOpenHashSetSerializable namesToCollect, Collection<GeoPlace> visited, Integer minChildPopulation, int recursionLevel) {
		if (visited.contains(info)) {
			return;
		}
		visited.add(info);
		
		if (minChildPopulation != null && recursionLevel > 0) {
			if ((info.getFeatureClass() == FeatureClass.ADM && info.getPopulation() > 0) || info.getFeatureClass() == FeatureClass.PPL) {
				if (info.getPopulation() < minChildPopulation) {
					return;
				}
			}
		}
		
		namesToCollect.add(info.id);
		
		for (GeoPlace child : info.getUnmodifiableChildren()) {
			addAllGeoIdsRecursivelly(child, namesToCollect, visited, minChildPopulation, recursionLevel+1);
		}
	}
	
	public final GeoPlace getFullInfoForID(int id) {
		return idsToNodes.get(id);
	}
	
	private final GeoPlace getOrRegisterInfo(int id) {
		GeoPlace currentInfo = idsToNodes.get(id);
		if (currentInfo == null) {
			currentInfo = new GeoPlace(id);
			idsToNodes.put(id, currentInfo);
		}
		
		return currentInfo;
	}
	
	private void addAllIdsrecursivelly(GeoPlace node, List<Integer> idsToCollect) {
		idsToCollect.add(node.id);
		
		if (node.children != null) {
			for (GeoPlace child : node.children) {
				addAllIdsrecursivelly(child, idsToCollect);
			}
		}
	}
}
