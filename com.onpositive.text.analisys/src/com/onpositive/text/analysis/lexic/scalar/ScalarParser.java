package com.onpositive.text.analysis.lexic.scalar;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Stack;

import com.onpositive.text.analysis.BasicParser;
import com.onpositive.text.analysis.IToken;
import com.onpositive.text.analysis.conditions.BinaryCondition;
import com.onpositive.text.analysis.lexic.ScalarToken;
import com.onpositive.text.analysis.rules.RuleExecutionEngine;
import com.onpositive.text.analysis.utils.Utils;
import com.onpositive.text.analysis.utils.VulgarFraction;

public class ScalarParser extends BasicParser {
	
	public ScalarParser() {	}
	
	private static final String[] acceptedSymbols = new String[]{",".intern(),".".intern(),";".intern()};
	
	private PatternManager patternManager = new PatternManager();
	
	
	@Override
	protected void combineTokens(Stack<IToken> sample, ProcessingData processingData){
		
		
		boolean gotPattern = false;
		List<DelimeterPattern> possiblePatterns = patternManager.getRankedPatterns();
		for(DelimeterPattern pattern : possiblePatterns){
				
			Collection<IToken> values = applyPattern(pattern, sample);
			if(values != null && !values.isEmpty()){
				gotPattern = true;
				processingData.addReliableTokens(values);
				break;
			}

		}
		if(!gotPattern){
			int size = sample.size();
			processingData.addReliableTokens(createBasicScalars(sample, size));
		}
	}

	protected List<IToken> createBasicScalars(Stack<IToken> tokens,	int size) {
		ScalarProcessingData pd = new ScalarProcessingData();
		for(int i = 0 ; i < size ; i++){
			IToken token = tokens.get(i);
			IToken scalar = null;
			int type = token.getType();
			if(type==IToken.TOKEN_TYPE_DIGIT){
				int startPosition = token.getStartPosition() ;
				int endPosition = token.getEndPosition() ;
				String val = token.getStringValue();
				
				IToken next = null;
				if(i<size-1){
					next = tokens.get(i+1);
					endPosition = next.getEndPosition() ;
					if(next.getType()!=IToken.TOKEN_TYPE_VULGAR_FRACTION){
						next = null;
					}
					i++;
				}
				scalar = createScalar(val,next,startPosition,endPosition);
			}
			else if(type == IToken.TOKEN_TYPE_VULGAR_FRACTION){
				scalar = createScalar(null,token,i,i+1);
			}
			else{
				continue;
			}
			pd.addScalar(scalar);			
		}
		ArrayList<IToken> result = pd.getTokens();
		return result;
	}
	
	static IToken createScalar(
			String evenPart,
			IToken token,
			int startPosition,
			int endPosition)
	{
		int iVal = 0;
		if(!Utils.isEmptyString(evenPart)){
			try{
				iVal = Integer.parseInt(evenPart);
			}catch (NumberFormatException e){
				// System.out.println("a");
			}
		}
		
		if(token==null){
			if(Utils.isEmptyString(evenPart)){
				return null;
			}
			return new ScalarToken(iVal, null, null, startPosition, endPosition);
		}
		else if(token.getType() == IToken.TOKEN_TYPE_DIGIT){
			if(Utils.isEmptyString(evenPart)){
				return null;
			}
			String decimalFractionPart = token.getStringValue();
			if(Utils.isEmptyString(decimalFractionPart)){
				return null;
			}
			Integer fVal = Integer.parseInt(decimalFractionPart);
			return new ScalarToken(iVal, fVal,true, null, null, startPosition, endPosition);
		}
		else if(token.getType() == IToken.TOKEN_TYPE_VULGAR_FRACTION){
			String vulgarFractionPart = token.getStringValue();
			if(Utils.isEmptyString(vulgarFractionPart)){
				return null;
			}
			int[] iPair = VulgarFraction.getIntegerPair(vulgarFractionPart.charAt(0));
			return new ScalarToken(iVal*iPair[1]+iPair[0], iPair[1], false,  null, null, startPosition, endPosition);
		}
		return null;
	}

