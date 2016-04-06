package com.onpositive.text.analysis;

import java.util.HashMap;

public class CreatedByParserRegistry {
	private static CreatedByParserRegistry instance;
	
	private HashMap<String, String> registry;
	
	private CreatedByParserRegistry() {
		registry = new HashMap<String, String>();
		registry.put("com.onpositive.text.analysis.lexic.PrimitiveTokenizer", "PrimitiveTokenizer"); // not parser but should be so
	}
	
	private String getValue(String key) {
		return registry.get(key);
	}
	
	private static CreatedByParserRegistry getInstance() {
		CreatedByParserRegistry localInstance = instance;
        if (localInstance == null) {
            synchronized (CreatedByParserRegistry.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new CreatedByParserRegistry();
                }
            }
        }
        return instance;
	}
	
	public static String getParserName() {
		CreatedByParserRegistry in = CreatedByParserRegistry.getInstance();
				
		try {
			throw new RuntimeException();			
		} catch (RuntimeException e) {
			for (StackTraceElement el : e.getStackTrace()) {
				String name = el.getClassName();
				String res = in.getValue(name);
				if (res == null) {
					try {
						Class<? extends IParser> clazz = in.getClass().getClassLoader().loadClass(name).asSubclass(IParser.class);
						res = clazz.getSimpleName();
						in.registry.put(name, res);
						return res;
					} catch (Exception e0) {
						in.registry.put(name, "");
						continue;
					}	
				} else {
					if (res.isEmpty()) continue; // this is not parser. continue
					return res;
				}		
			}
		}
		return "";
	}
	
}
