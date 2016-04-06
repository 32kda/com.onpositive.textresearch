package com.onpositive.text.analysis;

import java.util.ArrayList;
import java.util.List;

import com.onpositive.text.analysis.filtering.ITokenFilter;
import com.onpositive.text.analysis.syntax.SentenceToken;

public abstract class MorphologicParser extends AbstractParser {

	protected List<ITokenFilter> tokenFilters = new ArrayList<ITokenFilter>();
	private int filteredCount = 0;

	public MorphologicParser() {
		super();
	}

	public void addTokenFilter(ITokenFilter filter) {
		tokenFilters.add(filter);
	}

	protected void doFiltering(List<IToken> tokens) {
		for (IToken curToken : tokens) {
			if (!curToken.hasConflicts()) {
				continue;
			}
			for (ITokenFilter filter : tokenFilters) {
				if (filter.shouldFilterOut(curToken)) {
					curToken.setCorrelation(0.0, Double.POSITIVE_INFINITY);
					filteredCount++;
					break;
				}
			}
		}
	}

	protected List<List<IToken>> generateVariants(List<List<IToken>> prevResult, List<IToken> conflicts) {
			List<List<IToken>> result = new ArrayList<List<IToken>>();
			for (List<IToken> list : prevResult) {
				for (IToken curToken : conflicts) {
	//				if (curToken.getCorrelation() < E) {
	//					continue;
	//				}
					List<IToken> newList = new ArrayList<IToken>(list);
					newList.add(curToken);
					result.add(newList);
				}
			}
			return result;
		}

	protected void addItem(List<List<IToken>> result, IToken token) {
		for (List<IToken> list : result) {
			list.add(token);
		}
	}

	@Override
	public List<IToken> process(List<IToken> tokens) {
		if (tokens.isEmpty()) {
			return tokens;
		} else if (tokens.get(0) instanceof SentenceToken) {
			return processSentences(tokens);
		}
		return processPlain(tokens);
	}

	protected List<IToken> processSentences(List<IToken> tokens) {
		for (IToken sentence : tokens) {
			SentenceToken sentenceToken = (SentenceToken) sentence;
			processPlain(sentenceToken.getChildren());
		}
		return tokens;
	}

	protected abstract List<IToken> processPlain(List<IToken> tokens);

	public int getFilteredCount() {
		return filteredCount;
	}
	
}