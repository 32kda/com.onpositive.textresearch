package com.onpositive.text.analysis.syntax;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.onpositive.semantic.wordnet.Grammem;
import com.onpositive.semantic.wordnet.Grammem.Case;
import com.onpositive.semantic.wordnet.Grammem.Gender;
import com.onpositive.semantic.wordnet.Grammem.PartOfSpeech;
import com.onpositive.semantic.wordnet.Grammem.SingularPlural;
import com.onpositive.text.analysis.AbstractToken;
import com.onpositive.text.analysis.IToken;
import com.onpositive.text.analysis.lexic.WordFormToken;

public class SyntaxToken extends AbstractToken{
	
	protected static final List<GrammemSet> uniformGrammems = new ArrayList<GrammemSet>(
			Arrays.asList( new GrammemSet(Arrays.asList(
					Case.NOMN, Case.GENT, Case.DATV, Case.ACCS, Case.ABLT, Case.LOCT,
					SingularPlural.SINGULAR, SingularPlural.PLURAL, Gender.UNKNOWN))));

	public SyntaxToken(
			int tokenType,
			SyntaxToken mainGroup,
			Collection<GrammemSet> grammemSets,
			int startPosition,
			int endPosition) {
		
		super(tokenType, startPosition, endPosition);
		this.mainGroup = mainGroup;
		if(grammemSets!=null){
			this.grammemSets = new ArrayList<SyntaxToken.GrammemSet>(grammemSets);
		}
	}
	
	public SyntaxToken(
			int tokenType,
			SyntaxToken mainGroup,
			Collection<GrammemSet> grammemSets,
			int startPosition,
			int endPosition,
			boolean isDoubtful) {
		
		super(tokenType, startPosition, endPosition, isDoubtful);
		this.mainGroup = mainGroup;
		if(grammemSets!=null){
			this.grammemSets = new ArrayList<SyntaxToken.GrammemSet>(grammemSets);
		}
	}

	protected SyntaxToken mainGroup;
	
	protected List<GrammemSet> grammemSets;
	
	public List<GrammemSet> getGrammemSets(){
		if(grammemSets!=null){
			return grammemSets;
		}
		else if(mainGroup==null){
			return null;
		}
		else if(this != mainGroup){
			return mainGroup.getGrammemSets();
		}
		return null;
	}
	
	public PartOfSpeech getPartOfSpeech() {
		List<GrammemSet> sets = getGrammemSets();		
		if(sets==null){
			return null;
		}
		
		Optional<Grammem> part = sets.stream().flatMap(set -> set.grammems().stream()).filter(grammem -> (grammem instanceof PartOfSpeech)).findFirst();
		PartOfSpeech result = (PartOfSpeech) part.orElse(null);
		return result;
	}

	public boolean hasGrammem(Grammem gr){
		
		List<GrammemSet> sets = getGrammemSets();		
		if(sets==null){
			return false;
		}
		
		for(GrammemSet gs : sets){
			if(gs.hasGrammem(gr)){
				return true;
			}
		}
		return false;
	}
	
	
//	public Set<Grammem> getAllGrammems(){
//		
//		if(grammemSets==null){
//			initGrammemSets();
//		}
//		
//		Set<Grammem> grammems = new HashSet<Grammem>();
//		
//		WordFormToken mainWord = getMainWord();
//		MeaningElement meaningElement = mainWord.getMeaningElement();
//		Set<Grammem> meGr = meaningElement.getGrammems();
//		if(meGr!=null){
//			grammems.addAll(meGr);
//		}
//		List<GrammarRelation> grammarRelations = mainWord.getGrammarRelations();
//		if(grammarRelations!=null){
//			for(GrammarRelation rel : grammarRelations){
//				HashSet<Grammem> grammems2 = rel.getGrammems();				
//				if(grammems2!=null){
//					grammems.addAll(grammems2);
//				}
//			}
//		}
//		return grammems;
//	}
	
	
	public boolean hasAnyGrammem(Collection<? extends Grammem> col){
		
		List<GrammemSet> sets = getGrammemSets();		
		if(sets==null){
			return false;
		}
		
		for(GrammemSet gs : sets){
			if(gs.hasAnyGrammem(col)){
				return true;
			}
		}
		return false;
	}
	
	
	public boolean hasAllGrammems(Collection<? extends Grammem> col)
	{
		List<GrammemSet> sets = getGrammemSets();		
		if(sets==null){
			return false;
		}
		
		for(GrammemSet gs : sets){
			if(gs.hasAllGrammems(col)){
				return true;
			}
		}
		return false;
	}
	
