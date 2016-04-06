package com.onpositive.semantic.wordnet;

import java.io.Serializable;


public abstract class AbstractRelation<T extends RelationTarget> implements Serializable{

	protected transient AbstractWordNet owner;
	public AbstractWordNet getOwner() {
		return owner;
	}
	public final int conceptId;
	public final int relation;
	

	public void setOwner(AbstractWordNet w) {
		this.owner = w;
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public AbstractRelation(AbstractWordNet owner, T conceptId, int relation) {
		super();
		this.conceptId = conceptId.id();
		this.relation = relation;
		this.owner = owner;
	}
	
	public AbstractRelation(AbstractWordNet owner2, int conceptId, int relationId) {
		this.owner = owner2;
		this.conceptId = conceptId;
		this.relation = relationId;
	}
	
	public String getDescription(){
		return "";
	}
	
	public abstract T getWord();
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + relation;
		result = prime * result + conceptId;
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
		AbstractRelation<?> other = (AbstractRelation<?>) obj;
		if (relation != other.relation)
			return false;
		if (conceptId != other.conceptId)
			return false;
		return true;
	}
}