package com.onpositive.semantic.wikipedia2;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.lang.WordUtils;

import com.carrotsearch.hppc.IntIntOpenHashMapSerialable;
import com.carrotsearch.hppc.IntOpenHashSet;
import com.onpositive.semantic.categorization.core.SearchSystem;
import com.onpositive.semantic.categorization.manual.ICategorization;
import com.onpositive.semantic.search.core.ICategory;
import com.onpositive.semantic.search.core.IDocument;
import com.onpositive.semantic.search.core.ILearningCallback;
import com.onpositive.semantic.search.core.ISearchEngine;
import com.onpositive.semantic.search.core.ISearchResultCallback;
import com.onpositive.semantic.search.core.ScoredTopic;
import com.onpositive.semantic.search.core.SearchRequest;
import com.onpositive.semantic.wikipedia2.catrelations.ThemeIndex;
import com.onpositive.semantic.wikipedia2.fulltext.TextSearchIndex;
import com.onpositive.semantic.wikipedia2.internal.ContentAccess;
import com.onpositive.semantic.wikipedia2.internal.ContentPackager;
import com.onpositive.semantic.wikipedia2.internal.TextsAbstractIndex;
import com.onpositive.semantic.wikipedia2.search.WikiSearchEngine;
import com.onpositive.semantic.wikipedia2.services.RedirectsMap;
import com.onpositive.semantic.words3.hds.BidirectionalIntToIntArrayMap;
import com.onpositive.semantic.words3.hds.Renumberer;
import com.onpositive.semantic.words3.hds.StringVocabulary;

public class WikiEngine2 implements ISearchEngine{

	public static final String PAGE_RENUMBERER="pages.renumb";
	public static final String CATS_RENUMBERER="categories.renumb";
	public static final String CATS_GRAPH="categories.graph2";
	public static final String PAGES_GRAPH="pages.graph2";
	public static final String PAGES_TITLES="pages.titles";
	public static final String CATS_TITLES="categories.titles";
	public static final String ARTICLE_CONTENT="plainText.content";
	public static final String ARTICLE_CATALOG="plainText.catalog";
	protected Renumberer pageRenumberer;
	protected Renumberer categoryRenumberer;
	protected BidirectionalIntToIntArrayMap pageToParentCategories;
	protected BidirectionalIntToIntArrayMap categoryToParentCategories;
	protected StringVocabulary pageTitles;
	protected StringVocabulary categoryTitles;
	protected ContentAccess plainTextAccess;
	private String url;
	protected HashMap<Class<? extends WikiEngineService>, Object>instances;
	
	private File fileRoot;
	private String id;
	private String kind="ruwiki";
	private String state;
	int[] nri=null;
	private ThemeIndex themeIndex;
	WikiSearchEngine searchEngine=new WikiSearchEngine(this);

	private TextsAbstractIndex textsAbstractIndex;

	private TextSearchIndex textSearch;
	static WikiEngine2 vocabEngine;

	IntOpenHashSet blackListed;

