package com.onpositive.text.analisys.tests.model;

import java.util.List;

import com.onpositive.text.analysis.Euristic;
import com.onpositive.text.analysis.IToken;

public class EuristicMatch {

	public final Euristic euristic;
	
	public final List<IToken> sequence;

	public EuristicMatch(Euristic euristic, List<IToken> sequence) {
		super();
		this.euristic = euristic;
		this.sequence = sequence;
	}
	
	@Override
	public String toString() {
		return "{ Эвристика " + euristic.toString() + " Цепочка: " + sequence.toString() + "}";
	}
	
}
