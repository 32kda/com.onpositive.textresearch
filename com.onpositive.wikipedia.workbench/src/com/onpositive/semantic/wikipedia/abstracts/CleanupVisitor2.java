package com.onpositive.semantic.wikipedia.abstracts;

public class CleanupVisitor2 extends TextElementVisitor{

	@Override
	public void visit(TextAbstractElement element) {
		if (element instanceof CompositeTextElement){
			TextAbstractElement[] el= element.getChildren();
			for (int a=0;a<el.length;a++){
				TextAbstractElement currentElement=el[a];
				int boundary=a;
				if  (isPropogatingEvil(currentElement)){
					//let's look what we may kill before it
					for (int b=a-1;b>=0;b--){
						TextAbstractElement waveElement=el[b];
						if (!waveElement.valid){
							boundary=b;
						}
						else if  (brokesWave(waveElement)){
							break;
						}
					}
				}
				if (boundary!=a){
					for (int i=boundary;i<=a;i++){
						el[i].valid=false;
					}
				}
			}
		}
		if (element instanceof CompositeTextElement){
			TextAbstractElement[] el= element.getChildren();
			for (int a=0;a<el.length;a++){
				TextAbstractElement currentElement=el[a];
				if (currentElement instanceof OnelineTextElement){
					OnelineTextElement z=(OnelineTextElement) currentElement;
					if( z.cleared_text.endsWith(":")){
						boolean hasValid=false;
						for (int b=a+1;b<el.length;b++){
							TextAbstractElement waveElement=el[b];
							if (waveElement.valid){
								hasValid=true;
								break;
							}							
						}
						if (!hasValid){
							z.valid=false;
						}
					}
				}
			}
		}
	}

	private boolean brokesWave(TextAbstractElement waveElement) {
		if (waveElement instanceof Section){
			return true;
		}
		if (waveElement instanceof OnelineTextElement){
			OnelineTextElement q=(OnelineTextElement) waveElement;
			if (q.cleared_text.endsWith(":")){
				//FIXME split this on parts
				q.valid=false;
				return true;
			}
			if (OnelineTextElement.looksLikeActualParagraph(q.cleared_text)){
				return true;
			}
			return false;
		}
		return false;
	}

	private boolean isPropogatingEvil(TextAbstractElement currentElement) {
		boolean b = currentElement instanceof TableElement;
		return b;
	}
}
