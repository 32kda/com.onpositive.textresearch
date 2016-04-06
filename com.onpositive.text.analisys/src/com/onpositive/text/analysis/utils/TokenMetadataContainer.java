package com.onpositive.text.analysis.utils;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

import com.onpositive.text.analysis.IToken;

public class TokenMetadataContainer {
	
	private static TokenMetadataContainer instance;
	
	public static synchronized TokenMetadataContainer getInstance() {
		if (instance == null) {
			instance = new TokenMetadataContainer();
		}
		return instance;
	}
	
	private TokenMetadataContainer() {
	}
	
	private Map<IToken, Map<String, String>> metadata = new IdentityHashMap<IToken, Map<String, String>>();

	public String get(IToken token, String key) {
		Map<String, String> tokenData = metadata.get(token);
		if (tokenData != null) {
			return tokenData.get(key);
		}
		return null;
	}

	public String put(IToken token, String key, String value) {
		Map<String, String> tokenData = metadata.get(token);
		if (tokenData == null) {
			tokenData = new HashMap<String, String>();
			metadata.put(token, tokenData);
		}
		return tokenData.put(key, value);
	}

}
