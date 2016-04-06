package com.onpositive.text.analysis.lexic.dimension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;

import com.carrotsearch.hppc.IntLookupContainer;
import com.carrotsearch.hppc.IntObjectOpenHashMapSerialzable;
import com.carrotsearch.hppc.IntOpenHashSet;
import com.onpositive.semantic.wordnet.AbstractWordNet;
import com.onpositive.semantic.wordnet.GrammarRelation;
import com.onpositive.semantic.wordnet.MeaningElement;
import com.onpositive.semantic.wordnet.SemanticRelation;
import com.onpositive.semantic.wordnet.TextElement;
import com.onpositive.semantic.words3.MetaLayer;
import com.onpositive.semantic.words3.hds.IntArrayList;

public class UnitsProvider {
	
	private static final String UNIT_KIND_PREFIX = "_UNITS_";

	private static HashMap<String,Integer> scalePrefixMap = new HashMap<String, Integer>();
	
	private static int[] scalePrefixLengths;
	
	static{
		scalePrefixMap.put("дека", 1);
		scalePrefixMap.put("гекто", 2);
		scalePrefixMap.put("кило", 3);
		scalePrefixMap.put("мега", 6);
		scalePrefixMap.put("гига", 9);
		scalePrefixMap.put("тера", 12);
		scalePrefixMap.put("пета", 15);
		scalePrefixMap.put("экса", 18);
		scalePrefixMap.put("зетта", 21);
		scalePrefixMap.put("иотта", 24);

		scalePrefixMap.put("деци", -1);
		scalePrefixMap.put("санти", -2);
		scalePrefixMap.put("милли", -3);
		scalePrefixMap.put("микро", -6);
		scalePrefixMap.put("нано", -9);
		scalePrefixMap.put("пико", -12);
		scalePrefixMap.put("фемто", -15);
		scalePrefixMap.put("атто", -18);
		scalePrefixMap.put("зепто", -21);
		scalePrefixMap.put("иокто", -24);
		
		IntOpenHashSet list = new IntOpenHashSet(); 
		for(String str : scalePrefixMap.keySet()){
			list.add(str.length());
		}
		scalePrefixLengths = list.toArray();
		Arrays.sort(scalePrefixLengths);
	}
	
	public static String detectScalePrefix(String value) {
		
		for(int prefLength : scalePrefixLengths){
			if(value.length()<=prefLength){
				break;
			}
			String pref = value.substring(0,prefLength);
			if(scalePrefixMap.containsKey(pref)){
				return pref;
			}
		}
		return null;
	}
	
	private HashMap<UnitKind,Unit> primaryUnits;
	
	
	private HashMap<UnitKind,List<Unit>> unitsCache;
	
	
	public static Integer getScale(String scalePrefix) {
		
		return scalePrefixMap.get(scalePrefix);
	}
	
	public UnitsProvider(AbstractWordNet wordNet) {
		this.wordNet = wordNet;		
	}
	
	private final IntObjectOpenHashMapSerialzable<UnitKind> unitKindMap = new IntObjectOpenHashMapSerialzable<UnitKind>(); 

	private AbstractWordNet wordNet;
	
	private MeaningElement ultimateUnit;
	
	public List<Unit> getUnits(String str){
		
		init();
		
		LinkedHashSet<Unit> set=null;
		
		GrammarRelation[] possibleGrammarForms = wordNet.getPossibleGrammarForms(str);
		if (possibleGrammarForms==null){
			return Collections.emptyList();
		}
		for(GrammarRelation gr : possibleGrammarForms){
			MeaningElement[] concepts = gr.getWord().getConcepts();
			for(MeaningElement me : concepts){
				List<Unit> units = getUnits(me);
				if(units==null||units.isEmpty()){
					continue;
				}
				
				if(set==null){
					set = new LinkedHashSet<Unit>();
				}				
				set.addAll(units);
			}
		}
		if(set==null){
			return null;
		}
		return new ArrayList<Unit>(set);
	}
	
	public IntArrayList detectGeneralizations(MeaningElement element ,IntLookupContainer set){
		IntArrayList result = null;
		IntOpenHashSet passed = new IntOpenHashSet();
		passed.add(element.id());
		ArrayList<MeaningElement> toInspect = new ArrayList<MeaningElement>();
		toInspect.add(element);
		for(int i = 0 ; i < toInspect.size() ; i++){
			MeaningElement me = toInspect.get(i);
			SemanticRelation[] semanticRelations = me.getSemanticRelations();
			for(SemanticRelation sr : semanticRelations){
				if( sr.relation == SemanticRelation.GENERALIZATION || sr.relation == SemanticRelation.GENERALIZATION_BACK_LINK ){
					MeaningElement word = sr.getWord();
					if(set.contains(word.id())){
						if(result==null){
							result = new IntArrayList();
						}
						result.add(word.id());
					}
					else if(!passed.contains(word.id())){
						toInspect.add(word);
						passed.add(word.id());
					}					
				}
			}
		}		
		return result;
	}
	public boolean canBeUnitStart(String str){
		
		init();

		GrammarRelation[] possibleGrammarForms = wordNet.getPossibleGrammarForms(str);
		for(GrammarRelation gr : possibleGrammarForms){

			TextElement word = gr.getWord();
			TextElement[] possibleContinuations = wordNet.getPossibleContinuations(word);
			for(TextElement te : possibleContinuations){
				MeaningElement[] concepts = te.getConcepts();
				for(MeaningElement me : concepts){
					IntLookupContainer keys = unitKindMap.keys();
					IntArrayList unitTypes = detectGeneralizations(me,keys);
					if(unitTypes.size()>0){
						return true;
					}
				}
			}
		}
		
		return false;
	}
	
