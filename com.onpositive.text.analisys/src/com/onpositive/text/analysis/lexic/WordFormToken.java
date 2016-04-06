package com.onpositive.text.analysis.lexic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.onpositive.semantic.wordnet.GrammarRelation;
import com.onpositive.semantic.wordnet.Grammem;
import com.onpositive.semantic.wordnet.Grammem.PartOfSpeech;
import com.onpositive.semantic.wordnet.MeaningElement;
import com.onpositive.semantic.wordnet.TextElement;
import com.onpositive.text.analysis.syntax.SyntaxToken;

public class WordFormToken extends SyntaxToken {

	public WordFormToken(TextElement el, MeaningElement[] meaningElement,
			int startPosition, int endPosition) {
		super(TOKEN_TYPE_WORD_FORM, null, null, startPosition, endPosition);
		this.meaningElements = meaningElement;
		this.mainGroup = this;
		this.element = el;
	}

	public WordFormToken(MeaningElement me, int startPosition, int endPosition1) {
		super(TOKEN_TYPE_WORD_FORM, null, null, startPosition, endPosition1);
		this.meaningElements = new MeaningElement[] { me };
		this.mainGroup = this;
		this.element = me.getParentTextElement();
	}
	
	public WordFormToken(List<GrammemSet> grammemSets, int startPosition, int endPosition1) {
		super(TOKEN_TYPE_WORD_FORM, null, grammemSets, startPosition, endPosition1);
		this.meaningElements = new MeaningElement[]{};
		this.mainGroup = this;
		this.element = null;
	}

	protected TextElement element;
	private MeaningElement[] meaningElements;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((element == null) ? 0 : element.hashCode());
		result = prime
				* result
				+ ((grammarRelations == null) ? 0 : grammarRelations.hashCode());
		result = prime * result + Arrays.hashCode(meaningElements);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		WordFormToken other = (WordFormToken) obj;
		if (element == null) {
			if (other.element != null)
				return false;
		} else if (!element.equals(other.element))
			return false;
		if (grammarRelations == null) {
			if (other.grammarRelations != null)
				return false;
		} else if (!grammarRelations.equals(other.grammarRelations))
			return false;
		if (!Arrays.equals(meaningElements, other.meaningElements))
			return false;
		return true;
	}

	private ArrayList<GrammarRelation> grammarRelations = new ArrayList<GrammarRelation>();

	public MeaningElement[] getMeaningElements() {
		return meaningElements;
	}

	public short getGrammemCode() {
		return meaningElements != null && meaningElements.length > 0 ? meaningElements[0].getGrammemCode() : -1;
	}
	
	public void addGrammarRelation(GrammarRelation gr) {
		grammarRelations.add(gr);
	}

	public List<GrammarRelation> getGrammarRelations() {
		return grammarRelations;
	}

	@Override
	public List<GrammemSet> getGrammemSets() {
		if (this.grammemSets == null) {
			initGrammemSets();
		}
		return grammemSets;
	}

	protected void initGrammemSets() {
		this.grammemSets = new ArrayList<SyntaxToken.GrammemSet>();
		Set<Grammem> grammems = calcGram(meaningElements);
		List<GrammarRelation> grammarRelations = getGrammarRelations();
		if (grammarRelations != null) {
			for (GrammarRelation rel : grammarRelations) {
				HashSet<Grammem> relGrammems = new HashSet<Grammem>(
						rel.getGrammems());
				if (grammems != null) {
					relGrammems.addAll(grammems);
				}
				grammemSets.add(new GrammemSet(relGrammems));
			}
		} else if (grammems != null) {
			grammemSets.add(new GrammemSet(grammems));
		}
	}

	protected Set<Grammem> calcGram(MeaningElement[] meaningElement) {
		HashSet<Grammem> gr = new HashSet<Grammem>();
		for (MeaningElement q : meaningElement) {
			gr.addAll(q.getGrammems());
		}
		return gr;
	}

	public String getBasicForm() {
		if(element != null){
			return element.getBasicForm();
		}
		else{
			return children.get(0).getStringValue();
		}
	}

	@Override
	public String getStableStringValue() {
		if(meaningElements.length==0){
			return "Unknown(" + children.get(0).getStringValue() + ")";
		}
		else if (meaningElements.length == 1) {
			return meaningElements[0].toString();
		}
		Arrays.sort(meaningElements, new Comparator<MeaningElement>() {

			@Override
			public int compare(MeaningElement o1, MeaningElement o2) {
				return o1.getGrammems().toString()
						.compareTo(o2.getGrammems().toString());
			}
		});
		return "*(" + Arrays.toString(meaningElements) + ")";
	}

	@Override
	public String getStringValue() {
		if(meaningElements.length==0){
			return id() + "@Unknown(" + children.get(0).getStringValue() + ")";
		}
		else if (meaningElements.length == 1) {
			return id() + "@" + meaningElements[0].toString();
		}
		return id() + "@*(" + Arrays.toString(meaningElements) + ")";
	}

	public TextElement getParentTextElement() {
		return element;
	}
	
	public PartOfSpeech getPartOfSpeech() {
		if (meaningElements.length == 0) return null;
		else return meaningElements[0].getPartOfSpeech();
	}
	
	void merge(WordFormToken[] a) {
		LinkedHashSet<MeaningElement> z = new LinkedHashSet<MeaningElement>();
		for (WordFormToken t : a) {
			z.addAll(Arrays.asList(t.getMeaningElements()));
		}
		this.meaningElements = z.toArray(new MeaningElement[z.size()]);

	}

}
