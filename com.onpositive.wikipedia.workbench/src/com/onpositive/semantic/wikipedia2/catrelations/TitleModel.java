package com.onpositive.semantic.wikipedia2.catrelations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import com.onpositive.semantic.wordnet.AbstractWordNet;
import com.onpositive.semantic.wordnet.GrammarRelation;
import com.onpositive.semantic.wordnet.Grammem;
import com.onpositive.semantic.wordnet.Grammem.Case;
import com.onpositive.semantic.wordnet.MeaningElement;
import com.onpositive.semantic.wordnet.TextElement;
import com.onpositive.semantic.wordnet.WordNetProvider;

public class TitleModel {

	static AbstractWordNet instance = WordNetProvider.getInstance();

	static HashMap<String, TitleModel> nc = new HashMap<String, TitleModel>();
	static HashMap<String, TitleModel> hc = new HashMap<String, TitleModel>();

	public static TitleModel get(String model, boolean historyOk) {
		model = model.replace(' ', '_');
		if (hc.size() > 600000) {
			hc.clear();
		}
		if (nc.size() > 600000) {
			nc.clear();
		}
		if (historyOk) {
			if (hc.containsKey(model)) {
				return hc.get(model);
			}
			TitleModel titleModel = new TitleModel(model, historyOk);
			hc.put(model, titleModel);
			return titleModel;
		}
		if (nc.containsKey(model)) {
			return nc.get(model);
		}
		TitleModel titleModel = new TitleModel(model, historyOk);
		nc.put(model, titleModel);
		return titleModel;
		// return new TitleModel(model);
	}

	public static class WordModel {

		protected String text;

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((words == null) ? 0 : words.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			WordModel other = (WordModel) obj;
			if (words == null) {
				if (other.words != null)
					return false;
			} else if (!words.equals(other.words))
				return false;
			return true;
		}

		public String getText() {
			return text;
		}

		protected GrammarRelation[] relations;
		protected HashSet<TextElement> words;
		protected int index;
		protected boolean suggestions;

		public WordModel(String text, int index) {
			super();
			this.text = text;
			this.index = index;
			relations = instance.getPossibleGrammarFormsWithSuggestions(text);
			words = new HashSet<TextElement>();

			if (relations != null) {
				for (GrammarRelation q : relations) {

					if (q == null) {
						continue;
					}
					if (q instanceof com.onpositive.semantic.words3.suggestions.GuessedGrammarRelation) {
						suggestions = true;
					}
					TextElement word = q.getWord();
					if (word != null) {
						words.add(word);
					}
				}
			}
		}

		public boolean match(String text) {
			for (TextElement q : words) {
				if (q.getBasicForm().equals(text)) {
					return true;
				}
			}
			return false;
		}

		@Override
		public String toString() {
			return text;
		}

		public boolean isGoodNoun() {
			if (relations == null) {
				return false;
			}
			if (text.length() <= 2) {
				return false;
			}
			if (Grammem.PartOfSpeech.PREP.mayBeThisPartOfSpech(relations)) {
				return false;
			}
			if (Grammem.PartOfSpeech.CONJ.mayBeThisPartOfSpech(relations)) {
				return false;
			}
			for (GrammarRelation q : relations) {
				if (q == null) {
					continue;
				}
				boolean mayBeNoun = q.hasAllGrammems(Grammem.PartOfSpeech.NOUN,
						Grammem.Case.NOMN);
				if (mayBeNoun) {
					if (q.hasArLeastOneOfGrammems(
							Grammem.SingularPlural.PLURAL,
							Grammem.SingularPlural.Pl1,
							Grammem.SingularPlural.UNCHANGABLE)) {
						return true;
					}
				}
			}
			return false;
		}

		public boolean isAdj() {
			if (relations == null) {
				return false;
			}
			if (text.length() <= 2) {
				return false;
			}
			if (Grammem.PartOfSpeech.PREP.mayBeThisPartOfSpech(relations)) {
				return false;
			}
			if (Grammem.PartOfSpeech.CONJ.mayBeThisPartOfSpech(relations)) {
				return false;
			}
			for (GrammarRelation q : relations) {
				if (q == null) {
					continue;
				}
				boolean mayBeNoun = q.hasGrammem(Grammem.PartOfSpeech.ADJF);
				if (mayBeNoun) {

					return true;

				}
			}
			return false;
		}

