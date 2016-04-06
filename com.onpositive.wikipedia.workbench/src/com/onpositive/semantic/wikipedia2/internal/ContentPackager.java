package com.onpositive.semantic.wikipedia2.internal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.IntIntOpenHashMapSerialable;
import com.carrotsearch.hppc.IntObjectOpenHashMapSerialzable;
import com.onpositive.semantic.wikipedia2.WikiEngine2;
import com.onpositive.semantic.words3.hds.BidirectionalIntToIntArrayMap;
import com.onpositive.semantic.words3.hds.Renumberer;
import com.onpositive.semantic.words3.hds.StringVocabulary;
import com.onpositive.wikipedia.dumps.builder.ISqlConsumer;
import com.onpositive.wikipedia.dumps.builder.WikiSqlLoader;
import com.onpositive.wikipedia.dumps.builder.XMLPageParser;

public class ContentPackager {

	

	public static final class GraphWriter implements ISqlConsumer {
		private final IntObjectOpenHashMapSerialzable<IntArrayList> pageMap;
		private final StringVocabulary cats;
		private final StringVocabulary pages;
		private final IntObjectOpenHashMapSerialzable<IntArrayList> catMap;
		private final Renumberer pageNumberer;
		private final Renumberer catNumberer;
		int visitCount = 0;
		private IntIntOpenHashMapSerialable map;

		public GraphWriter(IntObjectOpenHashMapSerialzable<IntArrayList> pageMap,
				StringVocabulary cats,
				IntObjectOpenHashMapSerialzable<IntArrayList> catMap,
				Renumberer pageNumberer, Renumberer catNumberer,StringVocabulary pages, IntIntOpenHashMapSerialable map) {
			this.pageMap = pageMap;
			this.pages=pages;
			this.cats = cats;
			this.catMap = catMap;
			this.map=map;
			this.pageNumberer = pageNumberer;
			this.catNumberer = catNumberer;
		}

		@Override
		public void consume(Object[] data) {
			visitCount++;
			if (visitCount % 1000 == 0) {
				System.out.println(visitCount);
			}
			String type = (String) data[6];
			Integer from = (Integer) data[0];
			String to = (String) data[1];
			
			
			
			boolean equals = type.equals("page")||type.length()==0;
			if (equals){
				from=pageNumberer.getIndex(from);	
			}
			else{
				int c=catNumberer.getIndex(from);
				if (c==Integer.MIN_VALUE){
					c=catNumberer.getIndex(-from);					
				}
				from=c;
			}
			/*String naming=pagesMap.get(from);
			String naming2=cats.get(from);*/
			if(from==Integer.MIN_VALUE){
				return;
				//System.out.println("A");
			}
			/*String categoryTitle = wikiEngine.getCategoryTitle(from);
			if (categoryTitle == null || true) {
				String categoryTitle1 = wikiEngine.getPageTitle(from);
				if (categoryTitle1 != null) {

					if (type.equals("subcat")
							&& categoryTitle1.startsWith("���������:")) {
						from = wikiEngine.getCategoryId(categoryTitle1
								.substring("���������:".length()).replace(
										' ', '_'));
						if (from == -1) {
							return;
						}
					}
				} else {
					return;
				}
			}*/
			if (type.equals("page")) {
				int parentCategory = cats.get(to);
				if (parentCategory == Integer.MIN_VALUE) {
					return;
				}
				
				//System.out.println(cats.get(parentCategory)+"->"+pages.get(from));
				IntArrayList object = pageMap.get(parentCategory);
				if (object == null) {
					IntArrayList ll = new IntArrayList();
					pageMap.put(parentCategory, ll);
					object = ll;
					// pageMap.put(pageId, from);
				}
				object.add(from);

			}
			if (type.equals("subcat")) {
				
				int parentCat = cats.get(to);
				//System.out.println(cats.get(parentCat)+"->"+cats.get(from));
				if (parentCat == Integer.MIN_VALUE) {
					return;
				}
				
				IntArrayList object = catMap.get(parentCat);
				if (object == null) {
					IntArrayList ll = new IntArrayList();
					catMap.put(parentCat, ll);
					object = ll;

				}
				object.add(from);
			}
		}
	}

