package com.onpositive.text.analysis.utils;

import java.util.HashMap;

public class Exponent {
	
	private static final HashMap<String,Integer> map = new HashMap<String, Integer>();
	
	static{
		map.put("²",2);
		char[] chars = new char[]{ '\u2070','\u00B9','\u00B2','\u00B3',	'\u2074','\u2075','\u2076',	'\u2077','\u2078','\u2079'};
		for(int i = 0 ; i < chars.length ; i++){
			map.put(""+chars[i], i);
		}
	}
	public static boolean isExponent(String str){
		return map.containsKey(str);
	}
	
	public static Integer getExponentValue(String str){
		return map.get(str);
	}
	
}
