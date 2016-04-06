package com.onpositive.text.analysis.lexic;

import com.onpositive.text.analysis.IToken;
import com.onpositive.text.analysis.syntax.SyntaxToken;

public class DateToken extends SyntaxToken {

	

	public DateToken(SyntaxToken scalarToken, SyntaxToken unitToken,
			int startPosition, int endPosition) {
		super(IToken.TOKEN_TYPE_DATE, unitToken, unitToken.getGrammemSets(),
				startPosition, endPosition);
	}

	protected Integer year;
	public Integer getYear() {
		return year;
	}

	public void setYear(Integer year) {
		this.year = year;
	}

	public Integer getMonth() {
		return month;
	}

	public void setMonth(Integer month) {
		this.month = month;
	}

	public Integer getDay() {
		return day;
	}

	public void setDay(Integer day) {
		this.day = day;
	}

	protected Integer month;
	protected Integer day;
}
