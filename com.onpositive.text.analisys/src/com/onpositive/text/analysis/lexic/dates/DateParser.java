package com.onpositive.text.analysis.lexic.dates;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Stack;

import com.onpositive.text.analysis.BasicParser;
import com.onpositive.text.analysis.IToken;
import com.onpositive.text.analysis.lexic.DateToken;
import com.onpositive.text.analysis.lexic.ScalarToken;
import com.onpositive.text.analysis.lexic.WordFormToken;
import com.onpositive.text.analysis.syntax.SyntaxToken;

public class DateParser extends BasicParser {

	private static final String DECEMBER = "декабрь";
	private static final String NOVEMBER = "ноябрь";
	private static final String OCTOBER = "октябрь";
	private static final String SEPTEMBER = "сентябрь";
	private static final String AUGUST = "август";
	private static final String JULY = "июль";
	private static final String JUNE = "июнь";
	private static final String MAY = "май";
	private static final String APRIL = "апрель";
	private static final String MARCH = "март";
	private static final String FEBRUARY = "февраль";
	private static final String JANUARY = "январь";
	private static final String YEAR = "год";
	static HashSet<String> dates = new HashSet<String>();
	static ArrayList<String> months = new ArrayList<String>();

	static {
		dates.add(YEAR);
		months.add(JANUARY);
		months.add(FEBRUARY);
		months.add(MARCH);
		months.add(APRIL);
		months.add(MAY);
		months.add(JUNE);
		months.add(JULY);
		months.add(AUGUST);
		months.add(SEPTEMBER);
		months.add(OCTOBER);
		months.add(NOVEMBER);
		months.add(DECEMBER);
		dates.addAll(months);
	}

	@Override
	protected void combineTokens(Stack<IToken> sample, ProcessingData processingData) {
		if (sample.size() != 2) {
			return;
		}
		IToken token0 = sample.get(0);
		if (!(token0 instanceof ScalarToken)) {
			return;
		}
		ScalarToken scalarToken = (ScalarToken) token0;

		IToken token1 = sample.get(1);
		if (!(token1 instanceof SyntaxToken)) {
			return;
		}
		SyntaxToken unitToken = (SyntaxToken) token1;

		int startPosition = token0.getStartPosition();
		int endPosition = token1.getEndPosition();
		WordFormToken mainWord = unitToken.getMainWord();
		String basicForm = mainWord.getBasicForm();

		if (basicForm.equals(YEAR)) {
			DateToken dimensionToken = new DateToken(scalarToken, unitToken,
					startPosition, endPosition);
			if (!scalarToken.isFracture()&&!scalarToken.isDecimal()){
			int parseInt = (int) scalarToken.getValue();
			dimensionToken.setYear(parseInt);
			processingData.addReliableToken(dimensionToken);
			}
		} else {
			DateToken dimensionToken = new DateToken(scalarToken, unitToken,
					startPosition, endPosition);
			if (!scalarToken.isFracture()&&!scalarToken.isDecimal()){
			int parseInt = (int) scalarToken.getValue();
			if (parseInt > 0 && parseInt < 32) {
				dimensionToken.setDay(parseInt);
				dimensionToken.setMonth(months.indexOf(basicForm));
				processingData.addReliableToken(dimensionToken);
			}}
		}
	}

	@Override
	protected ProcessingResult continuePush(Stack<IToken> sample,
			IToken nextToken) {
		if (isDateWord(nextToken)){
			return ACCEPT_AND_BREAK;
		}
		return stepBack(sample.size());
	}

	static boolean isDateWord(IToken nextToken) {
		if (nextToken instanceof SyntaxToken) {
			SyntaxToken tc = (SyntaxToken) nextToken;
			WordFormToken mainWord = tc.getMainWord();
			if (mainWord != null) {
				String basicForm = mainWord.getBasicForm();
				if (dates.contains(basicForm)) {
					return true;
				}
			}
		}
		return false;
	}
	

	@Override
	protected ProcessingResult checkPossibleStart(IToken newToken) {
		int type = newToken.getType();
		if (type == IToken.TOKEN_TYPE_SCALAR) {
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
