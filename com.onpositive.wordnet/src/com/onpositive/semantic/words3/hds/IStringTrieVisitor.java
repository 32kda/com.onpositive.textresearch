package com.onpositive.semantic.words3.hds;

public interface IStringTrieVisitor<T> {

	public void visit(String word, T data);
	
}
