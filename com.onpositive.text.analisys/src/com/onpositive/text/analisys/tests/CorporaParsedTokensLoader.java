package com.onpositive.text.analisys.tests;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.onpositive.semantic.wordnet.Grammem;
import com.onpositive.semantic.wordnet.Grammem.PartOfSpeech;
import com.onpositive.text.analisys.tests.euristics.SimplifiedToken;

public class CorporaParsedTokensLoader extends BasicParsedTokensLoader {

	private static final String PARTCP_ID = "partcp";
	private static final String BREV_ID = "brev";
	private static final String INF_ID = "inf";
	private static final Map<String, PartOfSpeech> partsTable = new HashMap<>(); 
	
	static {
		partsTable.put("S", PartOfSpeech.NOUN); 
		partsTable.put("A", PartOfSpeech.ADJF);
		partsTable.put("A,brev", PartOfSpeech.ADJS);
		partsTable.put("V", PartOfSpeech.VERB);
		partsTable.put("V,inf", PartOfSpeech.INFN);
		partsTable.put("V,partcp", PartOfSpeech.PRTF);
		partsTable.put("V,ger", PartOfSpeech.GRND);
		partsTable.put("V,partcp,brev", PartOfSpeech.PRTS);
		partsTable.put("NUM", PartOfSpeech.NUMR);
		partsTable.put("A-NUM", PartOfSpeech.ADJF);
		partsTable.put("ANUM", PartOfSpeech.ADJF);
		partsTable.put("ADV", PartOfSpeech.ADVB);
		partsTable.put("PRAEDIC", PartOfSpeech.PRED);
		partsTable.put("PARENTH", PartOfSpeech.CONJ);
		partsTable.put("S-PRO", PartOfSpeech.NPRO);
		partsTable.put("A-PRO", PartOfSpeech.ADJF);
		partsTable.put("ADV-PRO", PartOfSpeech.ADVB);
		partsTable.put("PRAEDIC-PRO", PartOfSpeech.PRED);
		partsTable.put("PR", PartOfSpeech.PREP);
		partsTable.put("CONJ", PartOfSpeech.CONJ);
		partsTable.put("PART", PartOfSpeech.PRCL);
		partsTable.put("INTJ", PartOfSpeech.INTJ);
	}
	
	private List<SimplifiedToken> tokens = new ArrayList<SimplifiedToken>();
	List<String> sentences = new ArrayList<String>();
	private Stack<String> tagStack = new Stack<>();
	
	private DefaultHandler handler = new DefaultHandler() {
		
		private String curName;
		private StringBuilder textBuilder = new StringBuilder();
		private StringBuilder sentenceBuilder = new StringBuilder();
		private List<Grammem> grammems = new ArrayList<Grammem>();
		
		public void startElement(String uri, String localName, String qName, org.xml.sax.Attributes attributes) throws SAXException {
			tagStack.push(qName);
			if ("ana".equalsIgnoreCase(qName)) {
				boolean partcp = false;
				boolean brev = false;
				boolean inf = false;
				String gramList = attributes.getValue("gr");
				String[] grPairs = gramList.split(",");
				String partStr = "";
				if (grPairs.length > 0 && !grPairs[0].trim().isEmpty()) {
					partStr = grPairs[0].trim();
					int idx = partStr.indexOf('=');
					if (idx > 0) {
						partStr = partStr.substring(0, idx);
					}
				}
				for (int i = 0; i < grPairs.length; i++) {
					if (grPairs[i].contains(PARTCP_ID)) {
						partcp = true;
					}
					if (grPairs[i].contains(BREV_ID)) {
						brev = true;
					}
					if (grPairs[i].contains(INF_ID)) {
						inf = true;
					}
				}
				if (!partStr.isEmpty()) {
					if (partcp) {
						partStr += "," + PARTCP_ID;
					}
					if (brev) {
						partStr += "," + BREV_ID;
					}
					if (inf) {
						partStr += "," + INF_ID;
					}
					PartOfSpeech part = partsTable.get(partStr);
					if (part != null) {
						grammems.add(part);
					} else {
						System.err.println("Часть речи не найдена: " + partStr);
					}
				}
			} 
		};
		
		public void characters(char[] ch, int start, int length) throws SAXException {
			if ("w".equalsIgnoreCase(tagStack.peek())) {
				String str = new String(ch, start, length);
				curName = str.replaceAll("`","");
			}
			if ("se".equalsIgnoreCase(tagStack.peek())) {
				String str = new String(ch, start, length);
				if (!str.trim().isEmpty()) {
					sentenceBuilder.append(str.trim());
					sentenceBuilder.append(" ");
				}
				
//				if (textBuilder.length() > 0 && Character.isAlphabetic(textBuilder.charAt(textBuilder.length() - 1))) {
//					textBuilder.append(' ');
//				}
//				textBuilder.append(str);
			}
		};
		
		public void endElement(String uri, String localName, String qName) throws SAXException {
			tagStack.pop();
			if ("w".equalsIgnoreCase(qName)) {
				SimplifiedToken e = new SimplifiedToken(curName, grammems);
				if (!grammems.isEmpty() && grammems.get(0) != null) {
//					System.out.println("Token: " + curName + ", Grammmems: " + grammems.toString());
					tokens.add(e);
				}
				sentenceBuilder.append(curName);
				sentenceBuilder.append(" ");
				curName = null;
				grammems = new ArrayList<Grammem>();
			} else if ("se".equalsIgnoreCase(qName) && !tokens.isEmpty()) {
				String sentence = sentenceBuilder.toString();
				sentences.add(sentence);
				textBuilder.append(sentence);
				sentenceBuilder.delete(0, sentenceBuilder.length());
				chains.add(tokens);	
				tokens = new ArrayList<SimplifiedToken>();
			} 
		}
		
		public void endDocument() throws SAXException {
			initialText = textBuilder.toString().replaceAll("\\t","");
		};
	
	};
	
	
	
	public CorporaParsedTokensLoader(InputStream stream) {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		try {
			SAXParser parser = factory.newSAXParser();
			parser.parse(stream, handler);
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public List<String> getSentences() {
		return sentences;
	}

}
