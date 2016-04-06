package com.onpositive.semantic.words3.prediction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.onpositive.semantic.wordnet.GrammarRelation;
import com.onpositive.semantic.wordnet.Grammem;
import com.onpositive.semantic.wordnet.TextElement;
import com.onpositive.semantic.words3.ReadOnlyWordNet;
import com.onpositive.semantic.words3.suggestions.GuessedGrammarRelation;
import com.onpositive.semantic.words3.suggestions.GuessedTextElement;

import static com.onpositive.semantic.words3.prediction.PredictionUtil.*;

public class MapPredictionHelper implements IPredictionHelper {
	
	protected ReadOnlyWordNet wordNet;
	protected List<String> bases;
	protected Set<String> endings;
	
	public MapPredictionHelper(ReadOnlyWordNet wordNet, List<String> bases,
			Set<String> endings) {
		super();
		this.wordNet = wordNet;
		this.bases = bases;
		this.endings = endings;
	}
	
	public MapPredictionHelper(ReadOnlyWordNet wordNet) {
		this.wordNet = wordNet;
		bases = new ArrayList<String>();
		endings = new HashSet<String>();
		PredictionUtil.rebuildPredictionLists(wordNet, bases, endings);
	}

	@Override
	public GrammarRelation[] getForms(String word) {
		String reversedWord = reverse(word);                                                                      
		List<String> suitableEndings = new ArrayList<String>();                                                   
		for (int i = 0; i < reversedWord.length() - 2; i++) {                                                     
			String guessedEnding = reversedWord.substring(0, i+1);                                                
			if (endings.contains(guessedEnding)) {                                                                                        
				suitableEndings.add(guessedEnding);                                                               
			} else {                                                                                              
				break;                                                                                            
			}                                                                                                     
		}                                                                                                         
		                                                                                                          
		Collections.sort(suitableEndings, new Comparator<String>() {                                              
		                                                                                                          
			@Override                                                                                             
			public int compare(String o1, String o2) {                                                            
				return o2.length() - o1.length();                                                                 
			}                                                                                                     
		});                                                                                                       
		List<GrammarRelation> resList = new ArrayList<GrammarRelation>();                                         
		for (String ending : suitableEndings) {                                                                   
			String reversedBase = reverse(word.substring(0,word.length() - ending.length()));                     
			int baseIdx = Collections.binarySearch(bases, reversedBase);                                      
			String guessedBase;                                                                                   
			if (baseIdx >= 0) {                                                                                   
				guessedBase = word.substring(0,word.length() - ending.length());                                  
				GrammarRelation[] forms = wordNet.getPossibleGrammarForms(guessedBase + reverse(ending));         
				resList.addAll(Arrays.asList(forms));                                                             
			} else {                                                                                              
				int idx = -baseIdx - 1;                                                                           
				resList.addAll(Arrays.asList(guessWord(bases, wordNet, ending, reversedBase, idx - 1)));      
				resList.addAll(Arrays.asList(guessWord(bases, wordNet, ending, reversedBase, idx)));          
				resList.addAll(Arrays.asList(guessWord(bases, wordNet, ending, reversedBase, idx + 1)));      
			}                                                                                                     
		}                                                                                                         
		return resList.toArray(new GrammarRelation[0]);                                                           
	}
	
	private GrammarRelation[] guessWord(List<String> basesList,
			ReadOnlyWordNet wordNet, String ending, String reversedBase, int idx) {
		String guessedBase = basesList.get(idx);
//		int common = 0;
//		for (int i = 0; i < Math.min(guessedBase.length(), reversedBase.length()); i++) {
//			if (guessedBase.charAt(i) == reversedBase.charAt(i)) {
//				common++;
//			} else {
//				break;
//			}
//		}
		GrammarRelation[] possibleGrammarForms = wordNet.getPossibleGrammarForms(reverse(guessedBase) + reverse(ending));
		if (possibleGrammarForms != null) {
//			String basicForm = possibleGrammarForms[0].getWord().getBasicForm();
			if (possibleGrammarForms!=null&&possibleGrammarForms.length>0){
				GuessedGrammarRelation[] gs=new GuessedGrammarRelation[possibleGrammarForms.length];
				int j=0;
				for (GrammarRelation q:possibleGrammarForms){
					TextElement word = q.getWord();
					if (word==null){
						continue;
					}
					String basicForm = word.getBasicForm();
					if (basicForm.length()<5){
						if (Grammem.PartOfSpeech.VERB.mayBeThisPartOfSpech(possibleGrammarForms)){
							return new GrammarRelation[0]; 
						}
					}
					String guessedEnding = basicForm.substring(guessedBase.length()); 
					String guessedWord = reverse(reversedBase) + guessedEnding;
					GuessedTextElement ts=new GuessedTextElement(wordNet, guessedWord);
					gs[j++]=new GuessedGrammarRelation(wordNet, q.conceptId, q.relation, ts);
				}
				return gs;
			}
		}
		return new GrammarRelation[0];
	}
}
