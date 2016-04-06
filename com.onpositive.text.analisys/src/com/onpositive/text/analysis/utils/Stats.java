package com.onpositive.text.analysis.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class Stats {
	
	private static final String SYNTAX = "syntax";

	private static final String NEURAL = "neural";

	private static final String HEURISTIC = "heuristic";

	private static Map<String, Long> timings = new HashMap<String, Long>();
	
	private static Map<String, Long> starts = new HashMap<String, Long>();
	
	public static void start(String tag) {
		starts.put(tag, System.currentTimeMillis());
		if (timings.get(tag) == null) {
			timings.put(tag, (long) 0);
		}
	}
	
	public static void finish(String tag) {
		long value = timings.get(tag);
		value += (System.currentTimeMillis() - starts.get(tag));
		timings.put(tag, value);
	}
	
	public static void startHeuristic() {
		start(HEURISTIC);
	}
	
	public static void finishHeuristic() {
		finish(HEURISTIC);
	}
	
	public static void startNeural() {
		start(NEURAL);
	}
	
	public static void finishNeural() {
		finish(NEURAL);
	}
	
	public static void startSyntax() {
		start(SYNTAX);
	}
	
	public static void finishSyntax() {
		finish(SYNTAX);
	}
	
	public static String getInfo() {
		StringBuilder builder = new StringBuilder();
		timings.keySet().stream().sorted().map(key -> key + " : " + timings.get(key) + "ms").forEach(str -> builder.append(str +"\n"));
		return builder.toString();
	}

}
