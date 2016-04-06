package com.onpositive.semantic.wikipedia2.locations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.carrotsearch.hppc.IntOpenHashSetSerializable;
import com.google.common.collect.Sets;
import com.onpositive.semantic.wikipedia2.WikiEngine2;
import com.onpositive.semantic.wikipedia2.fulltext.StemProvider;
import com.onpositive.semantic.wikipedia2.locations.DocumentGeoIndex.DocumentGeoMatch;
import com.onpositive.semantic.wikipedia2.locations.DocumentGeoIndex.DocumentGeoMatchType;
import com.onpositive.semantic.wordnet.GrammarRelation;
import com.onpositive.semantic.wordnet.Grammem;
import com.onpositive.semantic.wordnet.WordNetProvider;

public class DocumentGeoIndexBuilder extends AbstractDocumentBasedBuilder {

	public static final String DOCUMENT_GEO_INDEX_FILE_NAME = "doc_geo_names_v7.index";

	private GeoIndex geoIndex;

	private List<DocumentGeoMatch> geoNames;

	private DocumentGeoIndex result = new DocumentGeoIndex();

	private static String russianLetters = "абвгдеёжзийклмнопрстуфхцчшщъыьэюяАБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫьЭЮЯ";

	private static HashSet<Character> ssm = new HashSet<Character>();
	static {
		{
			for (int a = 0; a < russianLetters.length(); a++) {
				ssm.add(russianLetters.charAt(a));
			}
		}
	}

	private static String russianCapitalLettersStr = "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫьЭЮЯ";
	private static HashSet<Character> capitalRussianLetters = new HashSet<Character>();
	static {
		{
			for (int a = 0; a < russianCapitalLettersStr.length(); a++) {
				capitalRussianLetters.add(russianCapitalLettersStr.charAt(a));
			}
		}
	}

	private static Set<String> locationPrepositions = Sets.newHashSet("в",
			"на", "за", "от");

	public int allDocumentsStartedToCheck = 0;
	public int allDocumentsChecked = 0;
	public int novosibirskDocumentsChecked = 0;

	public DocumentGeoIndexBuilder(WikiEngine2 documentGeoIndex,
			GeoIndex geoIndex) {
		super(documentGeoIndex);
		this.geoIndex = geoIndex;
	}

	public DocumentGeoIndex build() {
		processDocuments();

		return result;
	}

	protected void startProcessingDocument(int documentId) {
		allDocumentsStartedToCheck++;
		geoNames = new ArrayList<DocumentGeoMatch>();
	}

	protected void finishedProcessingDocument(int documentId) {
		allDocumentsChecked++;
		try {
			if (geoNames != null && geoNames.size() > 0) {
				result.registerData(documentId, geoNames);
			}
		} catch (Throwable th) {
			th.printStackTrace();
		}
	}

	protected void encounteredDocumentAbstract(int documentId,
			String abstractText) {
		Collection<DocumentGeoMatch> parsedNames = getNamesFromText(abstractText);
		if (parsedNames == null || parsedNames.size() == 0) {
			return;
		}

		geoNames.addAll(parsedNames);
		addParts(DocumentPart.FIRST_PARAGRAPH, parsedNames);
	}

	protected void encounteredDocumentBody(int document, String text) {

		boolean novosibirskFound = false;
		if (text != null && text.toLowerCase().contains("новосибирск")) {
			novosibirskFound = true;
			novosibirskDocumentsChecked++;
		}

		Collection<DocumentGeoMatch> parsedNames = getNamesFromText(text);
		if (novosibirskFound) {
			boolean novosisibrskGeoNameFound = false;
			for (DocumentGeoMatch match : parsedNames) {
				if (match.getGeoId() == 1496747) {
					novosisibrskGeoNameFound = true;
				}
			}

			if (!novosisibrskGeoNameFound) {
				System.out
						.println("---------------------------------------------------------");
				System.out.print(text);
				System.out
						.println("---------------------------------------------------------");
			}
		}

		if (parsedNames == null || parsedNames.size() == 0) {
			return;
		}

		geoNames.addAll(parsedNames);
		addParts(DocumentPart.OTHER, parsedNames);
	}

	protected void encouneredDocumentCategories(int documentId,
			List<String> categories) {
		for (String category : categories) {
			Collection<DocumentGeoMatch> parsedNames = getNamesFromText(category);
			if (parsedNames == null || parsedNames.size() == 0) {
				return;
			}

			geoNames.addAll(parsedNames);
			addParts(DocumentPart.CATEGORY, parsedNames);
		}
	}

	@Override
	protected void encounteredDocumentTitle(int documentId, String title) {
		Collection<DocumentGeoMatch> parsedNames = getNamesFromText(title);
		if (parsedNames == null || parsedNames.size() == 0) {
			return;
		}

		geoNames.addAll(parsedNames);
		addParts(DocumentPart.TITLE, parsedNames);
	}

