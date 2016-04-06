package com.onpositive.wikipedia.workbench.words.primary;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.carrotsearch.hppc.IntArrayList;
import com.onpositive.semantic.wikipedia2.WikiEngine2;
import com.onpositive.semantic.wordnet.Grammem;
import com.onpositive.semantic.wordnet.Grammem.Case;
import com.onpositive.semantic.wordnet.Grammem.PartOfSpeech;
import com.onpositive.semantic.wordnet.SemanticRelation;
import com.onpositive.semantic.wordnet.TextElement;
import com.onpositive.semantic.wordnet.WordNetProvider;
import com.onpositive.text.analysis.IToken;
import com.onpositive.text.analysis.lexic.SentenceSplitter;
import com.onpositive.text.analysis.lexic.SymbolToken;
import com.onpositive.text.analysis.lexic.WordFormToken;
import com.onpositive.text.analysis.syntax.SentenceToken;
import com.onpositive.text.analysis.syntax.SyntaxToken;

public class WordDefinitions extends WordsSequenceIndex {

	private Set<String> kindSynonims;
	
	public WordDefinitions(WikiEngine2 instance) {
		super(instance);
	}

	private void loadSynonims() {
		Set<String> result = new HashSet<>();
		String[] initialSynonims = new String[] {"вариант", "разновидность", "подкласс"};
		for (String word: initialSynonims) {
			TextElement textElement = WordNetProvider.getInstance().getWordElement(word);
			List<String> synonims = Arrays.stream(textElement.getConcepts()).flatMap(element -> Arrays.stream(element.getSemanticRelations())).
					filter(relation -> (relation.relation == SemanticRelation.SYNONIM || relation.relation == SemanticRelation.SYNONIM_BACK_LINK )).
					map(relation -> relation.getWord().getParentTextElement().getBasicForm()).collect(Collectors.toList());
			result.addAll(synonims);
			result.add(word);
		}
		kindSynonims = result;
	}