		public boolean isSomeNoun(Grammem.Case acceptGt) {
			if (relations == null) {
				return false;
			}
			if (text.length() < 4) {
				return false;
			}

			if (Grammem.PartOfSpeech.PREP.mayBeThisPartOfSpech(relations)) {
				return false;
			}
			if (Grammem.PartOfSpeech.CONJ.mayBeThisPartOfSpech(relations)) {
				return false;
			}
			for (GrammarRelation q : relations) {
				if (q == null) {
					continue;
				}
				if (acceptGt != null) {
					boolean mayBeNoun = q.hasAllGrammems(
							Grammem.PartOfSpeech.NOUN, Grammem.Case.NOMN);
					mayBeNoun |= q.hasAllGrammems(Grammem.PartOfSpeech.NOUN,
							acceptGt);
					if (mayBeNoun) {
						return true;
					}
				} else {
					boolean mayBeNoun = q.hasAllGrammems(
							Grammem.PartOfSpeech.NOUN, Grammem.Case.NOMN);
					if (mayBeNoun) {
						return true;
					}
				}
			}
			return false;
		}

		public boolean isPlural() {
			return Grammem.SingularPlural.PLURAL.containedIn(relations);
		}

		public String getBasicForm() {
			if (words.size() == 1) {
				return words.iterator().next().getBasicForm();
			}
			return null;
		}

		public boolean isGoodPluralNoun() {
			return false;
		}

		public boolean isPrtf() {
			if (relations != null) {
				if (Grammem.PartOfSpeech.PRTF.mayBeThisPartOfSpech(relations)) {
					return true;
				}
				if (Grammem.PartOfSpeech.GRND.mayBeThisPartOfSpech(relations)) {
					return true;
				}
			}
			return false;
		}

		public boolean isVrb() {
			if (relations != null) {
				if (Grammem.PartOfSpeech.VERB.mayBeThisPartOfSpech(relations)) {
					return true;
				}
			}
			return false;
		}

	}

	public ArrayList<WordModel> models = new ArrayList<TitleModel.WordModel>();
	private ArrayList<WordModel> candidates;
	boolean alternativeOptions;

	private boolean isIn;

	private boolean isVrb;

	public TitleModel(String title, boolean historyOk) {
		this.historyOk = historyOk;
		prepareModelSet(title);
		getAllNounsInGoodForm();
		if (!hasSingleCore()) {
			if (candidates.size() == 2) {
				WordModel a1 = candidates.get(0);
				WordModel a2 = candidates.get(1);
				if (a2.index - a1.index == 2) {
					if (models.get(a1.index + 1).text.equals("и")) {
						alternativeOptions = true;
					}
				}
			}
		}
		if (!alternativeOptions) {
			cleanupNouns();
		}
		if (!models.isEmpty()) {
			isPrep = models.get(0).isPrtf();
			isVrb = models.get(0).isVrb();
			if (models.size() > 1) {
				if (models.get(1).text.equals("в")
						|| models.get(1).text.equals("во")
						|| models.get(1).text.equals("на")) {
					isIn = true;
				}
			}
		}
	}

	public boolean isIn() {
		return isIn;
	}

	public boolean isPrtf() {
		return isPrep;
	}

	static HashSet<String> knownStopWords = new HashSet<String>();
	static {
		knownStopWords.add("на");
		knownStopWords.add("в");
		knownStopWords.add("для");
		knownStopWords.add("из");
		knownStopWords.add("по");
	}

