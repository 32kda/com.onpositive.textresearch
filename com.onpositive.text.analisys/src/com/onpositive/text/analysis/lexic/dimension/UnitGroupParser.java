package com.onpositive.text.analysis.lexic.dimension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import com.onpositive.semantic.wordnet.AbstractWordNet;
import com.onpositive.text.analysis.BasicParser;
import com.onpositive.text.analysis.IToken;
import com.onpositive.text.analysis.conditions.BinaryCondition;
import com.onpositive.text.analysis.conditions.BinaryDisjunction;
import com.onpositive.text.analysis.conditions.TokenMaskBinaryCondition;
import com.onpositive.text.analysis.lexic.ScalarToken;
import com.onpositive.text.analysis.lexic.UnitToken;
import com.onpositive.text.analysis.lexic.WordFormToken;
import com.onpositive.text.analysis.utils.Exponent;

public class UnitGroupParser extends BasicParser{
	
	private static HashMap<String,Integer> expMap = new HashMap<String, Integer>();
	static{
		expMap.put("квадратный",2);
		expMap.put("кубический",3);
	}
	
	
	public UnitGroupParser(AbstractWordNet wordNet) {
		this.unitsProvider = new UnitsProvider(wordNet);
	}

	private UnitsProvider unitsProvider;
	
	@Override
	protected void combineTokens(Stack<IToken> sample, ProcessingData processingData)
	{
		List<UnitToken> newUnitTokens = null;
		int startPosition = sample.get(0).getStartPosition();
		int endPosition = sample.peek().getEndPosition();
		if(sample.size()==2){
			compositeTransformCondition.setTokens(sample);
			newUnitTokens = compositeTransformCondition.compute(0);
		}
		else if(sample.size()==3){
			IToken token0 = sample.get(0);
			if(!(token0 instanceof UnitToken)){
				return;
			}
			UnitToken unitToken = (UnitToken) token0;			
			IToken token2 = sample.get(2);
			if(!(token2 instanceof ScalarToken)){
				return;
			}
			ScalarToken scalarToken = (ScalarToken) token2;
			double exp = scalarToken.getValue1();
			newUnitTokens = groupUnit(unitToken,(int)exp,startPosition,endPosition);
		}
		if(newUnitTokens!=null){
			if(newUnitTokens.size()==1){
				processingData.addReliableTokens(newUnitTokens);
			}
			else{
				processingData.addDoubtfulTokens(newUnitTokens);
			}
		}
	}

	@Override
	protected ProcessingResult continuePush(Stack<IToken> sample, IToken newToken )
	{
		if(sample.size()==1){
			ProcessingResult result = compositeDetectCondition.compute(sample.peek(), newToken);
			if(result!=null){
				return result;
			}
		}
		else if(sample.size()==2){
			int type = newToken.getType();
			if(type==IToken.TOKEN_TYPE_SCALAR){
				return ACCEPT_AND_BREAK;
			}
		}
		return stepBack(sample.size());
	}

	@Override
	protected ProcessingResult checkPossibleStart(IToken token) {
		int type = token.getType();
		if(type==IToken.TOKEN_TYPE_UNIT){
			return CONTINUE_PUSH;
		}
		else if(type == IToken.TOKEN_TYPE_WORD_FORM){
			return checkWordFormToken(token) ? CONTINUE_PUSH : DO_NOT_ACCEPT_AND_BREAK;
		}
		return DO_NOT_ACCEPT_AND_BREAK;
	}

	private static boolean checkWordFormToken(IToken token) {
		WordFormToken wft = (WordFormToken) token;
		String basicForm = wft.getBasicForm().toLowerCase();
		if(expMap.containsKey(basicForm)){
			return true;
		}
		return false;
	}
	
	

	@Override
	protected ProcessingResult checkToken(IToken newToken) {
		throw new UnsupportedOperationException("Check Token not supported for Unit Group Parser"); 
	}
	
	
	private List<UnitToken> groupUnit(UnitToken unitToken, Integer exp,int startPosition,int endPosition) {
		
		String shortName = unitToken.getUnit().getShortName();
		shortName += "^" +exp;
		List<Unit> units = unitsProvider.getUnits(shortName);
		if(units!=null&&!units.isEmpty()){
			ArrayList<UnitToken> list = new ArrayList<UnitToken>();
			for(Unit unit : units){
				UnitToken token = new UnitToken(unit, unitToken, null, startPosition, endPosition);
				list.add(token);				
			}
			return list;
		}
		List<Unit> constructed = unitsProvider.constructUnits(shortName);
		if(constructed != null&&!constructed.isEmpty()){
			ArrayList<UnitToken> list = new ArrayList<UnitToken>();
			for(Unit unit : constructed){
				UnitToken token = new UnitToken(unit, unitToken, null, startPosition, endPosition);
				list.add(token);
			}
			return list;
		}
		return null;
	}
	
