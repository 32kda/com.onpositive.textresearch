package com.onpositive.text.morphology.pairs;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.onpositive.semantic.wordnet.AbstractWordNet;
import com.onpositive.semantic.wordnet.GrammarRelation;
import com.onpositive.semantic.wordnet.TextElement;
import com.onpositive.semantic.wordnet.WordNetProvider;
import com.onpositive.semantic.wordnet.Grammem.PartOfSpeech;
import com.onpositive.text.morphology.AnalisysResult;
import com.onpositive.text.morphology.dataset.prepare.DataSetPreparation;

public class PairsEstimator {

	private Map<Long, Short> stablePairs;

	@SuppressWarnings("unchecked")
	public PairsEstimator(){
		InputStream stream = PairsEstimator.class.getResourceAsStream("pairs.dat");
		if (stream != null) {
			try {
				ObjectInputStream inputStream = new ObjectInputStream(new BufferedInputStream(stream));
				stablePairs = (Map<Long, Short>) inputStream.readObject();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} finally {
				try { stream.close(); } catch (IOException e) {}
			}
		}
	}
	
	public List<AnalisysResult> analyze(String s){
		List<AnalisysResult> results = new ArrayList<AnalisysResult>();
		String[] sentenceParts = s.split("[\\.|,|:|\\?|\\!]");
		for (String sentence : sentenceParts) {
			results.addAll(analyze(new ArrayList<>(Arrays.asList(sentence.split(" ")))));
		}
		return results;
	}
	public ArrayList<AnalisysResult> analyze(ArrayList<String>text){
		ArrayList<AnalisysResult>rs=new ArrayList<>();
		String prev=null;
		String next=null;
		int i=0;
		for (String s:text){
			if (i<text.size()-1){
				next=text.get(i+1);
			}
			else{
				next=null;
			}
			rs.add(estimate(s, prev, next));
			prev=s;
			i++;
		}
		return rs;
	}
	
	
	
	public AnalisysResult estimate(String toEstimate, String prev, String next) {
		toEstimate = toEstimate.toLowerCase().trim();
		if (prev != null) {
			prev = prev.toLowerCase().trim();
		}
		if (next != null) {
			next = next.toLowerCase().trim();
		}
		Set<PartOfSpeech> possiblePartsOfSpeach = DataSetPreparation.possiblePartsOfSpeech(toEstimate);
		if (possiblePartsOfSpeach.size() == 1) {
			return AnalisysResult.createConstant(toEstimate, possiblePartsOfSpeach.iterator().next());
		}
		Set<TextElement> estimatedElements = getTextElements(toEstimate);
		Set<PartOfSpeech> resultSet = new HashSet<>();
		if(prev != null) {
			Set<TextElement> textElements = getTextElements(prev);
			for (TextElement prevTextElement : textElements) {
				for (TextElement textElement : estimatedElements) {
					int prevFormId = prevTextElement.id();
					int formId = textElement.id();
					Short pair = stablePairs.get((long)prevFormId << 32 | formId & 0xFFFFFFFFL);
					if (pair != null) {
						byte formIdx = pair.byteValue();
						resultSet.add((PartOfSpeech) PartOfSpeech.get(formIdx));
					}
				}
				
			}
		}
		
		if(next != null) {
			Set<TextElement> textElements = getTextElements(next);
			for (TextElement nextTextElement : textElements) {
				for (TextElement textElement : estimatedElements) {
					int nextFormId = nextTextElement.id();
					int formId = textElement.id();
					Short pair = stablePairs.get((long)formId << 32 | nextFormId & 0xFFFFFFFFL);
					if (pair != null) {
						byte formIdx = (byte) (pair.shortValue() >> 8);
						resultSet.add((PartOfSpeech) PartOfSpeech.get(formIdx));
					}
				}
				
			}
		}
		if (resultSet.size() == 1) {
			return AnalisysResult.createConstant(toEstimate, resultSet.iterator().next());
		}
		
		return AnalisysResult.createEmpty(toEstimate);
	}
	
	private Set<TextElement> getTextElements(String word) {
		Set<TextElement> result = new HashSet<>();
		AbstractWordNet wordNet = WordNetProvider.getInstance();
		GrammarRelation[] possibleGrammarForms = wordNet
				.getPossibleGrammarForms(word.toLowerCase());
		HashSet<TextElement>te=new HashSet<TextElement>();
		if (possibleGrammarForms!=null&&possibleGrammarForms.length > 0) {
			for (GrammarRelation rel : possibleGrammarForms) {
				te.add(rel.getWord());
			}
		}
		result.addAll(te);
		return result;
	}
		
}
