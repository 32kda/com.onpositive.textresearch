package com.onpositive.wikipedia.workbench;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.onpositive.semantic.search.core.ICategorizable;
import com.onpositive.semantic.search.core.ICategory;
import com.onpositive.semantic.wikipedia2.catrelations.Connection;
import com.onpositive.semantic.wikipedia2.catrelations.IConnectionFactory;
import com.onpositive.semantic.wikipedia2.catrelations.ITokenChainPredicate;
import com.onpositive.semantic.wikipedia2.catrelations.isa.HeadWordCriteria;
import com.onpositive.semantic.wordnet.GrammarRelation;
import com.onpositive.semantic.wordnet.Grammem;
import com.onpositive.semantic.wordnet.Grammem.Case;
import com.onpositive.semantic.wordnet.Grammem.PartOfSpeech;
import com.onpositive.semantic.wordnet.TextElement;
import com.onpositive.semantic.wordnet.WordNetProvider;
import com.onpositive.text.analisys.tests.euristics.SimplifiedToken;
import com.onpositive.text.analysis.IToken;
import com.onpositive.text.analysis.filtering.AdditionalPartsPresetFilter;
import com.onpositive.text.analysis.filtering.IPartOfSpeechSelector;
import com.onpositive.text.analysis.lexic.PrimitiveTokenizer;
import com.onpositive.text.analysis.lexic.StringToken;
import com.onpositive.text.analysis.lexic.SymbolToken;
import com.onpositive.text.analysis.lexic.scalar.ScalarParser;
import com.onpositive.text.morphology.AnalisysResult;
import com.onpositive.text.morphology.Result;
import com.onpositive.text.morphology.dataset.prepare.DataSetPreparation;
import com.onpositive.text.morphology.neural.NeuralEstimator;
import com.onpositive.wikipedia.workbench.words.primary.SimpleTitleModel;
import com.onpositive.wikipedia.workbench.words.primary.WikiEngineProvider;
import com.onpositive.wikipedia.workbench.words.primary.WordDefinitions;

public class BasicConnectionFactory implements IConnectionFactory {
	
	private static final int MAX_CACHED_ENTRIES = 10;
	
	private static final WordDefinitions index = WikiEngineProvider.getInstance().getIndex(WordDefinitions.class);
	private static final NeuralEstimator ess = new NeuralEstimator();
	private static final IPartOfSpeechSelector presetFilter = new AdditionalPartsPresetFilter();
	
	protected List<ITokenChainPredicate> ignoringPredicates = new ArrayList<>();
	
	@SuppressWarnings("serial")
	protected LinkedHashMap<String, SimpleTitleModel> modelCache = new LinkedHashMap<String, SimpleTitleModel>() {
		protected boolean removeEldestEntry(java.util.Map.Entry<String,SimpleTitleModel> eldest) {
			return size() > MAX_CACHED_ENTRIES;
		};
	};
	
	public BasicConnectionFactory() {
		fillIgnoringPredicates();
	}

	protected void fillIgnoringPredicates() {
		ignoringPredicates.add(new SubdivisionIgnoringPredicate());
	}

