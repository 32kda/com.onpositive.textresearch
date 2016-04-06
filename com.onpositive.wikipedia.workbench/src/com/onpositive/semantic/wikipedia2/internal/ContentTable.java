package com.onpositive.semantic.wikipedia2.internal;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.onpositive.semantic.wikipedia2.WikiEngine2;
import com.onpositive.semantic.words3.hds.IntArrayList;
import com.onpositive.semantic.words3.hds.Renumberer;
import com.onpositive.semantic.words3.hds.StringVocabulary;
import com.onpositive.wikipedia.dumps.builder.IPageVisitor;
import com.onpositive.wikipedia.dumps.builder.PageModel;

public class ContentTable implements IPageVisitor {

	private static final String CATEGORY = "Категория:";
	protected DataOutputStream s;
	protected Renumberer numberer=new Renumberer();
	protected StringVocabulary vocubulary=new StringVocabulary(3000*1000);
	protected IntArrayList positions = new IntArrayList();
	protected int up = 0;
	File ps;

	public ContentTable(String path) throws IOException {
		ps = new File(path);
		if (path.contains("enwiki")){
			vocubulary=new StringVocabulary(15*1000*1000);
		}
		ps.mkdirs();
		s = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(
				new File(ps, WikiEngine2.ARTICLE_CONTENT))));
		positions.add(0);
	}

	public void close() throws IOException {
		s.close();
		positions.store(new File(ps, WikiEngine2.ARTICLE_CATALOG));
		numberer.store(new File(ps, WikiEngine2.PAGE_RENUMBERER));
		vocubulary.store(new File(ps, WikiEngine2.PAGES_TITLES));
	}

	long k = 0;
	int count;
	public Renumberer catRenumb;
	public StringVocabulary catVocub;

	protected void onPage(PageModel page) {
		if (page.getNamespace() != 0&&page.getNamespace()!=14) {
			return;
		}
		if (page.getNamespace()==14){
			//this is a category
			String title = page.getTitle();
			if (!title.startsWith(CATEGORY)){
				//throw new IllegalStateException();
				return;
			}
			title=title.substring(CATEGORY.length()).replace(' ', '_');
			int i = catVocub.get(title);
			if (i!=Integer.MIN_VALUE){
				throw new IllegalStateException();
			}
			catVocub.allwaysStore(title);
			catRenumb.add(page.getPageId());
			return;
		}
		if (count%1000==0){
			System.out.println(count+" pages processed"+numberer.size());
		}
		count++;
		if (k % 8 != 0) {
			throw new IllegalStateException();
		}
		long l = k / 8;
		if (l > ((long)Integer.MAX_VALUE)*2-1) {
			throw new IllegalStateException();
		}
		if (l>Integer.MAX_VALUE){
			l=-(l-Integer.MAX_VALUE);
		}
		positions.add((int) l);
		int pageId = page.getPageId();
		numberer.add(pageId);
		String title = page.getTitle();
		vocubulary.allwaysStore(title);
		up++;
		try {
			byte[] bytes = page.getText().getBytes("UTF-8");
			s.writeInt(bytes.length);
			s.write(bytes);
			int i = bytes.length + 4;
			int remaining=i%8;
			if (remaining!=0){
				remaining=8-remaining;
			}
			for (int a=0;a<remaining;a++){
				s.write(0);
			}
			k += i+remaining;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void visit(PageModel model) {
		onPage(model);
	}

}
