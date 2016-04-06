package com.onpositive.text.analysis.lexic;

import com.onpositive.text.analysis.IToken;
import com.onpositive.text.analysis.syntax.SyntaxToken;

public class LongNameToken extends SyntaxToken {

	public LongNameToken(SyntaxToken unitToken,
			int startPosition, int endPosition) {
		super(IToken.TOKEN_TYPE_LONG_NAME, unitToken, unitToken.getGrammemSets(),
				startPosition, endPosition);
	}
	
	@Override
	public String getStringValue() {
		return super.getStringValue();
	}

}
