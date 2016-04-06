package com.onpositive.semantic.words3;

import static com.onpositive.semantic.words3.hds.StringCoder.int0;
import static com.onpositive.semantic.words3.hds.StringCoder.int1;
import static com.onpositive.semantic.words3.hds.StringCoder.int2;
import static com.onpositive.semantic.words3.hds.StringCoder.makeInt;

import com.onpositive.semantic.wordnet.AbstractRelation;
import com.onpositive.semantic.wordnet.GrammarRelation;
import com.onpositive.semantic.wordnet.IGrammarVisitor;
import com.onpositive.semantic.wordnet.MeaningElement;
import com.onpositive.semantic.wordnet.MorphologicalRelation;
import com.onpositive.semantic.wordnet.SemanticRelation;

public class CodingUtils {

	public static GrammarRelation[] decodeGrammarRelations(int offset,
			byte[] source) {
		int len = source[offset];
		boolean oneWord = false;
		if (len == Byte.MIN_VALUE) {
			oneWord = false;
			offset++;
			len = makeInt((byte) 0, (byte) 0, source[offset + 1],
					source[offset]);
			offset++;
		}
		if (len < 0) {
			oneWord = true;
			len = -len;
		}
	
		GrammarRelation[] result = new GrammarRelation[len];
		offset++;
		if (oneWord) {
			byte b0 = source[offset++];
			byte b1 = source[offset++];
			byte b2 = source[offset++];
			int word = makeInt((byte) 0, b2, b1, b0);
			for (int a = 0; a < result.length; a++) {
				byte rel = source[offset++];
				byte rel1 = source[offset++];
				result[a] = new GrammarRelation(null, word, makeInt((byte)0,(byte) 0, rel1,rel));
			}
		} else {
			for (int a = 0; a < result.length; a++) {
				byte b0 = source[offset++];
				byte b1 = source[offset++];
				byte b2 = source[offset++];
				int word = makeInt((byte) 0, b2, b1, b0);
				byte rel = source[offset++];
				byte rel1 = source[offset++];
				result[a] = new GrammarRelation(null, word, makeInt((byte)0,(byte) 0, rel1,rel));
			}
		}
		return result;
	}
	
	public static void visitGrammarRelations(int offset,
			byte[] source, IGrammarVisitor visitor) {
		int length = source[offset];
		boolean oneWord = false;
		if (length == Byte.MIN_VALUE) {
			oneWord = false;
			offset++;
			length = makeInt((byte) 0, (byte) 0, source[offset + 1],
					source[offset]);
			offset++;
		}
		if (length < 0) {
			oneWord = true;
			length = -length;
		}
		
	
		offset++;
		if (oneWord) {
			byte b0 = source[offset++];
			byte b1 = source[offset++];
			byte b2 = source[offset++];
			int word = makeInt((byte) 0, b2, b1, b0);
			if (visitor.oneWord(word)) {
				return;
			}
			visitor.startVisit();
			for (int a = 0; a < length; a++) {
				byte rel = source[offset++];
				byte rel1 = source[offset++];
				int relatedWord = makeInt((byte)0,(byte) 0, rel1,rel);
				visitor.relation(word, relatedWord);
			}
			visitor.endVisit();
		} else {
			visitor.startVisit();
			for (int a = 0; a < length; a++) {
				byte b0 = source[offset++];
				byte b1 = source[offset++];
				byte b2 = source[offset++];
				int word = makeInt((byte) 0, b2, b1, b0);
				byte rel = source[offset++];
				byte rel1 = source[offset++];
				int relatedWord = makeInt((byte)0,(byte) 0, rel1,rel);
				visitor.relation(word, relatedWord);
			}
			visitor.endVisit();
		}
		
	}

	public static int estimateRelationsLength(int offset, byte[] source) {
		int len = source[offset];
		int ad = 1;
		boolean oneWord = false;
		if (len == Byte.MIN_VALUE) {
			oneWord = false;
			offset++;
			len = makeInt((byte) 0, (byte) 0, source[offset + 1],
					source[offset]);
			ad = 3;
		}
		if (len < 0) {
			oneWord = true;
			len = -len;
		}
		if (oneWord) {
			return len*2 + 4;
		} else {
			return len * 5 +  ad;
		}
	}

