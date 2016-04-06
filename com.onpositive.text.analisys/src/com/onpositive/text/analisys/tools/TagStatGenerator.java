package com.onpositive.text.analisys.tools;

	import java.util.List;

import com.carrotsearch.hppc.ObjectArrayList;
import com.carrotsearch.hppc.ShortArrayList;
import com.carrotsearch.hppc.cursors.ObjectCursor;
import com.onpositive.semantic.wordnet.AbstractWordNet;
import com.onpositive.text.analysis.IToken;
import com.onpositive.text.analysis.lexic.WordFormToken;
import com.onpositive.text.analysis.syntax.SyntaxParser;

public class TagStatGenerator extends SyntaxParser {

	AbstractWordNet wordnet;
	
	public TagStatGenerator(AbstractWordNet wordnet) {
		super(wordnet);
		this.wordnet = wordnet;		
	}
	
	public List<IToken> parse(String str){
		setText(str);
		List<IToken> primitiveTokens = primitiveTokenizer.tokenize(str);
		List<IToken> lexicProcessed = lexicParsers.process(primitiveTokens);
		
		List<IToken> sentences = sentenceSplitter.split(lexicProcessed);
		
		return sentences;
	}
	
	public class TypedWord {
		public String word1;
		public short grammem1;
		public String word2;
		public short grammem2;
		
		public TypedWord(String w1, String w2, short g1, short g2) { 
			word1 = w1;
			word2 = w2;
			grammem1 = g1;
			grammem2 = g2;
		}
	}
	
	public void computeStat(IToken token, ObjectArrayList<short[]> grammemStat, ObjectArrayList<TypedWord> typedWordStat)  {		 
		ShortArrayList cresult = new ShortArrayList();
		ObjectArrayList<WordFormToken> cct = new ObjectArrayList<WordFormToken>();

		if (token.childrenCount() == 0) return;
		
		for (IToken ch : token.getChildren()) {
			if (!(ch instanceof WordFormToken) || ch.getConflicts().size() > 0 || ((WordFormToken)ch).getMeaningElements().length < 1) {
				if (cresult.size() > 0) {
					grammemStat.add(cresult.toArray());
					cresult.clear();
					
					if (cct.size() < 2) continue;
					
					WordFormToken f1 = null, f2 = cct.get(0);
					for (ObjectCursor<WordFormToken> wft : cct) {
						if (wft.value == f2) continue;
						f1 = f2;
						f2 = wft.value;
						typedWordStat.add(new TypedWord(f1.getBasicForm(), f2.getBasicForm(), f1.getMeaningElements()[0].getGrammemCode(), f2.getMeaningElements()[0].getGrammemCode()));						
					}					
					cct.clear();
				}				
			} else {
				cresult.add(((WordFormToken) ch).getMeaningElements()[0].getGrammemCode());
				cct.add((WordFormToken)ch);
			}
		}
	}
	
	public void computeStats(List<IToken> sentences, ObjectArrayList<short[]> grammemStat, ObjectArrayList<TypedWord> typedWordStat) { 		
		for (IToken token : sentences)
			computeStat(token, grammemStat, typedWordStat);
	}
}
