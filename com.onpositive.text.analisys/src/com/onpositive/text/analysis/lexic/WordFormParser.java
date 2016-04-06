package com.onpositive.text.analysis.lexic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import com.carrotsearch.hppc.IntObjectOpenHashMap;
import com.carrotsearch.hppc.IntOpenHashSet;
import com.onpositive.semantic.wordnet.AbstractWordNet;
import com.onpositive.semantic.wordnet.GrammarRelation;
import com.onpositive.semantic.wordnet.Grammem;
import com.onpositive.semantic.wordnet.Grammem.Case;
import com.onpositive.semantic.wordnet.Grammem.Gender;
import com.onpositive.semantic.wordnet.Grammem.PartOfSpeech;
import com.onpositive.semantic.wordnet.Grammem.SemanGramem;
import com.onpositive.semantic.wordnet.Grammem.SingularPlural;
import com.onpositive.semantic.wordnet.MeaningElement;
import com.onpositive.semantic.wordnet.TextElement;
import com.onpositive.text.analysis.BasicParser;
import com.onpositive.text.analysis.IToken;
import com.onpositive.text.analysis.lexic.disambig.DisambiguatorProvider;
import com.onpositive.text.analysis.lexic.disambig.ILexicLevelDisambiguator;
import com.onpositive.text.analysis.rules.matchers.UnaryMatcher;
import com.onpositive.text.analysis.syntax.SyntaxToken;
import com.onpositive.text.analysis.syntax.SyntaxToken.GrammemSet;

public class WordFormParser extends BasicParser {
	
	private static final HashSet<Grammem> uniformGrammems = new HashSet<Grammem>(Arrays.asList(
			PartOfSpeech.NOUN,
			Case.NOMN, Case.GENT, Case.ACCS, Case.ABLT, Case.DATV, Case.LOCT,
			Gender.UNKNOWN,
			SingularPlural.SINGULAR
		));  

	public WordFormParser(AbstractWordNet wordNet) {
		super();
		this.wordNet = wordNet;
	}
	
	private AbstractWordNet wordNet;
	
	private ArrayList<WordSequenceData> dataList = new ArrayList<WordSequenceData>();
	
	private GrammarRelation[] firstWordForms;
	
	ILexicLevelDisambiguator disambiguator=DisambiguatorProvider.getInstance();
	
	private boolean isAbbr = false;
	
	private boolean ignoreCombinations = false;

	public ILexicLevelDisambiguator getDisambiguator() {
		return disambiguator;
	}

