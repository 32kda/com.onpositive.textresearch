package com.onpositive.wikipedia.workbench.words.primary;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import com.carrotsearch.hppc.IntArrayList;
import com.onpositive.semantic.wikipedia2.WikiDoc;
import com.onpositive.semantic.wikipedia2.WikiEngine2;
import com.onpositive.semantic.wikipedia2.services.RedirectsMap;
import com.onpositive.semantic.wordnet.TextElement;
import com.onpositive.semantic.wordnet.WordNetProvider;
import com.onpositive.semantic.words3.hds.IntToIntArrayMap;
import com.onpositive.text.analysis.IToken;
import com.onpositive.text.analysis.lexic.PrimitiveTokenizer;
import com.onpositive.text.analysis.lexic.SentenceSplitter;
import com.onpositive.text.analysis.lexic.WordFormParser;
import com.onpositive.text.analysis.syntax.SentenceToken;

public class RelatedWordsIndex extends WordsSequenceIndex {

	public RelatedWordsIndex(WikiEngine2 instance) {
		super(instance);
	}

	

	public void processDoc(WikiEngine2 instance, int doc, final String documentAbstract) {
		
		final BufferedReader bufferedReader = new BufferedReader(new StringReader(documentAbstract));
		try {
			String line = bufferedReader.readLine();
			line = line.replace("" + (char) 769, "");
			line = line.replace("" + (char) 160, "");
//			final List<IToken> wordFormTokens = new ArrayList<>();
			final List<IToken> process = tokens(line);
			final String string = instance.getPageTitles().get(doc).toLowerCase();
//			final List<IToken> tokens = tokens(string);
			final List<IToken> split = new SentenceSplitter().split(process);
			for (IToken t : split) {
				if (t instanceof SentenceToken) {
					final MainWordCollector2 mainWordCollector = new MainWordCollector2();
					mainWordCollector.visit(t);
					final HashSet<String> result = mainWordCollector.getResult();
					final HashSet<String> sm = new HashSet<>();
					
					IntArrayList ll = new IntArrayList();
					for (String q : result) {
						if (string.indexOf(q) == -1) {
							sm.add(q);
							final TextElement wordElement = WordNetProvider.getInstance().getWordElement(q);
							if (wordElement == null) {
								continue;
							}
							ll.add(wordElement.id());
						}
					}
					if (!ll.isEmpty()) {
						// System.out.println(string+":"+sm);
						map.add(doc, ll.toArray());
					}
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return;
	}

	@Override
	public String getFileName() {
		return "relatedWords.index";
	}
}
