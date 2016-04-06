package com.onpositive.text.analysis.utils;

import com.onpositive.text.analysis.IToken;

public interface ILogger {

	ILogger clean();

	ILogger writeTokens(IToken... tokens);

	ILogger writeTokens(Iterable<IToken> tokens);

	ILogger writeToken(IToken token);

	ILogger writeString(String str);
	
	ILogger writelnString(String str);

	ILogger newLine(int count);
	
	ILogger newLine();

	ILogger writelnTokens(Iterable<IToken> tokens);
	
	ILogger writelnTokens(IToken... tokens);
	
	ILogger writelnToken(IToken token);
}