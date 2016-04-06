package com.onpositive.semantic.wikipedia2.locations;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import com.carrotsearch.hppc.IntObjectOpenHashMapSerialzable;
import com.onpositive.semantic.wikipedia2.locations.GeoIndex.FeatureClass;


public class GeoIndexBuilder {
	
	private static final int MIN_POPULATION = 15000;

	private static class LocationData {
		public FeatureClass featureClass;
		public int population;
	}
	
	public static GeoIndex build(File alternateNamesFile, File hierarchyFile, File citiesFile,
			File allCountriesFile) throws FileNotFoundException {
		long startTime = System.currentTimeMillis();
		GeoIndex index = new GeoIndex();
		
		IntObjectOpenHashMapSerialzable<LocationData> locationDatas = parseAllCountries(allCountriesFile);
		
		System.out.println("Picked up " + locationDatas.size() + " locations from allCountries");
		
		addNames(index, alternateNamesFile, locationDatas);
		
		addHierarchy(index, hierarchyFile, locationDatas);
		
		addCitiesHierarchy(index, citiesFile, locationDatas);
		
		long endTime = System.currentTimeMillis();
		System.out.println("Geo index build time: " + (endTime-startTime));
		return index;
	}

	private static IntObjectOpenHashMapSerialzable<LocationData> parseAllCountries(
			File allCountriesFile) throws FileNotFoundException {
		if (!allCountriesFile.exists()) {
			return null;
		}

		
		BufferedReader reader = null;
		try {
			try {
				reader = new BufferedReader(
						new InputStreamReader(new FileInputStream(allCountriesFile), "UTF-8"));
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
				return null;
			}
			
			IntObjectOpenHashMapSerialzable<LocationData> result = new IntObjectOpenHashMapSerialzable<GeoIndexBuilder.LocationData>();
			
			String line;
			try {
				line = reader.readLine();
			
				while (line != null) {
					try {
						String[] components = line.split("\\t");
						
						if (components.length < 6) {
							continue;
						}
						
						int geoId = -1;
						try {
							geoId = Integer.parseInt(components[0]);
						} catch (Throwable th) {}
						
						if (geoId == -1) {
							continue;
						}
						
						String featureClassStr = components[6];
						FeatureClass featureClass = FeatureClass.fromString(featureClassStr);
						if (featureClass == null) {
							continue;
						}
						
						int population = 0;
						if (components.length >= 14) {
							String populationStr = components[14];
							if (populationStr != null) {
								try {
									population = Integer.parseInt(populationStr);
								} catch (NumberFormatException ex) {}
							}
						}
						
						LocationData data = new LocationData();
						data.featureClass = featureClass;
						data.population = population;
						
						result.put(geoId, data);
						
					} finally {
						line = reader.readLine();
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			return result;
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private static void addCitiesHierarchy(GeoIndex index, File citiesFile,
			IntObjectOpenHashMapSerialzable<LocationData> locationDatas) throws FileNotFoundException {
		if (!citiesFile.exists()) {
			return;
		}
		
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(
					new InputStreamReader(new FileInputStream(citiesFile), "UTF-8"));
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		String line;
		try {
			line = reader.readLine();
		
			while (line != null) {
				try {
					String[] components = line.split("\\t");
					if (components.length < 11) {
						continue;
					}
					
					String parentIndexStr = components[11];
					String childIndexStr = components[0];
					
					int parentIndex = 0;
					int childIndex = 0;
					try {
						parentIndex = Integer.parseInt(parentIndexStr);
						childIndex = Integer.parseInt(childIndexStr);
					} catch (Throwable th) {
						continue;
					}
					
					if (!validateLocation(parentIndex, locationDatas)) {
						continue;
					}
					
					if (!validateLocation(childIndex, locationDatas)) {
						continue;
					}
					
					index.registerHierarchy(parentIndex, childIndex);
					
				} finally {
					line = reader.readLine();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void addHierarchy(GeoIndex index, File hierarchyFile,
			IntObjectOpenHashMapSerialzable<LocationData> locationDatas) throws FileNotFoundException {
		if (!hierarchyFile.exists()) {
			return;
		}
		
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(
					new InputStreamReader(new FileInputStream(hierarchyFile), "UTF-8"));
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		String line;
		try {
			line = reader.readLine();
		
			while (line != null) {
				try {
					String[] components = line.split("\\t");
					if (components.length < 2) {
						continue;
					}
					
					String parentIndexStr = components[0];
					String childIndexStr = components[1];
					
					int parentIndex = 0;
					int childIndex = 0;
					try {
						parentIndex = Integer.parseInt(parentIndexStr);
						childIndex = Integer.parseInt(childIndexStr);
					} catch (Throwable th) {
						continue;
					}
					
					if (!validateLocation(parentIndex, locationDatas)) {
						continue;
					}
					
					if (!validateLocation(childIndex, locationDatas)) {
						continue;
					}
					
					index.registerHierarchy(parentIndex, childIndex);
					
				} finally {
					line = reader.readLine();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void addNames(GeoIndex index, File alternateNamesFile, 
			IntObjectOpenHashMapSerialzable<LocationData> locationDatas) throws FileNotFoundException {
		if (!alternateNamesFile.exists()) {
			return;
		}
		
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(
					new InputStreamReader(new FileInputStream(alternateNamesFile), "UTF-8"));
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		String line;
		try {
			line = reader.readLine();
		
			while (line != null) {
				try {
					String[] components = line.split("\\t");
					if (components.length < 4) {
						continue;
					}
					
					String languageCode = components[2];
					if (!"ru".equals(languageCode)) {
						continue;
					}
					
					String geoIdStr = components[1];
					int geoId = -1;
					try {
						geoId = Integer.parseInt(geoIdStr);
					} catch (Throwable th) {
						continue;
					}
					
					if (geoId == -1) {
						continue;
					}
					
					if (!validateLocation(geoId, locationDatas)) {
						continue;
					}
					
					String actualName = components[3];
					if (actualName == null) {
						continue;
					}
					
					actualName = actualName.toLowerCase();
					
					
					LocationData geoLocationData = locationDatas.get(geoId);
					if (geoLocationData == null) {
						continue;
					}
					
					index.registerGeoName(geoId, actualName, geoLocationData.featureClass, geoLocationData.population);
				} finally {
					line = reader.readLine();
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("blah");
	}
	
	private static boolean validateLocation(int locationId, IntObjectOpenHashMapSerialzable<LocationData> locationDatas) {
		if (locationDatas == null) {
			return true;
		}
		
		LocationData geoLocationData = locationDatas.get(locationId);
		if (geoLocationData == null) {
			return false;
		}
		
		if (geoLocationData.featureClass == null) {
			return false;
		}
		
		if (geoLocationData.featureClass == FeatureClass.PPL && (geoLocationData.population == 0 || 
				geoLocationData.population < MIN_POPULATION)) {
			//ignoring small towns and villages
			return false;
		}
		return true;
	}
}
