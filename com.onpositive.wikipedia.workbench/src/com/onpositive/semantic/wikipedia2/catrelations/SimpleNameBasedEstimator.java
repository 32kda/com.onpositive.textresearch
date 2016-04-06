package com.onpositive.semantic.wikipedia2.catrelations;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import com.onpositive.semantic.search.core.ICategory;
import com.onpositive.semantic.search.core.IDocument;
import com.onpositive.semantic.wikipedia2.WikiCat;
import com.onpositive.semantic.wikipedia2.WikiEngine2;
import com.onpositive.semantic.wikipedia2.catrelations.TitleModel.WordModel;
import com.onpositive.semantic.wordnet.Grammem;
import com.onpositive.semantic.wordnet.MeaningElement;
import com.onpositive.semantic.wordnet.SemanticRelation;
import com.onpositive.semantic.wordnet.TextElement;

public class SimpleNameBasedEstimator extends AbstractRelationEstimator {

	private static final String ID = "simple_name_based";

	public static final int RELATION_MATCH = 4;

	public static final int IDEAL_MATCH = 1;

	public static final int NO_RELATION = 10;

	private WikiEngine2 engine;

	private IExtraRelationComparer comparer;

	public IExtraRelationComparer getComparer() {
		return comparer;
	}

	public void setComparer(IExtraRelationComparer comparer) {
		this.comparer = comparer;
	}

	public SimpleNameBasedEstimator(WikiEngine2 eng) {
		super("theme_similarity", ID, "По имени");
		this.engine = eng;
	}

	protected boolean historyOk = true;

	public boolean isHistoryOk() {
		return historyOk;
	}

	public void setHistoryOk(boolean historyOk) {
		this.historyOk = historyOk;
	}

	@Override
	public byte relation(int c1, int c2, PrintStream log) {
		String string = engine.getCategoryTitles().get(c1);
		String string1 = engine.getCategoryTitles().get(c2);
		return calc(string, string1);
	}

	boolean allowRelations;

	public boolean isAllowRelations() {
		return allowRelations;
	}

	public void setAllowRelations(boolean allowRelations) {
		this.allowRelations = allowRelations;
	}

	public boolean isRelated(String parent, String child) {
		return calc(parent, child) != NO_RELATION;
	}

	byte calc(String string, String string1) {
		string = initialClian(string);
		string1 = initialClian(string1);
		if (string1.contains(":")) {
			return 15;
		}

		byte innerCheck = innerCheck(string, string1);
		if (innerCheck == NO_RELATION) {
			int max = lastCap(string);
			int max1 = lastCap(string1);
			if (max > 0 || max1 > 0) {
				String str = max > 0 ? cleanStuff(string.substring(0, max))
						: string;
				String str1 = max1 > 0 ? cleanStuff(string1.substring(0, max1))
						: string1;

				byte innerCheck2 = innerCheck(str, str1);
				if (innerCheck2 != NO_RELATION) {
					// System.out.println(str+":->"+str1);
					return innerCheck2;
				}
			}
		}
		return innerCheck;
	}

	static String cleanStuff(String substring) {
		substring = substring.replace('_', ' ');
		substring = substring.replace('(', ' ');
		substring = substring.replace('«', ' ');
		return substring.trim();
	}

	private String initialClian(String string) {
		if (string.startsWith("Статьи_проекта_")) {
			string = string.substring("Статьи_проекта_".length());
		}
		if (string.startsWith("Статьи_проекта_")) {
			string = string.substring("Статьи_проекта_".length());
		}
		if (string.startsWith("Добротные_статьи_проекта_")) {
			string = string.substring("Добротные_статьи_проекта_".length());
		}
		if (string.startsWith("Хорошие_статьи_проекта_")) {
			string = string.substring("Хорошие_статьи_проекта_".length());
		}
		if (string.startsWith("Незавершённые_статьи_проекта_")) {
			string = string.substring("Незавершённые_статьи_проекта_".length());
		}
		if (string.startsWith("Скрытые_категории")) {
			string = string.substring("Скрытые_категории".length());
		}
		if (string.startsWith("Избранные_списки")) {
			string = string.substring("Избранные_списки".length());
		}

		if (string.startsWith("Незавершённые_статьи_по_")) {
			string = string.substring("Незавершённые_статьи_по_".length());
		}

		return string;
	}

