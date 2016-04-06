package com.onpositive.semantic.wikipedia2.properties.parsing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import com.onpositive.semantic.categorization.core.SearchSystem;
import com.onpositive.semantic.wikipedia2.WikiDoc;
import com.onpositive.semantic.wikipedia2.WikiEngine2;
import com.onpositive.semantic.wikipedia2.properties.PropertyPackager.AllPropertiesStorage.DecodedProperty;
import com.onpositive.semantic.wikipedia2.services.CaseIgnoringSearcher;
import com.onpositive.semantic.wikipedia2.services.FromEnglishIdIndex;
import com.onpositive.semantic.wikipedia2.services.RedirectsMap;
import com.onpositive.semantic.wikipedia2.services.ToEnglishIdIndex;
import com.onpositive.text.analysis.IToken;
import com.onpositive.text.analysis.lexic.DimensionToken;
import com.onpositive.text.analysis.lexic.ScalarToken;
import com.onpositive.text.analysis.syntax.SyntaxToken;

public class PropertyParser {

	final WikiEngine2 enengine = (WikiEngine2) SearchSystem.getEngine("enwiki");

	public void preprocessLinks(SourceData data) {
		ArrayList<DecodedProperty> vals = data.allProperty;
		MultiMap<String, DecodedProperty> val = MultiMap.withList();
		MultiMap<String, String> captions = MultiMap.withSet();
		HashMap<String, String> captionToLink = new HashMap<String, String>();
		HashSet<String> vocabLinks = new HashSet<String>();
		WikiEngine2 vocabEngine2 = WikiEngine2.getVocabEngine();
		CaseIgnoringSearcher searcher = vocabEngine2
				.getIndex(CaseIgnoringSearcher.class);
		ToEnglishIdIndex toEng = vocabEngine2.getIndex(ToEnglishIdIndex.class);
		vals.stream().forEach(p -> p.tokenize().forEach(a -> {
			String link = a.getLink();

			if (link != null) {
				val.add(link, p);

				if (p.engine == vocabEngine2) {
					vocabLinks.add(link.toLowerCase());
				}

				String caption = a.getStringValue();
				if (a instanceof SyntaxToken) {
					SyntaxToken tk = (SyntaxToken) a;
					caption = tk.getBasicForm();
				}
				captions.add(link, caption);
				captionToLink.put(caption.toLowerCase(), link);
			}
		}));
		WikiEngine2 en_egine = (WikiEngine2) SearchSystem.getEngine("enwiki");
		HashMap<String, String> engNames = new HashMap<String, String>();
		if (en_egine != null) {
			vocabLinks.forEach(a -> {
				int v = searcher.search(a);
				if (v != -1) {
					int value = toEng.value(v);
					if (value > 0) {
						String string = en_egine.getPageTitles().get(value);
						engNames.put(string.toLowerCase(), a);
					}
				}
			});
		}
		for (SourceInformation q : data.informations) {
			for (DecodedProperty z : q.properties) {

				for (IToken t : z.tokenize()) {
					String link = t.getLink();
					if (link != null) {
						processLink(vocabEngine2, en_egine, engNames, q, z, t);
					}
					if (t instanceof ScalarToken) {
						ScalarToken tc = (ScalarToken) t;
						q.vals.add(new ScalarValue(tc));
					}
					if (t instanceof DimensionToken) {
						DimensionToken tc = (DimensionToken) t;
						q.vals.add(new DimensionValue(tc));
					}
				}

			}
		}
		simpleLinkRecognition(data, captionToLink, vocabEngine2, en_egine,
				engNames);
	}

	void simpleLinkRecognition(SourceData data,
			HashMap<String, String> captionToLink, WikiEngine2 vocabEngine2,
			WikiEngine2 en_egine, HashMap<String, String> engNames) {
		for (SourceInformation q : data.informations) {
			for (DecodedProperty z : q.properties) {
				List<IToken> tokenize = z.tokenize();
				l2: for (int a = 0; a < tokenize.size(); a++) {
					StringBuilder bld = new StringBuilder();
					for (int b = a; b < tokenize.size(); b++) {
						IToken iToken = tokenize.get(b);
						if (iToken.getLink() != null) {
							continue l2;
						}
						/*
						 * if (iToken.hasSpaceBefore()) { bld.append(' '); }
						 */
						if (iToken instanceof SyntaxToken) {
							SyntaxToken tk = (SyntaxToken) iToken;
							bld.append(tk.getBasicForm());

						} else {
							bld.append(iToken.getStringValue());
						}
						if (iToken.hasSpaceAfter()) {
							bld.append(' ');
						}
						String suggestedForm = bld.toString().toLowerCase()
								.trim();
						if (suggestedForm.length() > 2) {
							if (captionToLink.containsKey(suggestedForm)) {
								String link = captionToLink.get(suggestedForm);								
								innerLink(vocabEngine2, en_egine, engNames, q,
										z, link);
							}
						}
					}
				}
			}
		}
	}

	void processLink(WikiEngine2 vocabEngine, WikiEngine2 en_engine,
			HashMap<String, String> engNames, SourceInformation q,
			DecodedProperty z, IToken t) {
		String link = t.getLink();
		if (link != null) {
			innerLink(vocabEngine, en_engine, engNames, q, z, link);
		}
	}

	void innerLink(WikiEngine2 vocabEngine, WikiEngine2 en_engine,
			HashMap<String, String> engNames, SourceInformation q,
			DecodedProperty z, String link) {
		link = link.toLowerCase();
		WikiEngine2 eng = z.engine;
		String string = engNames.get(link);
		if (string != null) {
			link = string;
			eng = vocabEngine;
		}
		int search = eng.getIndex(CaseIgnoringSearcher.class).search(link);

		if (search != -1) {
			RedirectsMap index = eng.getIndex(RedirectsMap.class);
			if (index.isRedirect(search)) {
				search = index.value(search);
			}
			WikiDoc document = new WikiDoc(eng, search);
			// we should do another try here because of redirects
			if (eng == en_engine) {
				String title = document.getTitle().toLowerCase();
				string = engNames.get(title);
				if (string != null) {
					int search2 = vocabEngine.getIndex(
							CaseIgnoringSearcher.class).search(string);
					if (search2 != -1) {
						document = new WikiDoc(vocabEngine, search2);
					}
				}
				int searchRussian = vocabEngine.getIndex(
						FromEnglishIdIndex.class).value(document.getIntId());
				if (searchRussian != -1) {
					document = new WikiDoc(vocabEngine, searchRussian);
				}
			}
			q.vals.add(new LinkValue(new Link(document.getTitle(), document)));
		}
	}

	public ArrayList<PropertyValues> prepare(SourceData data) {
		ArrayList<PropertyValues> result = new ArrayList<PropertyValues>();
		preprocessLinks(data);
		for (SourceInformation v : data.informations) {
			result.add(new PropertyValues(v.source, v.vals, v.properties));
		}
		return result;
	}
}
