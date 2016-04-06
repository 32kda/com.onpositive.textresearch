package com.onpositive.semantic.wikipedia2.locations;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.IntIntOpenHashMap;
import com.carrotsearch.hppc.IntObjectOpenHashMapSerialzable;
import com.carrotsearch.hppc.IntOpenHashSetSerializable;
import com.carrotsearch.hppc.cursors.IntCursor;
import com.onpositive.compactdata.IntToByteArrayMap;
import com.onpositive.compactdata.IntToIntArrayMap;
import com.onpositive.semantic.wikipedia2.WikiEngine2;

public class DocumentGeoIndex {
	
	private IntToIntArrayMap idToNames = new IntToIntArrayMap();
	private IntToByteArrayMap idToParts = new IntToByteArrayMap();
	private IntToIntArrayMap idToPositions = new IntToIntArrayMap();
	private IntToByteArrayMap idToMatchTypes = new IntToByteArrayMap();
	
	protected static HashMap<WikiEngine2, DocumentGeoIndex>indexes=new HashMap<WikiEngine2, DocumentGeoIndex>();
	
	public static DocumentGeoIndex getInstance(WikiEngine2 eng){
		DocumentGeoIndex documentGeoIndex = indexes.get(eng);
		if (documentGeoIndex!=null){
			return documentGeoIndex;
		}
		DocumentGeoIndex loadDocumentGeoIndex = loadDocumentGeoIndex(eng);
		indexes.put(eng, loadDocumentGeoIndex);
		return loadDocumentGeoIndex;		
	}
	
	private static DocumentGeoIndex loadDocumentGeoIndex(WikiEngine2 eng) {
		String dir=eng.getLocation();
		File file = new File(dir, DocumentGeoIndexBuilder.DOCUMENT_GEO_INDEX_FILE_NAME);
		DocumentGeoIndex documentGeoIndex;
		if (file.exists()){
			documentGeoIndex = new DocumentGeoIndex();
			documentGeoIndex.load(file);
		} else {
			documentGeoIndex = buildDocumentGeoIndex(eng);
			documentGeoIndex.save(file);
		}
		return documentGeoIndex;
	}
	
	private static DocumentGeoIndex buildDocumentGeoIndex(WikiEngine2 engine) {
		GeoIndex index = GeoIndex.getIndex();
		DocumentGeoIndexBuilder builder = new DocumentGeoIndexBuilder(engine, index);
		DocumentGeoIndex result = builder.build();
		
		return result;
	}
	
	private IntObjectOpenHashMapSerialzable<Object> namesToIds = new IntObjectOpenHashMapSerialzable<Object>();
	
	public static enum DocumentGeoMatchType {
		UPPERCASED_DIRECT, //SAMPLE: ������
		UPPERCASED_STEMMED,//SAMPLE: ������
		PREPOSITIONED,//SAMPLE: � ������, �� �����, �� ������ 
		DIRECT,//SAMPLE: ������
		STEMMED;//SAMPLE: ������
		
		public final byte toByte() {
			return (byte)(this.ordinal() - 128);
		}
		
		public static DocumentGeoMatchType fromByte(byte bt) {
			int ordinal = ((int) bt)+128;
			return DocumentGeoMatchType.values()[ordinal];
		}
	}
	
	public static class DocumentGeoMatch {
		private int geoId;
		private DocumentPart documentPart;
		private int position;
		private DocumentGeoMatchType matchType;
		
		public DocumentGeoMatch(int geoId, DocumentPart documentPart,
				int position, DocumentGeoMatchType matchType) {
			this.geoId = geoId;
			this.documentPart = documentPart;
			this.position = position;
			this.matchType = matchType;
		}
		
		public int getGeoId() {
			return geoId;
		}
		public void setGeoId(int geoId) {
			this.geoId = geoId;
		}
		public DocumentPart getDocumentPart() {
			return documentPart;
		}
		public void setDocumentPart(DocumentPart documentPart) {
			this.documentPart = documentPart;
		}
		public int getPosition() {
			return position;
		}
		public void setPosition(int position) {
			this.position = position;
		}
		public DocumentGeoMatchType getMatchType() {
			return matchType;
		}
		public void setMatchType(DocumentGeoMatchType matchType) {
			this.matchType = matchType;
		}
		
	}
	
	public DocumentGeoIndex() {
	}
	