	static int lastCap(String string) {
		int max = 0;
		for (int a = 0; a < string.length(); a++) {

			if (Character.isUpperCase(string.charAt(a))) {
				max = a;
				if (a > 0) {
					break;
				}
			}
		}
		return max;
	}

	byte innerCheck(String string, String string1) {
		boolean historyOk = false;
		if (this.historyOk && string.toLowerCase().contains("история")
				&& string1.toLowerCase().contains("история")) {
			historyOk = true;
		}
		TitleModel tm = TitleModel.get(string, historyOk);
		TitleModel tm1 = TitleModel.get(string1, historyOk);
		if (tm.cores().isEmpty()){
			if (tm1.cores().isEmpty()){
				if (string.contains("_по_")||string.contains(" по ")){
					return RELATION_MATCH;
				}
			}
		}
		if (tm.isPrtf()) {
			if (tm1.isPrtf()) {
				if (tm.isIn() && tm1.isIn()) {
					return RELATION_MATCH;
				}
				if (tm.models.size() == 1) {
					return RELATION_MATCH;
				}
			}
		}
		if (tm1.isPrtf()) {
			if (tm.models.size() == tm1.models.size() - 1) {
				ArrayList<WordModel> arrayList = new ArrayList<WordModel>(
						tm.models);
				ArrayList<WordModel> arrayList1 = new ArrayList<WordModel>(
						tm1.models);
				arrayList1.remove(0);
				if (arrayList.equals(arrayList1)) {
					return RELATION_MATCH;
				}
			}

		}
		if (tm.isVrb() && tm1.isVrb()) {
			if (tm.isIn() && tm1.isIn()) {
				if (tm.models.get(0).getText()
						.equals(tm1.models.get(0).getText())) {
					return RELATION_MATCH;
				}
			}

		}
		if (tm.hasSingleCore()) {
			if (tm1.hasSingleCore()) {
				WordModel singleCore = tm.getSingleCore();
				WordModel singleCore1 = tm1.getSingleCore();
				return checkSimilarity(singleCore, singleCore1, allowRelations);
			} else {
				ArrayList<WordModel> cores = tm1.cores();
				if (!cores.isEmpty()&&Grammem.SingularPlural.PLURAL
						.containedIn(cores.get(0).relations)) {
					boolean allSuggestions = true;
					for (int a = 1; a < cores.size(); a++) {
						WordModel wordModel = cores.get(a);
						if (wordModel.suggestions
								&& !Grammem.SingularPlural.PLURAL
										.containedIn(wordModel.relations)) {
							continue;
						}
						allSuggestions = false;
					}
					if (allSuggestions) {
						WordModel singleCore = tm.getSingleCore();
						return checkSimilarity(singleCore, cores.get(0),
								allowRelations);
					}
				}
			}
		}
		byte checkAlternative = checkAlternative(tm, tm1);
		if (checkAlternative != NO_RELATION) {
			return checkAlternative;
		}
		checkAlternative = checkAlternative(tm1, tm);
		if (checkAlternative != NO_RELATION) {
			return checkAlternative;
		}
		// оба - альтернативы
		if (tm.alternativeOptions && tm1.alternativeOptions) {
			for (WordModel m : tm1.cores()) {
				WordModel singleCore1 = m;
				for (WordModel q : tm.cores()) {
					byte checkSimilarity = checkSimilarity(singleCore1, q,
							allowRelations);
					if (checkSimilarity != NO_RELATION) {
						return checkSimilarity;
					}
				}
			}
		}
		if (!tm.cores().isEmpty()) {
			if (tm.cores().equals(tm1.cores())) {
				return RELATION_MATCH;
			}
		}
		return NO_RELATION;
	}

