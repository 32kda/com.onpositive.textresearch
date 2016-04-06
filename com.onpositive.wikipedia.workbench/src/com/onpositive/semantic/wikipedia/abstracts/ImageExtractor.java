package com.onpositive.semantic.wikipedia.abstracts;

import java.util.ArrayList;

public class ImageExtractor extends TextElementVisitor{

	int extractedCount=0;
	
	@Override
	public void visit(TextAbstractElement element) {
		if (element instanceof OnelineTextElement){
			OnelineTextElement q=(OnelineTextElement) element;
			if (q.text.startsWith("[[Файл:")||q.text.startsWith("[[File:")){
				int m=q.text.indexOf('|');
				if (m==-1){
					m=balancedIndex(q);
				}
				if (m!=-1){
					String image=q.text.substring(7,m);
					//
					int m1=balancedIndex(q);
					String otherText=m1==-1?"":q.text.substring(m1+1);
					if (m1==-1){
						tryBalance(element);
					}
					CompositeTextElement compositeTextElement = (CompositeTextElement) element.getParent();
					ImageElement imageElement = new ImageElement(image,compositeTextElement);
					extractedCount++;
					compositeTextElement.replace(q,imageElement);
					if (otherText.length()>0){
						compositeTextElement.addAfter(imageElement,new OnelineTextElement(otherText.trim(), compositeTextElement));
					}
				}
			}
			if (q.text.startsWith("[[Изображение")||q.text.startsWith("[[Изображение")){
				int m=q.text.indexOf('|');
				if (m==-1){
					m=balancedIndex(q);
				}
				if (m!=-1){
					String image=q.text.substring("[[Изображение".length(),m);
					int m1=balancedIndex(q);
					String otherText=m1==-1?"":q.text.substring(m1+1);
					if (m1==-1){
						tryBalance(element);
					}
					CompositeTextElement compositeTextElement = (CompositeTextElement) element.getParent();
					ImageElement imageElement = new ImageElement(image,compositeTextElement);
					extractedCount++;
					compositeTextElement.replace(q,imageElement);
					if (otherText.length()>0){
						compositeTextElement.addAfter(imageElement,new OnelineTextElement(otherText.trim(), compositeTextElement));
					}
				}
			}
			if (q.text.startsWith("[[Image:")||q.text.startsWith("[[Image:")){
				int m=q.text.indexOf('|');
				if (m==-1){
					m=balancedIndex(q);
				}
				if (m!=-1){
					String image=q.text.substring("[[Image:".length(),m);
					int m1=balancedIndex(q);
					if (m1==-1){
						tryBalance(element);
					}
					String otherText=m1==-1?"":q.text.substring(m1+1);
					
					CompositeTextElement compositeTextElement = (CompositeTextElement) element.getParent();
					ImageElement imageElement = new ImageElement(image,compositeTextElement);
					extractedCount++;
					compositeTextElement.replace(q,imageElement);
					if (otherText.length()>0){
						compositeTextElement.addAfter(imageElement,new OnelineTextElement(otherText.trim(), compositeTextElement));
					}
				}
			}
				
		}
	}

	private void tryBalance(TextAbstractElement element) {
		CompositeTextElement q= (CompositeTextElement) element.getParent();
		int indexOf = q.elements.indexOf(element);
		int maxSteps=5;
		int i=0;
		for (int a=indexOf+1;a<q.elements.size();a++){
			i++;
			TextAbstractElement textAbstractElement = q.elements.get(a);
			//if (textAbstractElement )
			if (textAbstractElement instanceof OnelineTextElement){
				OnelineTextElement mm=(OnelineTextElement) textAbstractElement;
				int balancedIndex = balancedIndex(mm);
				if (balancedIndex>0){
					ArrayList<Object>mma=new ArrayList<Object>(q.elements);
					for (int j=indexOf+1;j<=a;j++){
						q.elements.remove(mma.get(j));
						
					}
					return;
					//System.out.println("a");
				}
			}
			else{
				break;
			}
			if (i>=maxSteps){
				break;
			}
		}
	}

	protected int balancedIndex(OnelineTextElement q) {
		String text = q.text;
		char pc='e';
		int level=0;
		for (int a=0;a<text.length();a++){
			char c=text.charAt(a);
			if (c=='['&&pc=='['){
				level++;
			}
			if (c==']'&&pc==']'){
				level--;
				if (level<=0){
					return a;
				}
			}
			pc=c;
		}
		return text.lastIndexOf("]]");
	}

}
