package com.onpositive.text.analysis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.onpositive.text.analysis.IToken.Direction;
import com.onpositive.text.analysis.SentenceTreeBuilder.DecisionRule;
import com.onpositive.text.analysis.basic.matchers.BasicRule;
import com.onpositive.text.analysis.basic.matchers.BasicRule.BoundData;
import com.onpositive.text.analysis.basic.matchers.ChainMatcher;
import com.onpositive.text.analysis.basic.matchers.ITokenMatcher;
import com.onpositive.text.analysis.basic.matchers.NotTokenArrayMatcher;
import com.onpositive.text.analysis.basic.matchers.NotTokenMatcher;
import com.onpositive.text.analysis.basic.matchers.OrTokenMatcher;
import com.onpositive.text.analysis.basic.matchers.TokenArrayMatcher;
import com.onpositive.text.analysis.basic.matchers.TokenMatcher;

public class SentenceTreeRuleFactory {
	
	
	public SentenceTreeRuleFactory() {
		super();
		initRules();
	}
	
	private DecisionRule bracketsRule= new DecisionRule("(", ")");
	
	private BasicRule quotesRule0 = new BasicRule("\"", "\"");
	
	private BasicRule quotesRule1 = new BasicRule("«", "»");
	
	private BasicRule quotesRule2 = new BasicRule("„", "“");
	
	private DecisionRule directSpeachRule0;
	
	private DecisionRule directSpeachRule1;
	
	private DecisionRule directSpeachRule2;
	
	private DecisionRule directSpeachRule3;
	
	private DecisionRule directSpeachRule4;
	
	private DecisionRule titleRule0;
	
	private DecisionRule titleRule1;
	
	private DecisionRule titleRule2;
	
	private DecisionRule enumerationRule0;
	
	private DecisionRule enumerationRule1;
	
	
	public List<BasicRule> getDetectionRules(){
		ArrayList<BasicRule> result =
			new ArrayList<BasicRule>(Arrays.asList(
				bracketsRule,
				quotesRule0,				
				quotesRule1,
				quotesRule2,
				enumerationRule0,
				enumerationRule1
			));
		
		return result;
	}
	
	
	public List<DecisionRule> getRules(){
		ArrayList<DecisionRule> result =
			new ArrayList<DecisionRule>(Arrays.asList(
				bracketsRule,
				directSpeachRule0,				
				directSpeachRule1,
				directSpeachRule2,
				directSpeachRule3,
				directSpeachRule4,
				titleRule0,
				titleRule1,
				titleRule2,
				enumerationRule0,
				enumerationRule1
			));
		
		return result;
	}
	
