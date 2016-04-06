package com.onpositive.text.analysis.lexic;

import java.util.List;

import com.onpositive.text.analysis.IToken;
import com.onpositive.text.analysis.syntax.SyntaxToken;

public class ComplexClause extends SyntaxToken {

	public ComplexClause(List<IToken> children, int startPosition,int endPosition)
	{
		super(IToken.TOKEN_TYPE_COMPLEX_CLAUSE, (SyntaxToken) children.get(0), null, startPosition, endPosition);
	}

}