	public static void checkContent(File fl) {
		File contentCatalog = new File(fl, WikiEngine2.ARTICLE_CATALOG);
		File pageNumberer = new File(fl, WikiEngine2.PAGE_RENUMBERER);
		File titlesList = new File(fl, WikiEngine2.PAGES_TITLES);
		Renumberer pagesRenumberer = null;
		Renumberer catsRenumberer = null;
		File categoryTitles = new File(fl, WikiEngine2.CATS_TITLES);
		File categoryRenumb = new File(fl, WikiEngine2.CATS_RENUMBERER);
		File pageGraph = new File(fl, WikiEngine2.PAGES_GRAPH);
		File catsGraph = new File(fl, WikiEngine2.CATS_GRAPH);
		if (!contentCatalog.exists() || !pageNumberer.exists()
				|| !titlesList.exists()||!categoryTitles.exists() || !categoryRenumb.exists()||
				!pageGraph.exists()||!catsGraph.exists()) {
			StringVocabulary catVocab=new StringVocabulary(600000);
			catsRenumberer=new Renumberer();
			IntIntOpenHashMapSerialable map=new IntIntOpenHashMapSerialable();
			pagesRenumberer = createContentMap(fl, contentCatalog,
					pageNumberer, titlesList,catVocab,catsRenumberer);
			
			catsRenumberer=createCategoryMap(fl, catsRenumberer, catVocab,map);
			if (pagesRenumberer == null) {
				try {
					pagesRenumberer = new Renumberer(pageNumberer);
				} catch (IOException e) {
					throw new IllegalStateException();
				}
			}
			if (catsRenumberer == null) {
				try {
					catsRenumberer = new Renumberer(categoryRenumb);
				} catch (IOException e) {
					throw new IllegalStateException();
				}
			}
			try{
			StringVocabulary cats=new StringVocabulary(categoryTitles);
			StringVocabulary pages=new StringVocabulary(titlesList);			
			buildCategoryGraph(fl, pages, cats, pagesRenumberer, catsRenumberer,map);
			}catch (IOException e) {
				throw new IllegalStateException(e);
			}
		}
		
		
		
		/*
		 * File Catalog = new File(fl, "category.list");
		 * 
		 * if (!contentCatalog.exists()||!titlesList.exists()) {
		 * StringVocabulary catMap=new StringVocabulary(); createCategoryMap(fl,
		 * contentCatalog,catMap); createStringVocabulary(fl,
		 * titlesList,catMap); try { DataOutputStream stream = new
		 * DataOutputStream( new BufferedOutputStream(new
		 * FileOutputStream(contentCatalog))); catMap.save(stream);
		 * stream.close(); } catch (Exception e) { e.printStackTrace(); throw
		 * new IllegalStateException(e); } }
		 * 
		 * 
		 * 
		 * File rtl = new File(fl, "redirects.list"); if (!rtl.exists()){
		 * 
		 * }
		 */
	}

	/*
	 * private static void createStringVocabulary(File fl, File file,
	 * StringVocabulary catMap) { String[] list = fl.list(); for (String s :
	 * list) { if (s.endsWith("articles.xml")) { buildStringVocabulary(new
	 * File(fl, s), file,catMap); } } }
	 * 
	 * private static void buildStringVocabulary(File file, File file2, final
	 * StringVocabulary catMap) { XMLPageParser pp = new XMLPageParser(); try {
	 * final StringVocabulary tm = new StringVocabulary(1000000); IPageVisitor
	 * visitor = new IPageVisitor() {
	 * 
	 * @Override public void visit(PageModel model) { if (model.getNamespace()
	 * == 0) { tm.addPage(model.getPageId(), model.getTitle()); } if
	 * (model.getNamespace() == 14&&model.getTitle().startsWith("���������:")) {
	 * String replace =
	 * model.getTitle().substring("���������:".length()).replace(' ', '_'); int
	 * i = catMap.get(replace); if (i==0){ catMap.addPage(model.getPageId(),
	 * replace); } tm.addPage(model.getPageId(), model.getTitle()); } } };
	 * pp.visitContent(new BufferedReader(new InputStreamReader( new
	 * FileInputStream(file), "UTF-8")), visitor); tm.save(new
	 * DataOutputStream(new BufferedOutputStream( new
	 * FileOutputStream(file2)))); } catch (FileNotFoundException e) { // TODO
	 * Auto-generated catch block e.printStackTrace(); } catch (IOException e) {
	 * // TODO Auto-generated catch block e.printStackTrace(); } }
	 */

	private static Renumberer createCategoryMap(File fl,Renumberer r,StringVocabulary tt,IntIntOpenHashMapSerialable map) {
		String[] list = fl.list();
		for (String s : list) {
			if (s.endsWith("category.sql")) {
				return buildCategoryMap(new File(fl, s), r, tt, map);
			}
		}
		return null;
	}

