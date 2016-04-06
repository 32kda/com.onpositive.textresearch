package com.onpositive.semantic.categorization.core;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.onpositive.semantic.search.core.ICategory;
import com.onpositive.semantic.search.core.IDocument;
import com.onpositive.semantic.search.core.ISearchEngine;
import com.onpositive.semantic.search.core.ISearchEngineFactory;
import com.onpositive.semantic.search.core.ISearchResultCallback;
import com.onpositive.semantic.search.core.ScoredTopic;
import com.onpositive.semantic.search.core.SearchMatch;
import com.onpositive.semantic.search.core.SearchRequest;
import com.onpositive.tstruct.CategoryIO;
import com.onpositive.tstruct.CategoryNode;

public class SearchSystem {

	private static final class WCallback implements ISearchResultCallback {
		private final ArrayList<SearchMatch> result;
		public int total;

		private WCallback(ArrayList<SearchMatch> result) {
			this.result = result;
		}

		@Override
		public void acceptDocument(SearchMatch d) {
			// if (result.size()<c1){
			result.add(d);
			// }
		}

		@Override
		public void done(int totals) {
			total = totals;
		}
	}

	public static final String DEFAULT_INDEX_FOLDER = "D:/se2";
	private static final String ENGINE_PROPERTIES = "engine.properties";
	private static final String ENGINE_CONFIG_DIR = "engineConfigDir";
	static Map<String, ISearchEngine> engines = new HashMap<String, ISearchEngine>();
	static {
		try {
			logStream = new PrintStream(new BufferedOutputStream(
					new FileOutputStream(new File(getRootIndexPath().toFile(),
							"system.log"), true)));
		} catch (FileNotFoundException e) {

			e.printStackTrace();
		}
		init();
	}
	
	public static void refreshBlackLists(){
		for (ISearchEngine q:engines.values()){
			q.clearBlackList();
		}
	}

	public static String getCategoryConfigPath() {
		Path rootIndexPath = SearchSystem.getRootIndexPath();
		String absolutePath = new File(rootIndexPath.toFile(), "cstruct.xml")
				.getAbsolutePath();
		return absolutePath;
	}

	static PrintStream logStream;

	public static Path getRootIndexPath() {
		String property = System.getProperty(ENGINE_CONFIG_DIR);
		if (property == null) {
			Properties properties = new Properties();
			try {
				properties.load(new FileInputStream(ENGINE_PROPERTIES));
			} catch (FileNotFoundException e) {
				System.out.println("No local property file found: "
						+ new File(ENGINE_PROPERTIES).getAbsolutePath());
			} catch (IOException e) {
				e.printStackTrace();
			}
			property = properties.getProperty(ENGINE_CONFIG_DIR);
			if (property == null) {
				property = DEFAULT_INDEX_FOLDER;
			}
			System.setProperty(ENGINE_CONFIG_DIR, property);
		}

		return Paths.get(property);
	}

	static CategoryNode cats;
	
	static boolean recalcHtml;

	public static boolean isRecalcHtml() {
		return recalcHtml;
	}

	public static void setRecalcHtml(boolean recalcHtml) {
		SearchSystem.recalcHtml = recalcHtml;
	}

