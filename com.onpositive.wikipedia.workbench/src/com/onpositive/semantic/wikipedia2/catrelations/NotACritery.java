package com.onpositive.semantic.wikipedia2.catrelations;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;

import com.onpositive.semantic.search.core.ICategory;
import com.onpositive.semantic.search.core.IDocument;
import com.onpositive.semantic.wikipedia2.WikiEngine2;
import com.onpositive.semantic.wikipedia2.WikiCat;
import com.onpositive.semantic.wikipedia2.catrelations.TitleModel.WordModel;
import com.onpositive.semantic.wikipedia2.fulltext.StemProvider;
import com.onpositive.semantic.wordnet.TextElement;

public class NotACritery extends AbstractRelationEstimator {

	public static final String ID = "primary_article_member";
	private WikiEngine2 engine;

	public NotACritery(WikiEngine2 eng) {
		super("theme_similarity", ID, "По имени основной статьи");
		this.engine = eng;
	}

	@Override
	public byte relation(int c1, int c2, PrintStream log) {
		String string = engine.getCategoryTitles().get(c1);
		String string1 = engine.getCategoryTitles().get(c2);
		return match(string, string1);
	}
	public boolean matchTitles(String par, String child) {
		return match(par, child)==6;
	}

	public byte match(String string, String string1) {
		TitleModel tm = TitleModel.get(string, false);
		TitleModel tm1 = TitleModel.get(string1, false);
		if (tm.hasSingleCore()) {
			WordModel singleCore = tm.getSingleCore();
			for (TextElement q : singleCore.words) {
				for (WordModel m : tm1.models) {
					if (matchStem(q, m)) {
						if (!tm1.cores().contains(m)&&!isAllowed(singleCore,tm1.cores())){
							return 6;
						}
					}
				}
			}
		}
		return 7;
	}
	static HashSet<String>allowedCombinations=new HashSet<String>();
	
	static{
		allowedCombinations.add("река->приток");
	}
	
	private boolean isAllowed(WordModel singleCore, ArrayList<WordModel> cores) {
		String basicForm = singleCore.getBasicForm();
		for (WordModel q:cores){
			if (allowedCombinations.contains(basicForm+"->"+q.getBasicForm())){
				return true;
			}
		}
		return false;
	}

	public boolean isNotA(ICategory c1,ICategory c2){
		if (relation(((WikiCat)c1).getIntId(), ((WikiCat)c2).getIntId(), null)==6){
			return true;
		}
		return false;
	}
	
	public boolean isNotA(ICategory c1,IDocument c2){
		if (match(c1.getTitle(),c2.getTitle())==6){
			return true;
		}
		return false;
	}

	boolean matchStem(TextElement q, WordModel m) {
		boolean contains = m.words.contains(q);
		if (!contains){
			String stem = StemProvider.getInstance().stem(q.getBasicForm());
			for (TextElement z:m.words){
				String basicForm = z.getBasicForm();
				if (basicForm.startsWith(stem)){
					return true;
				}
			}
		}
		return contains;
	}
}