	public void setDisambiguator(ILexicLevelDisambiguator disambiguator) {
		this.disambiguator = disambiguator;
	}

	
	@Override
	protected void combineTokens(Stack<IToken> sample, ProcessingData processingData){
		
		if(this.firstWordForms == null){
			for(IToken t : sample){
				ArrayList<GrammemSet> list = new ArrayList<GrammemSet>();
				list.add(new GrammemSet(uniformGrammems));
				SyntaxToken wft = new WordFormToken(list, t.getStartPosition(), t.getEndPosition());
				processingData.addReliableToken(wft);
			}
			return;
		}
		
		IntObjectOpenHashMap<WordFormToken> tokens = new IntObjectOpenHashMap<WordFormToken>();
		IToken firstToken = sample.get(0);
		int startPosition = firstToken.getStartPosition();
		int endPosition = firstToken.getEndPosition();
		
		boolean gotSequence = false;
		for (WordSequenceData data : dataList) {
			gotSequence |= data.gotMatch();
		}

		if (!gotSequence) {
			l0: for (GrammarRelation gr : firstWordForms) {
				gr.getGrammems();
				TextElement word = gr.getWord();
				if (word==null){
					continue;
				}
				MeaningElement[] concepts = word.getConcepts();
				for (MeaningElement me : concepts) {
					if (!corresponds(me, gr)) {
						continue;
					}

					boolean matchPrep = false;
					MeaningElement prep = refinePrepositionOrConjunction(me,startPosition,endPosition);
					if(prep!=null){
						me = prep;
						matchPrep = matchPreposition(firstToken);
						if (matchPrep) {
							tokens.clear();
						}
					}
					
					int id = me.id();
					WordFormToken token = tokens.get(id);
					int endPosition1 = considerAbbrEndPosition(sample,endPosition, me, gr);
					if(token == null){						
						token = new WordFormToken(me, startPosition, endPosition1);
						tokens.put(id, token);
					}
					token.setLink(firstToken.getLink());
					registerGrammarRelation(gr, token);
					
					if (matchPrep) {
						break l0;
					}
				}
			}
		} else {
			for (WordSequenceData data : dataList) {
				if (!data.gotMatch()) {
					continue;
				}
				TextElement te = data.textElement();				
				ArrayList<GrammarRelation> grammarRelations = computeGrammarRelations(te,sample);				
				MeaningElement[] concepts = te.getConcepts();
				for (MeaningElement me : concepts) {
					
					boolean matchPrep = false;
					MeaningElement prep = refinePrepositionOrConjunction(me,startPosition,endPosition);
					if(prep!=null){
						me = prep;
						matchPrep = matchPreposition(firstToken);
						if (matchPrep) {
							tokens.clear();
						}
					}
					
					int id = me.id();
					WordFormToken token = tokens.get(id);
					int endIndex = data.getSequenceLength() - 1;
					int endPosition1 = sample.get(endIndex).getEndPosition();
					if(token == null){
						token = new WordFormToken(me, startPosition, endPosition1);
						tokens.put(id, token);
					}
					WordFormToken wft = (WordFormToken) token;
					for (GrammarRelation gr : grammarRelations) {
						if (corresponds(me, gr)) {
							registerGrammarRelation(gr, wft);
						}
					}
					tokens.put(id, token);
					
					if (matchPrep) {
						break;
					}
				}
			}
		}
		WordFormToken[] array = tokens.values().toArray(WordFormToken.class);
		if (array.length > 1) {
			array = mergeMeanings(array);
		}
		if (disambiguator != null && array.length > 1) {
			array = disambiguator.disambiguate(array, firstToken);
		}
		if (array.length == 1) {
			processingData.addReliableToken(array[0]);
		} else if (array.length > 0) {
			processingData.addDoubtfulTokens(array);
		}
	}

	protected ArrayList<GrammarRelation> computeGrammarRelations(TextElement te, Stack<IToken> sample) {
		
		String bf0 = te.getBasicForm();
		List<IToken> tokenz = new PrimitiveTokenizer().tokenize(bf0, false);
		TextElement[] parts = te.getParts();
		
		boolean isNoun = false;
		boolean isSingular = false;
		boolean isNomn = false;
		
		int ind = -1;
		int mainInd = 0;
		int stepBack = 0;
		TextElement mainElement = null;
		for (int i = 0; i < parts.length; i++) {
			if (parts[i] == null) {
				stepBack++;
				continue;
			}
			
			IToken token = null;
			do {
				token = tokenz.get(++ind);
			} while (token.getType() != IToken.TOKEN_TYPE_LETTER);

			String basicForm = token.getStringValue();
			GrammarRelation[] forms = wordNet.getPossibleGrammarForms(basicForm);
			for(GrammarRelation gr: forms){
				boolean isNounLoc = gr.hasGrammem(PartOfSpeech.NOUN);
				if (isNoun && !isNounLoc) {
					continue;
				}
				if (isNounLoc) {
					if (!isNoun) {
						isNomn = false;
						isSingular = false;
					}
					isNoun = true;
				}
				isNoun |= isNounLoc;
				boolean isNomnLoc = gr.hasGrammem(Case.NOMN);
				if (isNomn && !isNomnLoc) {
					continue;
				}
				if (isNomnLoc) {
					if (!isNomn) {
						isSingular = false;
					}
					isNomn = true;
				}
				
				boolean isSingularLoc = gr.hasGrammem(SingularPlural.SINGULAR);
				if (isSingular && !isSingularLoc) {
					continue;
				}
				isSingular |= isSingularLoc;
				mainInd = i - stepBack;
				mainElement = parts[i];
			}
		}

		IToken mainToken = null;
		int ind0 = 0;
		for (int i = 0; i <= mainInd; i++) {
			while (sample.get(ind0++).getType() != IToken.TOKEN_TYPE_LETTER);
			mainToken = sample.get(ind0 - 1);
		}
		ArrayList<GrammarRelation> list = new ArrayList<GrammarRelation>();
		GrammarRelation[] forms = wordNet.getPossibleGrammarForms(mainToken.getStringValue().toLowerCase());
		for(GrammarRelation gr : forms){
			if(gr.getWord() == mainElement){
				list.add(gr);
			}
		}
		return list;
	}

