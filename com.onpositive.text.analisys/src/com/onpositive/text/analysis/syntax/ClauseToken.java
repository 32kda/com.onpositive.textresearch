package com.onpositive.text.analysis.syntax;

import com.onpositive.text.analysis.IToken;

public class ClauseToken extends SyntaxToken {

	public ClauseToken(SyntaxToken subject, SyntaxToken predicate, int startPosition, int endPosition) {
		super(IToken.TOKEN_TYPE_CLAUSE, null, null, startPosition, endPosition);
		this.subject = subject;
		this.predicate = predicate;
	}
	
	private SyntaxToken subject;
	
	private SyntaxToken predicate;

	public SyntaxToken getSubject() {
		return subject;
	}

	public SyntaxToken getPredicate() {
		return predicate;
	}

	public void setSubject(SyntaxToken subject) {
		this.subject = subject;
		adjustBounds(subject);
	}

	public void setPredicate(SyntaxToken predicate) {
		this.predicate = predicate;
		adjustBounds(predicate);
	}

	protected void adjustBounds(SyntaxToken member) {
		this.setStartPosition(Math.min(this.getStartPosition(), member.getStartPosition()));
		this.setEndPosition(Math.max(this.getEndPosition(), member.getEndPosition()));
	}

	@Override
	public String getStringValue() {
		StringBuilder bld = new StringBuilder();
		if(subject!=null){
			bld.append(subject);
		}
		else{
			bld.append("no subject");
		}
		bld.append(" ");
		if(predicate!=null){
			bld.append(predicate);
		}
		else{
			bld.append("no predicate");
		}
		return bld.toString().trim();
	}
	@Override
	public String getShortStringValue() {
		StringBuilder bld = new StringBuilder();
		if(subject!=null){
			bld.append(subject.getShortStringValue());
		}
		else{
			bld.append("no subject");
		}
		bld.append(" ");
		if(predicate!=null){
			bld.append(predicate.getShortStringValue());
		}
		else{
			bld.append("no predicate");
		}
		return bld.toString().trim();
	}
	

}
