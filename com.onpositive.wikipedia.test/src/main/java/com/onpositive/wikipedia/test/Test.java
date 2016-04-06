package com.onpositive.wikipedia.test;

import java.io.PrintStream;

import com.onpositive.semantic.wikipedia2.WikiEngine2;
import com.onpositive.semantic.wikipedia2.catrelations.isa.BlackList;
import com.onpositive.semantic.words3.hds.StringVocabulary;
import com.onpositive.wikipedia.workbench.words.primary.SimpleTitleModel;

public class Test {

	public static void main(String[] args) {
		WikiEngine2 eng = new WikiEngine2("D:/se2/ruwiki");
		try{
		PrintStream ts=new PrintStream("D:/se2/wikidata/ruwiki.txt");
		StringVocabulary categoryTitles = eng.getCategoryTitles();
		int goodCount = 0;
		int badCount = 0;
		for (String s : categoryTitles.getAllKeys()) {

			if (BlackList.getDefault().test(s)) {
				if (s.indexOf('_') != -1) {
					SimpleTitleModel simpleTitleModel = new SimpleTitleModel(s);
					if (simpleTitleModel.getMainWord() != null) {
						goodCount++;
						
						System.out.println(goodCount+":"+s.replace('_', ' ')+" - "+simpleTitleModel.getMainWord().getShortStringValue());
						ts.println(s.replace('_', ' ')+" - "+simpleTitleModel.getMainWord().getShortStringValue());
						if (goodCount%100==0){
							ts.flush();
						}
						if (goodCount==5000){
							break;
						}
					} else {
						badCount++;
						//System.out.println(s);
						//System.out.println(goodCount + ":" + badCount);
					}
				}
			}
		}
		}catch (Exception e){
			
		}
	}
}