	private boolean matchPreposition(IToken token) {

		String val = token.getStringValue().toLowerCase();
		UnaryMatcher<SyntaxToken> matcher = getPrepConjRegistry().getPrepCaseMatcher(val);
		if (matcher == null) {
			return false;
		}

		IToken next = token.getNext();
		ArrayList<WordFormToken> nextWFTs = new ArrayList<WordFormToken>();
		if (next != null) {
			nextWFTs.addAll(createDrafts(next));
		} else {
			List<IToken> nextTokens = token.getNextTokens();
			if (nextTokens != null) {
				for (IToken n : nextTokens) {
					nextWFTs.addAll(createDrafts(n));
				}
			}
		}
		for (SyntaxToken t : nextWFTs) {
			if (matcher.match(t)) {
				return true;
			}
		}
		return false;
	}

	private Collection<? extends WordFormToken> createDrafts(IToken next) {
		
		ArrayList<WordFormToken> list = new ArrayList<WordFormToken>();
		if (next.getType() != IToken.TOKEN_TYPE_LETTER) {
			return list;
		}
		
		int startPosition = next.getStartPosition();
		int endPosition = next.getEndPosition();
		
		IntObjectOpenHashMap<WordFormToken> map = new IntObjectOpenHashMap<WordFormToken>();
		
		String sv = next.getStringValue().toLowerCase();
		GrammarRelation[] possibleGrammarForms = wordNet.getPossibleGrammarForms(sv);		
		for(GrammarRelation gr : possibleGrammarForms){
			TextElement word = gr.getWord();
			String bf = word.getBasicForm();
			if (getPrepConjRegistry().getPreposition(bf) != null) {
				continue;
			}
			if (getPrepConjRegistry().getConjunction(bf) != null) {
				continue;
			}
			MeaningElement[] concepts = word.getConcepts();
			for (MeaningElement me : concepts) {
				
				if (!corresponds(me, gr)) {
					continue;
				}
				
				int id = me.id();
				WordFormToken t = map.get(id);
				if (t == null) {
					t = new WordFormToken(me, startPosition, endPosition);
					map.put(id, t);
				}
				registerGrammarRelation(gr, t);
			}
		}
		
		list.addAll(Arrays.asList(map.values().toArray(WordFormToken.class)));
		return list;
	}

	private MeaningElement refinePrepositionOrConjunction(MeaningElement me, int sp, int ep) {
		
		TextElement te = me.getParentTextElement();
		String str = te.getBasicForm();
		if (te.isMultiWord()) {
			StringBuilder bld = new StringBuilder();
			for (TextElement t : te.getParts()) {
				if (t == null) {
					continue;
				}
				bld.append(" ").append(t.getBasicForm());
			}
			str = bld.toString();
		}
		String txt = getText();
		if(txt != null && ep<txt.length()&&!str.endsWith(" ")&&txt.charAt(ep)=='.'&&me.getGrammems().contains(SemanGramem.ABBR)){
			return null;
		}
		
		MeaningElement preposition = getPrepConjRegistry().getPreposition(str);
		if (preposition != null) {
			return preposition;
		}
		MeaningElement conjunction = getPrepConjRegistry().getConjunction(str);
		if (conjunction != null) {
			return conjunction;
		}
		return null;
	}

	protected int considerAbbrEndPosition(Stack<IToken> sample,	int endPosition, MeaningElement me, GrammarRelation gr) {
		if(me.getGrammems().contains(SemanGramem.ABBR)||gr.hasGrammem(SemanGramem.ABBR)){
			if (sample.size()>1){
				IToken secondToken = sample.get(1);
				if (secondToken.getStringValue().equals(".")) {
					if (endPosition == secondToken.getStartPosition()) {
						endPosition = secondToken.getEndPosition();
					}
				}
			}
		}
		return endPosition;
	}

