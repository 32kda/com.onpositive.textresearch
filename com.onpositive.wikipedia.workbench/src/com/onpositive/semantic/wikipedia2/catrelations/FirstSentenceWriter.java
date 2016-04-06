package com.onpositive.semantic.wikipedia2.catrelations;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import com.onpositive.semantic.wikipedia2.WikiDoc;
import com.onpositive.semantic.wikipedia2.WikiEngine2;
import com.onpositive.semantic.wikipedia2.services.LexicRelated;
import com.onpositive.semantic.wordnet.Grammem;
import com.onpositive.semantic.wordnet.TextElement;
import com.onpositive.semantic.wordnet.WordNetProvider;
import com.onpositive.text.analysis.IToken;
import com.onpositive.text.analysis.ParserComposition;
import com.onpositive.text.analysis.lexic.SymbolToken;
import com.onpositive.text.analysis.lexic.WordFormParser;
import com.onpositive.text.analysis.lexic.WordFormToken;

public class FirstSentenceWriter extends LexicRelated {

	
	private ParserComposition createParsers;

	public FirstSentenceWriter(WikiEngine2 eng) {
		super(WordNetProvider.getInstance());
		//multiWordRegistry = new MultiWordRegistry(eng);
		createParsers = new ParserComposition(new WordFormParser(
				WordNetProvider.getInstance()) {

			
		});
	}

	public String getWords(WikiDoc doc) {
		String plainTextAbstract = doc.getPlainTextAbstract();
		BufferedReader rs = new BufferedReader(new StringReader(
				plainTextAbstract));
		while (true) {
			try {
				String readLine = rs.readLine();
				if (readLine == null) {
					break;
				}
				if (!readLine.trim().isEmpty()) {
					readLine = readLine.trim();
					
					if (!readLine.isEmpty()&&readLine.length()>10){
						readLine=readLine.trim();
						StringBuilder bld=new StringBuilder();
						boolean inParen=false;
						int ddCount=0;
						int ppos=0;
						for (int a=0;a<readLine.length();a++){
							char charAt = readLine.charAt(a);
							if (charAt=='('){
								inParen=true;
								continue;
							}
							if (charAt==')'){
								inParen=false;
								continue;
							}
							bld.append(charAt);
							if (!inParen){	
								if (charAt=='.'&&bld.length()>20){
									break;
								}
								if (charAt=='—'){
									ddCount++;
									ppos=bld.length();
								}
							}
						}
						if (ddCount==1){
							return bld.substring(ppos);
						}
						return bld.toString();
					}					
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public static void main(String[] args) {
		WikiEngine2 eng = new WikiEngine2("D:\\se2\\ruwiki");
		int[] notRedirectDocumentIDs = eng.getNotRedirectDocumentIDs();
		try {
			PrintStream st = new PrintStream(new FileOutputStream(new File(
					eng.getLocation(), "fs.txt")));
			FirstSentenceWriter primaryWordsDeterminer = new FirstSentenceWriter(
					eng);
			for (int r : notRedirectDocumentIDs) {
				WikiDoc wikiDoc = new WikiDoc(eng, r);
				st.println(wikiDoc.getTitle() + "->"
						+ primaryWordsDeterminer.getWords(wikiDoc));
			}
			st.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}