package com.onpositive.semantic.wikipedia.abstracts.svc;

import org.sweble.wikitext.engine.EngineException;
import org.sweble.wikitext.engine.PageId;
import org.sweble.wikitext.engine.PageTitle;
import org.sweble.wikitext.engine.WtEngineImpl;
import org.sweble.wikitext.engine.config.WikiConfig;
import org.sweble.wikitext.engine.nodes.EngProcessedPage;
import org.sweble.wikitext.engine.utils.DefaultConfigEnWp;
import org.sweble.wikitext.parser.parser.LinkTargetException;

public class SwcAbstractExtractor {

	WikiConfig config = DefaultConfigEnWp.generate();
	WtEngineImpl engine = new WtEngineImpl(config);

	public String extractPlainText(String title, String wikiText)
			throws LinkTargetException, EngineException {
		final int wrapCol = 80;
		PageTitle pageTitle = PageTitle.make(config, title);
		PageId pageId = new PageId(pageTitle, -1);
		EngProcessedPage cp = engine.postprocess(pageId, wikiText, null);
		TextConverter p = new TextConverter(config, wrapCol);
		return (String) p.go(cp.getPage());
	}

	public String extractHTML(String title, String wikiText,String wikiUrl)
			throws Exception {
		wikiText=wikiText.replace("Файл:", "File:");
		wikiText=wikiText.replace("Изображение:", "File:");
		wikiText=wikiText.replace("Категория:", "Category:");
		
		wikiText=InitialCleanup.initialCleanup(wikiText).trim();
		if (wikiText.startsWith(" —")){
			wikiText=title+wikiText;
		}
		PageTitle pageTitle = PageTitle.make(config, title);
		PageId pageId = new PageId(pageTitle, -1);
		EngProcessedPage cp = engine.postprocess(pageId, wikiText, null);
		String ourHtml = HtmlRenderer.print(new MyRendererCallback(wikiUrl), config,
				pageTitle, cp.getPage());
		
		return ourHtml;
	}	
	
	
	
}