	void cleanupNouns() {
		removeYear();
		if (candidates.size()==1){
			return;
		}
		if (candidates.size() > 1) {
			cleanGenerationalCaseIfPossible();
		}
		if (candidates.size() > 1) {
			removeGeo();
		}
		if (candidates.size() > 1) {
			removeADJECTIVES();
		}
		if (candidates.size() > 1) {
			tryDetectAdjective();
		}
		if (candidates.size() > 1) {
			cleanGenerationalCaseIfPossible();
		}
		WordModel cand = null;
		cleanupAfterCONJURATION(cand);
		if (candidates.size() > 1) {
			WordModel wordModel = candidates.get(0);
			if (wordModel.relations.length == 1) {
				if (!Grammem.Case.GENT.containedIn(wordModel.relations)) {
					if (Grammem.SingularPlural.PLURAL
							.containedIn(wordModel.relations)) {
						if (Grammem.Case.NOMN.containedIn(wordModel.relations)) {
							candidates.clear();
							candidates.add(wordModel);
						}
					}
				}
			}
		}
		if (candidates.size() > 1) {
			ArrayList<WordModel> nc = new ArrayList<TitleModel.WordModel>();

			for (WordModel q : candidates) {
				if (q.relations!=null){
					boolean hasS=false;
					for (GrammarRelation z:q.relations){
						MeaningElement[] concepts = z.getWord().getConcepts();
						if (concepts!=null){
							for (MeaningElement e:concepts){
								if (e.getGrammems().contains(Grammem.SemanGramem.SURN)) {
									hasS=true;
									break;
								}	
								if (e.getGrammems().contains(Grammem.SemanGramem.PATR)) {
									hasS=true;
									break;
								}
							}
						}
					}
					if (!hasS){
						nc.add(q);
					}
				}

			}
			if (nc.size()>0){
			candidates = nc;
			}
		}
	}

	private void tryDetectAdjective() {
		// TODO Auto-generated method stub
		if (candidates.size() > 1) {
			/*ArrayList<WordModel>mz=new ArrayList<TitleModel.WordModel>();
			for (WordModel m : candidates) {
				if (m.index>0){
					WordModel wordModel = models.get(m.index-1);
					System.out.println("a");
				}
				
			}*/			
		}
	}

	private void removeGeo() {
		if (candidates.size() > 1) {
			ArrayList<WordModel>mz=new ArrayList<TitleModel.WordModel>();
			for (WordModel m : candidates) {
				if (!isGeo(m)){
					mz.add(m);
				}
			}
			if  (!mz.isEmpty()){
				candidates=mz;
			}
		}
	}

	void cleanupAfterCONJURATION(WordModel cand) {
		if (candidates.size() > 1) {
			for (WordModel m : models) {
				if (candidates.contains(m)) {
					cand = m;
				}
				if (knownStopWords.contains(m.text)) {
					if (cand != null) {
						candidates.clear();
						candidates.add(cand);
						break;
					}
				}
			}
		}
	}

	void getAllNounsInGoodForm() {
		candidates = new ArrayList<TitleModel.WordModel>();
		for (WordModel q : models) {
			if (q.isSomeNoun((Case) acceptGT)) {
				candidates.add(q);
			}
		}
	}

	Grammem acceptGT = null;

	public boolean singleWord;

	void prepareModelSet(String title) {
		title = title.toLowerCase();
		title = title.replace(',', '_');
		title = title.replace(':', '_');
		int indexOf = title.indexOf('(');
		if (indexOf != -1) {
			int indexOf2 = title.indexOf(')');
			if (indexOf2 != -1 && indexOf2 > indexOf) {
				String substring = title.substring(indexOf + 1, indexOf2);
				boolean isRemark = false;
				try {
					substring = substring.replace("—", "0");
					substring = substring.replace("-", "0");
					Long.parseLong(substring.trim());
					isRemark = true;
				} catch (NumberFormatException e) {

				}
				if (substring.length() <= 3) {
					isRemark = true;
				}
				
				
				if (!isRemark) {
					TitleModel titleModel = TitleModel.get(substring, false);
					if  (!titleModel.isGeo()){
						title = substring;
					}
					else{
						title = title.substring(0, indexOf);
					}
				} else {
					title = title.substring(0, indexOf);
				}
			}
		}
		indexOf = title.indexOf('«');
		if (indexOf != -1) {
			title = title.substring(0, indexOf);
		}

		title = title.replace('(', '_');
		title = title.replace(')', '_');

		String replace = title.replace("_год_в_", "_");
		if (replace.length() != title.length()) {
			acceptGT = Case.LOCT;
		}
		title = replace;

		if (title.contains("статьи_o_")) {
			title = title.replace("статьи_o_", "_");
			acceptGT = Case.LOCT;
		}
		if (title.contains("статьи_о_")) {
			title = title.replace("статьи_о_", "");
			acceptGT = Case.LOCT;
		}
		if (title.contains("статьи_об_")) {
			title = title.replace("статьи_об_", "");
			acceptGT = Case.LOCT;
		}
		if (title.contains("статьи_по_")) {
			title = title.replace("статьи_по_", "");
			acceptGT = Case.LOCT;
		}

		String[] split = title.split("_");
		singleWord = split.length == 1;
		int a = 0;
		for (String word : split) {
			a++;
			if (word.equals("классификация")) {
				acceptGT = Case.GENT;
				continue;
			}
			if (word.equals("годы")) {
				acceptGT = Case.LOCT;
				continue;
			}
			if (word.equals("типы")) {
				acceptGT = Case.GENT;
				continue;
			}
			if (word.equals("разделы")) {
				acceptGT = Case.GENT;
				continue;
			}
			if (word.equals("виды")) {
				acceptGT = Case.GENT;
				continue;
			}
			if (word.equals("сорта")) {
				acceptGT = Case.GENT;
				continue;
			}
			if (historyOk) {
				if (word.equals("история")) {
					acceptGT = Case.GENT;
					continue;
				}
			}
			if (word.contains("Википедия:")) {
				continue;
			}

			models.add(new WordModel(word, a - 1));
		}
	}