	@Override
	public Connection createConnection(ICategorizable object,
			ICategory possibleParent) {
		String title = possibleParent.getTitle();
		if (isSpecial(title)) {
			return null;
		}
		title = title.toLowerCase().trim().replace('ё','е').replace('_',' ');
		List<SimplifiedToken> titleTokens = parseTitle(title);
		if (shouldIgnore(titleTokens)) {
			return null;
		}
		if (object instanceof ICategory) {
			
//			WordFormToken mainWord = new SimpleTitleModel(title).getMainWord();
			String childCatTitle = object.getTitle();
			childCatTitle = childCatTitle.toLowerCase().trim().replace('ё','е').replace('_',' ');
			if (isSpecial(childCatTitle)) {
				return null;
			}
//			WordFormToken childWord = new SimpleTitleModel(childCatTitle).getMainWord();
//			if (mainWord != null && childWord != null &&
//				mainWord.getBasicForm().equals(childWord.getBasicForm()) &&
//				mainWord.getPartOfSpeech() == childWord.getPartOfSpeech()) {
//				Connection connection = new Connection(object, possibleParent);
//				SimpleConnectionMetadataProvider.put(connection,"MAINWORD");
//				return connection;
//			}
			List<SimplifiedToken> childTitleTokens = parseTitle(childCatTitle);
			if (shouldIgnore(childTitleTokens)) {
				return null;
			}
			return doAnalyze(object, possibleParent, title, childCatTitle);
//			return doAnalyze1(object, possibleParent, titleTokens, childTitleTokens);
		} else {
//			if (object instanceof IDocument) {
//				String txt = ((IDocument) object).getPlainTextAbstract();
//				return doAnalyze(object, possibleParent, title, txt);
//			}
			ArrayList<TextElement> childTokens = index.getById(object.getIntId());
			List<SimplifiedToken> tokens = childTokens.stream().map(element -> new SimplifiedToken(element.getBasicForm(), element.allGrammems())).collect(Collectors.toList());
			if (shouldIgnore(tokens)) {
				return null;
			}
			return doAnalyze1(object, possibleParent, titleTokens, tokens);
		}
//		return null;
	}

	public boolean isSpecial(String title) {
		return title.indexOf(':') >= 0 || title.toLowerCase().contains("статьи");
	}

	public Connection doAnalyze1(ICategorizable object, ICategory possibleParent,
			List<SimplifiedToken> titleTokens,
			List<SimplifiedToken> childTitleTokens) {
		if (!childTitleTokens.isEmpty() && !titleTokens.isEmpty()) {
			if (titleTokens.stream().allMatch(element -> containsSimplified(childTitleTokens, element))) {
				Connection connection = new Connection(object, possibleParent);
				SimpleConnectionMetadataProvider.put(connection,"ALL");
				return connection;
			}
			if (titleTokens.stream().anyMatch(element -> hasSameNounSimplified(childTitleTokens, element))) {
				Connection connection = new Connection(object, possibleParent);
				SimpleConnectionMetadataProvider.put(connection,"NOUN");
				return connection;
			}
		}
		return null;
	}

	public Connection doAnalyze(ICategorizable object, ICategory possibleParent,
			String title, String childText) {
		if (HeadWordCriteria.isSimpleIsA(childText, title)) {
			Connection connection = new Connection(object, possibleParent);
			SimpleConnectionMetadataProvider.put(connection,"STM");
			return connection;
		}
		return null;
	}

//	private boolean hasSameNoun(ArrayList<TextElement> tokens,
//			SimplifiedToken element) {
//		return tokens.stream().anyMatch(x -> getBasicForm(element).equals(x.getBasicForm()) && isSameNoun(element.getGrammems(), x.allGrammems()));
//	}
	
	private boolean shouldIgnore(List<SimplifiedToken> titleTokens) {
		for (ITokenChainPredicate predicate : ignoringPredicates) {
			if (predicate.matches(titleTokens)) {
				return true;
			}
		}
		return false;
	}

	private boolean hasSameNounSimplified(List<SimplifiedToken> childTitleTokens,
			SimplifiedToken element) {
		return childTitleTokens.stream().anyMatch(x -> getBasicForm(element).equals(getBasicForm(x)) && isSameNoun(element.getGrammems(), x.getGrammems()));
	}