	protected TokenMaskBinaryCondition mask0 = new TokenMaskBinaryCondition(
			new int[]{IToken.TOKEN_TYPE_UNIT, IToken.TOKEN_TYPE_EXPONENT});
	
	protected TokenMaskBinaryCondition mask1 = new TokenMaskBinaryCondition(
			new int[]{IToken.TOKEN_TYPE_UNIT, IToken.TOKEN_TYPE_SYMBOL});
	
	protected TokenMaskBinaryCondition mask2 = new TokenMaskBinaryCondition(
			new int[]{IToken.TOKEN_TYPE_UNIT, IToken.TOKEN_TYPE_WORD_FORM});
	
	protected TokenMaskBinaryCondition mask3 = new TokenMaskBinaryCondition(
			new int[]{IToken.TOKEN_TYPE_WORD_FORM, IToken.TOKEN_TYPE_UNIT});
	
	protected BinaryCondition<List<UnitToken>> transformCondition0 =
			new BinaryCondition<List<UnitToken>>() {

		@Override
		public List<UnitToken> compute(IToken token0, IToken token1) {

			if (!mask0.compute(token0, token1)) {
				return null;
			}			
			int startPosition = token0.getStartPosition();
			int endPosition = token1.getEndPosition();
			UnitToken unitToken = (UnitToken) token0;			
			Integer exp = Exponent.getExponentValue(token1.getStringValue());
			List<UnitToken> result = groupUnit(unitToken,exp,startPosition,endPosition);
			return result;
		}
	};

	protected BinaryCondition<List<UnitToken>> transformCondition2 = 
			new BinaryCondition<List<UnitToken>>() {

		@Override
		public List<UnitToken> compute(IToken token0, IToken token1) {

			if (!mask2.compute(token0, token1)) {
				return null;
			}
			int startPosition = token0.getStartPosition();
			int endPosition = token1.getEndPosition();
			UnitToken unitToken = (UnitToken) token0;
			WordFormToken wft = (WordFormToken) token1;
			String expString = wft.getBasicForm();
			Integer exp = expMap.get(expString.toLowerCase());
			List<UnitToken> result = groupUnit(unitToken,exp,startPosition,endPosition);
			return result;
		}
	};

	protected BinaryCondition<List<UnitToken>> transformCondition3 = 
			new BinaryCondition<List<UnitToken>>() {

		@Override
		public List<UnitToken> compute(IToken token0, IToken token1) {

			if (!mask3.compute(token0, token1)) {
				return null;
			}
			int startPosition = token0.getStartPosition();
			int endPosition = token1.getEndPosition();
			WordFormToken wft = (WordFormToken) token0;
			UnitToken unitToken = (UnitToken) token1;			
			String expString = wft.getBasicForm();
			Integer exp = expMap.get(expString.toLowerCase());
			List<UnitToken> result = groupUnit(unitToken,exp,startPosition,endPosition);
			return result;
		}
	};
	
	@SuppressWarnings("unchecked")
	protected BinaryDisjunction<List<UnitToken>> compositeTransformCondition = new BinaryDisjunction<List<UnitToken>>(
			new BinaryCondition[]{transformCondition0, transformCondition2, transformCondition3});
	
	protected BinaryCondition<ProcessingResult> detectCondition0
		= new BinaryCondition<ProcessingResult>(){

		@Override
		public ProcessingResult compute(IToken token0, IToken token1) {
			
			if(!mask0.compute(token0, token1)){
				return null;
			}
			return ACCEPT_AND_BREAK;
		}
	};
	
	protected BinaryCondition<ProcessingResult> detectCondition1
		= new BinaryCondition<ProcessingResult>(){

		@Override
		public ProcessingResult compute(IToken token0, IToken token1) {
			
			if(!mask1.compute(token0, token1)){
				return null;
			}
			String value = token1.getStringValue();
			if(value.equals("^")){
				return CONTINUE_PUSH;
			}
			return null;
		}
	};
	
	protected BinaryCondition<ProcessingResult> detectCondition2
		= new BinaryCondition<ProcessingResult>(){

		@Override
		public ProcessingResult compute(IToken token0, IToken token1) {
			
			if(!mask2.compute(token0, token1)){
				return null;
			}
			return checkWordFormToken(token1) ? ACCEPT_AND_BREAK : null;
		}
	};
	
	protected BinaryCondition<ProcessingResult> detectCondition3
		= new BinaryCondition<ProcessingResult>(){

		@Override
		public ProcessingResult compute(IToken token0, IToken token1) {
			
			if(!mask3.compute(token0, token1)){
				return null;
			}
			return ACCEPT_AND_BREAK;
		}
	};
	
	@SuppressWarnings("unchecked")
	protected BinaryDisjunction<ProcessingResult> compositeDetectCondition = new BinaryDisjunction<ProcessingResult>(
			new BinaryCondition[]{detectCondition0, detectCondition1, detectCondition2, detectCondition3}); 

}
