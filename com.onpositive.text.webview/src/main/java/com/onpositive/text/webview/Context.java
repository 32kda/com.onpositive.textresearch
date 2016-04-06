package com.onpositive.text.webview;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

public class Context {

	
	static ThreadLocal<Context>ct=new ThreadLocal<>();
	
	public String localPath;
	public String globalPath;
	
	public String text;
	
	public HashMap<String,String>parameters=new HashMap<>();
	
	static void init(HttpServletRequest req){
		Context ctx=new Context();
		ctx.globalPath = req.getContextPath();
		ctx.localPath=req.getPathInfo();
		if (ctx.localPath == null) {
			   ctx.localPath = "/";
			  }
		ct.set(ctx);
		String queryString = req.getQueryString();
		if (queryString==null){
			queryString="";
		};
		String[] split = queryString.split("&");
		for (String s:split){
			try {
				
				String decoded=URLDecoder.decode(queryString,"UTF-8");
				String key=decoded;
				String value=decoded;
				int m=decoded.indexOf("=");
				if (m!=-1){
					key=decoded.substring(0, m);
					value=decoded.substring(m+1);
				}
				if (key.equals("text")){
					ctx.text=value;
				}
				ctx.parameters.put(key, value);
			} catch (UnsupportedEncodingException e) {
				throw new Error();
			}
				
		}
		
	}
	
	public static Context getContext(){
		return ct.get();
	}
}