	public String getStableStringValue(){
		return getStringValue();
	}
	@Override
	public String getStringValue() {
		
		StringBuilder bld = new StringBuilder();
		List<IToken> children = getChildren();
		
		if (children == null) return id() + "SyntaxToken";
		
		for(IToken t : children){
			bld.append(t.getStringValue()).append(" ");
		}
		if (children.size() == 1) 
			return id() + "@" + bld.toString();
		else return id() + "@[" + bld.toString() + "]";
	}

	@Override
	public String getShortStringValue() {
		
		StringBuilder bld = new StringBuilder();
		List<IToken> children = getChildren();
		for(IToken t : children){
			bld.append(t.getShortStringValue()).append(" ");
		}
		return bld.toString();
	}
	
	public WordFormToken getMainWord() {		
		SyntaxToken token = this;
		SyntaxToken mainGroup = this.getMainGroup();
		while(token!=mainGroup){
			if(mainGroup==null){
				return null;
			}
			token = mainGroup;
			mainGroup = token.getMainGroup();
		}
		if(!(token instanceof WordFormToken)){
			return null;
		}
		return (WordFormToken) token;
	}
	
	public SyntaxToken getMainGroup(){
		return mainGroup;
	}
	
	public boolean hasMainDescendant(int type){
		if(this.getType()==type){
			return true;
		}
		if(this.mainGroup == this){
			return false;
		}
		return this.mainGroup.hasMainDescendant(type);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((mainGroup == null||mainGroup==this) ? 0 : mainGroup.hashCode());
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
		SyntaxToken other = (SyntaxToken) obj;
		if (mainGroup == null) {
			if (other.mainGroup != null)
				return false;

		} else{
			if(other.mainGroup==null){
				return false;
			}
			if(mainGroup.getType() == IToken.TOKEN_TYPE_WORD_FORM){
				if(other.mainGroup.getType() != IToken.TOKEN_TYPE_WORD_FORM){
					return false;
				}
			}
			else if (!mainGroup.equals(other.mainGroup))		
				return false;
		}
		if(children==null){
			if(other.children!=null){
				return false;
			}
		}
		else{
			if(other.children==null){
				return false;
			}
			int size = this.children.size();
			if(size!=other.children.size()){
				return false;
			}
			for(int i = 0 ; i < size ; i++){
				if(!this.children.get(i).equals(other.children.get(i))){
					return false;
				}
			}
		}
		return true;
	}

	public String getBasicForm() {
		SyntaxToken mainWord = getMainWord();
		return mainWord==null?null:mainWord.getBasicForm();
	}


	public static class GrammemSet
	{
		public GrammemSet(Collection<? extends Grammem> grammems) {
			super();
			this.grammems = new ArrayList<Grammem>(grammems);
		}

		private ArrayList<Grammem> grammems;
		
		public List<Grammem> grammems(){
			return grammems;
		}
		
		public boolean hasGrammem(Grammem gr){
			return grammems.contains(gr);
		}
		
		public boolean hasAnyGrammem(Collection<? extends Grammem> col){
			for(Grammem gr : col){
				if(grammems.contains(gr)){
					return true;
				}
			}
			return false;
		}
		
		public boolean hasAllGrammems(Collection<? extends Grammem> col){
			return grammems.containsAll(col);
		}
		
		
		public <T extends Grammem> Set<T> extractGrammems(Class<T> clazz) {
			return extractGrammems(grammems, clazz);
		}
		
		
		@SuppressWarnings("unchecked")
		public static <T extends Grammem> Set<T> extractGrammems(Iterable<Grammem> col, Class<T> clazz) {

			HashSet<T> set = new HashSet<T>();
			for (Grammem gr : col) {
				if (clazz.isInstance(gr)) {
					set.add((T) gr);
				}
			}
			return set;
		}
		
		@Override
		public String toString() {
			return grammems.toString();
		}
	}
	
}