	private boolean isSameNoun(Collection <Grammem> grammems1, Collection <Grammem> grammems2) {
		Optional<Grammem> pos1 = grammems1.stream().filter(grammem -> grammem == PartOfSpeech.NOUN).findFirst();
		Optional<Grammem> pos2 = grammems2.stream().filter(grammem -> grammem == PartOfSpeech.NOUN).findFirst();
		boolean equals = pos1.equals(pos2);
		if (equals) {
			Optional<Grammem> case1 = grammems1.stream().filter(grammem -> grammem instanceof Case).findFirst();
			Optional<Grammem> case2 = grammems2.stream().filter(grammem -> grammem instanceof Case).findFirst();
			return case1.equals(case2);
		}
		return false;	
	}
	
//	private boolean contains(ArrayList<TextElement> parentTokens,
//			SimplifiedToken element) {
//		return parentTokens.stream().anyMatch(x -> getBasicForm(element).equals(x.getBasicForm()) && isSamePOS(element.getGrammems(), x.allGrammems()));
//	}
	
	private boolean containsSimplified(List<SimplifiedToken> childTokens,
			SimplifiedToken parentToken) {
		return childTokens.stream().anyMatch(x -> getBasicForm(parentToken).equals(getBasicForm(x)) && isSamePOS(parentToken.getGrammems(), x.getGrammems()));
	}

	protected String getBasicForm(SimplifiedToken element) {
		GrammarRelation[] possibleGrammarForms = WordNetProvider.getInstance().getPossibleGrammarForms(element.getWord());
		if (possibleGrammarForms != null && possibleGrammarForms.length > 0) {
			for (GrammarRelation grammarRelation : possibleGrammarForms) {
				return grammarRelation.getWord().getBasicForm();
			}
		}
		return element.getWord().toLowerCase().trim();
	}

	private boolean isSamePOS(Collection<Grammem> grammems1, Collection<Grammem> grammems2) {
		Optional<Grammem> pos1 = grammems1.stream().filter(grammem -> grammem instanceof PartOfSpeech).findFirst();
		Optional<Grammem> pos2 = grammems2.stream().filter(grammem -> grammem instanceof PartOfSpeech).findFirst();
		return pos1.equals(pos2); 
	}
	
	private List<SimplifiedToken> parseTitle(String title) {
		title = title.toLowerCase().trim().replace('ё','е').replace('_',' ');
		List<SimplifiedToken> result = new ArrayList<>();
		PrimitiveTokenizer tk = new PrimitiveTokenizer();
		List<IToken> tokenized = tk.tokenize(title);
		tokenized = new ScalarParser().process(tokenized);
		List<String> str = tokenized.stream().filter(t -> t instanceof StringToken).map(t -> t.getShortStringValue()).collect(Collectors.toList());
		boolean hasOmonimy = false;
		for (IToken t : tokenized) {
			if (t instanceof SymbolToken) {
				continue;
			}
			String value = t.getShortStringValue();
			Collection<PartOfSpeech> parts = DataSetPreparation.possiblePartsOfSpeech(value);
			parts = presetFilter.select(value,parts);
			if (parts.size() > 1) {
				hasOmonimy = true;
				break;
			} else {
				result.add(new SimplifiedToken(value,createGrammems(value,parts)));
			}
//			str.add(value);
//			this.results.add(es);
//			ts.put(t.getStartPosition(), es);
		}
		if (!hasOmonimy) {
			return result;
		} else if (str.size() == 1) { //Unable to handle homonimy; Return all options
			String value = str.get(0);
			Collection<PartOfSpeech> parts = DataSetPreparation.possiblePartsOfSpeech(value);
			parts = presetFilter.select(value,parts);
			return Collections.singletonList(new SimplifiedToken(value, createGrammems(value,parts)));
		} else {
			result.clear();
		}
	
		ArrayList<AnalisysResult> analize = ess.analize(str);
		for (AnalisysResult r : analize) {
			Result bestResult = r.getBestResult();
			if (bestResult != null) {
				result.add(new SimplifiedToken(r.content,createGrammems(r.content, Collections.singleton(bestResult.speach))));
			}
		}
		return result;
	}

	protected ArrayList<Grammem> createGrammems(String word, Collection<PartOfSpeech> parts) {
		ArrayList<Grammem> list = new ArrayList<Grammem>(parts);
		Set<Case> cases = DataSetPreparation.possibleCases(word);
		list.addAll(cases);
		return list;
	}

}
