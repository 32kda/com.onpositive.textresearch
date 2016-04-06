package com.onpositive.semantic.words2.builder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.onpositive.semantic.wordnet.GrammarRelation;
import com.onpositive.semantic.wordnet.Grammem;
import com.onpositive.semantic.wordnet.Grammem.PartOfSpeech;
import com.onpositive.semantic.wordnet.MorphologicalRelation;
import com.onpositive.semantic.words2.AbstractRelationTarget;
import com.onpositive.semantic.words2.SimpleWordNet;
import com.onpositive.semantic.words2.Word;
import com.onpositive.semantic.words2.WordNetContributor;

public class RuCorpusParser extends WordNetContributor{

	@Override
	public void contribute(File dataDirectory, final SimpleWordNet net) {
		File dict=new File(dataDirectory,"dict.opcorpora.xml");
		if (!dict.exists()){
			return;
		}
		final HashMap<HashSet<String>, Integer>allMaps=new HashMap<HashSet<String>, Integer>();
		final HashMap<Integer, AbstractRelationTarget>idToMeaning=new HashMap<Integer, AbstractRelationTarget>();
		DefaultHandler dh=new DefaultHandler(){
			
			Word currentWord;
			boolean inLemma;
			boolean inForm;
			String formText;
			
			
			HashSet<String>formElements=new HashSet<String>();
			HashSet<String>lemmaElements=new HashSet<String>();
			int id;
			
			@Override
			public void startElement(String uri, String localName,
					String qName, Attributes attributes) throws SAXException {
				if (qName.equals("lemma")){
					id=Integer.parseInt(attributes.getValue("id"));
				}
				if (qName.equals("f")){
					formText=attributes.getValue("t");
					inForm=true;
				}
				if (qName.equals("link")){
					formText=attributes.getValue("t");
					int from=Integer.parseInt(attributes.getValue("from"));
					int to=Integer.parseInt(attributes.getValue("to"));
					int type=Integer.parseInt(attributes.getValue("type"));
					AbstractRelationTarget fromTarget = idToMeaning.get(from);
					AbstractRelationTarget toTarget = idToMeaning.get(to);
					int knd0= type+MorphologicalRelation.MORPHOLOGICAL_OFFSET;
					int knd1=type+MorphologicalRelation.MORPHOLOGICAL_OFFSET+MorphologicalRelation.BACK_LINK_OFFSET;
					fromTarget.registerRelation(knd0, toTarget);
					toTarget.registerRelation(knd1, fromTarget);
				}
				if (qName.equals("g")){
					String value = attributes.getValue("v");
					if (inForm){
						if (value.length()>0){
						formElements.add(value);
						}
					}
					if (inLemma){
						lemmaElements.add(value);
					}
				}
				if (qName.equals("l")){
					inLemma=true;
					String value = attributes.getValue("t");
					if (value.length()==0){
						throw new IllegalStateException();
					}
					currentWord=net.getOrCreateWord(value);
				}
				
				super.startElement(uri, localName, qName, attributes);
			}
			
			
			@Override
			public void endElement(String uri, String localName, String qName)
					throws SAXException {
				if (qName.equals("lemma")){
					currentWord=null;
					lemmaElements=new HashSet<String>();
				}
				if (qName.equals("f")){
					inForm=false;
					Integer integer = allMaps.get(formElements);
					if (integer==null){
						allMaps.put(formElements, 1);
					}
					else{
						allMaps.put(formElements, integer+1);
					}
					LinkedHashSet<Grammem> grammems = toGrammems(formElements);
					LinkedHashSet<Grammem> grammems2 = toGrammems(lemmaElements);
					for (Grammem q:grammems2){
						if (q instanceof PartOfSpeech){
							grammems.add(q);
						}
					}
					int r=net.getGrammemCode(grammems);
					GrammarRelation form = new GrammarRelation(net, currentWord, r);
					formElements=new HashSet<String>();
					net.registerWordForm(formText,form);					
				}
				if (qName.equals("l")){
					inLemma=false;
					
					currentWord.setGrammemCode(getRelationCode(lemmaElements, net));
					AbstractRelationTarget cc=currentWord.getCurrentRelationTarget();
					AbstractRelationTarget put = idToMeaning.put(id, cc);
					if (put!=null){
						throw new IllegalStateException();
					}
					//lemma end;
				}
				
				super.endElement(uri, localName, qName);
			}
		};
		try {
			SAXParserFactory.newInstance().newSAXParser().parse(new InputSource(new BufferedReader(new InputStreamReader(new FileInputStream(dict), "UTF-8"))), dh);
			System.out.println(net.conceptCount());
			System.out.println(allMaps.size());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
	}

	protected int getRelationCode(HashSet<String> formElements,
			SimpleWordNet net) {
		LinkedHashSet<Grammem> mmm = toGrammems(formElements);
		int r=net.getGrammemCode(mmm);
		return r;
	}

	private LinkedHashSet<Grammem> toGrammems(HashSet<String> formElements) {
		LinkedHashSet<Grammem>mmm=new LinkedHashSet<Grammem>();
		for (String s:formElements){
			Grammem grammem = Grammem.get(s);
			if (grammem==null){
				throw new IllegalStateException(s);
			}
			mmm.add(grammem);
		}
		return mmm;
	}
}
