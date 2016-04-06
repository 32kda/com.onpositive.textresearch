package com.onpositive.wikipedia.workbench;

import com.onpositive.semantic.wikipedia2.WikiEngine2;
import com.onpositive.wikipedia.workbench.words.primary.RelatedWordsIndex;
import com.onpositive.wikipedia.workbench.words.primary.WikiEngineProvider;
import com.onpositive.wikipedia.workbench.words.primary.WordDefinitions;

public class FirstSentenceIndex {

	public static void main(String[] args) {
		final WikiEngine2 instance = WikiEngineProvider.getInstance();
		final WordDefinitions index = instance.getIndex(WordDefinitions.class);
		System.out.println(index.words("Уилл Гир"));
		
		// int q=instance.getDocumentByTitle("Город").getIntId();
		// index.processDocument(instance, q);
		//// final ICategory category =
		// instance.getCategory("Государства_по_алфавиту");
		// for (IDocument d:category.getPages()){
		// System.out.println(d+":"+index.getById(d.getIntId()));
		// }
	}
}