	private boolean corresponds(MeaningElement me, GrammarRelation gr) {
		
		Set<Grammem> grammems0 = me.getGrammems();
		Set<Grammem> grammems1 = gr.getGrammems();
		if (!matchGrammems(grammems0, grammems1, PartOfSpeech.class)) {
			return false;
		}
		if (!matchGrammems(grammems0, grammems1, Gender.class)) {
			return false;
		}
		return true;
	}

	protected boolean matchGrammems(Set<Grammem> grammems0, Set<Grammem> grammems1, Class<? extends Grammem> clazz) {
		Set<? extends Grammem> ext0 = GrammemSet.extractGrammems(grammems0, clazz);
		if(!ext0.isEmpty()){
			Set<? extends Grammem> ext1 = GrammemSet.extractGrammems(grammems1, clazz);
			if(!ext1.isEmpty()){
				if(!ext1.containsAll(ext0)){
					return false;
				}
			}
		}
		return true;
	}

	private void registerGrammarRelation(GrammarRelation gr, WordFormToken wft) {
		if (gr != null) {
			List<GrammarRelation> grammarRelations = wft.getGrammarRelations();
			boolean exists = false;
			for (GrammarRelation rel : grammarRelations) {
				if (rel.equals(gr)) {
					exists = true;
					break;
				}
			}
			if (!exists) {
				wft.addGrammarRelation(gr);
			}
		}
	}

	@Override
	protected ProcessingResult continuePush(Stack<IToken> sample, IToken newToken) {
		int type = newToken.getType();
		if (type == IToken.TOKEN_TYPE_DIGIT) {
			return DO_NOT_ACCEPT_AND_BREAK;
		}
		
		if(type==IToken.TOKEN_TYPE_SYMBOL){
			if(isAbbr&&sample.size()==1&&newToken.getStringValue()=="."){
				return ACCEPT_AND_BREAK;
			} else if (newToken.getStringValue() == "-") {
				for (WordSequenceData data : dataList) {
					if (data.checkHyphen()) {
						return CONTINUE_PUSH;
					}
				}
				return DO_NOT_ACCEPT_AND_BREAK;
			}
		}
		
		ProcessingResult result = CONTINUE_PUSH;
		String value = newToken.getStringValue();
		GrammarRelation[] possibleGrammarForms = wordNet.getPossibleGrammarForms(value.toLowerCase());
		
		if(dataList.isEmpty()){
			return DO_NOT_ACCEPT_AND_BREAK;
		} else {
			for (WordSequenceData data : dataList) {
				int pos = 0;
				for (IToken t : sample) {
					if (t.getType() == IToken.TOKEN_TYPE_LETTER || t.getStringValue().equals("-")) {
						pos++;
					}
				}
				data.setPosition(pos);
			}
			if(possibleGrammarForms!=null&&possibleGrammarForms.length!=0){
				
				boolean needContinue = false;
				for (WordSequenceData data : dataList) {
					for (GrammarRelation gr : possibleGrammarForms) {
						if (gr.getWord()==null){
							continue;
						}
						int wordId = gr.getWord().id();
						needContinue |= data.check(wordId);
					}
				}
				if (!needContinue) {
					result = DO_NOT_ACCEPT_AND_BREAK;
				}
			} else {
				result = DO_NOT_ACCEPT_AND_BREAK;
			}
		}
		return result;
	}

