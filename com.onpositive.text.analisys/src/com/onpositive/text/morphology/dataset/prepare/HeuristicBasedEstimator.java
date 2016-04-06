package com.onpositive.text.morphology.dataset.prepare;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.onpositive.semantic.wordnet.Grammem.PartOfSpeech;
import com.onpositive.semantic.wordnet.WordNetProvider;
import com.onpositive.text.analysis.BasicCleaner;
import com.onpositive.text.analysis.IToken;
import com.onpositive.text.analysis.lexic.PrimitiveTokenizer;
import com.onpositive.text.analysis.lexic.WordFormToken;
import com.onpositive.text.analysis.syntax.SentenceToken;
import com.onpositive.text.analysis.syntax.SyntaxParser;
import com.onpositive.text.analysis.utils.Stats;
import com.onpositive.text.morphology.dataset.prepare.DemoEstimationData.EstimatedToken;

public class HeuristicBasedEstimator {

	public static void render(String queryString) {
		SyntaxParser ps = new SyntaxParser(WordNetProvider.getInstance());
		List<IToken> parse = ps.parse(queryString);
		List<IToken> process = process(parse);
		MultiMap<Integer, WordFormToken> z = MultiMap.withList();
		fillMap(z, process, 0);
		PrimitiveTokenizer primitiveTokenizer = new PrimitiveTokenizer();

		List<IToken> tokenize = primitiveTokenizer.tokenize(queryString);
		for (IToken t : tokenize) {
			Set<PartOfSpeech> possiblePartsOfSpeach = DataSetPreparation.possiblePartsOfSpeech(t.getStringValue());
			Collection<WordFormToken> collection = z.get(t.getStartPosition());
			HashSet<PartOfSpeech> sp = new HashSet<>();
			if (collection == null) {
				sp = new HashSet<>(possiblePartsOfSpeach);
			} else {
				for (WordFormToken m : collection) {
					sp.add(m.getPartOfSpeech());
				}
			}

		}
	}

	public void process(DemoEstimationData demoEstimationData) {
		SyntaxParser ps = new SyntaxParser(WordNetProvider.getInstance());
		Stats.startSyntax();
		List<IToken> parse = ps.parse(demoEstimationData.originalString);
		Stats.finishSyntax();
		List<IToken> process = process(parse);
		MultiMap<Integer, WordFormToken> z = MultiMap.withList();
		fillMap(z, process, 0);
		PrimitiveTokenizer primitiveTokenizer = new PrimitiveTokenizer();

		List<IToken> tokenize = primitiveTokenizer.tokenize(demoEstimationData.originalString);
		for (IToken t : tokenize) {
			Set<PartOfSpeech> possiblePartsOfSpeach = DataSetPreparation.possiblePartsOfSpeech(t.getStringValue());
			Collection<WordFormToken> collection = z.get(t.getStartPosition());
			HashSet<PartOfSpeech> sp = new HashSet<>();
			if (collection == null) {
				sp = new HashSet<>(possiblePartsOfSpeach);
			} else {
				for (WordFormToken m : collection) {
					PartOfSpeech partOfSpeech = m.getPartOfSpeech();
					if (partOfSpeech != null) {
						sp.add(partOfSpeech);
					}
				}
				if (sp.isEmpty()) {
					sp = new HashSet<>(possiblePartsOfSpeach);
				}
			}
			EstimatedToken estimatedToken = demoEstimationData.ts.get(t.getStartPosition());
			if (estimatedToken != null) {
				estimatedToken.heuristic = sp;
			}
		}
	}

	private static void fillMap(MultiMap<Integer, WordFormToken> z, List<IToken> t, int level) {
		if (t == null) {
			return;
		}
		for (IToken tk : t) {
			if (tk instanceof WordFormToken) {
				if (level > 0) {
					z.add(tk.getStartPosition(), (WordFormToken) tk);
				}
			} else {
				fillMap(z, tk.getChildren(), level + 1);
			}
		}
	}

	public static void main(String[] args) {
		render("не покупать белил совсем");
	}

	static List<IToken> process(List<IToken> processed) {
		ArrayList<IToken> list = new ArrayList<IToken>();
		for (IToken t : processed) {
			if (t instanceof SentenceToken) {
				list.addAll(new BasicCleaner().clean(t.getChildren()));
			} else {
				list.add(t);
			}
		}
		return list;
	}

}