	private static Renumberer buildCategoryMap(File categoriesSql,final Renumberer renumberer,final StringVocabulary tt,final IntIntOpenHashMapSerialable map) {				
		ISqlConsumer consumer = new ISqlConsumer() {

			int a = 0;

			@Override
			public void consume(Object[] data) {
				Integer id = (Integer) data[0];
				if(id==690070){
					System.out.println("a");
				}
				String title = (String) data[1];
				int i = tt.get(title);
				if (i!=Integer.MIN_VALUE){
					map.put(id, i);
					return;
				}
				tt.allwaysStore(title);
				renumberer.add(-id);
				a++;
				if (a % 1000 == 0) {
					System.out.println(a + " category definitions processed");
				}
			}
		};
		try {
			new WikiSqlLoader().parse(new BufferedReader(new InputStreamReader(
					new FileInputStream(categoriesSql), "UTF-8")), consumer);
			tt.store(new File(categoriesSql.getParent(),
					WikiEngine2.CATS_TITLES));
			renumberer.store(new File(categoriesSql.getParent(),
					WikiEngine2.CATS_RENUMBERER));
			return renumberer;
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	private static Renumberer createContentMap(File fl, File contentCatalog,
			File pageNumberer, File titlesList, StringVocabulary catVocab, Renumberer catsRenumberer) {
		String[] list = fl.list();
		for (String s : list) {
			if (s.endsWith("articles.xml")) {
				return buildMap(new File(fl, s),catVocab,catsRenumberer);
			}
		}
		return null;
	}

	

	private static Renumberer buildMap(File file2, StringVocabulary catVocab, Renumberer catsRenumberer) {
		XMLPageParser pp = new XMLPageParser();
		try {
			ContentTable visitor = new ContentTable(file2.getParent());
			visitor.catRenumb=catsRenumberer;
			visitor.catVocub=catVocab;
			pp.visitContent(new BufferedReader(new InputStreamReader(
					new FileInputStream(file2), "UTF-8")), visitor);
			visitor.close();
			return visitor.numberer;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/*
	 * public static void checkState(String dir, WikiEngine2 wikiEngine) { File
	 * fl = new File(dir); File file = new File(fl, "category.graph"); if
	 * (!file.exists()) { createCategoryGraph(fl, file, wikiEngine); } }
	 */
	/*
	 * private static void createCategoryGraph(File fl, File file, WikiEngine2
	 * wikiEngine) { String[] list = fl.list(); for (String s : list) { if
	 * (s.endsWith("links.sql")) { buildCategoryGraph(new File(fl, s), fl,
	 * wikiEngine); } } }
	 */

	private static void buildCategoryGraph( File file2,final StringVocabulary pagesMap,
			final StringVocabulary cats,
			final Renumberer pageNumberer,final Renumberer catNumberer, final IntIntOpenHashMapSerialable map) {
		int i = pagesMap.get("Статьи‎");
		System.out.println(i);
		BidirectionalIntToIntArrayMap subCats = new BidirectionalIntToIntArrayMap();
		BidirectionalIntToIntArrayMap pages = new BidirectionalIntToIntArrayMap();
		final IntObjectOpenHashMapSerialzable<IntArrayList> pageMap = new IntObjectOpenHashMapSerialzable<IntArrayList>(
				1000 * 10000);
		final IntObjectOpenHashMapSerialzable<IntArrayList> catMap = new IntObjectOpenHashMapSerialzable<IntArrayList>(
				1000 * 10000);
		File file=null;
		for (String s:file2.list()){
			if (s.endsWith("links.sql")){
				if (!s.endsWith("langlinks.sql")){
					file=new File(file2,s);
				}
				//file=new File(file2,s);
			}
		}
		if (file==null){
			throw new IllegalStateException("Can not file links.sql");
		}
		ISqlConsumer consumer = new GraphWriter(pageMap, cats, catMap, pageNumberer, catNumberer, pagesMap,map);

		try {
			new WikiSqlLoader().parse(new BufferedReader(new InputStreamReader(
					new FileInputStream(file), "UTF-8")), consumer);
			for (int q : pageMap.keys().toArray()) {
				IntArrayList object = pageMap.get(q);
				int[] mm = object.toArray();
				pages.add(q, mm);

			}
			for (int q : catMap.keys().toArray()) {
				IntArrayList object = catMap.get(q);
				int[] mm = object.toArray();
				subCats.add((Integer) q, mm);
			}
			subCats.init();
			pages.init();
			/*int[] direct = pages.getDirect(pagesMap.get("�����"));
			for (int q:direct){
				System.out.println(cats.get(q));
			}
			direct = subCats.getDirect(pagesMap.get("�����"));
			for (int q:direct){
				System.out.println(cats.get(q));
			}*/
			subCats.store(new File(file2, WikiEngine2.CATS_GRAPH));
			pages.store(new File(file2, WikiEngine2.PAGES_GRAPH));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
