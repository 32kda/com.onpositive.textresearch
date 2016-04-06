package com.onpositive.semantic.wikipedia2.catrelations;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import com.onpositive.semantic.wikipedia2.WikiEngine2;
import com.onpositive.semantic.wikipedia2.fulltext.WordIndex;
import com.onpositive.semantic.wordnet.AbstractWordNet;
import com.onpositive.semantic.wordnet.TextElement;
import com.onpositive.semantic.wordnet.WordNetProvider;

public class SentenceLister {

	public static void main(String[] args) {
		WikiEngine2 eng = new WikiEngine2("D:\\se2\\ruwiki");
		AbstractWordNet instance = WordNetProvider.getInstance();
		int[] notRedirectDocumentIDs = eng.getNotRedirectDocumentIDs();
		int i = 0;
		for (int q : notRedirectDocumentIDs) {
			i++;
			if (i % 1000 == 0) {
				System.out.println(i);
			}
			if (i > 100000) {
				break;
			}
			String text = eng.getDocument(q).getPlainTextAbstract();
			StringReader rs = new StringReader(text);
			BufferedReader m = new BufferedReader(rs);
			while (true) {
				try {
					String readLine = m.readLine();
					if (readLine == null) {
						break;
					}
					String[] split = readLine.split("\\.");
					for (String sentence : split) {
						ArrayList<HashSet<TextElement>> elements2 = WordIndex
								.toWords(instance, sentence, null);
						for (HashSet<TextElement>sz:elements2){
							for (TextElement te:sz){
								
								if (te.getBasicForm().equals("взлетел")){
									System.out.println(sentence);
								}
							}
						}
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}
	}

}