	private boolean isGeo() {
		for (WordModel q : candidates) {
			boolean modelCanbeGeo = isGeo(q);
			if (modelCanbeGeo){
				return true;
			}

		}
		return false;
	}

	boolean isGeo(WordModel q) {
		boolean modelCanbeGeo=false;
		if (q.relations!=null){
			for (GrammarRelation z:q.relations){
				MeaningElement[] concepts = z.getWord().getConcepts();
				if (concepts!=null){
					for (MeaningElement e:concepts){
						if (e.getGrammems().contains(Grammem.SemanGramem.TOPONIM)) {
							modelCanbeGeo=true;
						}	
						if (e.getGrammems().contains(Grammem.SemanGramem.TOPONIM)) {
							modelCanbeGeo=true;
						}
					}
				}
			}				
		}
		return modelCanbeGeo;
	}

	boolean historyOk;

	private boolean isPrep;

	void removeADJECTIVES() {
		WordModel zz = candidates.get(0);
		for (GrammarRelation a : zz.relations) {
			if (a.hasGrammem(Grammem.PartOfSpeech.ADJF)) {
				// должно быть согласование по времени
				WordModel wordModel = candidates.get(1);
				if (a.hasGrammem(Grammem.SingularPlural.PLURAL)) {
					if (Grammem.SingularPlural.PLURAL
							.containedIn(wordModel.relations)) {
						candidates.remove(0);
						break;
					}
				}
				if (a.hasGrammem(Grammem.SingularPlural.SINGULAR)) {
					if (Grammem.SingularPlural.SINGULAR
							.containedIn(wordModel.relations)) {
						candidates.remove(0);
						break;
					}
				}

			}
		}
	}

	void cleanGenerationalCaseIfPossible() {
		/*
		 * WordModel cand = null; for (int a = 0; a < candidates.size(); a++) {
		 * WordModel wordModel = candidates.get(a); if (cand != null &&
		 * Grammem.PartOfSpeech.ADJF.containedIn(cand.relations)) { if
		 * (cand.index==wordModel.index-1){
		 * 
		 * } } cand = wordModel; }
		 */
		WordModel zz = candidates.get(0);
		boolean isClean = true;
		isClean &= !Grammem.PartOfSpeech.ADJF.containedIn(zz.relations);
		if (isClean) {
			ArrayList<WordModel> nc = new ArrayList<TitleModel.WordModel>();
			nc.add(zz);
			for (WordModel q : candidates) {
				if (q != zz) {
					if (q.index==0||!Grammem.Case.GENT.containedIn(q.relations)&&!Grammem.Case.GEN1.containedIn(q.relations)&&!Grammem.Case.GEN2.containedIn(q.relations)) {
						nc.add(q);
					}
				}
			}
			if (nc.size()>0){
				candidates = nc;
			}
		}

	}

	void removeYear() {
		if (candidates.size() > 1) {
			WordModel wordModel = candidates.get(candidates.size() - 1);
			if (wordModel.words.size() == 0) {
				candidates.remove(candidates.size() - 1);
			} else if (wordModel.words.size() == 1
					&& wordModel.words.iterator().next().getBasicForm()
							.equals("год")) {
				candidates.remove(candidates.size() - 1);
			}
		}
	}

	public WordModel getSingleCore() {
		return candidates.get(0);
	}

	public boolean hasSingleCore() {
		return candidates.size() == 1;
	}

	public ArrayList<WordModel> cores() {
		return candidates;
	}

	public boolean isVrb() {
		return isVrb;
	}

}