	byte checkAlternative(TitleModel tm, TitleModel tm1) {
		if (tm.alternativeOptions && tm1.hasSingleCore()) {
			WordModel singleCore1 = tm1.getSingleCore();
			for (WordModel q : tm.cores()) {
				byte checkSimilarity = checkSimilarity(singleCore1, q,
						allowRelations);
				if (checkSimilarity != NO_RELATION) {
					return checkSimilarity;
				}
			}
		}
		return NO_RELATION;
	}

	byte checkSimilarity(WordModel singleCore, WordModel singleCore1,
			boolean relations) {
		HashSet<TextElement> words = singleCore.words;
		HashSet<TextElement> words2 = singleCore1.words;
		if (words.equals(words2)) {
			return IDEAL_MATCH;
		}
		for (TextElement w1 : words) {
			for (TextElement w2 : words2) {
				if (w1.getBasicForm().endsWith(w2.getBasicForm())) {
					if (!w1.getBasicForm().startsWith("транс")) {
						return IDEAL_MATCH;
					}
				}
				if (w2.getBasicForm().endsWith(w1.getBasicForm())) {
					return IDEAL_MATCH;
				}
				if (relations) {
					byte compareRelations = compareRelations(w1, w2);
					if (compareRelations < NO_RELATION) {
						return compareRelations;
					}
				}
				// now lets check for semantic intersections
			}
		}
		return NO_RELATION;
	}

	byte compareRelations(TextElement w1, TextElement w2) {
		if (w1.hasGrammem(Grammem.SemanGramem.TOPONIM)
				|| w2.hasGrammem(Grammem.SemanGramem.TOPONIM)) {
			return NO_RELATION;
		}
		if (comparer != null) {
			if (comparer.isSub(w1.getBasicForm(), w2.getBasicForm())) {
				return RELATION_MATCH;
			}
		}
		MeaningElement[] concepts1 = w1.getConcepts();
		MeaningElement[] concepts2 = w2.getConcepts();
		for (MeaningElement concept1 : concepts1) {
			for (MeaningElement concept2 : concepts2) {
				HashSet<MeaningElement> hs1 = getSet(concept1);
				HashSet<MeaningElement> hs2 = getSet(concept2);
				for (MeaningElement q : hs2) {
					if (hs1.contains(q)) {
						return RELATION_MATCH;
					}
				}
				// hierarchically gather relations here
				// concept1.get
			}
		}
		return NO_RELATION;
	}

	static HashMap<MeaningElement, HashSet<MeaningElement>> mmms = new HashMap<MeaningElement, HashSet<MeaningElement>>();

	public static HashSet<MeaningElement> getSet(MeaningElement concept1) {
		if (mmms.containsKey(concept1)) {
			return mmms.get(concept1);
		}
		HashSet<MeaningElement> hs1 = new HashSet<MeaningElement>();
		gatherRelations(concept1, 0, hs1);
		mmms.put(concept1, hs1);
		return hs1;
	}

	private static void gatherRelations(MeaningElement concept1, int i,
			HashSet<MeaningElement> hashSet) {
		SemanticRelation[] semanticRelations = concept1.getSemanticRelations();
		for (SemanticRelation q : semanticRelations) {
			MeaningElement word = q.getWord();
			if (word == null) {
				continue;
			}
			if (hashSet.add(word)) {
				if (i < 4 && hashSet.size() < 100) {
					gatherRelations(concept1, i + 1, hashSet);
				}
			}
		}
	}

	public boolean isA(ICategory c1, ICategory c2) {
		if (relation(((WikiCat) c1).getIntId(), ((WikiCat) c2).getIntId(), null) < 5) {
			return true;
		}
		return false;
	}

	public boolean isA(ICategory cat, IDocument q) {
		if (calc(cat.getTitle(), q.getTitle()) < 5) {
			return true;
		}
		return false;
	}

}
