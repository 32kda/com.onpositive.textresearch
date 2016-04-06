package com.onpositive.wikipedia.workbench;

import java.util.HashMap;
import java.util.Map;

import com.onpositive.semantic.wikipedia2.catrelations.Connection;

public class SimpleConnectionMetadataProvider {
	
	private static Map<Connection, String> metadata = new HashMap<Connection, String>();
	
	public static void put(Connection connection, String meta) {
		metadata.put(connection, meta);
	}
	
	public static String get(Connection connection) {
		return metadata.get(connection);
	}

}
