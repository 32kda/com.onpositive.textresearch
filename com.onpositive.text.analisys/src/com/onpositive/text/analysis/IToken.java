package com.onpositive.text.analysis;

import java.util.Collection;
import java.util.List;

public interface IToken {
	
	public static enum Direction{
		
		START, END;
		
		public Direction opposite(){
			return this == START ? END : START;
		}
		
		public boolean isBeyondMyBound(int myBound, int other){
			return this == START ? myBound >= other : myBound <= other;
		}
		
		public int absolutBound(){
			return this == START ? Integer.MIN_VALUE : Integer.MAX_VALUE;
		}
	}
	
	static final int TOKEN_TYPE_UNDEFINED = 00001;
	
	static final int TOKEN_TYPE_DIGIT = 10001;
	
	static final int TOKEN_TYPE_LETTER = 10002;
	
	static final int TOKEN_TYPE_SYMBOL = 10003;
	
	static final int TOKEN_TYPE_VULGAR_FRACTION = 10004;
	
	static final int TOKEN_TYPE_EXPONENT = 10005;
	
	static final int TOKEN_TYPE_LINEBREAK = 100101;
	
	static final int TOKEN_TYPE_NON_BREAKING_SPACE = 100102;
	
	static final int TOKEN_TYPE_OTHER_WHITESPACE = 100103;
	
	static final int TOKEN_TYPE_SCALAR = 10201;
	
	static final int TOKEN_TYPE_DATE = 10202;
	
	static final int TOKEN_TYPE_DIMENSION = 10203;
	
	static final int TOKEN_TYPE_UNIT = 10213;
	
	static final int TOKEN_TYPE_REGION_BOUND = 10301;
	
	static final int TOKEN_TYPE_WORD_FORM = 11000;
	
	static final int TOKEN_TYPE_WORD_WITH_INDEX = 11001;
	
	static final int TOKEN_TYPE_SENTENCE = 11002;
	
	static final int TOKEN_TYPE_NOUN_ADJECTIVE = 11011;
	
	static final int TOKEN_TYPE_ADJECTIVE_ADVERB = 11012;
	
	static final int TOKEN_TYPE_VERB_ADVERB = 11013;
	
	static final int TOKEN_TYPE_DIRECT_OBJECT_NAME = 11014;
	
	static final int TOKEN_TYPE_DIRECT_OBJECT_INF = 11015;
	
	static final int TOKEN_TYPE_VERB_NOUN = 11016;
	
	static final int TOKEN_TYPE_VERB_ADJECTIVE = 11017;
	
	static final int TOKEN_TYPE_VERB_NOUN_PREP = 11018;
	
	static final int TOKEN_TYPE_VERB_ADJECTIVE_PREP = 11019;
	
	static final int TOKEN_TYPE_VERB_ADVERB_PREP = 11020;
	
	static final int TOKEN_TYPE_VERB_GERUND = 11021;
	
	static final int TOKEN_TYPE_COMPOSITE_VERB = 11022;
	
	static final int TOKEN_TYPE_NOUN_NAME_PREP = 11023;
	
	static final int TOKEN_TYPE_NOUN_PARTICIPLE = 11024;
	
	static final int TOKEN_TYPE_VERB_PARTICLE = 11025;
	
	static final int TOKEN_TYPE_CLAUSE = 11030;
	
	static final int TOKEN_TYPE_COMPLEX_CLAUSE = 11031;
	
	static final int TOKEN_TYPE_ADVERB_WITH_MODIFICATOR = 11041;
	
	static final int TOKEN_TYPE_UNIFORM_PREDICATIVE = 11051;
	
	static final int TOKEN_TYPE_UNIFORM_ADVERB = 11052;
	
	static final int TOKEN_TYPE_UNIFORM_ADJECTIVE = 11053;

	static final int TOKEN_TYPE_UNIFORM_NOUN = 11054;
	
	static final int TOKEN_TYPE_UNIFORM_VERB = 11055;

	static final int TOKEN_TYPE_MEASURED_NOUN = 11056;
	
	static final int TOKEN_TYPE_GENITIVE_CHAIN = 11057;
	
	static final int TOKEN_TYPE_PREPOSITION_GROUP = 11058;
	
	static final int TOKEN_TYPE_BRACKETS = 12001;
	
	static final int TOKEN_TYPE_ENUMERATION = 12002;
	
	static final int TOKEN_TYPE_DIRECT_SPEACH = 12003;
	
	static final int TOKEN_TYPE_TITLE = 12004;
	
	static final int TOKEN_TYPE_LINK = 20001;

	static final int TOKEN_TYPE_LONG_NAME = 20002;
	
	int id();
	
	void setId(int id);
	
	String getStringValue();
	String getStableStringValue();
	String getShortStringValue();
	
	int getType();
	
	int getStartPosition();
	
	int getEndPosition();
	
	int getBoundPosition(Direction dir);
	
	int getLength();
	
	IToken getNext();
	
	IToken getPrevious();
	
	IToken getNeighbour(Direction direction);
	
	void setNext(IToken token);
	
	void setPrevious(IToken token);
	
	void setNeighbour(IToken token, Direction direction);
	
	List<IToken> getNextTokens();
	
	List<IToken> getPreviousTokens();
	
	List<IToken> getConflicts();
	
	boolean hasConflicts();

	List<IToken> getNeighbours(Direction direction);
	
	void addNextToken(IToken token);
	
	void addPreviousToken(IToken token);
	
	void addConflict(IToken token);
	
	void addNeighbour(IToken token, Direction direction);
	
	void removeNextToken(IToken token);
	
	void removePreviousToken(IToken token);
	
	void removeConflict(IToken token);
	
	void removeNeighbour(Direction direction, IToken token);
	
	void cleanNeighbours(Direction direction);
	
	void cleanNextNeighbours();
	
	void cleanPreviousNeighbours();
	
	List<IToken> getChildren();
	
	void addChild(IToken child);
	
	void addChildren(Collection<IToken> children);
	
	void setChildren(Collection<IToken> children);
	
	IToken getFirstChild(Direction direction);
	
	IToken getChild(int pos, Direction direction);
	
	List<IToken> getParents();
	
	void addParent(IToken parent);

	void removeParent(IToken token);

	boolean hasSpaceAfter();
	
	boolean hasSpaceBefore();
	
	boolean isDoubtful();
	
	String getLink();

	void setLink(String link);
	
	int childrenCount();
	
	void replaceChild(IToken token, IToken newToken);

	void adjustStartPosition(int startPosition);

	void adjustEndPosition(int endPosition);

	String getParserName();

	void setCorrelation(double corellation, double weight);
	
	double getCorrelation();
	
	/**
	 * @return Whether correlation was already set for current token
	 */
	boolean hasCorrelation();
	
}
