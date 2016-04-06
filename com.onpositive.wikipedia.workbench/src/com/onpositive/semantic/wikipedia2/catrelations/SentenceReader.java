package com.onpositive.semantic.wikipedia2.catrelations;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

import com.onpositive.semantic.wikipedia2.WikiEngine2;
import com.onpositive.semantic.wikipedia2.properties.parsing.MultiMap;
import com.onpositive.semantic.wikipedia2.services.LexicRelated;
import com.onpositive.semantic.wordnet.Grammem;
import com.onpositive.semantic.wordnet.WordNetProvider;
import com.onpositive.text.analysis.BasicCleaner;
import com.onpositive.text.analysis.IToken;
import com.onpositive.text.analysis.ParserComposition;
import com.onpositive.text.analysis.lexic.WordFormParser;
import com.onpositive.text.analysis.lexic.WordFormToken;
import com.onpositive.text.analysis.syntax.AdjectiveAdverbParser;
import com.onpositive.text.analysis.syntax.GenitiveChainParser;
import com.onpositive.text.analysis.syntax.ParticipleAttachingParser;
import com.onpositive.text.analysis.syntax.SyntaxToken;
import com.onpositive.text.analysis.syntax.UniformAdjectivesParser;
import com.onpositive.text.analysis.syntax.UniformAdverbParser;
import com.onpositive.text.analysis.syntax.VerbNameParser;
import com.onpositive.text.analysis.syntax.VerbNamePrepositionParser;

public class SentenceReader {

	public static class DefinitionParser extends LexicRelated {
		private ParserComposition createParsers;
		private ParserComposition createParsers2;

		public DefinitionParser(WikiEngine2 eng) {
			super(WordNetProvider.getInstance());
			createParsers = new ParserComposition(new WordFormParser(
					WordNetProvider.getInstance()));
			createParsers2 =createParsers(new Class[]{
					UniformAdverbParser.class,
					AdjectiveAdverbParser.class,		
					UniformAdjectivesParser.class,GenitiveChainParser.class,VerbNamePrepositionParser.class,VerbNameParser.class,ParticipleAttachingParser.class}, false);
		}

		WikiEngine2 engine;
		int matchCount = 0;
		int tmatchCount = 0;

		public LinkedHashSet<String> parse(String title, String def) {
			LinkedHashSet<String>str=new LinkedHashSet<String>();
			String sm="у́";
			def=def.replace((CharSequence)(""+sm.charAt(1)),"");
			List<IToken> parse = createParsers.parse(def);
			int index = 0;
			MultiMap<Integer, IToken>alternatives=MultiMap.withList();
			for (IToken tz:parse){
				alternatives.add(tz.getStartPosition(), tz);
			}
			List<IToken>comp=new ArrayList<IToken>();
			l2:for (Integer m:alternatives.keySet()){
				Collection<IToken> collection = alternatives.get(m);
				if (collection.size()>1){
					for (IToken z:collection){
						if (z instanceof WordFormToken){
							WordFormToken mm=(WordFormToken) z;
							if (mm.hasGrammem(Grammem.PartOfSpeech.PREP)){
								comp.add(mm);
								continue l2;
							}
							if (mm.hasGrammem(Grammem.PartOfSpeech.CONJ)){
								comp.add(mm);
								continue l2;
							}
						}
					}
					comp.addAll(collection);
				}
				else{
					comp.addAll(collection);
				}
			}
			createParsers2.setBaseTokens(parse);
			parse=createParsers2.process(comp);
			parse=new BasicCleaner().clean(parse);
			
			for (IToken t : parse) {
				if (t instanceof SyntaxToken) {
					WordFormToken mainWord = ((SyntaxToken) t).getMainWord();
					if (mainWord.hasGrammem(Grammem.PartOfSpeech.NOUN)){
						if (mainWord.hasGrammem(Grammem.Case.NOMN)||mainWord.hasGrammem(Grammem.Case.GENT)){
						str.add(mainWord.getBasicForm());
						}
					}
				}
				index++;
			}
			return str;
			//System.out.println(ns);
		}
		
	}

	public static void main(String[] args) {
		WikiEngine2 eng = new WikiEngine2("D:\\se2\\ruwiki");
		DefinitionParser definitionParser = new DefinitionParser(eng);
		
		try {
			PrintStream st = new PrintStream(new FileOutputStream(new File(
					eng.getLocation(), "fs2.txt")));
			List<String> readAllLines = Files.readAllLines(new File(eng
					.getLocation(), "fs.txt").toPath());
			for (String s : readAllLines) {
				int indexOf = s.indexOf("->");
				String def = s.substring(indexOf + 2).trim();
				String orignal = s.substring(0, indexOf);
				try{
				LinkedHashSet<String> parse = definitionParser.parse(orignal, def);
				st.println(orignal+"->"+parse);
				}catch (Exception e){
					e.printStackTrace();
				}
			}
			st.println();
			System.out.println(definitionParser.matchCount + ":"
					+ definitionParser.tmatchCount);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
