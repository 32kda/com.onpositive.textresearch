package com.onpositive.text.analysis.lexic.scalar;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

class PatternManager{
	
	public DelimeterPattern[] getPatterns() {
		return patterns;
	}

	public void setPatterns(DelimeterPattern[] patterns) {
		this.patterns = patterns;
	}

	private DelimeterPattern[] patterns = new DelimeterPattern[]{
			new DelimeterPattern(",".intern(), ".".intern(), " ".intern(), 0),
			new DelimeterPattern(".".intern(), ",".intern(), " ".intern(),30),
			new DelimeterPattern(" ".intern(), ".".intern(), ",".intern(),10),
			new DelimeterPattern(" ".intern(), ",".intern(), ".".intern(),40),
			new DelimeterPattern(",".intern(), null        , ".".intern(),20)};
	
	private Map<String,List<DelimeterPattern>> valueDelimeterMap = new HashMap<String, List<DelimeterPattern>>();
	private Map<String,List<DelimeterPattern>> decimalDelimeterMap = new HashMap<String, List<DelimeterPattern>>();
	private Map<String,List<DelimeterPattern>> fractureDelimeterMap = new HashMap<String, List<DelimeterPattern>>();
	
	private HashSet<String> cancelledValueDelimeters = new HashSet<String>();
	private HashSet<String> cancelledDecimalDelimeters = new HashSet<String>();
	private HashSet<String> cancelledFractureDelimeters = new HashSet<String>();
	
	protected PatternManager() {
		try {
			fillMap(valueDelimeterMap,DelimeterPattern.class.getMethod("getValueDelimeter"));
			fillMap(decimalDelimeterMap,DelimeterPattern.class.getMethod("getDecimalDelimeter"));
			fillMap(fractureDelimeterMap,DelimeterPattern.class.getMethod("getFractureDelimeter"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	protected void resetPatterns(){
		for(DelimeterPattern dp : patterns){
			dp.reset();
		}
		cancelledDecimalDelimeters.clear();
		cancelledValueDelimeters.clear();
		cancelledFractureDelimeters.clear();
	}
	
	List<DelimeterPattern> getRankedPatterns(){
		ArrayList<DelimeterPattern> list = new ArrayList<DelimeterPattern>();
		for(DelimeterPattern dp : patterns){
			if(dp.rank<=0){
				list.add(dp);
			}
		}
		Collections.sort(list, new Comparator<DelimeterPattern>() {

			@Override
			public int compare(DelimeterPattern o1, DelimeterPattern o2) {
				int rankDif = o1.rank - o2.rank;
				if(rankDif!=0){
					return rankDif;
				}
				return o1.weight - o2.weight;
			}
			
		});
		return list;
	};
	
	protected boolean iscanCancelledValueDelimeter(String str){
		return cancelledValueDelimeters.contains(str);
	}
	
	protected boolean iscanCancelledDecimalDelimeter(String str){
		return cancelledDecimalDelimeters.contains(str);
	}
	
	protected boolean iscanCancelledFractureDelimeter(String str){
		return cancelledFractureDelimeters.contains(str);
	}

	private void fillMap(Map<String, List<DelimeterPattern>> map, Method method)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
	{
		for(DelimeterPattern dp : patterns)
		{
			Object value = method.invoke(dp);
			if(value==null){
				continue;
			}
			if(!(value instanceof String)){
				throw new IllegalAccessError();
			}
			String key = value.toString();
			List<DelimeterPattern> list = map.get(key);
			if(list==null){
				list = new ArrayList<DelimeterPattern>();
				map.put(key, list);
			}
			list.add(dp);
		}
	}
	
	protected void cancelValueDelimeter(String str)
	{
		cancelDelimeter(cancelledValueDelimeters,valueDelimeterMap,str);
	}
	
	protected void cancelDecimalDelimeter(String str)
	{
		cancelDelimeter(cancelledDecimalDelimeters,decimalDelimeterMap,str);
	}
	
	protected void cancelFractureDelimeter(String str)
	{
		cancelDelimeter(cancelledFractureDelimeters,fractureDelimeterMap,str);
	}
	
	private void cancelDelimeter(HashSet<String> set, Map<String, List<DelimeterPattern>> map, String str)
	{
		set.add(str);
		List<DelimeterPattern> list = map.get(str);
		if(list==null){
			return;
		}
		for(DelimeterPattern dp:list){
			dp.cancel();
		}
	}

	public void voteForDecimalDelimeter(String str) {
		voteForDelimeter(this.decimalDelimeterMap,str);
	}	

	public void voteForValueDelimeter(String str) {
		voteForDelimeter(this.valueDelimeterMap,str);
	}
	
	public void voteForFractureDelimeter(String str) {
		voteForDelimeter(this.fractureDelimeterMap,str);
	}
	
	private void voteForDelimeter(Map<String, List<DelimeterPattern>> map, String str)
	{
		List<DelimeterPattern> list = map.get(str);
		if(list==null){
			return;
		}
		for(DelimeterPattern dp : list){
			dp.vote();
		}
	}
}
