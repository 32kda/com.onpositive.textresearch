package com.onpositive.text.analysis;

import java.util.List;

import com.onpositive.text.analysis.lexic.PrimitiveTokenizer;
import com.onpositive.text.analysis.utils.DummyLogger;
import com.onpositive.text.analysis.utils.ILogger;

/**
 * Represents a composition of IParsers.
 * This parser processes the list of Tokens with each IParser specified in constructor. 
 */
public class ParserComposition implements IParser {

	public ParserComposition(IParser... parsers) {
		super();
		this.parsers = parsers;
	}
	
	public ParserComposition(boolean isGloballyRecursive, IParser... parsers) {
		super();
		this.parsers = parsers;
		this.isGloballyRecursive = isGloballyRecursive;
	}

	protected IParser[]parsers;
	
	private PrimitiveTokenizer tokenizer=new PrimitiveTokenizer();
	
	private boolean isGloballyRecursive = false;
	
	private String text;
	
	protected ILogger logger = new DummyLogger();
	
	protected ILogger errorLogger = new DummyLogger();
	
	public List<IToken> getBaseTokens() {
		if(parsers==null||parsers.length==0){
			return null;
		}
		return parsers[0].getBaseTokens();		
	}

	public void setBaseTokens(List<IToken> baseTokens) {
		for(IParser p : parsers){
			p.setBaseTokens(baseTokens);
		}
	}
	
	public PrimitiveTokenizer getTokenizer() {
		return tokenizer;
	}

	public void setTokenizer(PrimitiveTokenizer tokenizer) {
		this.tokenizer = tokenizer;
	}

	public List<IToken>parse(String str){
		setText(str);
		List<IToken> tokenize = tokenizer.tokenize(str);		
		tokenize = process(tokenize);
		return tokenize;
	}

	public void setText(String str) {
		this.text = str;
		for (IParser p:parsers){
			p.setText(str);			
		}
	}
	public IParser[] getParsers(){
		return this.parsers;
	}

	@Override
	public String getText() {
		return text;
	}

	public List<IToken> process(List<IToken> tokens) {
		
		boolean globalTriggered = true;
		boolean localTriggered;
		
		int count0 = 0;
		while(globalTriggered){
			if(count0>15){
				throw new RuntimeException("Infinite external cycle in Composite Parser.");
			}
			globalTriggered = false;
			for (IParser parser : parsers){
				logger.writelnString("Launching " + parser.getClass().getSimpleName());
				int count1 = 0;
				do{
					if(count1>100){
						throw new RuntimeException("Infinite external cycle in Composite Parser.");
					}
					tokens = applyParser(tokens, parser);
					localTriggered = parser.hasTriggered();
					globalTriggered |= localTriggered;
					count1++;
				}
				while(localTriggered&&parser.isRecursive());
			}
			if(!isGloballyRecursive){
				break;
			}
			count0++;
		}
		return tokens;
	}
	
	private final List<IToken> applyParser(List<IToken> tokens,	IParser parser) {
		parser.resetTrigger();
		List<IToken> result = parser.process(tokens);
		return result;
	}

	@Override
	public void resetTrigger() {
		for(IParser p : parsers){
			p.resetTrigger();
		}		
	}

	@Override
	public boolean hasTriggered() {
		for(IParser p : parsers){
			if(p.hasTriggered()){
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isRecursive() {
		return isGloballyRecursive;
	}

	@Override
	public void setHandleBounds(boolean handleBounds) {}

	@Override
	public List<IToken> getNewTokens() {
		return null;
	}

	public void setTokenIdProvider(TokenIdProvider tokenIdProvider) {
		for(IParser p : this.parsers){
			p.setTokenIdProvider(tokenIdProvider);
		}
	}

	public TokenIdProvider getTokenIdProvider() {
		return null;
	}

	public void clean() {
		for(IParser p : this.parsers){
			p.clean();
		}
	}
	
	@Override
	public void setLogger(ILogger logger) {
		this.logger = logger;
		for(IParser p : parsers){
			p.setLogger(logger);
		}
	}
	
	@Override
	public void setErrorLogger(ILogger logger) {
		this.errorLogger = logger;
		for(IParser p : parsers){
			p.setErrorLogger(logger);
		}
	}


}
