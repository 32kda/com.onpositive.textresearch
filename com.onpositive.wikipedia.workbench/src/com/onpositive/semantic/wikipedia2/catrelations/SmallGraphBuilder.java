package com.onpositive.semantic.wikipedia2.catrelations;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.onpositive.semantic.search.core.ICategory;
import com.onpositive.semantic.wikipedia2.WikiEngine2;
import com.onpositive.semantic.wikipedia2.catrelations.TitleModel.WordModel;
import com.onpositive.semantic.wikipedia2.properties.parsing.MultiMap;
import com.onpositive.semantic.wordnet.Grammem;
import com.onpositive.semantic.wordnet.TextElement;
import com.onpositive.semantic.wordnet.WordNetProvider;
import com.onpositive.semantic.wordnet.composite.RelationExtender;

public class SmallGraphBuilder {

	ByCategorizerCriteria bc;

	private SimpleNameBasedEstimator se;
	private NotACritery na;

	protected MultiMap<String, String> mm = MultiMap.withSet();

	protected HashMap<String, String> basicToNormal = new HashMap<String, String>();

	public static void main(String[] args) {
		try {
			BufferedReader rs = new BufferedReader(new InputStreamReader(
					new FileInputStream(new File("D:/all.txt")), "UTF-8"));
			PrintWriter ps = new PrintWriter(new File("D:/all2.txt"));
			while (true) {
				try {
					String readLine = rs.readLine();
					if (readLine == null) {
						ps.close();
						break;
					}
					int indexOf = readLine.indexOf("->");
					String pm = readLine.substring(0, indexOf);
					String p1 = readLine.substring(indexOf + 2);
					int k = p1.indexOf(' ');
					String pt = p1.substring(0, k);

					boolean w0 = to(pm);
					boolean w1 = to(pt);
					if (w0 || w1) {
						System.out.println(pm + ":" + pt);
					}
					if (!w0&&!w1){
						ps.println(readLine);
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	static boolean to(String pm) {
		boolean tp = false;
		TextElement wordElement = WordNetProvider.getInstance().getWordElement(
				pm);
		if (wordElement != null) {
			if (wordElement.hasGrammem(Grammem.SemanGramem.TOPONIM)) {
				tp = true;
			}
			wordElement = WordNetProvider.getInstance().getWordElement(pm);
			if (wordElement.hasGrammem(Grammem.SemanGramem.SURN)) {
				tp = true;
			}
		}
		return tp;
	}
	RelationExtender extender=new RelationExtender();

	public void build(WikiEngine2 engine) {
		bc = new ByCategorizerCriteria(engine);
		extender.load("D:/all2_nataly.txt");
		se = new SimpleNameBasedEstimator(engine);
		se.setAllowRelations(true);
		se.setComparer(new IExtraRelationComparer() {
			
			
			
			@Override
			public boolean isSub(String parent, String child) {
				return extender.isSub(parent, child);
			}
		});
		na = new NotACritery(engine);
		String[] allKeys = engine.getCategoryTitles().getAllKeys();
		for (String s : allKeys) {
			int i = engine.getCategoryTitles().get(s);
			if (s.contains(":")){
				continue;
			}
			ICategory category = engine.getCategory(i);
			ICategory[] subCategories = category.getSubCategories();
			l2:for (ICategory m : subCategories) {
				
				if (!se.isA(category, m)) {
					if (na.isNotA(category, m)) {
						continue;
					}
					ICategory[] parentCategories = m.getParentCategories();
					for (ICategory q:parentCategories){
						if (se.isA(q, m)){
							continue l2;
						}
					}
					TitleModel titleModel = TitleModel.get(category.getTitle(),
							false);
					TitleModel titleModel1 = TitleModel
							.get(m.getTitle(), false);
					/*if (titleModel.hasSingleCore()
							&& titleModel1.hasSingleCore()) {
						WordModel singleCore = titleModel.getSingleCore();
						WordModel singleCore1 = titleModel1.getSingleCore();
						String basicForm = singleCore.getBasicForm();
						String basicForm1 = singleCore1.getBasicForm();
						if (basicForm != null && basicForm1 != null) {
							mm.add(basicForm, basicForm1);
							continue;
						}
					}*/
					mm.add(cleanStuff(category.getTitle()),cleanStuff( m.getTitle()));
				}
			}
		}
		int relCount = 0;
		relCount = write();
		System.out.println("R:" + relCount);
	}
	public String cleanStuff(String string){
		
		int indexOf = string.indexOf("_по_");
		if (indexOf!=-1){
			string=string.substring(0, indexOf);
		}
		int max = SimpleNameBasedEstimator.lastCap(string);
		if (max>0){
			String str=max>0?SimpleNameBasedEstimator.cleanStuff(string.substring(0,max)):string;
			if (!str.endsWith("-")&&!str.endsWith("-")){
				return str.replace(' ', '_');	
			}
			
		}
		
		return string;	
	}
	HashSet<String>noChildren;

	int write() {
		int relCount = 0;
		try {
			PrintWriter ps = new PrintWriter(new File("D:/all.txt"));
			List<String> readAllLines = Files.readAllLines(Paths.get("D:/nochildren.txt"));
			noChildren=new HashSet<String>(readAllLines);
			Set<String> keySet = mm.keySet();
			ArrayList<String> sm = new ArrayList<String>(keySet);
			Collections.sort(sm);
			for (String q : sm) {
				if (q.startsWith("Незавершённые_статьи")) {
					continue;
				}
				if (q.startsWith("Избранные_списки")) {
					continue;
				}

				if (q.startsWith("Хорошие_статьи_")) {
					continue;
				}
				if (q.startsWith("Избранные_статьи")) {
					continue;
				}
				if (q.startsWith("Категории")) {
					continue;
				}
				if (q.startsWith("Похороненные_в_")) {
					continue;
				}
				if (q.startsWith("Родившиеся_в_")) {
					continue;
				}
				if (noChildren.contains(q)){
					continue;
				}
				if (q.startsWith("Появились_в_")) {
					continue;
				}
				if (q.startsWith("Исчезли_в_")) {
					continue;
				}
				if (q.startsWith("Похороненные_по_")) {
					continue;
				}
				if (q.startsWith("Категории_по_")) {
					continue;
				}

				if (q.startsWith("История_")) {
					continue;
				}
				if (q.startsWith("Умершие_в_")) {
					continue;
				}

				if (q.endsWith("алфавиту")) {
					continue;
				}
				if (q.endsWith("по_годам")) {
					continue;
				}
				if (q.endsWith("_году")) {
					continue;
				}
				if (q.endsWith("_годов")) {
					continue;
				}
				if (q.endsWith("_год")) {
					continue;
				}
				if (q.endsWith("_года")) {
					continue;
				}
				if (q.startsWith("User_")) {
					continue;
				}
				if (q.startsWith("Многозначные_термины")) {
					continue;
				}
				if (q.startsWith("Скрытые_")) {
					continue;
				}
				if (q.startsWith("Годы_")) {
					continue;
				}
				if (q.equals("Годы")) {
					continue;
				}
				if (q.equals("Города")) {
					continue;
				}
				if (q.equals("Десятилетия")) {
					continue;
				}
				
				if (q.startsWith("Списки_")) {
					continue;
				}
				if (q.startsWith("Статьи_проекта_")) {
					continue;
				}

				if (q.contains(":")) {
					continue;
				}
				if (q.contains("_век_до_")) {
					continue;
				}
				if (q.contains("_век_по_")) {
					continue;
				}
				if (q.contains("_век_во_")) {
					continue;
				}
				if (q.contains("_век_в_")) {
					continue;
				}
				if (q.contains("_век_на_")) {
					continue;
				}
				if (q.contains("_тысячелетие")) {
					continue;
				}
				if (q.contains("Десятилетия_")) {
					continue;
				}
				
				if (q.contains("Добротные_статьи_")) {
					continue;
				}
				if (q.endsWith("_век")) {
					continue;
				}
				if (q.contains("_год_до_")) {
					continue;
				}
				if (q.contains("_год_на_")) {
					continue;
				}
				if (q.contains("_год_в_")) {
					continue;
				}

				int indexOf = q.indexOf('_');
				if (indexOf != -1) {
					try {
						Integer.parseInt(q.substring(0, indexOf));
						continue;
					} catch (NumberFormatException e) {

					}
				}
				indexOf = q.indexOf('-');
				if (indexOf != -1) {
					try {
						Integer.parseInt(q.substring(0, indexOf));
						continue;
					} catch (NumberFormatException e) {

					}
				}
				Collection<String> collection = mm.get(q);
				relCount += collection.size();
				// uncovered.remove(q);
				for (String z : collection) {
					if (z.contains(":")) {
						continue;
					}
					if (z.endsWith("_году")) {
						continue;
					}
					if (z.endsWith("_года")) {
						continue;
					}
					if (z.startsWith("Умершие_в_")) {
						continue;
					}
					if (z.startsWith("Появились_в_")) {
						continue;
					}
					if (z.startsWith("Родившиеся_")) {
						continue;
					}
					if (z.startsWith("Похороненные_")) {
						continue;
					}
					if (z.startsWith("Добротные_статьи_")) {
						continue;
					}
					
					
					
					if (z.startsWith("категория")) {
						continue;
					}
					if (z.startsWith("Родившиеся_в_")) {
						continue;
					}
					if (z.startsWith("Умершие_")) {
						continue;
					}
					
					if (q.equalsIgnoreCase(z)) {
						continue;
					}
					process(ps, q, z);
					// uncovered.remove(z);
				}
			}
			ps.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return relCount;
	}

	void process(PrintWriter ps, String q, String z) {
		boolean w0 = to(q);
		boolean w1 = to(z);
		if (!w0&&!w1){
			ps.println(q + "->" + z + " ");
			
		}		
	}

}