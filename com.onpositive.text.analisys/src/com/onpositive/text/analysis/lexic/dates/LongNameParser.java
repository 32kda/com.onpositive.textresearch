package com.onpositive.text.analysis.lexic.dates;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Stack;

import com.onpositive.semantic.wordnet.Grammem;
import com.onpositive.text.analysis.BasicParser;
import com.onpositive.text.analysis.IToken;
import com.onpositive.text.analysis.IToken.Direction;
import com.onpositive.text.analysis.lexic.LongNameToken;
import com.onpositive.text.analysis.lexic.StringToken;
import com.onpositive.text.analysis.lexic.SymbolToken;
import com.onpositive.text.analysis.lexic.WordFormToken;
import com.onpositive.text.analysis.syntax.SyntaxToken;

public class LongNameParser extends BasicParser {

	@Override
	protected ProcessingResult continuePush(Stack<IToken> sample,
			IToken nextToken) {
		// String stringValue = nextToken.getStringValue();
		ProcessingResult looksLikeName = looksLikeName(nextToken);
		if (looksLikeName == CONTINUE_PUSH) {
			return CONTINUE_PUSH;
		}
		if (nextToken instanceof SymbolToken) {
			SymbolToken mm = (SymbolToken) nextToken;
			if (mm.getStringValue().equals(".")) {
				IToken peek = sample.peek();
				if (peek.getStringValue().length() == 1) {
					return CONTINUE_PUSH;
				}
			}
		}
		if (sample.size() > 1) {
			return DO_NOT_ACCEPT_AND_BREAK;
		}
		return stepBack(sample.size());
	}

	@Override
	protected ProcessingResult checkPossibleStart(IToken newToken) {
		ProcessingResult looksLikeName = looksLikeName(newToken);
		if (looksLikeName==CONTINUE_PUSH){
			if (!isOtherNameToken(newToken)){
				return CONTINUE_PUSH;
			}
		}
		return DO_NOT_ACCEPT_AND_BREAK;

	}

	ProcessingResult looksLikeName(IToken newToken) {
		if (newToken instanceof WordFormToken) {
			WordFormToken tc = (WordFormToken) newToken;
			if (tc.hasGrammem(Grammem.SemanGramem.NAME)
					|| tc.hasGrammem(Grammem.SemanGramem.SURN)
					|| tc.hasGrammem(Grammem.SemanGramem.PATR)) {
				return CONTINUE_PUSH;
			}
			
			IToken child = tc.getChild(0, Direction.START);
			if (child != null
					&& Character.isUpperCase(child.getStringValue().charAt(0))) {
				if (tc.getPreviousTokens() != null
						&& !tc.getPreviousTokens().isEmpty()) {
					return CONTINUE_PUSH;
				}
			}
			return DO_NOT_ACCEPT_AND_BREAK;
		} else {
			if (newToken instanceof StringToken) {
				StringToken tc = (StringToken) newToken;
				if (Character.isUpperCase(tc.getStringValue().charAt(0))) {
					return CONTINUE_PUSH;
				}
				return DO_NOT_ACCEPT_AND_BREAK;
			}
			return DO_NOT_ACCEPT_AND_BREAK;
		}
	}

	@Override
	protected ProcessingResult checkToken(IToken newToken) {
		throw new UnsupportedOperationException(
				"Check Token not supported for Date Parser");
	}

	@Override
	protected void combineTokens(Stack<IToken> sample,
			ProcessingData processingData) {

		// WordFormToken sr = null;

		if (sample.size() > 1) {
			LinkedHashSet<IToken> consumed = new LinkedHashSet<IToken>();
			int a = 0;
			for (IToken tk : sample) {
				if (consumed.contains(tk)){
					continue;
				}
				boolean canBeName = canBeName(tk);
				boolean surName = canBeSurName(tk);
				boolean surBePatrName = canBePatr(tk);
				if (canBeName || surName || surBePatrName) {
					ArrayList<IToken> tc = trySelect(a, sample, consumed);
					consumed.addAll(tc);
					if (tc.size() > 1) {
						consumTokens(processingData, tc);
					}
					else if (canBeSurName(tc.get(0))){
						for (int i=a+1;i<sample.size();i++){
							if(tc.size()==3){
								break;
							}
							IToken iToken = sample.get(i);
							
							if (iToken instanceof SyntaxToken){
								SyntaxToken m=(SyntaxToken) iToken;
								if (m.getBasicForm().length()==1){
									tc.add(m);
									continue;
								}
							}
							
							if (iToken instanceof StringToken){
								StringToken mm=(StringToken) iToken;
								if (mm.getStringValue().length()==1){
									tc.add(mm);
									continue;
								}
							}
							break;
						}
						if (tc.size()>1){
							consumTokens(processingData, tc);
							consumed.addAll(tc);
						}
					}
				}
				a++;
			}

		}
	}

	void consumTokens(ProcessingData processingData, ArrayList<IToken> tc) {
		int start = Integer.MAX_VALUE;
		int end = Integer.MIN_VALUE;
		WordFormToken mn = null;
		for (IToken q : tc) {
			end = Math.max(end, q.getEndPosition());
			start = Math.min(start, q.getStartPosition());
			if (isBetter(mn, q)) {
				mn = (WordFormToken) q;
			}
		}
		if (mn == null) {
			for (IToken q : tc) {
				if (q instanceof WordFormToken) {
					mn = (WordFormToken) q;
					break;
				}
			}
		}
		if (mn != null) {
			LongNameToken longNameToken = new LongNameToken(mn,
					start, end);
			processingData.addReliableToken(longNameToken);
		}
	}

