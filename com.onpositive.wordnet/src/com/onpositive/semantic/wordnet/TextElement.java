package com.onpositive.semantic.wordnet;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * This class represents word or common word sequence in a natural language
 * @author kor
 */
public abstract class TextElement extends RelationTarget{
	
	public TextElement(AbstractWordNet owner) {
		super(owner);
	}

	public abstract int id();
	
	public abstract String getBasicForm();
	
	public abstract MeaningElement[] getConcepts();
	
	public abstract boolean isMultiWord();
	
	public abstract TextElement[] getParts();
	
	@Override
	public String toString() {
		if (getConcepts().length==1){
			return getConcepts()[0].toString();
		}
		return Arrays.toString(getConcepts());
	}
	
	public boolean hasGrammem(Grammem g){
		MeaningElement[] concepts = getConcepts();
		for (MeaningElement e:concepts){
			Set<Grammem> grammems = e.getGrammems();
			if (grammems.contains(g)){
				return true;
			}
		}
		return false;		
	}
	
	public HashSet<Grammem>allGrammems(){
		HashSet<Grammem>result=new HashSet<Grammem>();
		MeaningElement[] concepts = getConcepts();
		for (MeaningElement e:concepts){
			Set<Grammem> grammems = e.getGrammems();
			result.addAll(grammems);
		}
		return result;	
	}
	
	public boolean hasOnlyGrammemOfKind(Grammem g){
		MeaningElement[] concepts = getConcepts();
		Class<? extends Grammem> class1 = g.getClass();
		for (MeaningElement e:concepts){
			Set<Grammem> grammems = e.getGrammems();
			for (Grammem z:grammems){
				if (z.getClass()==class1){
					if (z!=g){
						return false;
					}
				}
			}
			if (grammems.contains(g)){
				return true;
			}
		}
		return false;		
	}
}
