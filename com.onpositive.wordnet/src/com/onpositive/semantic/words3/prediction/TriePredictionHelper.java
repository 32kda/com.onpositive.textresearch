package com.onpositive.semantic.words3.prediction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import com.onpositive.semantic.wordnet.GrammarRelation;
import com.onpositive.semantic.wordnet.Grammem;
import com.onpositive.semantic.wordnet.TextElement;
import com.onpositive.semantic.words3.ReadOnlyWordNet;
import com.onpositive.semantic.words3.hds.StringToByteTrie;
import com.onpositive.semantic.words3.suggestions.GuessedGrammarRelation;
import com.onpositive.semantic.words3.suggestions.GuessedTextElement;

import static com.onpositive.semantic.words3.prediction.PredictionUtil.*;

public class TriePredictionHelper implements IPredictionHelper {
	
	protected StringToByteTrie basesTrie;
	protected StringToByteTrie endingsTrie;
	protected final ReadOnlyWordNet wordNet;

	public TriePredictionHelper(ReadOnlyWordNet wordNet, StringToByteTrie basesTrie, StringToByteTrie endingsTrie) {
		this.wordNet = wordNet;
		this.basesTrie = basesTrie;
		this.endingsTrie = endingsTrie;
	}
	
	/* (non-Javadoc)
	 * @see com.onpositive.semantic.words3.prediction.IPredictionHelper#getForms(java.lang.String)
	 */
	@Override
	public GrammarRelation[] getForms(String word) {
		List<GrammarRelation> resList = new ArrayList<GrammarRelation>();
		try {
			String reversedWord = reverse(word);
			List<String> suitableEndings = new ArrayList<String>();
			for (int i = 0; i < reversedWord.length() - 2; i++) {
				String guessedEnding = reversedWord.substring(0, i+1);
				if (endingsTrie.get(guessedEnding) != null) {
					suitableEndings.add(guessedEnding);
				} else {
					break;
				}
			}
			Collections.reverse(suitableEndings);
			for (String ending : suitableEndings) {
				String reversedBase = reverse(word.substring(0,word.length() - ending.length()));
				List<String> words = basesTrie.getStrings(reversedBase);
				GrammarRelation[] forms = null;
				if (words.size() == 1) {
					forms = wordNet.getPossibleGrammarForms(reverse(words.get(0)) + reverse(ending));
				}
				if (forms != null) {
					resList.addAll(Arrays.asList(forms));
				} else {
					for (String currentWord : words) {
							resList.addAll(Arrays.asList(guessWord(currentWord, wordNet, ending, reversedBase)));
					}
				}
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return resList.toArray(new GrammarRelation[0]);
	}
	
	private GrammarRelation[] guessWord(String guessedBase,
			ReadOnlyWordNet wordNet, String ending, String reversedBase) {
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
