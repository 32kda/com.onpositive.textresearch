package com.onpositive.wordnet.tests;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import com.onpositive.semantic.wordnet.AbstractRelation;
import com.onpositive.semantic.wordnet.AbstractWordNet;
import com.onpositive.semantic.wordnet.GrammarRelation;
import com.onpositive.semantic.wordnet.Grammem;
import com.onpositive.semantic.wordnet.MeaningElement;
import com.onpositive.semantic.wordnet.MorphologicalRelation;
import com.onpositive.semantic.wordnet.SemanticRelation;
import com.onpositive.semantic.wordnet.TextElement;
import com.onpositive.semantic.wordnet.WordNetProvider;

public class BasicTest extends TestCase{

	static{
		WordNetProvider.setInstance(null);
		WordNetProvider.killDatabase();
	}
	public static void main(String[] args) {
		AbstractWordNet instance = WordNetProvider.getInstance();
		int wordCount = instance.wordCount();
		System.out.println("======");
		for(int i=0;i<wordCount;i++){
			TextElement wordElement = instance.getWordElement(i);
			if (wordElement!=null){
				MeaningElement[] concepts = wordElement.getConcepts();
				for(MeaningElement q:concepts){
					Set<Grammem> grammems = q.getGrammems();
					if(grammems!=null&&grammems.contains(Grammem.PartOfSpeech.NUMR)){
						System.out.println("<meta w='"+wordElement.getBasicForm()+"' v='' l='numeric' />");
					}
				}
			}
		}
	}
	
	public void testContinuations(){
		AbstractWordNet mm = WordNetProvider.getInstance();
		TextElement wordElement = mm.getWordElement("метр");
		TestCase.assertTrue(wordElement!=null);
	}
	public void testContinuationNormal(){
		AbstractWordNet mm = WordNetProvider.getInstance();
		TextElement wordElement = mm.getWordElement("ковер");
		TextElement[] possibleContinuations = mm.getPossibleContinuations(wordElement);
		TestCase.assertTrue(possibleContinuations.length>0);
	}
	public void testHour(){
		AbstractWordNet mm = WordNetProvider.getInstance();
		GrammarRelation[] possibleGrammarForms = mm.getPossibleGrammarForms("час");
		for (GrammarRelation g:possibleGrammarForms){
			TestCase.assertTrue(g.getWord().getBasicForm().equals("час"));
		}
	}
	
	public void testInit(){
		AbstractWordNet instance = WordNetProvider.getInstance();
		TestCase.assertNotNull(instance);
		GrammarRelation[] possibleGrammarForms = instance.getPossibleGrammarForms("петроченко");
		Set<Grammem> grammems = possibleGrammarForms[0].getWord().getConcepts()[0].getGrammems();
		TestCase.assertTrue(grammems.contains(Grammem.SemanGramem.SURN));		
	}
	
	public void testRelations(){
		AbstractWordNet instance = WordNetProvider.getInstance();
		TextElement wordElement = instance.getWordElement("вертолёт");
		MeaningElement[] concepts = wordElement.getConcepts();
		AbstractRelation<MeaningElement>[] semanticRelations = concepts[0].getAllRelations();
		TestCase.assertTrue(semanticRelations.length>0);
		boolean found=false;
		for (AbstractRelation<MeaningElement>q:semanticRelations){
			if (q instanceof SemanticRelation){
				SemanticRelation sr=(SemanticRelation) q;
				if (sr.getWord().getParentTextElement().getBasicForm().equals("летательный аппарат")){
					if (sr.relation==SemanticRelation.GENERALIZATION){
						found=true;
						//TestCase.assertTrue(false);
					}
				}
			}
		}
		TestCase.assertTrue(found);
	}
	
	public void testSynonims(){
		GrammarRelation[] possibleGrammarForms = WordNetProvider.getInstance().getPossibleGrammarForms("вертолёта");
		for (GrammarRelation r:possibleGrammarForms){
			String basicForm = r.getWord().getBasicForm();
			TestCase.assertTrue(basicForm.equals("вертолёт"));
		}
		System.out.println(Arrays.toString(possibleGrammarForms));
		TestCase.assertTrue(possibleGrammarForms.length==1);
		TextElement wordElement = possibleGrammarForms[0].getWord();
		HashSet<Grammem> grammemSet = possibleGrammarForms[0].getOwner().getGrammemSet(possibleGrammarForms[0].relation);
		System.out.println(grammemSet);
		AbstractRelation<MeaningElement>[] semanticRelations = wordElement.getConcepts()[0].getAllRelations();
		TestCase.assertTrue(semanticRelations.length>0);
	}

	public void testMultiMeaning(){
		TextElement wordElement = WordNetProvider.getInstance().getWordElement("целина");
		MeaningElement[] concepts = wordElement.getConcepts();
		AbstractRelation<MeaningElement>[] semanticRelations = concepts[0].getAllRelations();
		boolean found=false;
		for (AbstractRelation<MeaningElement> q:semanticRelations){
			TextElement parentTextElement = q.getWord().getParentTextElement();
			if (parentTextElement.getBasicForm().equals("цель")){
				found=true;
				TestCase.assertEquals(q.relation, SemanticRelation.SYNONIM_BACK_LINK);
			}			
		}
		TestCase.assertTrue(found);
	}
	
