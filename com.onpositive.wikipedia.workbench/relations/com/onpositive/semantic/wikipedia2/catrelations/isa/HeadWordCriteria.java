package com.onpositive.semantic.wikipedia2.catrelations.isa;

import com.onpositive.wikipedia.workbench.words.primary.SimpleTitleModel;
import com.onpositive.wikipedia.workbench.words.primary.TitleModelCache;

public class HeadWordCriteria {

	static int errorCount=0;
	
	public static boolean isSimpleIsA(String str1,String str2){
		if (str1.isEmpty() || str2.isEmpty()) {
			return false;
		}
		int s1=str1.charAt(0);
		if (s1==65279){
			str1=str1.substring(1);
		}
		if (str2.toLowerCase().endsWith(str1.toLowerCase())){
			return true;
		}
		if (str2.endsWith("ание")&&!str1.endsWith("ание")){
			return false;
		}
		SimpleTitleModel tm1=TitleModelCache.getInstance().getModel(str1);
		SimpleTitleModel tm2=TitleModelCache.getInstance().getModel(str2);
		if (tm1.hasSameMainWords(tm2)){
			return true;
		}
//		final WikiEngine2 instance = WikiEngineProvider.getInstance();
//		final ICategory category = instance.getCategory(str1.trim());
//		final ICategory category1 = instance.getCategory(str2.trim());
//		final IDocument document0= instance.getDocumentByTitle(str2.trim());
//		if (category==null||(category1==null&&document0==null)){
//			errorCount++;
//			System.out.println("Nothing found:"+str1+":"+str2+":"+errorCount);
//		} else {
//			final IsACriteria isACriteria = new IsACriteria(category, category1, document0, tm1, tm2);
//			
//			if (isACriteria.isA()){
//				return true;
//			}
//		}
		return false;
	}
}