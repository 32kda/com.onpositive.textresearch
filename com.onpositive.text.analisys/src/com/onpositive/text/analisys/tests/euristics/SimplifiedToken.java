package com.onpositive.text.analisys.tests.euristics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

import com.onpositive.semantic.wordnet.Grammem;
import com.onpositive.semantic.wordnet.Grammem.PartOfSpeech;
import com.onpositive.text.analysis.syntax.SyntaxToken;
import com.onpositive.text.analysis.syntax.SyntaxToken.GrammemSet;

public class SimplifiedToken {
	
	private String word;
	
	private Collection<Grammem> grammems;
	
	private boolean isSeparator;

	public boolean isSeparator() {
		return isSeparator;
	}

	public void setSeparator(boolean isSeparator) {
		this.isSeparator = isSeparator;
	}

	public SimplifiedToken(String word, Collection<Grammem> grammems) {
		super();
		this.word = word;
		this.grammems = new LinkedHashSet<Grammem>(grammems);
	}

	public String getWord() {
		return word;
	}

	public Collection<Grammem> getGrammems() {
		return grammems;
	}
	
	public List<Grammem> getMissedGrammems(SyntaxToken wordFormToken) {
		List<GrammemSet> grammemSets = wordFormToken.getGrammemSets();
		List<Grammem> missedGrammems = grammems.stream().filter(grammem -> {for (GrammemSet grammemSet : grammemSets) {
				if (grammemSet.hasGrammem(grammem)) {
					return true;
				}
			} 
			return false;
		}).collect(Collectors.toList());
		return missedGrammems;		
	}
	
	public boolean wordEquals(SyntaxToken comparedToken) {
		String stringValue = comparedToken.getShortStringValue().trim();
		if (stringValue.length() > 0) {
			int end = stringValue.length() - 1;
			while (end >= 0 && !Character.isLetter(stringValue.charAt(end)) && !Character.isDigit(stringValue.charAt(end)) && stringValue.charAt(end) != '-') {
				end--;
			}
			if (end >= 0 && end < stringValue.length() - 1) {
				stringValue = stringValue.substring(0, end + 1);
			}
		}
		stringValue = stringValue.replaceAll(" ","");
		return word.trim().equalsIgnoreCase(stringValue.trim());
	}
	
	public boolean hasValidGrammemSet() {
		for (Grammem grammem : grammems) {
			if (grammem instanceof PartOfSpeech) {
				return true;
			}
		}
		return false;
	}
	
	public PartOfSpeech getPartOfSpeech() {
		PartOfSpeech sp=null;
		for (Grammem grammem : grammems) {
			if (grammem instanceof PartOfSpeech) {
				if (sp==null){
					sp=(PartOfSpeech) grammem;
				}
				else{
					sp=null;
					return null;
				}
			}
		}
		return sp;
	}
	
	public List<PartOfSpeech> getPartsOfSpeech() {
		List<PartOfSpeech> result = new ArrayList<PartOfSpeech>();
		for (Grammem grammem : grammems) {
			if (grammem instanceof PartOfSpeech) {
				result.add((PartOfSpeech) grammem);
			}
		}
		return result;
	}
		
	@Override
	public String toString() {
		return word + ", Grammmems: " + grammems.toString();
	}

	public boolean hasGrammem(Grammem grammem) {
		return grammems.contains(grammem);
	}
	
}
