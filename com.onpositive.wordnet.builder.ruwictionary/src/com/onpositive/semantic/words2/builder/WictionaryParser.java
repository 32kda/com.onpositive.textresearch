package com.onpositive.semantic.words2.builder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;

import com.onpositive.semantic.wordnet.Grammem;
import com.onpositive.semantic.wordnet.Grammem.PartOfSpeech;
import com.onpositive.semantic.wordnet.MeaningElement;
import com.onpositive.semantic.wordnet.SemanticRelation;
import com.onpositive.semantic.words2.AbstractRelationTarget;
import com.onpositive.semantic.words2.SimpleWordNet;
import com.onpositive.semantic.words2.Word;
import com.onpositive.semantic.words2.WordNet;
import com.onpositive.semantic.words2.WordNetContributor;
public class WictionaryParser extends WordNetContributor {

	public static final class PageProcessor implements IPageVisitor {
		private final HashSet<Character> mm;
		private final WordNet wi;
		private TempExtras extras;

		public PageProcessor(WordNet wi, TempExtras etras) {
			this.wi = wi;
			this.extras=etras;
			mm = new HashSet<Character>();
			for (char c : russianLetters.toCharArray()) {
				mm.add(c);
				mm.add(Character.toUpperCase(c));
			}
		}

		@Override
		public void visit(PageModel model) {
			if (model.getNamespace()!=0){
				return;
			}						
			if (model.getText().startsWith("#")) {
				String lowerCase = model.getText()
						.toLowerCase();
				if (lowerCase.startsWith("#redirect [[")) {
					String sm = model.getText().substring(
							"#REDIRECT [[".length());
					sm = sm.substring(0, sm.length() - 2);
					wi.markRedirect(model.getTitle(), sm);
					return;
				}
				if (lowerCase
						.startsWith("#перенаправление [[")) {
					String sm = model.getText().substring(
							"#перенаправление [[".length());
					sm = sm.substring(0, sm.length() - 2);
					wi.markRedirect(model.getTitle(), sm);
					return;
				}
			}
			if (!isRussianPage(model)) {
				return;
			}
			BufferedReader rs = createReader(model);
			boolean areWeParsingKnownRelation = false;
			int kind = -1;
			Word orCreate = (Word) wi.getOrCreateWord(model
					.getTitle().toLowerCase());
			boolean inRuPart=true;
			while (true) {
				String readLine;
				try {
					readLine = rs.readLine();
					if (readLine == null) {
						break;
					}
					inRuPart = langSwitchIfNeeded(inRuPart, readLine);
					if (!inRuPart){
						continue;
					}									
					processWordKind(orCreate, readLine);
					processFeatures(orCreate, readLine);
					parseFoundations(extras.getInfo(orCreate), readLine);
					int nkind = parseSemanticRelationHeader(readLine);
					if (nkind!=-1){
						areWeParsingKnownRelation = true;
						kind=nkind;
					}
					else if (areWeParsingKnownRelation) {
						if (readLine.startsWith("==")) {
							areWeParsingKnownRelation = false;
						} else {
							if (readLine.startsWith("#")) {
								readLine = readLine
										.substring(1)
										.trim();
							}
							processLineWithRelations(kind,
									orCreate, readLine);
						}
					}
				} catch (IOException e) {
					break;
				}
			}
			orCreate.commitTempSet();
		}

		public int parseSemanticRelationHeader(String readLine) {
			int nkind=-1;
			if (readLine.contains("= Гиперонимы =")) {
				nkind = SemanticRelation.GENERALIZATION;
				
			}
			if (readLine.contains("= Синонимы =")) {
				nkind = SemanticRelation.SYNONIM;
				
			}
			if (readLine.contains("= Антонимы =")) {
				nkind = SemanticRelation.ANTONIM;
				
			}
			if (readLine.contains("= Меронимы =")) {
				nkind = SemanticRelation.MERONIM;
				
			}
			if (readLine.contains("= Гипонимы =")) {
				nkind = SemanticRelation.SPECIALIZATION;
			}
			return nkind;
		}

		public boolean langSwitchIfNeeded(boolean inRuPart, String readLine) {
			if (readLine.contains("= {{-ru-}} =")){
				inRuPart=true;
			}
			if (inRuPart){
				inRuPart = testLanguageSwitch(inRuPart, readLine);
			}
			return inRuPart;
		}

