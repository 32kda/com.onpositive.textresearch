package com.onpositive.semantic.wikipedia2.catrelations;

import com.onpositive.semantic.search.core.ICategorizable;
import com.onpositive.semantic.search.core.ICategory;

public class Connection {
	
	public enum Type {
		IS_A,
		NOT_IS_A
	}
	
	private ICategory category;
	private ICategorizable entity;
	private Type type;
	private double trust = 1.0;
	
	public Connection(ICategorizable entity, ICategory category) {
		this(entity, category, Type.IS_A);
	}
	
	public Connection(ICategorizable entity, ICategory category, Type type) {
		super();
		if (entity == null) {
			throw new IllegalArgumentException("Entity can't be null");
		}
		if (category == null) {
			throw new IllegalArgumentException("Category can't be null");
		}
		this.category = category;
		this.entity = entity;
		this.type = type != null ? type : Type.IS_A;
	}
	
	public Connection(ICategorizable entity, ICategory category, Type type, double trust) {
		this(entity, category, type);
		this.trust = trust;
	}

	public double getTrust() {
		return trust;
	}

	public void setTrust(double trust) {
		this.trust = trust;
	}

	public ICategory getCategory() {
		return category;
	}

	public ICategorizable getEntity() {
		return entity;
	}

	public Type getType() {
		return type;
	}

	public int getCategoryId() {
		return category.getIntId();
	}
	
	public int getEntityId() {
		return entity.getIntId();
	}
	
	@Override
	public String toString() {
		String relation = "->";
		if (type == Type.NOT_IS_A) {
			relation = "!>";
		}
		String text = entity.getTitle() + " " + relation + " " + category.getTitle();
		if (trust != 1.0) {
			text += " trust " + String.format("%1$,.2f", trust);
		}
		return text;
	}
}
