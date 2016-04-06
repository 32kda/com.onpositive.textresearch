package com.onpositive.text.analysis.lexic.scalar;

import com.onpositive.text.analysis.IToken;
import com.onpositive.text.analysis.conditions.BinaryCondition;
import com.onpositive.text.analysis.conditions.TernaryCondition;
import com.onpositive.text.analysis.conditions.TokenMaskBinaryCondition;
import com.onpositive.text.analysis.conditions.TokenMaskTernaryCondition;
import com.onpositive.text.analysis.rules.ConditionRule;
import com.onpositive.text.analysis.rules.RuleCallback;
import com.onpositive.text.analysis.rules.RuleExecutionEngine;

public class PatternCancallationRules {
	
	public PatternCancallationRules(PatternManager patternManager) {
		super();
		this.patternManager = patternManager;
	}
	
	private static final String SPACE_STRING = " ";
	
	private PatternManager patternManager;

	private RuleExecutionEngine executionEngine;

	public RuleExecutionEngine getEngine(){
		
		if(executionEngine==null){
			executionEngine = new RuleExecutionEngine(new ConditionRule[]{
				new SpaceDecimalDelimeter(),
				new DecimalDelimeter(),
				new FractureDelimeter(),
				new FractureDelimeterBeforeVulgarFraction(),
				new SpaceValueDelimeter(),
				new FractureDelimeterPromotion()
			});
		}
		return  executionEngine;
	}
	
	private class SpaceDecimalDelimeter extends ConditionRule{
		
		public SpaceDecimalDelimeter() {
			super( new BinaryCondition<String>() {

				private final TokenMaskBinaryCondition maskCondition
						= new TokenMaskBinaryCondition(new int[] {
								IToken.TOKEN_TYPE_DIGIT,
								IToken.TOKEN_TYPE_DIGIT
					});

				@Override
				public String compute(IToken token0, IToken token1)
				{
					if (!maskCondition.compute(token0, token1)) {
						return null;
					}
					int l0 = token0.getLength();
					if(l0>3){
						return SPACE_STRING;
					}
					int l2 = token1.getLength();
					if(l2!=3){
						return SPACE_STRING;
					}
					patternManager.voteForDecimalDelimeter(SPACE_STRING);
					return null;
				}
			}, new RuleCallback<String>() {
				@Override
				public void execute(String arg) {
					patternManager.cancelDecimalDelimeter(SPACE_STRING);
				}
			});
		}
	}
	
	private class SpaceValueDelimeter extends ConditionRule{
		
		public SpaceValueDelimeter() {
			super( new BinaryCondition<String>() {

				private final TokenMaskBinaryCondition maskCondition
						= new TokenMaskBinaryCondition(new int[] {
								IToken.TOKEN_TYPE_DIGIT,
								IToken.TOKEN_TYPE_DIGIT
					});

				@Override
				public String compute(IToken token0, IToken token1)
				{
					if (!maskCondition.compute(token0, token1)) {
						return null;
					}
					int l0 = token0.getLength();
					if(l0<=3){					
						int l2 = token1.getLength();
						if(l2==3){
							//int l2_ = ("" + Integer.parseInt(token1.getStringValue())).length();
//							if(l2!=l2_)
							return SPACE_STRING;
						}
					}
					patternManager.voteForValueDelimeter(SPACE_STRING);
					return null;
				}
			}, new RuleCallback<String>() {
				@Override
				public void execute(String arg) {
					patternManager.cancelValueDelimeter(SPACE_STRING);
				}
			});
		}
	}	
	
	private class DecimalDelimeter extends ConditionRule{