		public boolean testLanguageSwitch(boolean inRuPart, String readLine) {
			if (readLine.contains("= {{-be-}} =")){
				inRuPart=false;
			}	
			if (readLine.contains("= {{-bg-}} =")){
				inRuPart=false;
			}
			if (readLine.contains("= {{-mk-}} =")){
				inRuPart=false;
			}
			if (readLine.contains("= {{-mdf-}} =")){
				inRuPart=false;
			}
			if (readLine.contains("= {{-uk-}} =")){
				inRuPart=false;
			}
			if (readLine.contains("= {{-udm-}} =")){
				inRuPart=false;
			}
			if (readLine.contains("= {{-sr-}} =")){
				inRuPart=false;
			}
			if (readLine.contains("= {{-ba-}} =")){
				inRuPart=false;
			}
			if (readLine.contains("= {{-ab-}} =")){
				inRuPart=false;
			}
			if (readLine.contains("= {{-xal-}} =")){
				inRuPart=false;
			}
			if (readLine.contains("= {{-az-}} =")){
				inRuPart=false;
			}
			if (readLine.contains("= {{-abq-}} =")){
				inRuPart=false;
			}
			if (readLine.contains("= {{-ru-old-}} =")){
				inRuPart=false;
			}
			if (readLine.contains("= {{-tt-}} =")){
				inRuPart=false;
			}
			if (readLine.contains("= {{-ady-}} =")){
				inRuPart=false;
			}
			if (readLine.contains("= {{-slovio-}} =")){
				inRuPart=false;
			}
			if (readLine.contains("= {{-cu-}} =")){
				inRuPart=false;
			}
			if (readLine.contains("= {{-sah-}} =")){
				inRuPart=false;
			}
			if (readLine.contains("= {{-cv-}} =")){
				inRuPart=false;
			}
			if (readLine.contains("= {{-ky-}} =")){
				inRuPart=false;
			}
			if (readLine.contains("= {{-alt-}} =")){
				inRuPart=false;
			}
			if (readLine.contains("= {{-evn-}} =")){
				inRuPart=false;
			}
			if (readLine.contains("= {{-inh-}} =")){
				inRuPart=false;
			}
			if (readLine.contains("= {{-isv-}} =")){
				inRuPart=false;
			}
			if (readLine.contains("= {{-orv-}} =")){
				inRuPart=false;
			}
			if (readLine.contains("= {{-tg-}} =")){
				inRuPart=false;
			}
			if (readLine.contains("= {{-os-}} =")){
				inRuPart=false;
			}
			if (readLine.contains("= {{-av-}} =")){
				inRuPart=false;
			}
			if (readLine.contains("= {{-kbd-}} =")){
				inRuPart=false;
			}
			if (readLine.contains("= {{-kk-}} =")){
				inRuPart=false;
			}
			if (readLine.contains("= {{-mn-}} =")){
				inRuPart=false;
			}
			if (readLine.contains("= {{-rom-}} =")){
				inRuPart=false;
			}
			if (readLine.contains("= {{-tkr-}} =")){
				inRuPart=false;
			}
			if (readLine.contains("= {{-chm-}} =")){
				inRuPart=false;
			}
			if (readLine.contains("= {{-agx-}} =")){
				inRuPart=false;
			}
			if (readLine.contains("= {{-lez-}} =")){
				inRuPart=false;
			}
			if (readLine.contains("= {{-kum-}} =")){
				inRuPart=false;
			}
			if (readLine.contains("= {{-chu-ru-}} =")){
				inRuPart=false;
			}
			if (readLine.contains("= {{-tab-}} =")){
				inRuPart=false;
			}
			if (readLine.contains("= {{-mul-}} =")){
				inRuPart=false;
			}
			if (readLine.contains("= {{-kpy-}} =")){
				inRuPart=false;
			}
			if (readLine.contains("= {{-bua-}} =")){
				inRuPart=false;
			}
			if (readLine.contains("= {{-gld-}} =")){
				inRuPart=false;
			}
			if (readLine.contains("= {{-rue-}} =")){
				inRuPart=false;
			}
			if (readLine.contains("= {{-cjs-}} =")){
				inRuPart=false;
			}
			if (readLine.contains("= {{-myv-}} =")){
				inRuPart=false;
			}
			if (readLine.contains("= {{-ce-}} =")){
				inRuPart=false;
			}
			if (readLine.contains("= {{-nio-}} =")){
				inRuPart=false;
			}
			if (readLine.contains("= {{-koi-}} =")){
				inRuPart=false;
			}
			if (readLine.contains("= {{-mo-}} =")){
				inRuPart=false;
			}
			if (readLine.contains("= {{-krc-}} =")){
				inRuPart=false;
			}
			if (readLine.contains("= {{-aqc-}} =")){
				inRuPart=false;
			}
			if (readLine.contains("= {{-eve-}} =")){
				inRuPart=false;
			}
			if (readLine.contains("= {{-dar-}} =")){
				inRuPart=false;
			}
			if (readLine.contains("= {{-mns-}} =")){
				inRuPart=false;
			}
			if (readLine.contains("= {{-dng-}} =")){
				inRuPart=false;
			}
			if (readLine.contains("= {{-gag-}} =")){
				inRuPart=false;
			}
			if (readLine.contains("= {{-niv-}} =")){
				inRuPart=false;
			}
			if (readLine.contains("= {{-ttt-}} =")){
				inRuPart=false;
			}
			if (readLine.contains("= {{-lbe-}} =")){
				inRuPart=false;
			}
			if (readLine.contains("= {{-ykg-}} =")){
				inRuPart=false;
			}
			if (readLine.contains("= {{-udi-}} =")){
				inRuPart=false;
			}
			return inRuPart;
		}

