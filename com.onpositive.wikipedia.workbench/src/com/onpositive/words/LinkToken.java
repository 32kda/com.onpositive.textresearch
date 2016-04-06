package com.onpositive.words;

import com.onpositive.text.analysis.IToken;
import com.onpositive.text.analysis.lexic.StringToken;

public class LinkToken extends StringToken{

	protected LinkToken(String value,  int startPosition,
			int endPosition) {
		super(value, IToken.TOKEN_TYPE_LINK, startPosition, endPosition);
	}

}
