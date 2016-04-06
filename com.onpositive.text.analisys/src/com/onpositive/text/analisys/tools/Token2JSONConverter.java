package com.onpositive.text.analisys.tools;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.onpositive.text.analysis.IToken;

public class Token2JSONConverter {
	
	public String convertToken(IToken token){
		
		JSONObject obj = new JSONObject();
		try {
			obj.put("id", token.id());
			JSONArray arr = new JSONArray();
			obj.put("children", arr);
			for(IToken ch : token.getChildren()){
				arr.put(ch.id());
			}
			return obj.toString();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
		
	}

}
