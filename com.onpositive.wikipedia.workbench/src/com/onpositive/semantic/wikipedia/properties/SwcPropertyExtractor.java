package com.onpositive.semantic.wikipedia.properties;

import java.util.ArrayList;

import org.sweble.wikitext.engine.PageId;
import org.sweble.wikitext.engine.PageTitle;
import org.sweble.wikitext.engine.WtEngineImpl;
import org.sweble.wikitext.engine.config.WikiConfig;
import org.sweble.wikitext.engine.nodes.EngProcessedPage;
import org.sweble.wikitext.engine.utils.DefaultConfigEnWp;

public class SwcPropertyExtractor {

	WikiConfig config = DefaultConfigEnWp.generate();
	WtEngineImpl engine = new WtEngineImpl(config);


	public ArrayList<PropertyInfo> extractProperties(String title,
			String wikiText, String wikiUrl) throws Exception {
		wikiText = wikiText.replace("Файл:", "File:");
		wikiText = wikiText.replace("Изображение:", "File:");
		wikiText = wikiText.replace("Категория:", "Category:");
		PageTitle pageTitle = PageTitle.make(config, title);
		PageId pageId = new PageId(pageTitle, -1);
		EngProcessedPage cp = engine.postprocess(pageId, wikiText, null);
		PropertyGatheringVisitor propertyGatheringVisitor = new PropertyGatheringVisitor();
		propertyGatheringVisitor.go(cp);
		return propertyGatheringVisitor.infos;
	}
}