		private void processLineWithRelations(int kind, Word orCreate,
				String readLine) {
			if (readLine.length() == 0) {
				return;
			}
			readLine = readLine.replace(
					';', ',');
			String[] split = readLine
					.split(",");
			for (String s : split) {
				s = s.trim();
				if (s.startsWith("[[")) {
					if (s.endsWith("]]")) {
						String value = s
								.substring(
										2,
										s.length() - 2);
						MeaningElement[] concepts = orCreate.getConcepts();
						AbstractRelationTarget t= (AbstractRelationTarget) concepts[concepts.length-1];
						t.registerRelation(
								kind,
								wi.getOrCreateRelationTarget(value));
					}
				}
			}
		}

		private void processWordKind(Word orCreate, String readLine) {
			TempWordInfo gi=extras.getInfo(orCreate);
			if (readLine.startsWith("{{")) {
				String rl = readLine.substring(2)
						.trim();
				if (rl.startsWith("сущ")
						|| rl.startsWith("сущ")) {
					setupTemplate(wi, gi, rl);
					orCreate.setPartOfSpeech(Grammem.PartOfSpeech.NOUN);
				}
				if (rl.startsWith("conj")) {
					setupTemplate(wi, gi, rl);
					orCreate.setPartOfSpeech(Grammem.PartOfSpeech.CONJ);
				}
				if (rl.startsWith("числ")) {
					setupTemplate(wi, gi, rl);
					orCreate.setPartOfSpeech(Grammem.PartOfSpeech.NUMR);
				}
				
				if (rl.startsWith("прил")) {
					setupTemplate(wi, gi, rl);
					orCreate.setPartOfSpeech(Grammem.PartOfSpeech.ADJF);
				}
				if (rl.startsWith("гл")) {
					setupTemplate(wi, gi, rl);
					orCreate.setPartOfSpeech(Grammem.PartOfSpeech.VERB);
				}
			}
		}

		private void processFeatures(Word orCreate, String readLine) {
			if (readLine.contains("{{собств.}}")) {
				orCreate.setFeature(Grammem.SemanGramem.NAME);
			}
			if (readLine.contains("{{топоним}}")) {
				orCreate.setFeature(Grammem.SemanGramem.TOPONIM);
			}
			if (readLine.contains("топоним")) {
				orCreate.setFeature(Grammem.SemanGramem.TOPONIM);
			}
		}

		private void parseFoundations(TempWordInfo orCreate, String readLine) {
			String s = readLine;
			if (s.startsWith("|pt=")&&s.contains("1")){
				orCreate.setPluralTantum(true);
			}
			if (s.startsWith("|st=")&&s.contains("1")){
				orCreate.setSingularTantum(true);
			}
			while (s.indexOf("|основа") > -1) {
				s=parseFoundation(orCreate, s);
			}
		}

		private String parseFoundation(TempWordInfo orCreate, String s) {
			s = s.substring(s
					.indexOf("|основа"));
			
			if (s.startsWith("|основа")) {
				if (s.indexOf('=') > 0) {
					char c = s
							.charAt("|основа"
									.length());
					int oo = 0;
					if (Character
							.isDigit(c)) {
						oo = Integer
								.parseInt(""
										+ c);
					}
					String trim = s
							.substring(
									s.indexOf('=') + 1)
							.trim();
					if (trim.length() == 0) {
						trim = null;
					}
					if (trim != null) {
						int indexOf = trim
								.indexOf('|');
						if (indexOf != -1) {
							trim = trim
									.substring(
											0,
											indexOf);
						}
						indexOf = trim
								.indexOf('}');
						if (indexOf != -1) {
							trim = trim
									.substring(
											0,
											indexOf);
						}
						orCreate.registerFoundation(
								oo, trim);
					}
				}
			}
			s = s.substring("|основа"
					.length());
			return s;
		}

		public boolean isRussianPage(PageModel model) {
			String title=model.getTitle();
			int length=title.length();
			for (int a = 0; a < length; a++) {
				if (!mm.contains(title.charAt(a))&&title.charAt(a)!=' ') {
					return false;
				}
			}
			return model.getText().contains("{{-ru-}}");
		}

