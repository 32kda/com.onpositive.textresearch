package com.onpositive.text.analysis.lexic.dimension;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Stack;

import com.onpositive.semantic.wordnet.AbstractWordNet;
import com.onpositive.semantic.wordnet.TextElement;
import com.onpositive.text.analysis.BasicParser;
import com.onpositive.text.analysis.IToken;
import com.onpositive.text.analysis.lexic.PrepConjRegistry;
import com.onpositive.text.analysis.lexic.StringToken;
import com.onpositive.text.analysis.lexic.UnitToken;
import com.onpositive.text.analysis.lexic.WordFormToken;
import com.onpositive.text.analysis.rules.matchers.UnaryMatcher;
import com.onpositive.text.analysis.syntax.SyntaxToken;

public class UnitParser extends BasicParser {
	
	public UnitParser(AbstractWordNet wordNet) {
		this.unitsProvider = new UnitsProvider(wordNet);
		this.prepConjRegistry = new PrepConjRegistry(wordNet);
	}	  
	
	private UnitsProvider unitsProvider;
	PrepConjRegistry prepConjRegistry;

	@Override
	protected void combineTokens(Stack<IToken> sample, ProcessingData processingData)
	{
		IToken token = sample.peek();
		if(token instanceof WordFormToken){
			
			List<IToken> tokens = processWordForm((WordFormToken) token);
			if(tokens!=null&&!tokens.isEmpty()){
				appendTokens(tokens,processingData);
				return;
			}			
		}		
		String unitName = getUnitName(token);
		if(unitName==null){
			return;
		}
		List<Unit> constructed = unitsProvider.constructUnits(unitName);
		if(constructed==null||constructed.isEmpty()){
			return;
		}
		
		if(canBePreposition(token)){
			return;
		}
		
		int startPosition = token.getStartPosition();
		int endPosition = token.getEndPosition();
		SyntaxToken mainGroup = (token instanceof SyntaxToken) ? (SyntaxToken)token : null;
		ArrayList<IToken> tokens = createUnitTokens(constructed, mainGroup, startPosition, endPosition);
		appendTokens(tokens,processingData);
	}

	private boolean canBePreposition(IToken token) {
		
		String val = token.getStringValue();
		UnaryMatcher<SyntaxToken> matcher = prepConjRegistry.getPrepCaseMatcher(val);
		if(matcher==null){
			return false;
		}
		
		IToken next = token.getNext();
		if(next!=null){
			if(matcher.match(next)){
				return true;
			}
		}
		else{
			List<IToken> nextTokens = token.getNextTokens();
			if(nextTokens!=null){
				for(IToken n : nextTokens){
					if(matcher.match(n)){
						return true;
					}					
				}
			}
		}
		
		return false;
	}

	private String getUnitName(IToken token) {
		
		if(token instanceof WordFormToken){
			TextElement te = ((WordFormToken)token).getParentTextElement();
			if(te==null){
				return null;
			}
			return te.getBasicForm();
		}
		else if( token instanceof StringToken){
			return token.getStringValue();
		}
		return null;
	}

	private void appendTokens(List<IToken> tokens, ProcessingData processingData) {
		if(tokens==null||tokens.isEmpty()){
			return;
		}
		else if(tokens.size()==1){
			processingData.addReliableToken(tokens.get(0));
		}
		else{
			processingData.addDoubtfulTokens(tokens);
		}
	}

	private List<IToken> processWordForm(WordFormToken token) {

		List<Unit> units = unitsProvider.getUnits(token.getMeaningElements());
		if(units==null){
			return null;
		}
		if(canBePreposition(token)){
			return null;
		}
		
		int startPosition = token.getStartPosition();
		int endPosition = token.getEndPosition();
		
		ArrayList<IToken> tokens = createUnitTokens(units, token, startPosition, endPosition);
		return tokens;
	}

	private ArrayList<IToken> createUnitTokens(
			Collection<Unit> units,
			SyntaxToken mainGroup,
			int startPosition,
			int endPosition)
	{
		ArrayList<IToken> tokens = new ArrayList<IToken>();
		for(Unit unit : units){
			UnitToken unitToken = new UnitToken(unit, mainGroup, null, startPosition, endPosition);
			tokens.add(unitToken);
		}
		return tokens;
	}

	@Override
	protected ProcessingResult checkToken(IToken newToken) {
		
		int type = newToken.getType();
		if(type==IToken.TOKEN_TYPE_WORD_FORM||type==IToken.TOKEN_TYPE_LETTER){
			return ACCEPT_AND_BREAK;
		}		
		return DO_NOT_ACCEPT_AND_BREAK;
	}

}
