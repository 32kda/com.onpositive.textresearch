package com.onpositive.text.analysis.utils;

import com.onpositive.text.analysis.IToken;

public class AdditionalMetadataHandler {
	
	public static final String FILTER_KEY = "filtered";
	
	public static final String NEURAL_KEY = "neural";
	
	public static final String EURISTIC_KEY = "euristic";
	
	private static final boolean STORE_METADATA = true;
	
	public static void store(IToken token, String key, String value) {
		if (STORE_METADATA) {
			TokenMetadataContainer.getInstance().put(token, key, value);
		}
	}

	public static String get(IToken token, String key) {
		if (STORE_METADATA) {
			return TokenMetadataContainer.getInstance().get(token, key);
		}
		return null;
	}

}
