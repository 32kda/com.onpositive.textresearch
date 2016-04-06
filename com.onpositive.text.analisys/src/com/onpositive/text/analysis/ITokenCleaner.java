package com.onpositive.text.analysis;

import java.util.List;

public interface ITokenCleaner {

	public abstract List<IToken> clean(List<IToken> tokens);

}