	/**
	 * Nulls are not acceptable as a members of lists
	 */
	public void registerData(int documentId, List<DocumentGeoMatch> matches) {
		if (matches == null || matches.size() == 0) {
			return;
		}
		
		final int maxLength = 256;
		int length = matches.size() < maxLength ? matches.size() : maxLength;
		
		int[] geoNamesArray = new int[length];
		for (int i = 0; i < length; i++) {
			geoNamesArray[i] = matches.get(i).getGeoId();
			addDocumentToGeoName(geoNamesArray[i], documentId);
		}
		idToNames.add(documentId, geoNamesArray);
		
		byte[] partsArray = new byte[length];
		for (int i = 0; i < length; i++) {
			partsArray[i] = matches.get(i).getDocumentPart().toByte();
		}
		idToParts.add(documentId, partsArray);
		
		int[] positionsArray = new int[length];
		for (int i = 0; i < length; i++) {
			positionsArray[i] = matches.get(i).getPosition();
		}
		idToPositions.add(documentId, positionsArray);
		
		byte[] matchTypeArray = new byte[length];
		for (int i = 0; i < length; i++) {
			matchTypeArray[i] = matches.get(i).getMatchType().toByte();
		}
		idToMatchTypes.add(documentId, matchTypeArray);
	}
	
	public List<String> getGeoNames(int documentId, GeoIndex geoIndex) {
		
		List<String> result = new ArrayList<String>();
		for (Integer geoName : getGeoNames(documentId)) {
			result.addAll(geoIndex.getNamesById(geoName));
		}
		
		return result;
	}
	
	public List<Integer> getGeoNames(int documentId) {
		int[] names = idToNames.get(documentId);
		if (names == null) {
			return Collections.emptyList();
		}
		
		List<Integer> result = new ArrayList<Integer>(names.length);
		for (int i = 0; i < names.length; i++) {
			result.add(names[i]);
		}
		
		return result;
	}
	
	public List<DocumentGeoMatch> getGeoMatches(int documentId) {
		return getGeoMatches(documentId, null);
	}
	
	/**
	 * Gets goe matches for the document, if filter is specified (not null, only return matches contained in the filter set). 
	 * @param documentId
	 * @param filter
	 * @return
	 */
	public List<DocumentGeoMatch> getGeoMatches(int documentId, IntOpenHashSetSerializable filter) {
		int[] names = idToNames.get(documentId);
		byte[] parts = idToParts.get(documentId);
		int[] positions = idToPositions.get(documentId);
		byte[] matchTypes = idToMatchTypes.get(documentId);
		
		if (names == null || names.length == 0) {
			return Collections.emptyList();
		}
		
		List<DocumentGeoMatch> result = null;
		for (int i = 0; i < names.length; i++) {
			if (filter != null && !filter.contains(names[i])) {
				continue;
			}
			
			DocumentPart part = DocumentPart.fromByte(parts[i]);
			DocumentGeoMatchType matchType = DocumentGeoMatchType.fromByte(matchTypes[i]);
			DocumentGeoMatch match = new DocumentGeoMatch(names[i], part, positions[i], matchType);
			
			if (result == null) {
				result = new ArrayList<DocumentGeoIndex.DocumentGeoMatch>(names.length);
			}
			
			result.add(match);
		}
		
		if (result == null) {
			return Collections.emptyList();
		}
		
		return result; 
	}
	
	public int[] findDocuments(int geoName) {
		int[] documentIds = getDocumentsByGeoName(geoName);
		
	
		
		return documentIds;
	}
	
	public IntArrayList findAllDocuments(int geoNameId, GeoIndex geoIndex) {
		List<Integer> geoNames = geoIndex.getAllChildren(geoNameId);
		IntArrayList result = new IntArrayList();
		
		for (Integer geoName : geoNames) {
			int[] documentIds = getDocumentsByGeoName(geoName);
			
			for (int i = 0; i < documentIds.length; i++) {
				result.add(documentIds[i]);
			}
		}
		
		return result;
	}
	
	public IntOpenHashSetSerializable findAllDocuments(String geoNameStr) {
		String geoName = geoNameStr.trim().toLowerCase();//Porter.stem(geoNameStr);
		
		GeoIndex geoIndex=GeoIndex.getIndex();
		int[] geoNameIds = geoIndex.getIdsByName(geoName);
		
		IntOpenHashSetSerializable result = new IntOpenHashSetSerializable();
		for (int geoNameId : geoNameIds) {
			result.addAll(findAllDocuments(geoNameId, geoIndex));
		}
		
		return result;
	}
	
