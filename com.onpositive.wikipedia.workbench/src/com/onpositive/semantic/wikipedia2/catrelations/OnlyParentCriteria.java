package com.onpositive.semantic.wikipedia2.catrelations;

import java.io.PrintStream;

import com.onpositive.semantic.wikipedia2.WikiEngine2;

public class OnlyParentCriteria extends AbstractRelationEstimator {

	public static final String ID = "primary_article_member";
	private WikiEngine2 engine;

	public OnlyParentCriteria(WikiEngine2 eng) {
		super("theme_similarity", ID, "По имени основной статьи");
		this.engine = eng;
	}
	@Override
	public byte relation(int c1, int c2, PrintStream log) {
		//String string = engine.getCategoryTitles().get(c1);
		//String string1 = engine.getCategoryTitles().get(c2);				
		int[] direct2 = engine.getCategoryToParentCategories().getInverse(c2);
		if (direct2.length==1){
			if (direct2[0]==c1){
				return 5;
			}
		}
		return 10;
	}
}