	public <T extends WikiEngineService>T getIndex(Class<T> class1) {
		if (instances==null){
			instances=new HashMap<Class<? extends WikiEngineService>, Object>();
		}
		if (instances.containsKey(class1)){
			return class1.cast(instances.get(class1));
		}
		try{
		T newInstance = class1.getConstructor(WikiEngine2.class).newInstance(this);
		instances.put(class1, newInstance);
		return newInstance; 
		}catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}
	@Override
	public String toString() {
		return id;
	}
	public final int[] getSubCategories(int id){
		int[] direct = getCategoryToParentCategories().getDirect(id);
		return direct;
	}
	public final int[] getPages(int catId){
		int[] direct = getPageToParentCategories().getDirect(catId);
		return direct;
	}
	public String getTextAbstract(int k){
		if (textsAbstractIndex==null){
		textsAbstractIndex = TextsAbstractIndex.AbstractIndexProvider.getInstance().get(this);
		}
		return textsAbstractIndex.getAbstract(k);
	}
	public Renumberer getPageRenumberer(){
		if (pageRenumberer==null){
			try{
			pageRenumberer=new Renumberer(new File(fileRoot,PAGE_RENUMBERER));
			}catch (IOException e) {
				throw new IllegalStateException(e);
			}
		}
		return pageRenumberer;
	}
	public Renumberer getCategoryRenumberer(){
		if (categoryRenumberer==null){
			try{
			categoryRenumberer=new Renumberer(new File(fileRoot,CATS_RENUMBERER));
			}catch (IOException e) {
				throw new IllegalStateException(e);
			}
		}
		return categoryRenumberer;
	}
	public BidirectionalIntToIntArrayMap getPageToParentCategories(){
		if (pageToParentCategories==null){
			try{
			pageToParentCategories=new BidirectionalIntToIntArrayMap(new File(fileRoot,PAGES_GRAPH));
			}catch (IOException e) {
				throw new IllegalStateException(e);
			}
		}
		return pageToParentCategories;
	}
	public BidirectionalIntToIntArrayMap getCategoryToParentCategories(){
		if (categoryToParentCategories==null){
			try{
				categoryToParentCategories=new BidirectionalIntToIntArrayMap(new File(fileRoot,CATS_GRAPH));
			}catch (IOException e) {
				throw new IllegalStateException(e);
			}
		}
		return categoryToParentCategories;
	}
	public StringVocabulary getPageTitles(){
		if (pageTitles==null){
			try{
				pageTitles=new StringVocabulary(new File(fileRoot,PAGES_TITLES));
			}catch (IOException e) {
				throw new IllegalStateException(e);
			}
		}
		return pageTitles;
	}
	public StringVocabulary getCategoryTitles(){
		if (categoryTitles==null){
			try{
				categoryTitles=new StringVocabulary(new File(fileRoot,CATS_TITLES));
			}catch (IOException e) {
				throw new IllegalStateException(e);
			}
		}
		return categoryTitles;
	}
	public ContentAccess getPlainTextAccess(){
		if (plainTextAccess==null){
			try{
				plainTextAccess=new ContentAccess(fileRoot);
			}catch (IOException e) {
				throw new IllegalStateException(e);
			}
		}
		return plainTextAccess;
	}


	public String getKind() {
		return kind;
	}

	public String getState() {
		return state;
	}

	public WikiEngine2(String directory){
		this.fileRoot=new File(directory);
		ContentPackager.checkContent(fileRoot);
		id=this.fileRoot.getName();
		loadAll();
	}
	public WikiEngine2(String dir, String state, String url,
			String kind) {
		this.fileRoot=new File(dir);
		ContentPackager.checkContent(fileRoot);
		id=this.fileRoot.getName();
		this.url=url;
		this.kind=kind;
		if (kind.equals("vocab")){
			vocabEngine=this;
		}
		this.state=state;
		loadAll();
	}

	private void loadAll() {
		long l0=System.currentTimeMillis();
		getPageRenumberer();
		getCategoryRenumberer();
		getPageToParentCategories();
		getCategoryToParentCategories();
		getPageTitles();
		getCategoryTitles();
		getPlainTextAccess();
		long l1=System.currentTimeMillis();
		System.out.println("Loading time:"+(l1-l0));
	}
	public String getLocation() {
		return fileRoot.getAbsolutePath();
	}
	public final String getPlainContent(int key) {
		return getPlainTextAccess().getPage(key);
	}
	public final int getPageId(String tm) {
		return getPageTitles().get(tm);
	}
	public int[] getDocumentIDs() {
		return pageRenumberer.getIds();
	}

	public String getDocumentAbstract(int pageId) {
		return TextsAbstractIndex.AbstractIndexProvider.getInstance().get(this).getAbstract(pageId);
	}
	public int[] getNotRedirectDocumentIDs() {
		if(nri!=null){
			return nri;
		}
		com.carrotsearch.hppc.IntArrayList ll=new com.carrotsearch.hppc.IntArrayList();
		for(int q:getDocumentIDs()){
			if (getIndex(RedirectsMap.class).isRedirect(q)){
				continue;
			}
			ll.add(q);
		}
		nri=ll.toArray();
		return nri;
	}

	public String id() {
		return id;
	}

	public String getUrl() {
		return url;
	}
	@Override
	public void search(SearchRequest request, ISearchResultCallback callback) {
		searchEngine.search(request,callback);
	}