	private static void init() {
		logStream.println("System init:" + new Date());
		long l0 = System.currentTimeMillis();
		fl = getRootIndexPath().toFile();
		File configuration = new File(fl, "engines.xml");

		try {
			cats = new CategoryNode();
			File f = new File(getCategoryConfigPath());
			if (f.exists()) {
				cats = CategoryIO.read(getCategoryConfigPath());
			}
			Document parse = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder()
					.parse(new FileInputStream(configuration));
			Element documentElement = parse.getDocumentElement();
			String attribute = documentElement.getAttribute("logResult");
			logResult = Boolean.parseBoolean(attribute);
			attribute = documentElement.getAttribute("logQueries");
			if (attribute.length() != 0) {
				logQueries = Boolean.parseBoolean(attribute);
			}
			attribute = documentElement.getAttribute("recalcHTML");
			if (attribute.length() != 0) {
				recalcHtml = Boolean.parseBoolean(attribute);
			}
			else{
				logQueries=true;
			}
			NodeList childNodes = documentElement.getChildNodes();
			for (int a = 0; a < childNodes.getLength(); a++) {
				Node item = childNodes.item(a);
				if (item instanceof Element) {
					Element el = (Element) item;
					if (el.getNodeName().equals("engine")) {
						String kind = el.getAttribute("kind");
						String dir = el.getAttribute("homedir");
						String state = el.getAttribute("state");
						String url = el.getAttribute("url");
						String kindOfEngine = el.getAttribute("role");
						initEngine(kind, dir, state, url, kindOfEngine);
					}
				}
			}
			for (ISearchEngine e : engines.values()) {
				e.postInit();
			}
			long l1 = System.currentTimeMillis();
			System.out.println("Engines init: " + (l1 - l0) + "ms ["
					+ engines.size() + " engines]");

			logStream.println("Engines init: " + (l1 - l0) + "ms ["
					+ engines.size() + " engines]");
		} catch (SAXException e) {
			throw new IllegalStateException(e);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	private static File fl;

	private static void initEngine(String kind, String dir, String state,
			String url, String kindOfEngine) {
		try {
			if (state != null && state.equals("stopped")) {
				return;
			}
			ISearchEngineFactory s = (ISearchEngineFactory) Class.forName(
					"com.onpositive.semantic.wikipedia2.WikiEngineFactory")
					.newInstance();
			SubSystemConfiguration configuration = new SubSystemConfiguration(
					kind);
			configuration.properties.put("dir",
					new File(fl, dir).getAbsolutePath());
			configuration.properties.put("state", state);
			configuration.properties.put("url", url);
			configuration.properties.put("kind", kindOfEngine);
			ISearchEngine create = s.create(configuration);
			engines.put(dir, create);
			System.out.println("Register engine: " + kind);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static SearchResult search(String category, String place,
			String keywords, Integer count, Integer offset, String date, boolean onlyEvents, boolean showBlackList, String sortMode, int maxRelevancy) {
		SearchRequest request = new SearchRequest();
		request.setOnlyEvents(true);
//		request.setMaxRelevancy(1200);
		request.setCategory(category);
		request.setOffset(offset == null ? 0 : offset);
		request.setLocation(place);
		request.setDate(date);
		request.setMaxRelevancy(maxRelevancy);
		request.setOnlyEvents(onlyEvents);
		request.setShowBlackList(showBlackList);
		request.setSearchMode(sortMode);
		request.setKeyword(keywords);
		if (count == null) {
			count = 20;
		}
		request.setCount(count);
		request.setUseQuickSearch(false);
		return search(request);
	}

	static boolean logResult ;
	static boolean logQueries;

	public static synchronized SearchResult search(SearchRequest request) {
		try {
			long l0 = System.currentTimeMillis();
			if (logQueries) {
				request.print(logStream);
			}
			final ArrayList<SearchMatch> result = new ArrayList<SearchMatch>();
			int totalCount = 0;
			totalCount = queryEngines(request, result, totalCount);
			afterSort(request, result);
			List<SearchMatch> subList = result.subList(
					request.getOffset(),
					Math.min(result.size(),
							request.getOffset() + request.getCount()));
			SearchMatch[] array = subList.toArray(new SearchMatch[subList
					.size()]);
			long l1 = System.currentTimeMillis();
			if (logQueries) {
				logStream.println("#" + System.identityHashCode(request) + " "
						+ (l1 - l0) + " ms" + " total:" + totalCount
						+ " repored:" + array.length);
			}
			if (logResult){
				for (int a=0;a<subList.size();a++){
					logStream.println("#" + System.identityHashCode(request)+" #"+a+" "+subList.get(a));
				}
			}
			if (logQueries||logResult){
				logStream.flush();
			}
			return new SearchResult(totalCount, array,"#"+System.identityHashCode(request));
		} catch (Exception e) {
			e.printStackTrace(logStream);
			e.printStackTrace();
		}
		return new SearchResult(0, new SearchMatch[0],"#"+System.identityHashCode(request));
	}

	static int queryEngines(SearchRequest request,
			final ArrayList<SearchMatch> result, int totalCount) {
		for (ISearchEngine eng : engines.values()) {
			if (request.getEngines() != null) {
				if (!request.getEngines().contains(eng.id())) {
					continue;
				}
			}
			WCallback callback = new WCallback(result);
			eng.search(request, callback);
			totalCount += callback.total;
		}
		return totalCount;
	}

	static void afterSort(SearchRequest request,
			final ArrayList<SearchMatch> result) {
		if (request.getSearchMode() != null
				&& request.getSearchMode()
						.equals(SearchRequest.MODE_POPULARITY)) {
			Collections.sort(result, new Comparator<SearchMatch>() {

				@Override
				public int compare(SearchMatch o1, SearchMatch o2) {
					if (o1.getRank() == o2.getRank()) {
						return o1.getRelevancy() - o2.getRelevancy();
					}
					return o2.getDocument().getPopularity()
							- o1.getDocument().getPopularity();
				}
			});
		} else if (request.getSearchMode() != null
				&& request.getSearchMode().equals(SearchRequest.MODE_MIXED)) {
			Collections.sort(result, new Comparator<SearchMatch>() {

				@Override
				public int compare(SearchMatch o1, SearchMatch o2) {
					if (o1.getRank() == o2.getRank()) {
						return o1.getRelevancy() - o2.getRelevancy();
					}
					return o1.getRelevancy() - o2.getRelevancy();
				}
			});
		} else {
			Collections.sort(result, new Comparator<SearchMatch>() {

				@Override
				public int compare(SearchMatch o1, SearchMatch o2) {

					return o1.getRelevancy() - o2.getRelevancy();

				}
			});
		}
	}

	public static List<ICategory> getCategories(String string,
			String filterEngine) {
		ArrayList<ICategory> cats = new ArrayList<ICategory>();
		for (ISearchEngine e : engines.values()) {
			if (filterEngine != null && !filterEngine.equals(e.id())) {
				continue;
			}
			ICategory[] categories2 = e.getCategories(string);
			if (categories2 != null) {
				cats.addAll(Arrays.asList(categories2));
			}
		}
		return cats;
	}

	public static List<ICategory> getCategoriesByName(String string,
			String filterEngine) {
		ArrayList<ICategory> cats = new ArrayList<ICategory>();
		for (ISearchEngine e : engines.values()) {
			if (filterEngine != null && !filterEngine.equals(e.id())) {
				continue;
			}
			ICategory[] categories2 = e.getCategoriesByName(string);
			if (categories2 != null) {
				cats.addAll(Arrays.asList(categories2));
			}
		}
		return cats;
	}

	public static IDocument document(String id) {
		if (id==null){
			return null;
		}
		int indexOf = id.indexOf('/');
		String engineId = id.substring(0, indexOf);
		String documentId = id.substring(indexOf + 1);
		for (ISearchEngine e : engines.values()) {
			if (e.id().equals(engineId)) {
				return e.getDocument(documentId);
			}
		}
		return null;
	}

	public static Collection<ISearchEngine> getEngines() {
		return engines.values();
	}

	public static ISearchEngine getEngine(String kind) {
		return engines.get(kind);
	}

	public static List getArticlesStartingWith(String filter,
			String filterEngine) {
		ArrayList<IDocument> cats = new ArrayList<IDocument>();
		for (ISearchEngine e : engines.values()) {
			if (filterEngine != null && !filterEngine.equals(e.id())) {
				continue;
			}
			IDocument[] categories2 = e.getDocumentsStartingWith(filter);
			if (categories2 != null) {
				cats.addAll(Arrays.asList(categories2));
			}
		}
		return cats;
	}

	public static void reinit() {
		logStream.println("System reinit scheduled:" + new Date());
		cats = new CategoryNode();
		File f = new File(getCategoryConfigPath());
		if (f.exists()) {
			cats = CategoryIO.read(getCategoryConfigPath());
		}
		for (ISearchEngine e : engines.values()) {
			e.learn(null, null);
		}
		logStream.println("System reinit completed:" + new Date());
	}

	public static CategoryNode getSchema() {
		return cats;
	}

	public static ScoredTopic[] matchTopics(String text) {
		ArrayList<ScoredTopic> ss = new ArrayList<ScoredTopic>();
		for (ISearchEngine e : engines.values()) {
			ScoredTopic[] matchTopics = e.matchTopics(text);
			ss.addAll(Arrays.asList(matchTopics));
		}
		return ss.toArray(new ScoredTopic[ss.size()]);
	}
}
