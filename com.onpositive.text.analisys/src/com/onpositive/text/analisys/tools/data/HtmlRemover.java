package com.onpositive.text.analisys.tools.data;

public class HtmlRemover {
	
	public static  String removeHTML(String str){
		
		StringBuilder bld = new StringBuilder();
		int prev = 0;
		for(int ind = str.indexOf("<"); ind >=0 ; ind = str.indexOf("<",prev) ){
			
			bld.append(str.substring(prev, ind));
			prev = str.indexOf(">", ind);
			if(prev<0){
				prev = str.length();
			}
			else{
				prev++;
			}
		}
		bld.append(str.substring(prev, str.length()));
		String result = bld.toString();
		return result;
	}

}
