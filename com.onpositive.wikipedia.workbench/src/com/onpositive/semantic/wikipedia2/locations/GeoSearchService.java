package com.onpositive.semantic.wikipedia2.locations;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import com.carrotsearch.hppc.IntOpenHashSetSerializable;
import com.onpositive.semantic.wikipedia2.WikiEngine2;
import com.onpositive.semantic.wikipedia2.WikiEngineService;
import com.onpositive.semantic.wikipedia2.locations.DocumentGeoIndex.DocumentGeoMatch;

public class GeoSearchService extends WikiEngineService{

	protected DocumentGeoIndex index;
	
	public GeoSearchService(WikiEngine2 engine) {
		super(engine);
		index=DocumentGeoIndex.getInstance(engine);
	}

	@Override
	protected void doLoad(File fl) throws IOException {
		
	}

	@Override
	protected void build(WikiEngine2 enfine) {
		
	}

	@Override
	protected void doSave(File fl) throws IOException {
		
	}
	protected LinkedHashMap<String, IntOpenHashSetSerializable>locationCache=new LinkedHashMap<String, IntOpenHashSetSerializable>();
	
	public IntOpenHashSetSerializable getAllDocumentsWithLocation(String locationStr){
		if (locationCache.containsKey(locationStr)){
			return locationCache.get(locationStr);
		}
		IntOpenHashSetSerializable locationIdsFromString = getLocationIdsFromString(locationStr);
		IntOpenHashSetSerializable documentsByGeoNames = index.getDocumentsByGeoNames(locationIdsFromString);
		if (locationCache.size()>100){
			locationCache.remove(locationCache.keySet().iterator().next());
		}
		locationCache.put(locationStr, documentsByGeoNames);
		return documentsByGeoNames;
	}
	
	
	private IntOpenHashSetSerializable getLocationIdsFromString(String locationStr) {
		return GeoIndex.getIndex().getAllAlternativeAndChildGeoIds(locationStr.toLowerCase(), 500000);		
	} 

	@Override
	public String getFileName() {
		return "doc_geo_names_v7.index";
	}

	public List<DocumentGeoMatch> getMatches(int id, String location) {
		IntOpenHashSetSerializable locationIdsFromString = getLocationIdsFromString(location);
		List<DocumentGeoMatch> geoMatches = index.getGeoMatches(id, locationIdsFromString);
		return geoMatches;
	}

	public List<DocumentGeoMatch> getMatches(int id) {
		List<DocumentGeoMatch> geoMatches = index.getGeoMatches(id, null);
		return geoMatches;
	}
}
