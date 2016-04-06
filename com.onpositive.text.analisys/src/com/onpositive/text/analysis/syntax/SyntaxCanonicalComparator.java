package com.onpositive.text.analysis.syntax;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import com.onpositive.text.analysis.CanonicalComparator;
import com.onpositive.text.analysis.CompositToken;
import com.onpositive.text.analysis.IToken;
import com.onpositive.text.analysis.lexic.WordFormToken;

public class SyntaxCanonicalComparator extends CanonicalComparator {

	private static HashSet<Integer> equalRightTypes = new HashSet<Integer>(Arrays.asList(IToken.TOKEN_TYPE_CLAUSE,
			IToken.TOKEN_TYPE_COMPLEX_CLAUSE, IToken.TOKEN_TYPE_UNIFORM_ADJECTIVE, IToken.TOKEN_TYPE_UNIFORM_ADVERB,
			IToken.TOKEN_TYPE_UNIFORM_NOUN, IToken.TOKEN_TYPE_UNIFORM_PREDICATIVE, IToken.TOKEN_TYPE_UNIFORM_VERB,
			IToken.TOKEN_TYPE_LONG_NAME));

	@Override
	protected TokenCanonicCode buildTokenCode(IToken token) {

		TokenCanonicCode code = null;
		if (token instanceof SyntaxToken) {

			int type = token.getType();
			SyntaxToken st = (SyntaxToken) token;
			if (equalRightTypes.contains(type)) {
				code = encodeEqualRightTokens(st);
			} else if (st.getMainWord() == st) {
				code = encodeSyntaxToken(st);
			} else {
				code = encodeSyntaxToken(st);
			}
		} else if (token instanceof CompositToken) {
			code = encodeEqualRightTokens(token);
		} else {
			int id = token.id();
			code = new TokenCanonicCode(id, id);
		}

		return code;
	}

	private TokenCanonicCode encodeSyntaxToken(SyntaxToken token) {

		WordFormToken mainWord = token.getMainWord();
		if (mainWord == null) {
			return new TokenCanonicCode(token.id(), token.id());
		} else if (mainWord == token) {
			return new TokenCanonicCode(token.id(), token.id());
		}
		TokenCanonicCode code = new TokenCanonicCode(token.id(), mainWord.id());
		TokenCanonicCode mainCode = getTokenCode(mainWord);

		code.addAttachment(new AttachmentCanonicCode(0, mainCode));

		for (SyntaxToken t = token; t != mainWord; t = t.getMainGroup()) {

			int type = t.getType();
			List<IToken> children = t.getChildren();
			SyntaxToken mainGroup = t.getMainGroup();
			if (children != null) {
				for (IToken ch : children) {
					if (ch == mainGroup) {
						continue;
					}
					TokenCanonicCode chCode = getTokenCode(ch);
					AttachmentCanonicCode att = new AttachmentCanonicCode(type, chCode);
					code.addAttachment(att);
				}
			}
		}
		return code;
	}

	private TokenCanonicCode encodeEqualRightTokens(IToken token) {

		int type = token.getType();
		int id = token.id();
		List<IToken> children = token.getChildren();
		int mainId = Integer.MAX_VALUE;

		ArrayList<AttachmentCanonicCode> list = new ArrayList<AttachmentCanonicCode>();
		if (children != null) {

			for (IToken ch : children) {
				TokenCanonicCode chCode = getTokenCode(ch);
				AttachmentCanonicCode att = new AttachmentCanonicCode(type, chCode);
				list.add(att);
				mainId = Math.min(mainId, att.id());
			}
		}

		TokenCanonicCode code = new TokenCanonicCode(id, mainId);
		for (AttachmentCanonicCode att : list) {
			code.addAttachment(att);
		}
		return code;
	}

}
