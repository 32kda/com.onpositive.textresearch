package com.onpositive.semantic.wikipedia.abstracts;

public class TextAbstractEstimator {

	int goodEleementsScore=0;
	int badElementsCount;
	public int estimate(RootElement element){
		badElementsCount=0;
		goodEleementsScore=0;
		element.accept(new TextElementVisitor() {
			
			CleanupVisitor vv=new CleanupVisitor();
			
			@Override
			public void visit(TextAbstractElement element) {
				if (element instanceof TableElement){
					TableElement q=(TableElement) element;
					badElementsCount+=q.elements.size()*2;
					return;
				}
				if (element instanceof AbstractList){
					AbstractList l=(AbstractList) element;
					//badElementsCount+=l.elements.size();
					return;
				}
				if (element instanceof OnelineTextElement){
					if (element.getParent() instanceof  TableElement){
						return;
					}
					if (element.getParent() instanceof  AbstractList){
						return;
					}
					OnelineTextElement mm=(OnelineTextElement) element;
					if (!vv.validate(mm.cleared_text)){
						badElementsCount++;
					}
					if (OnelineTextElement.looksLikeActualParagraph(mm.cleared_text)){
						goodEleementsScore+=5;
					}
				}
			}
		});
		if (badElementsCount>goodEleementsScore&&goodEleementsScore>30){
			return 10;
		}
		return goodEleementsScore-badElementsCount;
	}
}