	@Override
	public IDocument getDocument(String id) {
		return new WikiDoc(this, Integer.parseInt(id));		
	}

	@Override
	public ICategory[] getCategories(String startingWith) {
		startingWith=startingWith.replace(' ', '_');
		if (startingWith == null) {
			return new ICategory[0];
		}
		String[] array = categoryTitles.getAllKeys();
		ArrayList<ICategory> cs = new ArrayList<ICategory>();
		for (final String q : array) {
			if (q.startsWith(startingWith)) {
				final int string = categoryTitles.get(q);
				cs.add(new WikiCat( string,this));
			}
		}
		return cs.toArray(new ICategory[cs.size()]);		
	}

	@Override
	public ICategory[] getCategoriesByName(String startingWith) {
		startingWith=startingWith.replace(' ', '_');
		if (startingWith==null){
			return new ICategory[0];
		}
		int i = categoryTitles.get(startingWith);
		if (i >0) {
			return new ICategory[] { new WikiCat(i,this) };
		}
		return null;		
	}

	public ICategory getCategory(String startingWith) {
		if (startingWith==null){
			return null;
		}
		String str = startingWith.replace(' ', '_');
		int i = categoryTitles.get(str);
		if (i < 0) {
			str = WordUtils.capitalize(startingWith).replace(' ', '_'); //Try to capitalize all letters
			i = categoryTitles.get(str);
		}
		if (i < 0) {
			str = WordUtils.capitalize(startingWith.replace(' ', '_')); //Try to capitalize first letter
			i = categoryTitles.get(str);
		}
		if (i >= 0) {
			return new WikiCat(i,this) ;
		}
		return null;		
	}
	
	@Override
	public void learn(ICategorization manual, ILearningCallback callback) {
		blackListed=null;
		getThemeIndex().clean();
		searchEngine.clear();
	}

	@Override
	public void postInit() {
		
	}

	@Override
	public IDocument[] getDocumentsStartingWith(String filter) {
		return null;
	}

	@Override
	public ScoredTopic[] matchTopics(String text) {
		return null;
	}
	public ICategory getRootCategory() {
		if (getRole().equals("envocab")){
			return getCategoriesByName("Contents")[0];
		}
		if (getRole().equals("vocab")){
			return getCategoriesByName("Статьи")[0];			
		}
		return getCategoriesByName("Всё")[0];		
		
	}
	public ICategory getCategory(int q) {
		return new WikiCat(q, this);
	}
	
	public ThemeIndex getThemeIndex(){
		if (themeIndex==null){
			themeIndex=new ThemeIndex(this);
		}
		return themeIndex;		
	}
	public final int[] getDocumentCats(int iDocument) {
		int[] direct = getPageToParentCategories().getInverse(iDocument);
		return direct;
	}
	public IntIntOpenHashMapSerialable getRelevantDocs(String category) {
		return getThemeIndex().getAllDocs(category);
	}
	public int getCategoryId(String s) {
		return categoryTitles.get(s);		
	}
	
	public TextSearchIndex getTextSearchIndex(){
		if (textSearch==null){
			textSearch=new TextSearchIndex(this);
		}
		return textSearch;
	}
	void clearPop() {
		searchEngine.clearPopularity();
	}
	
	public boolean isBlackListed(int q) {
		if (blackListed==null){
			blackListed=new IntOpenHashSet();
			for (String s:SearchSystem.getSchema().getBlockedElemens()){
				int i = getPageTitles().get(s);
				if (i>0){
					blackListed.add(i);
				}
			}
		}
		return blackListed.contains(q);
	}
	@Override
	public void clearBlackList() {
		blackListed=null;
		searchEngine.clearPopularity();
	}
	public String getRole() {
		return kind;
	}
	public static WikiEngine2 getVocabEngine() {
		return vocabEngine;
	}
	public WikiDoc getDocumentByTitle(String string) {
		int i = getPageTitles().get(string);
		if (i>0){
			return new WikiDoc(this, i);
		}
		return null;
	}
	public WikiDoc getDocument(int q) {
		return new WikiDoc(this, q);
	}
}
