package com.onpositive.semantic.wikipedia2.docclasses;

import java.io.Serializable;
import java.util.HashMap;

import com.carrotsearch.hppc.IntOpenHashSetSerializable;

public class DocumentClasses implements Serializable{

	public static final String METACLASS_TECH="TECH";
	public static final String METACLASS_CULTURE="MEDIA";
	public static final String METACLASS_SPORT="SPORT";
	private static final String METACLASS_COMPUTERS = "COMPUTERS";
	private static final String METACLASS_OUT = "OUT";
	private static final String METACLASS_BIOLOGY = "OUT";
	
	public static HashMap<String, String[]>metaClassMappings=new HashMap<String, String[]>();
	
		
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final String PERSONCLASS="PERSON";
	public static final String LOCATIONCLASS="LOCATION";
	public static final String ORGANIZATIONCLASS="ORGANIZATION";
	public static final String SPIECESCLASS="SPIECES";
	public static final String SUBSTANCECLASS="SUBSTANCE";
	public static final String LISTCLASS="LIST";
	public static final String MUSICGROUPCLASS="MUSICGROUP";
	public static final String MUSICALBUMCLASS="MUSICALBUM";
	public static final String FILMCLASS="FILM";
	public static final String SINGLECLASS="SINGLE";
	public static final String VIDEOGAMES="VIDEOGAMES";
	public static final String SOFTWAREENTITY="PROGRAMM";
	protected static final String BOOKCLASS = "BOOK";
	public static final String EVENTCLASS = "EVENT";
	public static final String ASTROCLASS = "ASTEROOBJECT";
	public static final String REDIRECTCLASS = "REDIRECT";
	public static final String MATCHCLASS = "MATCH";
	public static final String SPORTCOMMAND = "SPORTOMMAND";
	
	
	
	public static final String SPACE_DEVICE = "SPACE_DEVICE";
	public static final String MILITARY_CONFLICT = "MILITARY_CONFLICT";
	public static final String MILITARY_UNIT = "MILITARY_UNIT";
	public static final String SIGHTSEEING = "SIGHTSEEING";
	public static final String RELIGIOUSBUILDING = "RELIGION_BUILDING";
	public static final String PRESS= "PRESS";
	public static final String DECEASECLASS = "DECEASE";
	public static final String CULTUREENTITY = "CULTUREENTITY";
	public static final String TECHDEVICE = "TECHDEVICE";
	public static final String TREATMENT = "TREATMENT";
	public static final String AIRCRAFT_CRASH = "AIRCRAFT_CRASH";
	
	
	protected static final String SHIPCLASS = "SHIP";
	protected static final String SHIP = "SHIP";
	protected static final String RAIL = "RAIL";
	protected static final String AUTO = "AUTO";
	protected static final String AIRSHIP = "AIRSHIP";
	protected static final String TANK = "TANK";
	protected static final String WEAPON = "WEAPON";
	
	
	
	static{
		metaClassMappings.put(METACLASS_TECH, new String[]{
				SPACE_DEVICE,
				TECHDEVICE,
				SHIPCLASS,
				RAIL,
				AUTO,
				AIRSHIP,
				TANK,
				WEAPON
		});
		metaClassMappings.put(METACLASS_CULTURE, new String[]{
				FILMCLASS,
				SINGLECLASS,
				MUSICALBUMCLASS,
				BOOKCLASS,
				CULTUREENTITY
		});
		metaClassMappings.put(METACLASS_SPORT, new String[]{
				SPORTCOMMAND,
				MATCHCLASS
		});
		metaClassMappings.put(METACLASS_COMPUTERS, new String[]{
				VIDEOGAMES,
				SOFTWAREENTITY
		});
		metaClassMappings.put(METACLASS_OUT, new String[]{
				LISTCLASS,
				ORGANIZATIONCLASS
		});
		metaClassMappings.put(METACLASS_BIOLOGY, new String[]{
				SPIECESCLASS,				
		});
	}

	
	protected HashMap<String, IntOpenHashSetSerializable>documentsByClass=new HashMap<String, IntOpenHashSetSerializable>();
	protected IntOpenHashSetSerializable classified=new IntOpenHashSetSerializable();

	public void markClass(String str,int document){
		IntOpenHashSetSerializable intOpenHashSet = documentsByClass.get(str);
		if (intOpenHashSet==null){
			intOpenHashSet=new IntOpenHashSetSerializable();
			documentsByClass.put(str, intOpenHashSet);
		}
		intOpenHashSet.add(document);
		classified.add(document);
	}
	
	public boolean hasClass(int document){
		return classified.contains(document);
	}
	
	public boolean hasClass(int document,String entityClass){
		IntOpenHashSetSerializable intOpenHashSetSerializable = documentsByClass.get(entityClass);
		if (intOpenHashSetSerializable!=null){
			return intOpenHashSetSerializable.contains(document);
		}
		return false;
	}
}