	public void testMinus(){
		GrammarRelation[] possibleGrammarForms = WordNetProvider.getInstance().getPossibleGrammarForms("-");
		TestCase.assertTrue(possibleGrammarForms==null||possibleGrammarForms.length==0);
	}
	
	public void testMultiMeaning2(){
		TextElement wordElement = WordNetProvider.getInstance().getWordElement("цель");
		MeaningElement[] concepts = wordElement.getConcepts();
		AbstractRelation<MeaningElement>[] semanticRelations = concepts[0].getAllRelations();
		boolean found=true;
		for (AbstractRelation<MeaningElement> q:semanticRelations){
			TextElement parentTextElement = q.getWord().getParentTextElement();
			if (parentTextElement.getBasicForm().equals("целина")){
				found=true;
				TestCase.assertEquals(q.relation, SemanticRelation.SYNONIM);
			}			
		}
		TestCase.assertTrue(found);
	}
	

	
	public void testSequence(){
		TextElement wordElement = WordNetProvider.getInstance().getWordElement("политический деятель");
		TestCase.assertTrue(wordElement.isMultiWord());
		TextElement[] parts = wordElement.getParts();
		TestCase.assertTrue(parts.length==2);
	}
	
	public void testSequenceParsing(){
		TextElement wordElement = WordNetProvider.getInstance().getWordElement("политический деятель");
		TestCase.assertTrue(wordElement.isMultiWord());
		TextElement[] parts = wordElement.getParts();
		TestCase.assertTrue(Grammem.PartOfSpeech.ADJF.isDefinitelyThisPartOfSpech(parts[0]));
		TestCase.assertTrue(Grammem.PartOfSpeech.NOUN.isDefinitelyThisPartOfSpech(parts[1]));			
	}
	
	public void testWordApi(){
		boolean found=false;
		TextElement wordElement = WordNetProvider.getInstance().getWordElement("политический");
		TextElement[] possibleContinuations = WordNetProvider.getInstance().getPossibleContinuations(wordElement);
		for (TextElement z:possibleContinuations){
			if (z.getBasicForm().equals("политический деятель")){
				found=true;
			}
		}
		TestCase.assertTrue(found);		
	}
	
	public void testConjuration(){
		TextElement wordElement = WordNetProvider.getInstance().getWordElement("после того как");
		TestCase.assertTrue(Grammem.PartOfSpeech.CONJ.isDefinitelyThisPartOfSpech(wordElement));
	}
	public void testPrep1(){
		TextElement wordElement = WordNetProvider.getInstance().getWordElement("в");
		TestCase.assertTrue(Grammem.PartOfSpeech.PREP.mayBeThisPartOfSpech(wordElement));
		GrammarRelation[] wordElement1 = WordNetProvider.getInstance().getPossibleGrammarForms("в");
		TestCase.assertTrue(Grammem.PartOfSpeech.PREP.mayBeThisPartOfSpech(wordElement1));
	}
	
	public void testPrep2(){
		TextElement wordElement = WordNetProvider.getInstance().getWordElement("с");
		TestCase.assertTrue(Grammem.PartOfSpeech.PREP.mayBeThisPartOfSpech(wordElement));
		GrammarRelation[] wordElement1 = WordNetProvider.getInstance().getPossibleGrammarForms("с");
		TestCase.assertTrue(Grammem.PartOfSpeech.PREP.mayBeThisPartOfSpech(wordElement1));
	}
	
	public void testPreposition(){
		TextElement wordElement = WordNetProvider.getInstance().getWordElement("в отличие от");
		TestCase.assertTrue(Grammem.PartOfSpeech.PREP.isDefinitelyThisPartOfSpech(wordElement));
	}
	
	public void testComparativ(){
		TextElement wordElement = WordNetProvider.getInstance().getWordElement("длиннее");
		TestCase.assertTrue(Grammem.PartOfSpeech.COMP.isDefinitelyThisPartOfSpech(wordElement));		
	}
	
	public void testMorphology(){
		GrammarRelation[] possibleGrammarForms = WordNetProvider.getInstance().getPossibleGrammarForms("вышла");
		TextElement wordElement = possibleGrammarForms[0].getWord();
		TestCase.assertTrue(wordElement.hasOnlyGrammemOfKind(Grammem.PartOfSpeech.VERB));	
		MorphologicalRelation[] morphologicalRelations = wordElement.getConcepts()[0].getMorphologicalRelations();
		TestCase.assertTrue(morphologicalRelations.length==1);
		TestCase.assertTrue(morphologicalRelations[0].getWord().getParentTextElement().getBasicForm().equals("выйти"));
	}
	
	public void testIo(){
		GrammarRelation[] possibleGrammarForms = WordNetProvider.getInstance().getPossibleGrammarForms("вертолеты");
		TestCase.assertTrue(possibleGrammarForms.length>0);
	}
	
	public void testI2o(){
		GrammarRelation[] possibleGrammarForms = WordNetProvider.getInstance().getPossibleGrammarForms("самолеты");
		TestCase.assertTrue(possibleGrammarForms.length>0);
	}
	
	
}