	public List<Unit> getUnits(MeaningElement me){
		
		init();
		
		Double relationToPrimary = Double.NaN;
		MetaLayer<Object> layer = wordNet.getMetaLayers().getLayer("relation_to_primary");
		if(layer!=null){		
			Object value = layer.getValue(me);
			if(value!=null){
				try{
					relationToPrimary = Double.parseDouble(value.toString());
				}
				catch(Exception e){}
			}
		}
		
		LinkedHashSet<Unit> set=null;
		IntArrayList unitTypes = detectGeneralizations(me,unitKindMap.keys());
		if(unitTypes==null){
			return null;
		}
		String unitName = me.getParentTextElement().getBasicForm();
		
		int size = unitTypes.size();
		for(int a=0;a<size;a++){
			int ut=unitTypes.get(a);
			UnitKind kind = unitKindMap.get(ut);
			Unit unit = createNewUnit(unitName, relationToPrimary, kind);
			if(set==null){
				set = new LinkedHashSet<Unit>();
			}
			set.add(unit);
		}
		if(set==null){
			return null;
		}
		return new ArrayList<Unit>(set);
	}
	public List<Unit> getUnits(MeaningElement[] me){
		ArrayList<Unit> arrayList =null;
		for(MeaningElement q:me){
			List<Unit> units = getUnits(q);
			if (units!=null){
				if (arrayList==null){
					arrayList = new ArrayList<Unit>();
				}
			arrayList.addAll(units);
			}
		}
		return arrayList;
	}

	private void init() {
		if(ultimateUnit!=null)
			return;
		
		ultimateUnit = getMeaning("_ALL_DIMENSION_UNITS".toLowerCase());
		
		for( UnitKind uk : UnitKind.values()){
			String name = uk.name();
			MeaningElement meaning = getMeaning((UNIT_KIND_PREFIX+name).toLowerCase());
			if(meaning!=null){
				unitKindMap.put(meaning.id(), uk);
			}
		}		
		
		if(ultimateUnit==null){
			//throw new RuntimeException("Wordnet does not support measure units.");
		}
	}

	private MeaningElement getMeaning(String name) {
		GrammarRelation[] pgf = wordNet.getPossibleGrammarForms(name);
		if(pgf!=null&&pgf.length!=0){
			MeaningElement[] concepts = pgf[0].getWord().getConcepts();
			if(concepts!=null&&concepts.length!=0){
				return concepts[0];
			}
		}
		return null;
	}
	
	public List<Unit> constructUnits(String str){
		
		String pref = detectScalePrefix(str);
		if(pref!=null){
			int exp = getScale(pref);
			String baseValue = str.substring(pref.length());
			List<Unit> baseUnits = getUnits(baseValue);
			if(baseUnits!=null){
				ArrayList<Unit> units = new ArrayList<Unit>();
				double rel = Math.pow(10, exp);
				for(Unit baseUnit : baseUnits){
					UnitKind kind = baseUnit.getKind();
					String unitName = pref + baseUnit.getShortName();					
					Unit unit = createNewUnit(unitName, rel, kind);
					units.add(unit);
				}
				return units;
			}
		}
		return null;
	}

	private Unit createNewUnit(String unitName, double relationToPrimary,UnitKind unitKind)
	{
		Unit unit = getFromCache(unitName,unitKind);
		if(unit!=null){
			return unit;
		}
		
		Unit primaryUnit = getPromaryUnit(unitKind);					
		unit = new Unit(unitName, unitKind, relationToPrimary, primaryUnit);
		
		putToCache(unit);
		return unit;
	}
	
	private void putToCache(Unit unit) {
		if(unitsCache==null){
			unitsCache = new HashMap<UnitKind, List<Unit>>();
		}
		UnitKind kind = unit.getKind();
		List<Unit> units = unitsCache.get(kind);
		if(units==null){
			units = new ArrayList<Unit>();
			unitsCache.put(kind, units);
		}
		String unitName = unit.getShortName();
		for(Unit u : units){
			if(u.getShortName().equals(unitName)){
				return;
			}
		}
		units.add(unit);
	}

	private Unit getFromCache(String unitName, UnitKind unitKind) {
		
		if(unitsCache==null){
			unitsCache = new HashMap<UnitKind, List<Unit>>();
			initPrimaryUnits();
		}
		List<Unit> units = unitsCache.get(unitKind);
		if(units==null){
			return null;
		}
		for(Unit u : units){
			if(u.getShortName().equals(unitName)){
				return u;
			}
		}
		return null;
	}

	protected Unit getPromaryUnit(UnitKind unitKind){
		if(primaryUnits==null){
			initPrimaryUnits();
		}
		return primaryUnits.get(unitKind);
	}

	private void initPrimaryUnits() {
		
		primaryUnits = new HashMap<UnitKind, Unit>();
		MetaLayer<Object> layer = wordNet.getMetaLayers().getLayer("primary_unit");
		if(layer==null){		
			return;
		}
		int[] allMeanings = layer.getAllIds();
		for(int id : allMeanings){
			MeaningElement me = wordNet.getConceptInfo(id);
			String unitKindeString = me.getParentTextElement().getBasicForm().toUpperCase();
			if(!unitKindeString.startsWith(UNIT_KIND_PREFIX)){
				continue;
			}
			UnitKind unitKind = UnitKind.valueOf(unitKindeString.substring(UNIT_KIND_PREFIX.length()));
			if(unitKind==null){
				continue;
			}
			
			MeaningElement mePrim = (MeaningElement) layer.getValue(id);
			String primaryUnitName = mePrim.getParentTextElement().getBasicForm();
			Unit primaryUnit = createNewUnit(primaryUnitName, 1, unitKind);
			primaryUnits.put(unitKind, primaryUnit);
			putToCache(primaryUnit);
		}
	}

}
