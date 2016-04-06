package com.onpositive.text.analysis.lexic.scalar;

import com.onpositive.text.analysis.IToken;
import com.onpositive.text.analysis.conditions.DetectTokenBinaryCondition;
import com.onpositive.text.analysis.conditions.DetectTokenUnaryCondition;
import com.onpositive.text.analysis.rules.ConditionRule;
import com.onpositive.text.analysis.rules.RuleAlternative;
import com.onpositive.text.analysis.rules.RuleCallback;
import com.onpositive.text.analysis.rules.RuleExecutionEngine;
import com.onpositive.text.analysis.rules.TokenRule;

public class ScalarProcessingRules {
	
	public ScalarProcessingRules(ScalarProcessingData processingData) {
		this.processingData = processingData;
	}
	
	public RuleExecutionEngine getEngine(){
		if(this.patternRuleEngine==null){
			patternRuleEngine = new RuleExecutionEngine( new TokenRule[]{
				 new RuleAlternative( new TokenRule[]{ twoDigitRule, digitRule}), nonBreakingSpaceRule, vulgarFractionRule, symbolRule
			});
		}
		return patternRuleEngine;
	}

	private ScalarProcessingData processingData;
	
	private RuleExecutionEngine patternRuleEngine;
	
	private ConditionRule twoDigitRule = new ConditionRule(
			new DetectTokenBinaryCondition(new int[]{IToken.TOKEN_TYPE_DIGIT,IToken.TOKEN_TYPE_DIGIT}),			
			new RuleCallback<IToken>() {
				@Override
				public void execute(IToken token) {
					DelimeterPattern pattern = processingData.getPattern();
					if(token.hasSpaceAfter()){						
						if(" ".equals(pattern.valueDelimeter)){
							processingData.appendToken(token,true);
						}
						else if(" ".equals(pattern.decimalDelimeter)){
							processingData.appendToken(token,false);
						}
						else{
							processingData.setInvalidPattern(true);
						}
					}
					else{
						processingData.appendToken(token,false);
					}
				}
			});
	
	private ConditionRule digitRule = new ConditionRule(
			new DetectTokenUnaryCondition(IToken.TOKEN_TYPE_DIGIT),			
			new RuleCallback<IToken>() {
				@Override
				public void execute(IToken token) {
					processingData.appendToken(token,false);
				}
			});
	
	private ConditionRule nonBreakingSpaceRule = new ConditionRule(
			new DetectTokenUnaryCondition(IToken.TOKEN_TYPE_NON_BREAKING_SPACE),			
			new RuleCallback<IToken>() {
				@Override
				public void execute(IToken token) {
					DelimeterPattern pattern = processingData.getPattern();
					if(" ".equals(pattern.valueDelimeter)){
						processingData.appendToken(null,true);
					}
					else if(" ".equals(pattern.decimalDelimeter)){

					}
					else{
						processingData.setInvalidPattern(true);
					}
				}
			});
	
	private ConditionRule vulgarFractionRule = new ConditionRule(
			new DetectTokenUnaryCondition(IToken.TOKEN_TYPE_VULGAR_FRACTION),			
			new RuleCallback<IToken>() {
				@Override
				public void execute(IToken token) {
					processingData.appendToken(token,true);
				}
			});
	
	private ConditionRule symbolRule = new ConditionRule(
			new DetectTokenUnaryCondition(IToken.TOKEN_TYPE_SYMBOL),			
			new RuleCallback<IToken>() {
				@Override
				public void execute(IToken token) {
					DelimeterPattern pattern = processingData.getPattern();
					String val = token.getStringValue();
					if(val.equals(pattern.valueDelimeter)){
						processingData.appendToken(null,true);
					}
					else if(val.equals(pattern.decimalDelimeter)){

					}
					else if( val.equals(pattern.fractureDelimeter)){
						processingData.setFraction(true);
					}
					else{
						processingData.setInvalidPattern(true);
					}
				}
			});

}