	private ScalarProcessingData processingData = new ScalarProcessingData();

	

	RuleExecutionEngine patternRuleEngine;

	private Collection<IToken> applyPattern(DelimeterPattern pattern,	
			Stack<IToken> tokens) {
		
		if(patternRuleEngine==null){
			patternRuleEngine = new ScalarProcessingRules(processingData).getEngine();
		}
		
		processingData.reset(pattern);
		patternRuleEngine.applyRules(tokens);
		processingData.appendToken(null, true);
		ArrayList<IToken> result = processingData.getTokens();
		return result;		
	}
	
	private RuleExecutionEngine patternCancellationEngine;

	
	protected void beforeProcess(List<IToken> tokens){
		patternManager.resetPatterns();
		
		if(patternCancellationEngine==null){
			patternCancellationEngine = new PatternCancallationRules(patternManager).getEngine();
		}
		patternCancellationEngine.applyRules(tokens);
	};

	
	//accept digit, symbol, vulgar fraction and non breaking whitespace.
	@Override
	protected ProcessingResult continuePush(Stack<IToken> tokens, IToken newToken) {
		IToken previousToken = tokens.size() > 0 ? tokens.peek() : null;
		ProcessingResult result = breakCondition.compute(previousToken,newToken);
		return result;
	}
	
	private BinaryCondition<ProcessingResult> breakCondition = new BinaryCondition<ProcessingResult>() {

		@Override
		public ProcessingResult compute(IToken token0, IToken token1) {
			
			int type1 = token1.getType();
			if(token1.hasSpaceBefore()){
				if(token0!=null){
				int type0 = token0.getType();
					if(type1==IToken.TOKEN_TYPE_DIGIT){
						if(type0==IToken.TOKEN_TYPE_DIGIT){
							return CONTINUE_PUSH;
						}
						else{
							return new ProcessingResult(1,false,true);
						}
					}
					else{
						if(type0==IToken.TOKEN_TYPE_DIGIT){
							return DO_NOT_ACCEPT_AND_BREAK;
						}
						else{
							return new ProcessingResult(1,false,true);
						}
					}
				}
			}

			
			if(type1 == IToken.TOKEN_TYPE_LINEBREAK){
				return DO_NOT_ACCEPT_AND_BREAK;
			}
			else if(type1 == IToken.TOKEN_TYPE_DIGIT){
				return CONTINUE_PUSH;
			}
			else if(type1 == IToken.TOKEN_TYPE_VULGAR_FRACTION){
				return ACCEPT_AND_BREAK;
			}
			if(token0 == null){
				return DO_NOT_ACCEPT_AND_BREAK;
			}
			int type0 = token0.getType();
			if(type0==IToken.TOKEN_TYPE_DIGIT){
				if( type1==IToken.TOKEN_TYPE_SYMBOL&&isOneOf(token1, acceptedSymbols)){
					return CONTINUE_PUSH;
				}
				else if(type1==IToken.TOKEN_TYPE_NON_BREAKING_SPACE){
					return CONTINUE_PUSH;
				}
				else{
					return DO_NOT_ACCEPT_AND_BREAK;
				}
			}
			return new ProcessingResult(1,false,true);
		}
	};

	@Override
	protected ProcessingResult checkPossibleStart(IToken unit) {
		if(unit.getType() == IToken.TOKEN_TYPE_DIGIT
				|| unit.getType() == IToken.TOKEN_TYPE_VULGAR_FRACTION){
			return CONTINUE_PUSH;
		}		
		return DO_NOT_ACCEPT_AND_BREAK;
	}
	
	

	@Override
	protected ProcessingResult checkToken(IToken newToken) {
		throw new UnsupportedOperationException("Check Token not supported for Scalar Parser"); 
	}
}
