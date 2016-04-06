package com.onpositive.text.analisys.tests;

import java.io.BufferedInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import com.onpositive.semantic.wordnet.Grammem;
import com.onpositive.text.analisys.tests.euristics.SimplifiedToken;

public class SimpleTextTokensLoader extends AbstractTokensLoader{
	
	
	public SimpleTextTokensLoader() {
		Scanner scanner = new Scanner(new BufferedInputStream(getClass().getResourceAsStream("testset.txt")));
		List<String> lines = new ArrayList<String>();
		while (scanner.hasNext()) {
			String nextLine = scanner.nextLine().trim();
			if (!nextLine.isEmpty()) {
				lines.add(nextLine);
			}
		}
		scanner.close();
		StringBuilder textBuilder = new StringBuilder();
		String[] array = lines.toArray(new String[0]);
		for (int i = 0; i < array.length - 1; i+=2) {
			List<SimplifiedToken> curChain = new ArrayList<SimplifiedToken>();
			textBuilder.append(array[i].trim());
			if (textBuilder.length() > 0 && Character.isLetter(textBuilder.charAt(textBuilder.length() - 1))) {
				textBuilder.append('.');	
			}
			String[] words = array[i].split("\\s");
			String[] grammems = array[i+1].split("\\s");
			for (int j = 0; j < grammems.length && j < words.length ; j++) {
				Grammem gr = Grammem.get(grammems[j]);
				if (gr == null) {
					System.err.println("Invalid grammem code: " + grammems[j]);
				}
				curChain.add(new SimplifiedToken(words[j], Collections.singletonList(gr)));
			}
			chains.add(curChain);
		}
		initialText = textBuilder.toString();
	}
	
	public static void main(String[] args) {
		SimpleTextTokensLoader loader = new SimpleTextTokensLoader();
		System.out.println(loader.getInitialText());
	}
	
}
