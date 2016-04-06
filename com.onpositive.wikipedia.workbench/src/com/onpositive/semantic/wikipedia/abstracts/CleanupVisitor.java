package com.onpositive.semantic.wikipedia.abstracts;

public class CleanupVisitor extends TextElementVisitor implements NotifyOnEndVisit{

	@Override
	public void visit(TextAbstractElement element) {
		if (element instanceof OnelineTextElement){
			OnelineTextElement mm=(OnelineTextElement) element;
			if (!validate(mm.cleared_text)){
				mm.valid=false;
				
				/*CompositeTextElement mp= (CompositeTextElement) element.getParent();
				mp.remove(element);*/
			}
			if (mm.text.startsWith("[[Категория:")){
				mm.valid=false;
			}
		}
	}

	public boolean validate(String text) {
		if (text.length()==0){
			return false;
		}
		if (text.charAt(0)=='<'){
			return false;
		}
		if (text.charAt(0)=='|'){
			return false;
		}
		if (text.startsWith("Категория:")){
			return false;
		}
		
		if (text.startsWith("thumb|")){
			return false;
		}
		if (text.startsWith("frame|")){
			return false;
		}
		if (text.contains("px|")){
			return false;
		}
		if (text.startsWith("File:")){
			return false;
		}
		if (text.startsWith("Файл:")){
			return false;
		}
		boolean letterFound=false;
		for (int a=0;a<text.length();a++){
			char c=text.charAt(a);
			if (Character.isLetter(c)){
				letterFound=true;
			}
		}
		return letterFound;
	}

	@Override
	public void endVisit(CompositeTextElement q) {
		boolean hasValid=false;
		for (TextAbstractElement z:q.elements){
			if (z.valid){
				hasValid=true;
			}
		}
		if (!hasValid){
			q.valid=false;
		}
	}

}
