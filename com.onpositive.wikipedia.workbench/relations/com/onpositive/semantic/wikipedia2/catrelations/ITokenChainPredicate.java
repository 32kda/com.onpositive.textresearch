package com.onpositive.semantic.wikipedia2.catrelations;

import java.util.List;

import com.onpositive.text.analisys.tests.euristics.SimplifiedToken;

@FunctionalInterface
public interface ITokenChainPredicate {
	
	public boolean matches(List<SimplifiedToken> tokens);

}