	public void processDoc(WikiEngine2 instance, int doc, final String documentAbstract) {
		if (kindSynonims == null) {
			loadSynonims();
		}
		
		final BufferedReader bufferedReader = new BufferedReader(new StringReader(documentAbstract));
		try {
			String line = bufferedReader.readLine();
			line = line.replace("" + (char) 769, "");
			line = line.replace("" + (char) 160, "");
//			line = replaceSpecials(line);
			final List<IToken> process = tokens(line);
			final List<IToken> split = new SentenceSplitter().split(process);

			for (IToken t : split) {
				if (t instanceof SentenceToken) {
					final List<IToken> children = t.getChildren();
					ArrayList<WordFormToken>cands=new ArrayList<>();
					boolean in = false;
					boolean inparen = false;
					boolean inQ = false;
					boolean started = false;
					int last = -1;
					for (int i = 0; i < children.size(); i++) {
						IToken curToken = children.get(i);
						if (started) {
							if (curToken.getStartPosition() == last) {
								continue;
							}
							if (!(curToken instanceof WordFormToken)) {
								break;
							}
							last = curToken.getStartPosition();
							WordFormToken w = (WordFormToken) curToken;
							if (w.getPartOfSpeech() == PartOfSpeech.ADJS) {
								int idx = checkAdjShort(children, i);
								if (idx > 0) {
									for (int j = i; j <= idx; j++) {
										IToken candidate = children.get(j);
										if (candidate instanceof WordFormToken) {
											cands.add((WordFormToken) candidate);
										}
									}
									i = idx;
									continue;
								}
							}
							if (!w.hasGrammem(Grammem.PartOfSpeech.NOUN)&&!w.hasGrammem(Grammem.PartOfSpeech.ADJF)&&!w.hasGrammem(Grammem.PartOfSpeech.ADJS)) {
								break;
							}
							if (!w.hasGrammem(PartOfSpeech.ADJS) && !(w.hasGrammem(Grammem.Case.NOMN) || w.hasGrammem(Grammem.Case.GENT))) {
								break;
							}
							cands.add(w);
							continue;
						}
						last = curToken.getStartPosition();
						if (curToken.getStableStringValue().equals("«")) {
							inQ = true;
							cands.clear();
						}
						if (curToken.getStableStringValue().equals("»")) {
							inQ = false;
							cands.clear();
						}
						if (curToken.getStableStringValue().equals("(")) {
							inparen = true;
							cands.clear();
						}
						if (curToken.getStableStringValue().equals(")")) {
							inparen = false;
							cands.clear();
						}
						if (inparen || inQ) {
							continue;
						}
						if (curToken.getStableStringValue().equals("—")) {
							in = true;
							cands.clear();
						}

						if (curToken.getStableStringValue().equals(",")) {
							if (in) {
								break;
							}
						}
						if (curToken instanceof WordFormToken && in) {
							WordFormToken w = (WordFormToken) curToken;
							if (w.hasGrammem(Grammem.PartOfSpeech.NOUN)||w.hasGrammem(Grammem.PartOfSpeech.ADJF)||w.hasGrammem(Grammem.PartOfSpeech.ADJS)) {
								if (w.hasGrammem(Grammem.Case.NOMN) || w.hasGrammem(Grammem.Case.GENT)) {
									if (w.getBasicForm().length() > 3 || (!w.hasGrammem(Grammem.PartOfSpeech.PREP)
											&& !w.hasGrammem(Grammem.PartOfSpeech.CONJ))) {
										if (!w.hasGrammem(Grammem.SemanGramem.NAME)
												&& !w.hasGrammem(Grammem.SemanGramem.SURN)) {
											if (w.getShortStringValue().length() > 2) {
												cands.add(w);
												started = true;
											}
											else{
												break;
											}
											// break;
										}
									}
								}
							}
						}
					}
					List<WordFormToken> toRemove = new ArrayList<>();
					for (int i = 0; i < cands.size() - 1; i++) { //Для преобразования типа "вариант самолёта"=>"самолёт"
						WordFormToken wordFormToken = cands.get(i);
						if (kindSynonims.contains(wordFormToken.getShortStringValue().trim().toLowerCase())) {
							WordFormToken token = getNext(cands,i);
							if (token != null && (token.hasGrammem(Case.GENT) || token.hasGrammem(Case.GEN1) || token.hasGrammem(Case.GEN2))) {
								toRemove.add(wordFormToken);
							}
						}

					}
					cands.removeAll(toRemove);
					
					IntArrayList resultList=new IntArrayList();
					
					for (WordFormToken w:cands){
						String basicForm=w.getBasicForm();
						TextElement textElement=WordNetProvider.getInstance().getWordElement(basicForm);
						if (textElement==null){
							return;
						}
						else{
							resultList.add(textElement.id());
						}
					}
					if (!resultList.isEmpty()){
						map.add(doc, resultList.toArray());
					}
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return;
	}

	
	private int checkAdjShort(List<IToken> children, int i) {
		int j = i;
		IToken token = children.get(j);
		while (j < children.size() - 1 && (token instanceof SymbolToken && "-".equals(token.getStringValue())) ||
			   (token instanceof SyntaxToken && ((SyntaxToken) token).getPartOfSpeech() == PartOfSpeech.ADJS)) {
			token = children.get(++j);
		}
		if (j < children.size()) {
			token = children.get(j);
			if (token instanceof SyntaxToken && ((SyntaxToken) token).getPartOfSpeech() == PartOfSpeech.ADJF) {
				return j;
			}
			
		}
		return -1;
	}

	private WordFormToken getNext(ArrayList<WordFormToken> cands, int i) {
		WordFormToken current = cands.get(i);
		int j = i + 1;
		for (; j < cands.size() && (cands.get(j).getConflicts().contains(current)); j++);
		if (j < cands.size()) {
			return cands.get(j);
		}
		return null;
	}

	private String replaceSpecials(String line) {
		line = line.replaceAll("разг\\.", "разговорный");
		return line;
	}

	@Override
	public String getFileName() {
		return "word2word.index";
	}
}
