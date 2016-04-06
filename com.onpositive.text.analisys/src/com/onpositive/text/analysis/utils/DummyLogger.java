package com.onpositive.text.analysis.utils;

import com.onpositive.text.analysis.IToken;

public class DummyLogger implements ILogger{

	@Override
	public ILogger clean() { return this; }

	@Override
	public ILogger writeTokens(IToken... tokens)  { return this; }

	@Override
	public ILogger writeTokens(Iterable<IToken> tokens)  { return this; }

	@Override
	public ILogger writeToken(IToken token)  { return this; }

	@Override
	public ILogger writeString(String str)  { return this; }
	
	@Override
	public ILogger writelnString(String str)  { return this; }

	@Override
	public ILogger newLine(int i)  { return this; }

	@Override
	public ILogger newLine()  { return this; }

	@Override
	public ILogger writelnTokens(Iterable<IToken> tokens) {
		return this;
	}

	@Override
	public ILogger writelnTokens(IToken... tokens) {
		return this;
	}

	@Override
	public ILogger writelnToken(IToken token) {
		return this;
	}

}