	private void addParts(DocumentPart part,
			Collection<DocumentGeoMatch> matchesToPatch) {
		for (DocumentGeoMatch match : matchesToPatch) {
			match.setDocumentPart(part);
		}
	}

	public Collection<DocumentGeoMatch> getNamesFromText(String text) {

		if (text == null) {
			return Collections.emptyList();
		}

		Set<DocumentGeoMatch> result = new LinkedHashSet<DocumentGeoMatch>();
		try {

			String previousWord = null;
			boolean previousWordFirstLetterCapital = false;

			String previousPreviousWord = null;
			int previousTextPos = 0;

			StringBuilder bld = new StringBuilder();

			for (int textPos = 0; textPos < text.length(); textPos++) {
				char c = text.charAt(textPos);
				if (Character.isLetter(c)) {
					bld.append(c);
				} else {
					String string = bld.toString();

					try {
						if (string.length() > 0) {
							boolean allRussian = true;
							for (int b = 0; b < string.length(); b++) {
								if (!ssm.contains(string.charAt(b))) {
									allRussian = false;
									break;
								}
							}
							if (allRussian) {
								boolean firstLetterCapital = capitalRussianLetters
										.contains(string.charAt(0));
								String originalNameToCheck = string
										.toLowerCase();

								if (string.length() > 3) {

									/*String stemmedNameToCheck = StemProvider
											.getInstance().stem(
													originalNameToCheck);*/

									Collection<DocumentGeoMatch> wordMatches = getWordMatches(
											originalNameToCheck,
											originalNameToCheck,
											firstLetterCapital, previousWord,
											textPos);

									if (wordMatches.size() != 0) {
										result.addAll(wordMatches);
									} else if (previousWord != null) {
										String combined = previousWord + " "
												+ originalNameToCheck;
										String combinedStem = previousWord
												+ " " + originalNameToCheck;
										result.addAll(getWordMatches(combined,
												combinedStem,
												previousWordFirstLetterCapital,
												previousPreviousWord,
												previousTextPos));
									}
								}

								previousPreviousWord = previousWord;
								previousWord = originalNameToCheck;
								previousWordFirstLetterCapital = firstLetterCapital;
								previousTextPos = textPos;
							}
						}
					} finally {
						bld.delete(0, bld.length());
					}
				}
			}
		} catch (Throwable th) {
			th.printStackTrace();
		}

		return result;
	}

	private Collection<DocumentGeoMatch> getWordMatches(
			String originalNameToCheck, String stemmedNameToCheck,
			boolean firstLetterCapital, String previousWord, int position) {

		List<DocumentGeoMatch> result = new ArrayList<DocumentGeoMatch>();
		GrammarRelation[] possibleGrammarForms = WordNetProvider.getInstance()
				.getPossibleGrammarForms(originalNameToCheck);
		//checking word net knowledge
		boolean wordnetThinksGeo=false;
		HashSet<String>basicForms=new HashSet<String>();
		if (possibleGrammarForms != null) {
			for (GrammarRelation q : possibleGrammarForms) {
				if (q.hasGrammem(Grammem.SemanGramem.TOPONIM)){
					wordnetThinksGeo=true;
				}
				if (q.getWord().hasGrammem(Grammem.SemanGramem.TOPONIM)){
					wordnetThinksGeo=true;
				}
				basicForms.add(q.getWord().getBasicForm());
			}
		}
		final boolean isCommonWord = true;

		try {

			boolean prepositioned = previousWord != null
					&& locationPrepositions.contains(previousWord);
			IntOpenHashSetSerializable intOpenHashSet = new IntOpenHashSetSerializable();
			// checking direct name
			int[] geoNameIds = geoIndex.getIdsByName(originalNameToCheck);
			intOpenHashSet.add(geoNameIds);
			for (String s:basicForms){
				intOpenHashSet.add(geoIndex.getIdsByName(s));
			}
			for (int geoNameId : intOpenHashSet.toArray()) {
				boolean directMatch = false;

				Collection<String> names = geoIndex.getNamesById(geoNameId);
				if (names != null && names.contains(originalNameToCheck)) {
					directMatch = true;
				}
				for (String s:basicForms){
					if (names != null && names.contains(s)) {
						directMatch = true;
						break;
					}
				}
				if (directMatch&&!wordnetThinksGeo){
					directMatch=false;
				}
				DocumentGeoMatchType matchType = null;
				if (directMatch) {
					if (firstLetterCapital) {
						matchType = DocumentGeoMatchType.UPPERCASED_DIRECT;
					} else if (prepositioned) {
						matchType = DocumentGeoMatchType.PREPOSITIONED;
					} else {
						matchType = DocumentGeoMatchType.DIRECT;
					}
				} 
				if (!isCommonWord
						|| matchType == DocumentGeoMatchType.UPPERCASED_DIRECT) {
					DocumentGeoMatch match = new DocumentGeoMatch(geoNameId,
							null, position, matchType);
					result.add(match);
				}
			}
		} catch (Throwable th) {
			th.printStackTrace();
		}

		return result;
	}
}