	protected ProcessingResult checkPossibleStart(IToken token) {
		if (token.getType() != IToken.TOKEN_TYPE_LETTER) {
			return DO_NOT_ACCEPT_AND_BREAK;
		}
		String value = token.getStringValue();
		GrammarRelation[] possibleGrammarForms = wordNet.getPossibleGrammarForms(value.toLowerCase());
		if(possibleGrammarForms==null||possibleGrammarForms.length==0){
			firstWordForms = null;
			return ACCEPT_AND_BREAK;
		}
		
		firstWordForms = possibleGrammarForms;
		checkAbbr();
		IntOpenHashSet set = new IntOpenHashSet();
		for (GrammarRelation gr : possibleGrammarForms) {
			TextElement word = gr.getWord();
			TextElement[] possibleContinuations = null;
			if (getPrepConjRegistry().getConjunction(value) == null && getPrepConjRegistry().getPreposition(value) == null) // only check for continuations if not
				possibleContinuations = wordNet.getPossibleContinuations(word);												// preposition or conjunction
			
			if(possibleContinuations!=null){
				for(TextElement te : possibleContinuations){
					if (ignoreCombinations && te.getBasicForm().indexOf(' ')>=0) {
						continue;
					}
					
					if(set.contains(te.id())){
						continue;
					}

					if (te.getParts().length < 2)
						continue;

					WordSequenceData data = new WordSequenceData(te);
					dataList.add(data);
					set.add(te.id());
				}
			}
		}
		
		return CONTINUE_PUSH;
	}
	
	protected void checkAbbr() {
		isAbbr = false;
		for (GrammarRelation gr : firstWordForms) {
			TextElement word = gr.getWord();
			if (word==null){
				return ;
			}
			if (word.hasGrammem(SemanGramem.ABBR)) {
				isAbbr = true;
				return;
			}
		}
	};

	protected ProcessingResult checkToken(IToken unit) {
		throw new UnsupportedOperationException("Check Token not supported for Word Form Parser");
	}
	
	protected void prepare() {
		dataList.clear();
		firstWordForms = null;
	}

	protected void cleanUp() {
		dataList.clear();
		firstWordForms = null;
	}
	
	protected void rollBackState(int stepCount) {
		for (WordSequenceData data : dataList) {
			data.rollBack(stepCount);
		}
	}
	
	protected boolean keepInputToken() {
		return false;
	}

	public static class WordSequenceNode {
		private int value;

		public int getValue() {
			return value;
		}

		public void setValue(int value) throws Exception {
			if (isRegular())
				this.value = value;
			else
				throw new Exception("Cannot set value to a hypen Node");
		}

		public boolean isHyphen() {
			return this == WordSequenceNode.HYPHEN;
		}

		public boolean isRegular() {
			return this != WordSequenceNode.HYPHEN;
		}

		public static WordSequenceNode HYPHEN = new WordSequenceNode();

		private WordSequenceNode() {
			value = Integer.MIN_VALUE;
		}

		public WordSequenceNode(int value) {
			this.value = value;
		}

		@Override
		public String toString() {
			return isHyphen() ? "-" : Integer.toString(value);
		}
	}

	private static class WordSequenceData {

		@Override
		public String toString() {
			return "@" + this.textElement.toString();
		}

		public WordSequenceData(TextElement textElement) {

			String basicForm = textElement.getBasicForm();
			int pos = 0;
			TextElement[] parts = textElement.getParts();

			for (TextElement part : parts) {
				if (part != null) {
					while (pos < basicForm.length()) {
						if (basicForm.charAt(pos) == ' ') {
							pos += 1;
							continue;
						} else if (basicForm.charAt(pos) == '-') {
							pos += 1;
							sequence.add(WordSequenceNode.HYPHEN);
						} else {
							// TODO maybe check if word we skip has the same
							// basic form.
							while (pos < basicForm.length()	&& Character.isLetter(basicForm.charAt(pos)))
								pos += 1;

							sequence.add(new WordSequenceNode(part.id()));
							break;
						}
					}
				}
			}
			this.textElement = textElement;
		}

		private final List<WordSequenceNode> sequence = new ArrayList<WordSequenceNode>();

		private final TextElement textElement;
		
		private int matchPosition = 0;
		
		private int currentPosition = 0;
		
		private boolean gotMatch() {
			boolean result = matchPosition == sequence.size() - 1;
			return result;
		}
		
		private boolean check(int wordId) {
			if (currentPosition < sequence.size() && sequence.get(currentPosition).getValue() == wordId) {				
				if (currentPosition == matchPosition + (sequence.get(currentPosition - 1).isHyphen() ? 2 : 1))				
					 matchPosition = currentPosition;
			}
			
			return matchPosition == currentPosition;
		}
		