	private void initRules() {
		
		ITokenMatcher letterMatcher = TokenMatcher.forString("q").get(0);
		ITokenMatcher colonMatcher = TokenMatcher.forString(":").get(0);
		NotTokenMatcher notColonMatcher = new NotTokenMatcher(":");
		ITokenMatcher commaMatcher = TokenMatcher.forString(",").get(0);
		ITokenMatcher pointMatcher = TokenMatcher.forString(".").get(0);
		ITokenMatcher leftQuotesMatcher = new OrTokenMatcher("\"«„");
		ITokenMatcher rightQuotesMatcher = new OrTokenMatcher("\"»“");
		ITokenMatcher exclamationQuestionMatcher = new OrTokenMatcher("!?");
		ITokenMatcher sentenceEndMatcher = new OrTokenMatcher(".!?");
		ITokenMatcher dashMatcher = new OrTokenMatcher("—-");
		
		//START " blah !? " -
		directSpeachRule0 = new DecisionRule(leftQuotesMatcher,
				new TokenArrayMatcher(Arrays.asList(
						exclamationQuestionMatcher,
						rightQuotesMatcher,
						dashMatcher)));
		directSpeachRule0.setAcceptStart(true);
		directSpeachRule0.setAcceptEnd(true);
		directSpeachRule0.setParentDirection(Direction.END);
		directSpeachRule0.setStartBound(new BoundData(0,-1,1));
		directSpeachRule0.setEndBound(new BoundData(0,-1,2));		
		
		//START " blah " , -
		directSpeachRule1 = new DecisionRule(leftQuotesMatcher,
				new TokenArrayMatcher(Arrays.asList(
						letterMatcher,
						rightQuotesMatcher,
						commaMatcher,
						dashMatcher)));
		directSpeachRule1.setAcceptStart(true);
		directSpeachRule1.setParentDirection(Direction.END);
		directSpeachRule1.setStartBound(new BoundData(0,-1,1));
		directSpeachRule1.setEndBound(new BoundData(0,-1,3));
		
		// : " blah !? " -
		directSpeachRule2 = new DecisionRule(
				new TokenArrayMatcher(Arrays.asList(
						colonMatcher,
						leftQuotesMatcher)),
				new TokenArrayMatcher(Arrays.asList(
						exclamationQuestionMatcher,
						rightQuotesMatcher,
						dashMatcher)));
		directSpeachRule2.setAcceptEnd(true);
		directSpeachRule2.setStartBound(new BoundData(-1,-1,2));
		directSpeachRule2.setEndBound(new BoundData(0,-1,2));
		
		// : " blah ".
		directSpeachRule3 = new DecisionRule(
				new TokenArrayMatcher(Arrays.asList(
						colonMatcher,
						leftQuotesMatcher)),
				new TokenArrayMatcher(Arrays.asList(
						letterMatcher,
						rightQuotesMatcher,
						pointMatcher)));
		directSpeachRule3.setStartBound(new BoundData(-1,-1,2));
		directSpeachRule3.setEndBound(new BoundData(0,-1,1));
		
		// : " blah " , -
		directSpeachRule4 = new DecisionRule(
				new TokenArrayMatcher(Arrays.asList(
						colonMatcher,
						leftQuotesMatcher)),
				new TokenArrayMatcher(Arrays.asList(
						letterMatcher,
						rightQuotesMatcher,
						commaMatcher,
						dashMatcher)));
		directSpeachRule4.setStartBound(new BoundData(-1,-1,2));
		directSpeachRule4.setEndBound(new BoundData(0,-1,3));

		//START " blah " [!]( , - )		
		titleRule0 = new DecisionRule(leftQuotesMatcher,
				new ChainMatcher(
						new TokenArrayMatcher(rightQuotesMatcher),
						new NotTokenArrayMatcher(commaMatcher, dashMatcher)
					));
		titleRule0.setParentDirection(null);
		titleRule0.setAcceptStart(true);
		titleRule0.setStartBound(new BoundData(0,-1,1));
		
		
		//[!]: " blah " [!]( , - )
		titleRule1 = new DecisionRule(
				new TokenArrayMatcher(notColonMatcher, leftQuotesMatcher),
				new ChainMatcher(
						new TokenArrayMatcher(rightQuotesMatcher),
						new NotTokenArrayMatcher(commaMatcher, dashMatcher)
					));
		titleRule1.setParentDirection(null);
		titleRule1.setStartBound(new BoundData(0,-1,1));
		
		
		//[!]: " blah "
		titleRule2 = new DecisionRule(
				new TokenArrayMatcher(notColonMatcher, leftQuotesMatcher),
				new TokenArrayMatcher(rightQuotesMatcher));
		titleRule2.setParentDirection(null);
		titleRule2.setStartBound(new BoundData(0,-1,1));
		
		
		//: [!]" blah ,-
		enumerationRule0 = new DecisionRule(
				new ChainMatcher(new TokenArrayMatcher(":"), new NotTokenArrayMatcher(leftQuotesMatcher)),
				new TokenArrayMatcher(commaMatcher,dashMatcher));
		enumerationRule0.setEndBound(new BoundData(0,0,2));
		
		
		//: [!]" blah .!?
		enumerationRule1 = new DecisionRule(
				new ChainMatcher(new TokenArrayMatcher(":"), new NotTokenArrayMatcher(leftQuotesMatcher)),
				new TokenArrayMatcher(sentenceEndMatcher));
		enumerationRule1.setEndBound(new BoundData(0,0,0));
		
		bracketsRule.setResultTokenType(IToken.TOKEN_TYPE_BRACKETS);
		directSpeachRule0.setResultTokenType(IToken.TOKEN_TYPE_DIRECT_SPEACH);
		directSpeachRule1.setResultTokenType(IToken.TOKEN_TYPE_DIRECT_SPEACH);
		directSpeachRule2.setResultTokenType(IToken.TOKEN_TYPE_DIRECT_SPEACH);
		directSpeachRule3.setResultTokenType(IToken.TOKEN_TYPE_DIRECT_SPEACH);
		directSpeachRule4.setResultTokenType(IToken.TOKEN_TYPE_DIRECT_SPEACH);
		titleRule0.setResultTokenType(IToken.TOKEN_TYPE_TITLE);
		titleRule1.setResultTokenType(IToken.TOKEN_TYPE_TITLE);
		titleRule2.setResultTokenType(IToken.TOKEN_TYPE_TITLE);
		enumerationRule0.setResultTokenType(IToken.TOKEN_TYPE_ENUMERATION);
		enumerationRule1.setResultTokenType(IToken.TOKEN_TYPE_ENUMERATION);
		
	}

}
