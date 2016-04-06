package com.onpositive.text.analysis.projection;

import java.util.List;

import com.onpositive.text.analysis.IToken;

public interface IProjectionCreator {
	
	boolean isApplicable(List<IToken> chain);
	
	void applyTo(List<IToken> chain);

}