		public DecimalDelimeter() {
			super( new TernaryCondition<String>() {

				private final TokenMaskTernaryCondition symbolMaskCondition
						= new TokenMaskTernaryCondition(new int[] {
								IToken.TOKEN_TYPE_DIGIT,
								IToken.TOKEN_TYPE_SYMBOL,
								IToken.TOKEN_TYPE_DIGIT,
					});
				
				private final TokenMaskTernaryCondition nonBreakingSpaceMaskCondition
						= new TokenMaskTernaryCondition(new int[] {
							IToken.TOKEN_TYPE_DIGIT,
							IToken.TOKEN_TYPE_NON_BREAKING_SPACE,
							IToken.TOKEN_TYPE_DIGIT,
				});

				@Override
				public String compute(IToken token0, IToken token1,	IToken token2)
				{
					if ( !symbolMaskCondition.compute(token0, token1, token2)
							&& !nonBreakingSpaceMaskCondition.compute(token0, token1, token2))
					{
							return null;
					}
					String str = token1.getType() == IToken.TOKEN_TYPE_NON_BREAKING_SPACE ? " " : token1.getStringValue();
					int l0 = token0.getLength();
					if(l0>3){
						return str;
					}
					int l2 = token2.getLength();
					if(l2!=3){
						return str;
					}
					patternManager.voteForDecimalDelimeter(str);
					return null;
				}
			}, new RuleCallback<String>() {
				@Override
				public void execute(String arg) {
					patternManager.cancelDecimalDelimeter(arg);
				}
			});
		}
	}
	
	private class FractureDelimeter extends ConditionRule{
		
		public FractureDelimeter() {
			super( new TernaryCondition<String>() {

				private final TokenMaskTernaryCondition maskCondition
						= new TokenMaskTernaryCondition(new int[] {
								IToken.TOKEN_TYPE_SYMBOL,
								IToken.TOKEN_TYPE_DIGIT,
								IToken.TOKEN_TYPE_SYMBOL
					});

				@Override
				public String compute(IToken token0, IToken token1,	IToken token2)
				{
					if (!maskCondition.compute(token0, token1, token2)) {
						return null;
					}
					String val0 = token0.getStringValue();
					String val2 = token2.getStringValue();
					if (val0.equals(val2)) {
						return val0;
					}
					for(DelimeterPattern dp : patternManager.getPatterns()){
						if(val0.equals(dp.fractureDelimeter)&&val2.equals(dp.decimalDelimeter)){
							dp.cancel();
						}
					}
					return null;
				}
			} , new RuleCallback<String>() {
				@Override
				public void execute(String arg) {
					patternManager.cancelFractureDelimeter(arg);
				}
			});
		}		
	}

	
	private class FractureDelimeterBeforeVulgarFraction extends ConditionRule{
		
		public FractureDelimeterBeforeVulgarFraction() {
			super( new TernaryCondition<String>() {

				private final TokenMaskBinaryCondition maskCondition
						= new TokenMaskBinaryCondition(new int[] {
								IToken.TOKEN_TYPE_SYMBOL,
								IToken.TOKEN_TYPE_VULGAR_FRACTION
					});

				@Override
				public String compute(IToken token0, IToken token1,	IToken token2)
				{
					String val = token0.getStringValue();
					if(token0.getType()==IToken.TOKEN_TYPE_SYMBOL && patternManager.iscanCancelledFractureDelimeter(val)){
						return null;
					}
					if (!maskCondition.compute(token0, token1)) {
						return null;
					}
					return val;
				}
			} , new RuleCallback<String>() {
				@Override
				public void execute(String arg) {
					patternManager.cancelFractureDelimeter(arg);
				}
			});
		}		
	}
	
	private class FractureDelimeterPromotion extends ConditionRule{
		
		public FractureDelimeterPromotion() {
			super( new TernaryCondition<String>() {

				private final TokenMaskTernaryCondition maskCondition
						= new TokenMaskTernaryCondition(new int[] {
								IToken.TOKEN_TYPE_DIGIT,
								IToken.TOKEN_TYPE_SYMBOL,
								IToken.TOKEN_TYPE_DIGIT								
					});

				@Override
				public String compute(IToken token0, IToken token1,	IToken token2)
				{
					String val = token1.getStringValue();
					if(token0.getType()==IToken.TOKEN_TYPE_SYMBOL && patternManager.iscanCancelledFractureDelimeter(val)){
						return null;
					}
					if (!maskCondition.compute(token0, token1, token2)) {
						return null;
					}					
					return val;
				}
			} , new RuleCallback<String>() {
				@Override
				public void execute(String arg) {
					patternManager.voteForFractureDelimeter(arg);
				}
			});
		}		
	}

}
