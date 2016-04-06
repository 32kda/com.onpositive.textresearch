package com.onpositive.text.analysis.utils;

import java.util.HashMap;

public class VulgarFraction {
	
	private static final HashMap<Character,int[]> vulgarFractionMap = new HashMap<Character, int[]>();
	static{
		vulgarFractionMap.put('\u00BC', new int[]{1,4});
		vulgarFractionMap.put('\u00BD', new int[]{1,2});
		vulgarFractionMap.put('\u00BE', new int[]{3,4});
		vulgarFractionMap.put('\u2153', new int[]{1,3});
		vulgarFractionMap.put('\u2154', new int[]{2,3});
		vulgarFractionMap.put('\u2155', new int[]{1,5});
		vulgarFractionMap.put('\u2156', new int[]{2,5});
		vulgarFractionMap.put('\u2157', new int[]{3,5});
		vulgarFractionMap.put('\u2158', new int[]{4,5});
		vulgarFractionMap.put('\u2159', new int[]{1,6});
		vulgarFractionMap.put('\u215A', new int[]{5,6});
		vulgarFractionMap.put('\u215B', new int[]{1,8});
		vulgarFractionMap.put('\u215C', new int[]{3,8});
		vulgarFractionMap.put('\u215D', new int[]{5,8});
		vulgarFractionMap.put('\u215E', new int[]{7,8});		
	}
	
	public static boolean isVulgarFraction(char ch){
		return vulgarFractionMap.containsKey(ch);
	}
	
	public static int[] getIntegerPair(char ch){
		return vulgarFractionMap.get(ch);
	}
	
	public static Double getRealValue(char ch){
		int[] iVal = vulgarFractionMap.get(ch);
		if(iVal==null){
			return null;
		}
		return (double)iVal[0]/iVal[1];
	}

}
