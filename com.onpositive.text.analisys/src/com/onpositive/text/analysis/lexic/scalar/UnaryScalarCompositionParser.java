package com.onpositive.text.analysis.lexic.scalar;

import java.util.List;
import java.util.Stack;

import com.onpositive.text.analysis.BasicParser;
import com.onpositive.text.analysis.IToken;
import com.onpositive.text.analysis.lexic.ScalarToken;
import com.onpositive.text.analysis.lexic.SymbolToken;

public class UnaryScalarCompositionParser extends BasicParser {

	@Override
	protected void combineTokens(Stack<IToken> sample, ProcessingData processingData) {
		
		if (sample.size() != 2) return;
		else if (sample.get(0).getType() != IToken.TOKEN_TYPE_SYMBOL) return;
		else if (sample.get(1).getType() != IToken.TOKEN_TYPE_SCALAR) return;
		
		SymbolToken sign = (SymbolToken) sample.get(0);
		ScalarToken token = (ScalarToken) sample.get(1);
		
		boolean isMinus = sign.getStringValue().equals("-");
				
		double value = (isMinus ? -1.0 : 1.0) * token.getValue();
		double value1 = (isMinus ? -1 : 1) * token.getValue1();
		double value2 = token.getValue2();
		
		ScalarToken result;
		
		if (token.isFracture())
			result = new ScalarToken((int) value1, (int) value2, token.isDecimal(), null, null, sign.getStartPosition(), token.getEndPosition());
		else
			result = new ScalarToken(value, null, null, sign.getStartPosition(), token.getEndPosition());
		
		processingData.addReliableToken(result);		
	}

	@Override
	protected ProcessingResult checkPossibleStart(IToken unit) {
		IToken prev = unit.getPrevious();
		if (prev == null) {
			List<IToken> prevs = unit.getPreviousTokens();
			if (prevs != null)
				for (IToken p : prevs)
					if (p.getType() == IToken.TOKEN_TYPE_SCALAR) 
						return DO_NOT_ACCEPT_AND_BREAK;			
		} else if (prev.getType() == IToken.TOKEN_TYPE_SCALAR) 
			return DO_NOT_ACCEPT_AND_BREAK;
		
		if (unit.getType() == IToken.TOKEN_TYPE_SYMBOL && ((SymbolToken) unit).isUnaryOperator() == true)
			return CONTINUE_PUSH;
		else return DO_NOT_ACCEPT_AND_BREAK;
	}

	@Override
	protected ProcessingResult checkToken(IToken newToken) {
		throw new UnsupportedOperationException("Check Token not supported for Scalar Parser"); 
	}
	
	@Override
	protected ProcessingResult continuePush(Stack<IToken> tokens, IToken newToken) {
			return (tokens.size() == 1 && 
					newToken.getType() == IToken.TOKEN_TYPE_SCALAR ? CONTINUE_PUSH : DO_NOT_ACCEPT_AND_BREAK);		
	}
}
