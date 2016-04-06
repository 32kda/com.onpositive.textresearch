package com.onpositive.semantic.wikipedia2.catrelations;

import java.io.PrintStream;

import com.onpositive.semantic.wikipedia2.WikiEngine2;
import com.onpositive.semantic.wikipedia2.catrelations.TitleModel.WordModel;
import com.onpositive.semantic.wikipedia2.services.RedirectsMap;
import com.onpositive.semantic.wordnet.TextElement;

public class PrimaryArticleMembershipCriteria extends AbstractRelationEstimator{

	public static final String ID = "primary_article_member";
	private WikiEngine2 engine;
	
	public PrimaryArticleMembershipCriteria(WikiEngine2 eng) {
		super("theme_similarity", ID, "По имени основной статьи");
		this.engine = eng;
	}
	@Override
	public byte relation(int c1, int c2, PrintStream log) {
		//String string = engine.getCategoryTitles().get(c1);
		String string1 = engine.getCategoryTitles().get(c2);
		
		//TitleModel tm = new TitleModel(string,false);
		TitleModel tm1 = TitleModel.get(string1,false);
		if (tm1.hasSingleCore()){
			WordModel singleCore = tm1.getSingleCore();
			for (TextElement q:singleCore.words){
				String basicForm = q.getBasicForm();
				int i = engine.getPageTitles().get(Character.toUpperCase(basicForm.charAt(0))+basicForm.substring(1));
				if (i<=0){
					continue;
				}
				RedirectsMap index = engine.getIndex(RedirectsMap.class);
				if (index.isRedirect(i)){
					i=index.value(i);
				}
				//System.out.println(engine.getPageTitles().get(i));
				if(i>0){
					int[] direct = engine.getPageToParentCategories().getInverse(i);
					//System.out.println(direct);
					for (int q1:direct){
						if (q1==c1){
							return 3;
						}
						if (q1==c2){
							continue;
						}
						/*String string = engine.getCategoryTitles().get(q1);
						System.out.println(string);
						int[] direct2 = engine.getCategoryToParentCategories().getDirect(q1);
						for (int d:direct2){
							string = engine.getCategoryTitles().get(d);
							System.out.println("C>"+string);
						}*/
					}
				}
			}
		}
		return 10;
	}
}