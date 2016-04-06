package com.onpositive.semantic.search.core;


import java.util.Arrays;
import java.util.List;

public class SearchMatch {
	protected IDocument document;
	protected SearchRequest request;

	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((document == null) ? 0 : document.hashCode());
		result = prime * result
				+ ((extraInfo == null) ? 0 : extraInfo.hashCode());
		result = prime * result + Arrays.hashCode(matches);
		result = prime * result + rank;
		result = prime * result + relevancy;
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SearchMatch other = (SearchMatch) obj;
		if (document == null) {
			if (other.document != null)
				return false;
		} else if (!document.equals(other.document))
			return false;
		if (extraInfo == null) {
			if (other.extraInfo != null)
				return false;
		} else if (!extraInfo.equals(other.extraInfo))
			return false;
		if (!Arrays.equals(matches, other.matches))
			return false;
		if (rank != other.rank)
			return false;
		if (relevancy != other.relevancy)
			return false;
		return true;
	}
	public SearchMatch(IDocument iDocument, int i, Object object,SearchRequest request) {
		this.document=iDocument;
		this.relevancy=i;
		this.extraInfo=object;
		this.request=request;
		this.rank=iDocument.getPopularity();
	}
	public IDocument getDocument() {
		return document;
	}
	public void setDocument(IDocument document) {
		this.document = document;
	}
	public int getRelevancy() {
		return relevancy;
	}
	public void setRelevancy(int relevancy) {
		this.relevancy = relevancy;
	}
	public Object getExtraInfo() {
		return extraInfo;
	}
	public void setExtraInfo(Object extraInfo) {
		this.extraInfo = extraInfo;
	}
	protected int relevancy;
	protected int rank=1000;
	public int getRank() {
		return rank;
	}
	public void setRank(int rank) {
		this.rank = rank;
	}
	protected Object extraInfo;
	private MatchDescription[] matches;
	
	@Override
	public String toString() {
		return document.toString()+"("+relevancy+")"+(extraInfo==null?"":extraInfo)+"(p:"+getDocument().getPopularity()+")";		
	}
	
	public String getPlainTextAbstract(){
		return document.getPlainTextAbstract();
	}
	public String getPlainText(){
		return document.getOriginalMarkup();
	}
	
	public List<String>getImages(){
		return Arrays.asList(document.getImages());
	}
	
	public MatchDescription[] getMatchDescription(){
		if (matches==null){
			matches = document.getMatches(request);
		}
		return matches;		
	}
	
	public List<ICategory> getCategories(){
		return Arrays.asList(document.getCategories());
	}
}
