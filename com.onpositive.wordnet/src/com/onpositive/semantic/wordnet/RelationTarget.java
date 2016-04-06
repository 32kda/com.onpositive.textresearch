package com.onpositive.semantic.wordnet;


/**
 * Super class of all  elements which may be in semantic relations with other elements
 * @author kor
 *
 */
public abstract class RelationTarget {

	public abstract int id() ;
	
	protected final AbstractWordNet owner;
	
	public RelationTarget(AbstractWordNet owner) {
		super();
		this.owner = owner;
	}

	public AbstractWordNet getOwner(){
		return owner;
	}
}
