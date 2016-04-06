package com.onpositive.semantic.wikipedia2.locations;

import java.util.ArrayList;
import java.util.List;
import com.onpositive.semantic.search.core.ICategory;
import com.onpositive.semantic.wikipedia2.WikiDoc;
import com.onpositive.semantic.wikipedia2.WikiEngine2;

public abstract class AbstractDocumentBasedBuilder {
	private WikiEngine2 engine;
	
	public AbstractDocumentBasedBuilder(WikiEngine2 engine) {
		this.engine = engine;
	}
	
	protected void processDocuments() {
		int[] documentIDs = engine.getDocumentIDs();
		
		int counter = 0;
		for (int documentId : documentIDs) {
			String documentTitle = engine.getPageTitles().get(documentId);
			if (documentTitle != null && !documentTitle.startsWith("���������:")) {
				startProcessingDocument(documentId);
				try {
					WikiDoc document = new WikiDoc(engine, documentId);
					
					String shortAbstract = document.getPlainTextAbstract();
					
					String content = document.getOriginalMarkup();
					
					encounteredDocumentTitle(documentId, documentTitle);
					
					if (shortAbstract != null && shortAbstract.length() > 0 && content != null && content.length() > 0
							&& !shortAbstract.contains("���������")) {
						ICategory[] categories = document.getCategories();
						List<String> categoryTitles = new ArrayList<String>();
						if (categories != null) {
							for (ICategory category : categories) {
								String categoryTitle = category.getTitle();
								if (categoryTitle != null) {
									categoryTitles.add(categoryTitle);
								}
							}
						}
						
						encouneredDocumentCategories(documentId, categoryTitles);
					
						encounteredDocumentAbstract(documentId, shortAbstract);
					
					
						//encounteredDocumentBody(documentId, content);
					}
				
				} catch (Throwable th) {
				}
				finally {
					finishedProcessingDocument(documentId);
				}
			}
			
			counter++;
			if (counter%100000 == 0) {
				System.out.println("Processed " + counter + " of " + documentIDs.length);
			}
		}
	}
	
	protected abstract void startProcessingDocument(int documentId);
	
	protected abstract void finishedProcessingDocument(int documentId);
	
	protected abstract void encounteredDocumentAbstract(int documentId, String abstractText);
	
	protected abstract void encounteredDocumentBody(int document, String text);
	
	protected abstract void encouneredDocumentCategories(int documentId, List<String> categories);
	
	protected abstract void encounteredDocumentTitle(int documentId, String title);
}