		public void setupTemplate(final WordNet wi,
				TempWordInfo orCreate, String rl) {
			String substring = cleanTemplate(rl);
			WordFormTemplate findTemplate = innerFindTemplate(
					wi, substring);
			if (findTemplate != null) {
				orCreate.setTemplate(findTemplate);
			}
		}

		private WordFormTemplate innerFindTemplate(
				final WordNet wi, String substring) {
			int indexOf = substring.indexOf('|');
			if (indexOf != -1) {
				substring = substring.substring(0, indexOf);
			}
			return extras.findTemplate("Шаблон:" + substring);
		}

		private String cleanTemplate(String readLine) {
			String substring = readLine;
			if (substring.endsWith("\"")) {
				substring = substring.substring(0,
						substring.length() - 1);
			}
			if (substring.endsWith("'")) {
				substring = substring.substring(0,
						substring.length() - 1);
			}
			substring = substring.trim();
			return substring;
		}
	}

	static final String russianLetters = "абвгдеёжзийклмнопрстуфхцчшщъыьэюя";

	static void fill(XMLPageParser pp, final WordNet wi, String name) {
		TempExtras etras=new TempExtras();
		registerTemplates(pp, wi, name, etras);
		processPages(pp, wi, name,etras);
		try{
		registerWords("conj.txt",Grammem.PartOfSpeech.CONJ,wi);
		registerWords("prep.txt",Grammem.PartOfSpeech.PREP,wi);
		}catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void registerWords(String string, PartOfSpeech conj, WordNet wi) throws UnsupportedEncodingException, IOException {
		URL resource = WictionaryParser.class.getResource(string);
		InputStreamReader ss=new InputStreamReader(resource.openStream(),"UTF-8");
		BufferedReader rr=new BufferedReader(ss);
		while (true){
			String readLine = rr.readLine();
			if (readLine==null){
				break;
			}
			readLine=readLine.trim();	
				
			Word orCreateWord = wi.getOrCreateWord(readLine.toLowerCase());
			orCreateWord.setPartOfSpeech(conj);
			orCreateWord.commitTempSet();
		}
	}

	public static void processPages(XMLPageParser pp, final WordNet wi,
			String name, TempExtras etras) {
		try {
			try {
				pp.visitContent(new BufferedReader(new InputStreamReader(
						new FileInputStream(name), "UTF-8")),
						new PageProcessor(wi,etras));

			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public static void registerTemplates(XMLPageParser pp, final WordNet wi,
			String name,final TempExtras extras) {
		try {
			System.out.println("Templates collecting");
			pp.visitContent(new BufferedReader(new InputStreamReader(
					new FileInputStream(name), "UTF-8")), new IPageVisitor() {

				@Override
				public void visit(PageModel model) {
					if (model.getNamespace() == 10) {
						if (model.getTitle().startsWith("Шаблон:сущ ru")) {
							try {
								WordFormTemplate wordFormTemplate = new WordFormTemplate(
										model.getTitle(), model.getText(), wi,
										Grammem.PartOfSpeech.NOUN.intId);
								extras.registerTemplate(wordFormTemplate);
							} catch (IOException e) {
							}
						}
						if (model.getTitle().startsWith("Шаблон:прил ru")) {
							try {
								WordFormTemplate wordFormTemplate = new WordFormTemplate(
										model.getTitle(), model.getText(), wi,
										Grammem.PartOfSpeech.ADJF.intId);
								extras.registerTemplate(wordFormTemplate);
							} catch (IOException e) {
							}
						}
						if (model.getTitle().startsWith("Шаблон:гл ru")) {
							try {
								WordFormTemplate wordFormTemplate = new WordFormTemplate(
										model.getTitle(), model.getText(), wi,
										Grammem.PartOfSpeech.VERB.intId);
								extras.registerTemplate(wordFormTemplate);
							} catch (Exception e) {
							}
						}
					}
				}

			});
		} catch (UnsupportedEncodingException e1) {
		} catch (FileNotFoundException e1) {

		}
		System.out.println("Templates built!!");
	}

	private static BufferedReader createReader(PageModel model) {
		return new BufferedReader(
				new StringReader(model.getText()));
	}

	@Override
	public void contribute(File dataDirectory, SimpleWordNet net) {
		File[] listFiles = dataDirectory.listFiles();
		for (File f : listFiles) {
			if (f.getName().startsWith("ruwik")) {
				if (f.getName().endsWith(".xml")) {
					SimpleWordNet wi = net;
					WictionaryParser.fill(new XMLPageParser(), wi,
							f.getAbsolutePath());
					return;
				}
			}
		}
		System.err.println("ruwictionary file not found in data directory:"
				+ dataDirectory.getAbsolutePath());
	}
}