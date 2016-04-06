package com.onpositive.semantic.wikipedia2.catrelations;

import java.io.PrintStream;
import com.onpositive.semantic.wikipedia2.WikiEngine2;

/**
 * Выставляет по
 * @author kor
 *
 */
public class ByCategorizerCriteria extends AbstractRelationEstimator {

	public static final int NO_RELATION = 10;
	public static final String ID = "primary_article_member";
	private WikiEngine2 engine;

	public ByCategorizerCriteria(WikiEngine2 eng) {
		super("theme_similarity", ID, "По имени основной статьи");
		this.engine = eng;
	}


	@Override
	public byte relation(int c1, int c2, PrintStream log) {
		String string = engine.getCategoryTitles().get(c1);
		String string1 = engine.getCategoryTitles().get(c2);
		return relation(string, string1);
	}


	byte relation(String string, String string1) {
		if (string.startsWith("Типы")){
			return 6;
		}
		if (string.startsWith("Разделы")){
			return 6;
		}
		if (string.startsWith("Виды")){
			return 6;
		}
		if (string.contains("_по_")){
			if (string1.contains("_по_")){
				//double switch
				return 8;
			}
			return 1;
		}
		return NO_RELATION;
	}
}