		public void setPosition(int pos) {
			this.currentPosition = pos;
			
		}
		
		private void rollBack(int stepCount) {
			currentPosition -= stepCount;
			matchPosition = Math.min(currentPosition, matchPosition);
		}

		public TextElement textElement() {
			return textElement;
		}
		
		public int getSequenceLength() {
			return sequence.size();
		}
		
		public boolean checkHyphen() {
			String completeForm = textElement.getBasicForm();
			int count = 0;
			int i = 0;
			int length = completeForm.length();
			for ( ; i < length ; i++){
				if (!Character.isLetter(completeForm.charAt(i))){
					count++;
				}
				if (count > currentPosition) {
					break;
				}
			}
			if (i < length && completeForm.charAt(i) == '-') {
				return true;
			}
			return false;
		}

	}
	
	private static PrepConjRegistry prepConjRegistry;

	private static ParticiplesRegistry partRegistry;
	
	protected PrepConjRegistry getPrepConjRegistry() {
		if (prepConjRegistry == null) {
			prepConjRegistry = new PrepConjRegistry(wordNet);
		}
		return prepConjRegistry;
	}

	protected ParticiplesRegistry getParticiplesRegistry() {
		if (partRegistry == null) {
			partRegistry = new ParticiplesRegistry(wordNet);
		}
		return partRegistry;
	}
	
	static class Key {
		public final Grammem gr;

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((el == null) ? 0 : el.hashCode());
			result = prime * result + ((gr == null) ? 0 : gr.hashCode());
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
			Key other = (Key) obj;
			if (el == null) {
				if (other.el != null)
					return false;
			} else if (!el.equals(other.el))
				return false;
			if (gr == null) {
				if (other.gr != null)
					return false;
			} else if (!gr.equals(other.gr))
				return false;
			return true;
		}

		public Key(Grammem gr, TextElement el) {
			super();
			this.gr = gr;
			this.el = el;
		}
		public final TextElement el;
	}

	WordFormToken[] mergeMeanings(WordFormToken[] wordFormTokens) {
		HashMap<Key, LinkedHashSet<WordFormToken>> map = new HashMap<Key, LinkedHashSet<WordFormToken>>();
		ArrayList<WordFormToken> noPartOfSpeech = new ArrayList<WordFormToken>();
		for (WordFormToken t : wordFormTokens) {
			TextElement parentTextElement = t.getParentTextElement();
			MeaningElement[] meaningElements = t.getMeaningElements();
			for (MeaningElement z : meaningElements) {
				Set<Grammem> grammems = z.getGrammems();
				boolean gotPartOfSpeech = false;
				for (Grammem g : grammems) {
					if (g instanceof Grammem.PartOfSpeech) {
						gotPartOfSpeech = true;
						Key c = new Key(g, parentTextElement);
						LinkedHashSet<WordFormToken> arrayList = map.get(c);
						if (arrayList == null) {
							arrayList = new LinkedHashSet<WordFormToken>();
							map.put(c, arrayList);
						}
						arrayList.add(t);
					}
				}
				if (!gotPartOfSpeech) {
					noPartOfSpeech.add(t);
				}
			}
		}
		for (LinkedHashSet<WordFormToken> set : map.values()) {
			set.addAll(noPartOfSpeech);
		}
		LinkedHashSet<WordFormToken>tks=new LinkedHashSet<WordFormToken>(Arrays.asList(wordFormTokens));
		for (LinkedHashSet<WordFormToken>t:map.values()){
			if (t.size()>1){
				WordFormToken[] a = new WordFormToken[t.size()];
				t.toArray(a);
				a[0].merge(a);
				for (SyntaxToken tc : a) {
					if (tc != a[0]) {
						tks.remove(tc);
					}
				}
			}
		}
		if (tks.size() != wordFormTokens.length) {
			wordFormTokens = tks.toArray(new WordFormToken[tks.size()]);
		}
		return wordFormTokens;
	}

	public boolean isIgnoreCombinations() {
		return ignoreCombinations;
	}

	public void setIgnoreCombinations(boolean ignoreCombinations) {
		this.ignoreCombinations = ignoreCombinations;
	}
}
