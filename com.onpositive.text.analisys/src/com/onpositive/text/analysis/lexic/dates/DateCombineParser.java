package com.onpositive.text.analysis.lexic.dates;

import java.util.Stack;

import com.onpositive.text.analysis.BasicParser;
import com.onpositive.text.analysis.IToken;
import com.onpositive.text.analysis.lexic.DateToken;
import com.onpositive.text.analysis.syntax.SyntaxToken;

public class DateCombineParser extends BasicParser {

	@Override
	protected void combineTokens(Stack<IToken> sample, ProcessingData processingData) {
		if (sample.size() != 2) {
			return;
		}
		IToken token0 = sample.get(0);

		if (token0 instanceof DateToken) {
			DateToken dateToken0 = (DateToken) token0;
			IToken token1 = sample.get(1);
			if (!(token1 instanceof DateToken)) {
				return;
			}
			DateToken dateToken1 = (DateToken) token1;
			DateToken result = new DateToken(dateToken0, dateToken1,
					dateToken0.getStartPosition(), dateToken1.getEndPosition());
			if (dateToken0.getMonth() != null && dateToken1.getYear() != null
					&& dateToken0.getYear() == null
					&& dateToken1.getMonth() == null) {
				result.setMonth(dateToken0.getMonth());
				result.setYear(dateToken1.getYear());
				result.setDay(dateToken0.getDay());
				processingData.addReliableToken(result);
			}
		} else {
			IToken token1 = sample.get(1);
			if (!(token1 instanceof DateToken)) {
				return;
			}
			SyntaxToken dateToken0 = (SyntaxToken) sample.get(0);
			DateToken dateToken1 = (DateToken) token1;
			DateToken result = new DateToken(dateToken0, dateToken1,
					dateToken0.getStartPosition(), dateToken1.getEndPosition());
			if (dateToken1.getYear() != null && dateToken1.getMonth() == null) {
				result.setYear(dateToken1.getYear());
				String basicForm = dateToken0.getMainWord().getBasicForm();
				int indexOf = DateParser.months.indexOf(basicForm);
				if (indexOf != -1) {
					result.setMonth(indexOf);
					processingData.addReliableToken(result);
				}
			}
		}
	}

	@Override
	protected ProcessingResult continuePush(Stack<IToken> sample,
			IToken nextToken) {
		if (sample.size() == 1) {
			if (nextToken.getType() == IToken.TOKEN_TYPE_DATE) {
				return ACCEPT_AND_BREAK;
			}
		}
		return stepBack(sample.size());
	}

	@Override
	protected ProcessingResult checkPossibleStart(IToken newToken) {
		int type = newToken.getType();
		if (type == IToken.TOKEN_TYPE_DATE) {
			return CONTINUE_PUSH;
		}
		if (DateParser.isDateWord(newToken)) {
			return CONTINUE_PUSH;
		}
		return DO_NOT_ACCEPT_AND_BREAK;
	}

	@Override
	protected ProcessingResult checkToken(IToken newToken) {
		throw new UnsupportedOperationException(
				"Check Token not supported for Date Parser");
	}
}