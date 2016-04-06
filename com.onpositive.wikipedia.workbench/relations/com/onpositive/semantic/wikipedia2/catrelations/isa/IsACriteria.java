package com.onpositive.semantic.wikipedia2.catrelations.isa;

import com.onpositive.semantic.search.core.ICategorizable;
import com.onpositive.semantic.search.core.ICategory;
import com.onpositive.semantic.search.core.IDocument;
import com.onpositive.semantic.wordnet.Grammem;
import com.onpositive.text.analysis.syntax.SyntaxToken.GrammemSet;
import com.onpositive.wikipedia.workbench.words.primary.SimpleTitleModel;
import com.onpositive.wikipedia.workbench.words.primary.TitleModelCache;
import com.onpositive.wikipedia.workbench.words.primary.WikiEngineProvider;

public class IsACriteria {

	private ICategory c0;
	private ICategory c1;
	private IDocument d1;

	SimpleTitleModel t0;
	SimpleTitleModel t1;

	public IsACriteria(ICategory c0, ICategory c1, IDocument d0, SimpleTitleModel t0, SimpleTitleModel t1) {
		super();
		this.c0 = c0;
		this.c1 = c1;
		this.t0 = t0;
		this.t1 = t1;
		this.d1 = d0;
		if (d0 == null) {
			if (t1.getMainWord() != null) {
				if (d0 == null) {
					String basicForm = t1.getMainWord().getBasicForm();
					basicForm = Character.toUpperCase(basicForm.charAt(0)) + basicForm.substring(1);
					final IDocument document = WikiEngineProvider.getInstance().getDocumentByTitle(basicForm);
					if (document != null) {
						this.d1 = document;
					}
				}
			}
		}
	}

	public boolean isA() {
		if (c1!=null&&c0!=null){
			if (t0.getMainWord()!=null&&t1.getMainWord()!=null){
				boolean p1=false;
				boolean p2=false;
				for (GrammemSet g0:t0.getMainWord().getGrammemSets()){
					if (g0.hasGrammem(Grammem.SingularPlural.PLURAL)){
						p1=true;
					}
				}
				for (GrammemSet g0:t1.getMainWord().getGrammemSets()){
					if (g0.hasGrammem(Grammem.SingularPlural.PLURAL)){
						p2=true;
					}
				}
				if (p1){
					return true;
				}
			}
		}
		if (c1 != null && !checkElement(c1)) {
			return false;
		}
		if (d1 != null && !checkElement(d1)) {
			return false;
		}			
		return true;
	}

	public boolean checkElement(ICategorizable element) {
		final ICategory[] categories = element.getCategories();
		for (ICategory m : categories) {
			if (!m.getTitle().equals(element)) {
				SimpleTitleModel tm1=TitleModelCache.getInstance().getModel(m.getTitle());
				SimpleTitleModel tm2=TitleModelCache.getInstance().getModel(element.getTitle());
				if (tm1.hasSameMainWords(tm2)&&!(m.getTitle().equals(element.getTitle()))){
					return false;
				}
			}
		}
		return true;
	}

}