	private boolean isBetter(WordFormToken mn, IToken q) {
		if (q instanceof WordFormToken) {
			if (mn == null) {
				return true;
			}
			WordFormToken mz = (WordFormToken) q;
			if (mz.hasGrammem(Grammem.SemanGramem.NAME)) {
				return true;
			}
			if (mz.hasGrammem(Grammem.SemanGramem.PATR)
					&& !mn.hasGrammem(Grammem.SemanGramem.NAME)) {
				return true;
			}
			if (mz.hasGrammem(Grammem.SemanGramem.SURN)
					&& !mn.hasGrammem(Grammem.SemanGramem.NAME)
					&& !mn.hasGrammem(Grammem.SemanGramem.PATR)) {
				return true;
			}
			
		}
		return false;
	}
	//FIXME Looks like piece of shit
	private ArrayList<IToken> trySelect(int a, Stack<IToken> sample,
			LinkedHashSet<IToken> consumed) {
		ArrayList<IToken> result = new ArrayList<IToken>();
		IToken e = sample.get(a);
		boolean canGoForward = true;
		boolean canGoBack = true;
		boolean hasName = canBeName(e);
		boolean hasSurName = canBeSurName(e);
		boolean hasPatr = canBePatr(e);
		result.add(e);
		if (canBePatr(e)) {
			canGoForward = false;
		}
		if (canGoForward) {
			for (int i = a + 1; i < sample.size(); i++) {
				IToken tk = sample.get(i);
				if (consumed.contains(tk)) {
					break;
				}
				if (canBeName(tk)) {
					//if (!hasName) {
						result.add(tk);
						hasName = true;
						continue;
					//} else {
					//	break;
					//}
				}
				if (canBeSurName(tk)) {
					//if (!hasSurName) {
						result.add(tk);
						hasSurName = true;
						continue;
					//}
				}
				if (canBePatr(tk) && !hasPatr) {
					result.add(tk);
					hasPatr = true;
					if (hasSurName&&hasName){
						break;
					}
				}
				if (hasName && hasPatr && hasSurName) {
					return result;
				}
				if (isCommonWord(tk) || isOtherNameToken(tk)) {
					break;
				}
			}
		}
		if (canGoBack) {
			for (int i = a - 1; i > 0; i--) {
				IToken tk = sample.get(i);
				if (consumed.contains(tk)) {
					break;
				}
				if (canBeName(tk)) {
					//if (!hasName) {
						result.add(tk);
						hasName = true;
						continue;
					//} else {
//						break;
					//}
				}
				if (canBeSurName(tk)) {
					//if (!hasSurName) {
						result.add(tk);
						hasSurName = true;
						continue;
					///}
				}
				if (canBePatr(tk) && !hasPatr) {
					result.add(tk);
					hasPatr = true;
					break;
				}
				if (hasName && hasPatr && hasSurName) {
					return result;
				}
				if (isCommonWord(tk) || isOtherNameToken(tk)) {
					break;
				}
			}
		}
		if (result.size() >= 3) {
			return result;
		}
		for (int i = a + 1; i < sample.size(); i++) {
			IToken tk = sample.get(i);
			if (consumed.contains(tk)) {
				break;
			}
			if (!result.contains(tk)) {
				if (isCommonWord(tk) || isOtherNameToken(tk)) {
					break;
				} else {
					result.add(tk);
				}
			}
			if (result.size() >= 3) {
				break;
			}
		}
		for (int i = a - 1; i >= 0; i--) {
			IToken tk = sample.get(i);
			if (consumed.contains(tk)) {
				break;
			}
			if (!result.contains(tk)) {
				if (isCommonWord(tk) || isOtherNameToken(tk)) {
					break;
				} else {
					result.add(tk);
				}
			}
			if (result.size() >= 3) {
				break;
			}
		}
		return result;
	}

	private boolean isCommonWord(IToken tk) {
		if (tk instanceof WordFormToken){
			String stringValue = tk.getChildren().get(0).getStringValue();
			if (Character.isUpperCase(stringValue.charAt(0))){
				return false;
			}
			return true;
		}
		return false;
	}

	private boolean isOtherNameToken(IToken tk) {
		if (tk instanceof WordFormToken){
			WordFormToken ml=(WordFormToken) tk;
			if (ml.hasGrammem(Grammem.SemanGramem.ORGN)){
				return true;
			}
			if (ml.hasGrammem(Grammem.SemanGramem.TOPONIM)){
				return true;
			}
			if (ml.hasGrammem(Grammem.SemanGramem.TRADE_MARK)){
				return true;
			}
		}
		return false;
	}

	private boolean canBePatr(IToken tk) {
		if (tk instanceof WordFormToken){
			WordFormToken ml=(WordFormToken) tk;
			if (ml.hasGrammem(Grammem.SemanGramem.PATR)){
				return true;
			}			
		}
		return false;
	}

	private boolean canBeSurName(IToken tk) {
		if (tk instanceof WordFormToken){
			WordFormToken ml=(WordFormToken) tk;
			if (ml.hasGrammem(Grammem.SemanGramem.SURN)){
				return true;
			}			
		}
		return false;
	}

	private boolean canBeName(IToken tk) {
		if (tk instanceof WordFormToken){
			WordFormToken ml=(WordFormToken) tk;
			if (ml.hasGrammem(Grammem.SemanGramem.NAME)){
				return true;
			}			
		}
		return false;
	}
}