package com.onpositive.wordnet.tests;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.onpositive.semantic.wordnet.AbstractRelation;
import com.onpositive.semantic.wordnet.AbstractWordNet;
import com.onpositive.semantic.wordnet.GrammarRelation;
import com.onpositive.semantic.wordnet.Grammem;
import com.onpositive.semantic.wordnet.MeaningElement;
import com.onpositive.semantic.wordnet.MorphologicalRelation;
import com.onpositive.semantic.wordnet.SemanticRelation;
import com.onpositive.semantic.wordnet.TextElement;
import com.onpositive.semantic.wordnet.WordNetProvider;
import com.onpositive.semantic.wordnet.edit.IWordNetEditInterface;
import com.onpositive.semantic.wordnet.edit.WordNetPatch;
import com.onpositive.semantic.words2.SimpleWordNet;
import com.onpositive.semantic.words3.MetaLayer;

import junit.framework.TestCase;

public class WordNetEditTest extends TestCase{

	
	static {
		WordNetProvider.setInstance(null);
		AbstractWordNet instance = WordNetProvider.getInstance();
		SimpleWordNet simpleWordNet = new SimpleWordNet(instance);
		TestCase.assertEquals(instance.wordCount(), simpleWordNet.wordCount());
		WordNetProvider.setInstance(simpleWordNet);
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
//		System.out.println(Arrays.toString(possibleGrammarForms));
		TestCase.assertTrue(possibleGrammarForms.length==1);
		TextElement wordElement = possibleGrammarForms[0].getWord();
		HashSet<Grammem> grammemSet = possibleGrammarForms[0].getOwner().getGrammemSet(possibleGrammarForms[0].relation);
//		System.out.println(grammemSet);
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
		AbstractWordNet instance = WordNetProvider.getInstance();
		TextElement[] possibleContinuations = instance.getPossibleContinuations(WordNetProvider.getInstance().getWordElement("политический"));
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
	
	public void testCommandParse(){
		try {
			WordNetPatch parse = WordNetPatch.parse(new InputStreamReader(WordNetEditTest.class.getResourceAsStream("tst.xml"),"UTF-8"));
			TestCase.assertEquals(27, parse.size());
			IWordNetEditInterface editable = WordNetProvider.editable(WordNetProvider.getInstance());
			TextElement wordElement3 = editable.getWordNet().getWordElement("метр");
			TestCase.assertTrue(wordElement3!=null);
			TextElement[] possibleContinuations = editable.getWordNet().getPossibleContinuations(wordElement3);
			parse.execute(editable);
//			if (possibleContinuations!=null&&possibleContinuations.length>0)
//			{
//				System.out.println("Continuations already here");
//			}
			TextElement wordElement = editable.getWordNet().getWordElement("_ALL_DIMENSION_UNITS".toLowerCase());
			SemanticRelation[] semanticRelations = wordElement.getConcepts()[0].getSemanticRelations();
			for (SemanticRelation q:semanticRelations){
				if (q.relation!=SemanticRelation.SPECIALIZATION){
					TestCase.assertTrue(false);		
				}
			}
			File createTempFile = File.createTempFile("aaaa", "zzzz");
			editable.store(createTempFile);
			com.onpositive.semantic.words3.ReadOnlyMapWordNet mm=new com.onpositive.semantic.words3.ReadOnlyMapWordNet(new DataInputStream(new BufferedInputStream(new FileInputStream(createTempFile))));
			createTempFile.deleteOnExit();
			wordElement = mm.getWordElement("_ALL_DIMENSION_UNITS".toLowerCase());
			SemanticRelation[]semanticRelations1 = wordElement.getConcepts()[0].getSemanticRelations();
			if (!Arrays.equals(semanticRelations, semanticRelations1)){
				TestCase.assertTrue(false);
			}
			TextElement wordElement2 = mm.getWordElement("м/c");
			SemanticRelation[] semanticRelations2 = wordElement2.getConcepts()[0].getSemanticRelations();
			for (SemanticRelation q:semanticRelations2){
				TestCase.assertTrue(q.getWord().getParentTextElement().getBasicForm().equals("_UNITS_SPEED".toLowerCase()));
				TestCase.assertTrue(q.relation==SemanticRelation.GENERALIZATION_BACK_LINK);
			}
			wordElement3 = mm.getWordElement("метр");
			TestCase.assertTrue(wordElement3!=null);
			possibleContinuations = mm.getPossibleContinuations(wordElement3);
			HashSet<String>ss=new HashSet<String>();
			for (TextElement q:possibleContinuations){
				TextElement[] parts = q.getParts();
//				System.out.println(Arrays.toString(parts));
				ss.add(q.getBasicForm());
			}
			TestCase.assertTrue(ss.contains("метр в секунду"));
			wordElement3 = mm.getWordElement("кмч");
			MetaLayer<Object> layer = mm.getMetaLayers().getLayer("relation_to_primary");
			Object value = layer.getValue(wordElement3.getConcepts()[0]);
			TestCase.assertEquals(value, 0.27777777);
			wordElement3 = mm.getWordElement("километр");
			possibleContinuations = mm.getPossibleContinuations(wordElement3);
			TestCase.assertTrue(possibleContinuations.length>0);
			layer = mm.getMetaLayers().getLayer("formula");
			TextElement wordElement4 = mm.getWordElement("_units_area");
			Object value2 = layer.getValue(wordElement4);
			TestCase.assertEquals("_UNITS_SIZE*_UNITS_SIZE", value2);
			layer = mm.getMetaLayers().getLayer("primary_unit");
			wordElement4 = mm.getWordElement("_units_size");
			value2 = layer.getValue(wordElement4);
			TestCase.assertEquals(mm.getWordElement("метр").getConcepts()[0], value2);			 
		} catch (Exception e) {
			TestCase.assertTrue(false);
		}
	}
}
