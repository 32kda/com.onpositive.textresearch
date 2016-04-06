	package com.onpositive.wikipedia.workbench.words.primary;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.onpositive.semantic.wikipedia2.WikiDoc;
import com.onpositive.semantic.wikipedia2.WikiEngine2;
import com.onpositive.semantic.wikipedia2.WikiEngineService;
import com.onpositive.semantic.wikipedia2.services.RedirectsMap;
import com.onpositive.semantic.wordnet.TextElement;
import com.onpositive.semantic.wordnet.WordNetProvider;
import com.onpositive.semantic.words3.hds.IntToIntArrayMap;
import com.onpositive.text.analysis.IToken;
import com.onpositive.text.analysis.lexic.PrimitiveTokenizer;
import com.onpositive.text.analysis.lexic.WordFormParser;

public abstract class WordsSequenceIndex extends WikiEngineService {

	protected IntToIntArrayMap map;

	public WordsSequenceIndex(WikiEngine2 engine) {
		super(engine);
	}

	@Override
	protected void doLoad(File fl) throws IOException {
		map = new IntToIntArrayMap(fl);
	}

	public ArrayList<TextElement> words(String word) {

		WikiDoc documentByTitle = this.engine.getDocumentByTitle(word);
		if (documentByTitle == null) {
			documentByTitle = this.engine.getDocumentByTitle(Character.toUpperCase(word.charAt(0)) + word.substring(1));

		}
		if (documentByTitle == null) {
			return new ArrayList<>();
		}
		int id = documentByTitle.getIntId();
		
		return getById(id);
	}

	public ArrayList<TextElement> getById(int id) {
		final RedirectsMap index = engine.getIndex(RedirectsMap.class);
		int iter = 0;
		while (index.isRedirect(id)) {
			id = index.value(id);
			iter++;
			if (iter > 10) {
				break;
			}

		}
		final int[] is = map.get(id);
		if (is!=null){
			ArrayList<TextElement>result=new ArrayList<>();
			for (int v:is){
				result.add(WordNetProvider.getInstance().getWordElement(v));
			}
			return result;
		}
		return new ArrayList<>();
	}
	
	@Override
	protected final void build(WikiEngine2 instance) {
		System.out.println("Building related words index");
		map = new IntToIntArrayMap();
		final int[] notRedirectDocumentIDs = instance.getNotRedirectDocumentIDs();
		int pos = 0;
		for (int doc : notRedirectDocumentIDs) {
			if (pos % 100 == 0) {
				System.out.println(pos + " of " + notRedirectDocumentIDs.length + " processed");
				
			}
			
			pos++;
			final String documentAbstract = processDocument(instance, doc);
		}
	}

	public final String processDocument(WikiEngine2 instance, int doc) {
		final String documentAbstract = instance.getDocumentAbstract(doc);
		if (documentAbstract.isEmpty()) {
			return documentAbstract;
		}
		processDoc(instance, doc, documentAbstract);
		return documentAbstract;
	}
	
	protected abstract void processDoc(WikiEngine2 instance, int doc, String documentAbstract) ;

	public List<IToken> tokens(String line) {
		final List<IToken> tokenize = new PrimitiveTokenizer().tokenize(line);
		final WordFormParser wordFormParser = new WordFormParser(WordNetProvider.getInstance());
		wordFormParser.setIgnoreCombinations(true);
		final List<IToken> process = wordFormParser.process(tokenize);
		return process;
	}

	@Override
	protected void doSave(File fl) throws IOException {
		map.store(fl);
	}

}