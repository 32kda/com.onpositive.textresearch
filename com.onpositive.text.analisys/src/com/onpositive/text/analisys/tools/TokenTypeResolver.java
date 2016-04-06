package com.onpositive.text.analisys.tools;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;

import com.onpositive.text.analysis.IToken;

public class TokenTypeResolver {
	
	private static TokenTypeResolver instance;
	
	public static TokenTypeResolver getInstance() {
		if(instance==null){
			instance = new TokenTypeResolver();
		}
		return instance;
	}

	private TokenTypeResolver() {
		init();
	}
	
	private static final String TOKEN_TYPE_NAME_PREFIX = "TOKEN_TYPE_";
	
	private static HashMap<Integer,String> map = new HashMap<Integer, String>();

	private void init() {
		Field[] fields = IToken.class.getFields();
		for(Field f : fields){
			int mdf = f.getModifiers();
			if(!Modifier.isStatic(mdf)){
				continue;
			}
			
			if( f.getType() != int.class ){
				continue;
			}
			
			String fName = f.getName();
			if(!fName.startsWith(TOKEN_TYPE_NAME_PREFIX)){
				continue;
			}
			
			f.setAccessible(true);
			
			try {
				int code = (Integer) f.get(null);
				map.put(code, fName.substring(TOKEN_TYPE_NAME_PREFIX.length()));
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}			
		}		
	}
	
	private String doResolve(int code){
		return map.get(code);
	}
	
	public static String resolveType(int code){
		return getInstance().doResolve(code);
	}
	
	public static String getResolvedType(IToken token){
		return resolveType(token.getType());
	}

	

}
