package com.onpositive.wikipedia.workbench.words.primary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;

import com.onpositive.semantic.wordnet.Grammem.PartOfSpeech;
import com.onpositive.semantic.wordnet.MeaningElement;
import com.onpositive.semantic.wordnet.SemanticRelation;
import com.onpositive.semantic.wordnet.WordNetProvider;
import com.onpositive.text.analysis.BasicCleaner;
import com.onpositive.text.analysis.IToken;
import com.onpositive.text.analysis.lexic.WordFormToken;
import com.onpositive.text.analysis.lexic.disambig.ILexicLevelDisambiguator;
import com.onpositive.text.analysis.syntax.SyntaxParser;
import com.onpositive.text.morphology.dataset.prepare.DemoEstimationData;
import com.onpositive.text.morphology.dataset.prepare.DemoEstimationData.EstimatedToken;

public class SimpleTitleModel {

	private static boolean USE_FILTERING = true;
	private String text;
	private WordFormToken mainWord;

	private List<IToken> wordFormTokens;
	private LinkedHashSet<WordFormToken>mainwords=new LinkedHashSet<>();

	public SimpleTitleModel(String t) {
		wordFormTokens = getWordFormTokens(t);
		text = t.replace('_', ' ').toLowerCase();
		MainWordCollector mainWordCollector = new MainWordCollector();
		wordFormTokens.stream().forEach(x -> mainWordCollector.visit(x));
		if (mainWordCollector.tks.size() == 1) {
			mainWord = mainWordCollector.tks.iterator().next();
		} 
		mainwords.addAll(mainWordCollector.tks);
	}

	public WordFormToken getMainWord() {
		return mainWord;
	}

	public String getText() {
		return text;
	}

	public List<IToken> getSyntaxParseResults() {
		return wordFormTokens;
	}

	public boolean isPlural(){
		return false;
	}

	public boolean hasSameMainWords(SimpleTitleModel tm){
		for (WordFormToken t:this.mainwords){
			for (WordFormToken t1:tm.mainwords){
				if (interleave(t,t1)){
					return true;
				}
			}
		}
		return false;
	}
	
	private boolean interleave(WordFormToken t, WordFormToken t1) {
		if (t.getBasicForm().equals(t1.getBasicForm())){
			return true;
		}
		for (MeaningElement e:t.getMeaningElements()){
			for (MeaningElement e1:t1.getMeaningElements()){
				final SemanticRelation[] semanticRelations = e.getSemanticRelations();
				final SemanticRelation[] semanticRelations1 = e1.getSemanticRelations();
				for (SemanticRelation r:semanticRelations){
					if (r.relation!=SemanticRelation.MERONIM&&r.relation!=SemanticRelation.ANTONIM){
						if (r.getWord().equals(e1)){
							return true;
						}
					}
				}
				for (SemanticRelation r:semanticRelations1){
					if (r.relation!=SemanticRelation.MERONIM&&r.relation!=SemanticRelation.ANTONIM){
						if (r.getWord().equals(e)){
							return true;
						}
					}
				}
			}
		}
		
		return false;
	}

	public static List<IToken> getWordFormTokens(String str) {
		str = str.replace('_', ' ');
		HashMap<String, HashSet<PartOfSpeech>> filterMap = buildPartOfSpeechFilters(str);

		SyntaxParser syntaxParser = new SyntaxParser(WordNetProvider.getInstance());
		syntaxParser.setIgnoreCombinations(true);
		if (!filterMap.isEmpty()) {
			syntaxParser.setDisambiguator(new ILexicLevelDisambiguator() {

				@Override
				public WordFormToken[] disambiguate(WordFormToken[] wordFormTokens, IToken origToken) {
					ArrayList<WordFormToken> ts = new ArrayList<>();
					HashSet<PartOfSpeech> set = filterMap.get(origToken.getStringValue());
					for (WordFormToken t : wordFormTokens) {
						if (set != null) {
							if (set.contains(t.getPartOfSpeech())) {
								ts.add(t);
							}
						} else {
							ts.add(t);
						}
					}
					return ts.toArray(new WordFormToken[ts.size()]);
				}
			});
		}
		List<IToken> syntax = syntaxParser.parse(str);
		syntax = new BasicCleaner().clean(syntax);
		return syntax;
	}

	private static HashMap<String, HashSet<PartOfSpeech>> buildPartOfSpeechFilters(String str) {
		HashMap<String, HashSet<PartOfSpeech>> filterMap = new HashMap<>();
		if (USE_FILTERING) {
			DemoEstimationData ds = new DemoEstimationData(str);
			ArrayList<EstimatedToken> results = ds.results;
			ArrayList<IToken> primitives = new ArrayList<>();
			for (EstimatedToken t : results) {
				HashSet<PartOfSpeech> doSelect = t.doSelect();
				primitives.add(t.primitiveToken);
				filterMap.put(t.primitiveToken.getStringValue(), doSelect);
			}
		}
		return filterMap;
	}

	public static String basicForm(IToken t) {

		return t.getShortStringValue();
	}
	
	@Override
	public String toString() {
		return mainwords.toString();
	}

}
