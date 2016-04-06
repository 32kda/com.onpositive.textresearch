package com.onpositive.text.analysis.lexic;

import com.onpositive.text.analysis.IToken;

public interface ITokenizerExtension {
	
	IToken readUnit(String str, int pos);
}
