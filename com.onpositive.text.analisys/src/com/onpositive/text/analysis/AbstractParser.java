package com.onpositive.text.analysis;

import java.util.List;

import com.onpositive.text.analysis.utils.DummyLogger;
import com.onpositive.text.analysis.utils.ILogger;

public abstract class AbstractParser implements IParser {

	protected String text;
	protected boolean hasTriggered = false;
	protected List<IToken> baseTokens;
	protected ILogger logger = new DummyLogger();
	protected ILogger errorLogger = new DummyLogger();

	public AbstractParser() {
		super();
	}

	public List<IToken> getBaseTokens() {
		return baseTokens;
	}

	public void setBaseTokens(List<IToken> baseTokens) {
		this.baseTokens = baseTokens;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public boolean hasTriggered() {
		return hasTriggered;
	}

	public void resetTrigger() {
		this.hasTriggered = false;
	}

	protected void setTriggered(boolean value) {
		this.hasTriggered |= value;
	}

	@Override
	public void setLogger(ILogger logger) {
		this.logger = logger;
	}

	@Override
	public void setErrorLogger(ILogger logger) {
		this.errorLogger = logger;
	}

}