	@SuppressWarnings("unchecked")
	public static AbstractRelation<MeaningElement>[] decodeMeaningRelations(int offset,
			byte[] source) {
		int len = source[offset];
		boolean oneWord = false;
		if (len == Byte.MIN_VALUE) {
			oneWord = false;
			offset++;
			len = makeInt((byte) 0, (byte) 0, source[offset + 1],
					source[offset]);
			offset++;
		}
		if (len < 0) {
			oneWord = true;
			len = -len;
		}
	
		AbstractRelation<MeaningElement>[] result = new AbstractRelation[len];
		offset++;
		if (oneWord) {
			byte b0 = source[offset++];
			byte b1 = source[offset++];
			byte b2 = source[offset++];
			int word = makeInt((byte) 0, b2, b1, b0);
			for (int a = 0; a < result.length; a++) {
				byte rel = source[offset++];
				byte rel1 = source[offset++];
				result[a] = createRelation(word, rel, rel1);
			}
		} else {
			for (int a = 0; a < result.length; a++) {
				byte b0 = source[offset++];
				byte b1 = source[offset++];
				byte b2 = source[offset++];
				int word = makeInt((byte) 0, b2, b1, b0);
				byte rel = source[offset++];
				byte rel1 = source[offset++];
				result[a] = createRelation(word, rel, rel1);				
			}
		}
		return result;
	}
	
	static AbstractRelation<MeaningElement> createRelation(int word, byte rel, byte rel1) {
		int makeInt = makeInt((byte)0,(byte) 0, rel1,rel);
		if (makeInt>=MorphologicalRelation.MORPHOLOGICAL_OFFSET){
			return new MorphologicalRelation(null, word, makeInt);
		}
		return new SemanticRelation(null, word, makeInt);
	}

	public static byte[] encodeRelations(AbstractRelation<?>[] relations) {
		int w = -1;
		boolean customCase = false;
		boolean oneWord = true;
		if (relations.length >= Byte.MAX_VALUE) {
			oneWord = false;
			customCase = true;
		}
		for (int a = 0; a < relations.length; a++) {
			if (w != -1 && w != relations[a].conceptId) {
				oneWord = false;
			}
			if (w == -1) {
				w = relations[a].conceptId;
			}
		}
		if (relations.length == 0) {
			oneWord = false;
		}
		int delta = 0;
		if (oneWord) {
			delta = relations.length*2 + 3 + 1;
		} else {
			delta = relations.length * 5 + 1;
			if (customCase) {
				delta += 2;
			}
		}
		byte[] result = new byte[delta];
		CodingUtils.encodeRelations(relations, w, oneWord, result, 0, customCase);		
		return result;
	}

	public static void encodeRelations(AbstractRelation<?>[] relations, int w,
			boolean oneWord, byte[] target, int position, boolean customCase) {
		
		if (customCase) {
			target[position++] = Byte.MIN_VALUE;
			target[position++] = int0(relations.length);
			target[position++] = int1(relations.length);
			for (AbstractRelation<?> q : relations) {
				int word = q.conceptId;
				if (q.relation>Short.MAX_VALUE){
					throw new IllegalStateException();
				}
				target[position++] = int0(word);
				target[position++] = int1(word);
				target[position++] = int2(word);
				target[position++] = (byte) int0(q.relation);
				target[position++] = (byte) int1(q.relation);
			}
		} else {
			target[position++] = (byte) (oneWord ? -relations.length
					: relations.length);
			if (oneWord) {
				target[position++] = int0(w);
				target[position++] = int1(w);
				target[position++] = int2(w);
				for (AbstractRelation<?> q : relations) {
					if (q.relation>Short.MAX_VALUE){
						throw new IllegalStateException();
					}
					target[position++] = (byte) int0(q.relation);
					target[position++] = (byte) int1(q.relation);
				}
			} else {
				for (AbstractRelation<?> q : relations) {
					int word = q.conceptId;
					target[position++] = int0(word);
					target[position++] = int1(word);
					target[position++] = int2(word);
					if (q.relation>Short.MAX_VALUE){
						throw new IllegalStateException();
					}
					target[position++] = (byte) int0(q.relation);
					target[position++] = (byte) int1(q.relation);
				}
			}
		}
	}

	public static int length(AbstractRelation<?>[] relations) {
		int w = -1;
		boolean oneWord = true;
		boolean customCase = false;
		if (relations.length >= Byte.MAX_VALUE) {
			oneWord = false;
			customCase = true;
		}
		for (int a = 0; a < relations.length; a++) {
			if (w != -1 && w != relations[a].conceptId) {
				oneWord = false;
			}
			if (w == -1) {
				w = relations[a].conceptId;
			}
		}
		if (relations.length == 0) {
			oneWord = false;
		}
		int delta = 0;
		if (oneWord) {
			delta = relations.length*2 + 3 + 1;
		} else {
			delta = relations.length * 5 + 1;
			if (customCase) {
				delta += 2;
			}
		}
		return delta;
	}

}
