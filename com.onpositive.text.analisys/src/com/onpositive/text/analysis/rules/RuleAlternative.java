package com.onpositive.text.analysis.rules;

import java.util.ArrayList;
import java.util.List;

import com.onpositive.text.analysis.IToken;

public class RuleAlternative extends TokenRule {
	
	public RuleAlternative(TokenRule[] ruleCollection) {
		for(TokenRule rule : ruleCollection){
			rules.add(rule);
		}
	}

	public RuleAlternative(Iterable<TokenRule> ruleCollection) {
		for(TokenRule rule : ruleCollection){
			rules.add(rule);
		}
	}

	ArrayList<TokenRule> rules = new ArrayList<TokenRule>();

	@Override
	public boolean execute(int pos) {
		for(TokenRule rule : rules){
			if(rule.execute(pos)){
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void setTokens(List<IToken> tokens) {		
		super.setTokens(tokens);
		for(TokenRule tr : rules){
			tr.setTokens(tokens);
		}
	}
}