	public IntOpenHashSetSerializable findDocuments(String geoName, GeoIndex geoIndex) {
		int[] geoNameIds = geoIndex.getIdsByName(geoName);
		
		IntOpenHashSetSerializable result = new IntOpenHashSetSerializable();
		for (int geoNameId : geoNameIds) {
			int[] findDocuments = findDocuments(geoNameId);
			for (int q:findDocuments){
				result.add(q);
			}
			//result.addAll(findDocuments);
		}

		return result;
	}
 
	public void load(File file) {
		DataInputStream is;
		try {
			is = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
			idToNames.read(is);
			idToParts.read(is);
			idToPositions.read(is);
			idToMatchTypes.read(is);
			loadNamesToIds(is);
			is.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void save(File file) {
		DataOutputStream objectOutputStream;
		try {
			objectOutputStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
			idToNames.store(objectOutputStream);
			idToParts.store(objectOutputStream);
			idToPositions.store(objectOutputStream);
			idToMatchTypes.store(objectOutputStream);
			
			storeNamesToIds(objectOutputStream);
			objectOutputStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private final int[] getDocumentsByGeoName(int nameId) {
		Object documentIdsObject = namesToIds.get(nameId);
		if (documentIdsObject instanceof Integer) {
			return new int[]{(Integer)documentIdsObject};
		} else if (documentIdsObject instanceof int[]) {
			return (int[]) documentIdsObject;
		}
		
		return new int[]{};
	}
	
	public final IntOpenHashSetSerializable getDocumentsByGeoNames(IntOpenHashSetSerializable names){
		IntOpenHashSetSerializable set=new IntOpenHashSetSerializable(100000);
		for (int a=0;a<names.keys.length;a++){
			boolean b=names.allocated[a];
			if (b){
				int key=names.keys[a];
				Object documentIdsObject = namesToIds.get(key);
				if (documentIdsObject instanceof Integer) {
					Integer documentIdsObject2 = (Integer)documentIdsObject;
					set.add(documentIdsObject2);
				} else if (documentIdsObject instanceof int[]) {
					int[]all=(int[]) documentIdsObject;
					set.add(all);
				}				
			}
		}
		return set;		
	}
	
	
	
	private final void addDocumentToGeoName(int nameId, int documentId) {
		Object documentIdsObject = namesToIds.get(nameId);
		if (documentIdsObject == null) {
			namesToIds.put(nameId, documentId);
		}
		else if (documentIdsObject instanceof Integer) {
			int oldDocumentId = (Integer)documentIdsObject;
			if (oldDocumentId != documentId) {
				//only adding a different value
				int[] toPut = new int[]{oldDocumentId, documentId};
				namesToIds.put(nameId, toPut);
			}
		} else if (documentIdsObject instanceof int[]) {
			int[] oldDocumentIds = (int[]) documentIdsObject;
			boolean contains = false;
			for (int i = 0; i < oldDocumentIds.length; i++) {
				if (oldDocumentIds[i] == documentId) {
					contains = true;
					break;
				}
			}
			
			if (!contains) {
				//only adding a different value
				int[] newDocumentIds = Arrays.copyOf(oldDocumentIds, oldDocumentIds.length+1);
				newDocumentIds[newDocumentIds.length - 1] = documentId;
				
				namesToIds.put(nameId, newDocumentIds);
			}
		}
	}
	
	private void loadNamesToIds(DataInputStream is) throws IOException {
		int size = is.readInt();
		for (int i = 0; i < size; i++) {
			int geoName = is.readInt();
			int documentIdsLength = is.readInt();
			int[] documentIds = new int[documentIdsLength];
			for (int j = 0; j < documentIdsLength; j++) {
				documentIds[j] = is.readInt();
			}
			
			if (documentIdsLength == 1) {
				addDocumentToGeoName(geoName, documentIds[0]);
			} else {
				namesToIds.put(geoName, documentIds);
			}
		}
	}
	
	private void storeNamesToIds(DataOutputStream objectOutputStream) throws IOException {
		objectOutputStream.writeInt(namesToIds.size());
		Iterator<IntCursor> iterator = namesToIds.keys().iterator();
		while (iterator.hasNext()) {
			int geoName = iterator.next().value;
			int[] documentIds = getDocumentsByGeoName(geoName);
			if (documentIds.length > 0) {
				
				objectOutputStream.writeInt(geoName);
				objectOutputStream.writeInt(documentIds.length);
				for (int i = 0; i < documentIds.length; i++) {
					objectOutputStream.writeInt(documentIds[i]);
				}
			}
		}
	}
}