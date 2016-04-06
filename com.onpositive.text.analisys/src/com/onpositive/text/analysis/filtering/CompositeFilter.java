package com.onpositive.text.analysis.filtering;

import java.util.ArrayList;
import java.util.List;

import com.onpositive.text.analysis.IToken;

public class CompositeFilter implements ITokenFilter {
	
	protected List<ITokenFilter> filters = new ArrayList<ITokenFilter>();
	
	public CompositeFilter() {
	}
	
	public CompositeFilter(ITokenFilter... filters) {
		for (ITokenFilter filter : filters) {
			this.filters.add(filter);
		}
	}
	
	public void addFilter(ITokenFilter filter) {
		filters.add(filter);
	}


	@Override
	public boolean shouldFilterOut(IToken token) {
		for (ITokenFilter curFilter : filters) {
			if (curFilter.shouldFilterOut(token)) {
				return true;
			}
		}
		return false;
	}

}
