package com.onpositive.text.analysis.filtering;

import java.util.List;

import com.onpositive.text.analysis.IToken;

public interface IPossibleChainsFilter {

	List<List<IToken>> getFilteredOut(int tokenIndex, List<List<IToken>> original);
	
}
