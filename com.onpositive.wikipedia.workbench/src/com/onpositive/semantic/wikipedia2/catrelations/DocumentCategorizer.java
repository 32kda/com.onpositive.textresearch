package com.onpositive.semantic.wikipedia2.catrelations;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;

import com.onpositive.semantic.search.core.ICategory;
import com.onpositive.semantic.wikipedia2.WikiDoc;
import com.onpositive.semantic.wikipedia2.WikiEngine2;
import com.onpositive.semantic.wikipedia2.docclasses.DocumentClassService;

public class DocumentCategorizer {

	public static void main(String[] args) {
		WikiEngine2 eng = new WikiEngine2("D:\\se2\\ruwiki");

		try {
			List<String> readAllLines = Files.readAllLines(new File(eng
					.getLocation(), "mps.txt").toPath());
			int count = 0;
			int tcount = 0;
			for (String s : readAllLines) {
				int indexOf = s.indexOf("-[");
				if (indexOf == -1) {
					continue;
				}
				String substring = s.substring(0, indexOf);
				String substring1 = s.substring(indexOf + 2, s.length() - 1);
				String[] split = substring1.split(",");
				HashSet<String> ms = new HashSet<String>();
				for (String q : split) {
					ms.add(q.trim());
				}
				WikiDoc documentByTitle = eng.getDocumentByTitle(substring);
				if (eng.getIndex(DocumentClassService.class).isClassified(
						documentByTitle.getIntId())) {
					continue;
				}
				;
				ICategory[] categories = getFullSetOfCategories(documentByTitle);
				boolean f = false;
				for (ICategory c : categories) {
					TitleModel titleModel = TitleModel.get(c.getTitle(), false);
					if (titleModel.hasSingleCore()) {
						String basicForm = titleModel.getSingleCore()
								.getBasicForm();
						if (ms.contains(basicForm)) {
							f = true;
						}
					}
				}
				if (f) {
					tcount++;
				} else {
					System.out.println(s);
				}
				count++;
				if (count % 1000 == 0) {
					System.out.println(tcount + ":" + count);
				}
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	static ICategory[] getFullSetOfCategories(WikiDoc documentByTitle) {
		ICategory[] categories = documentByTitle.getCategories();
		LinkedHashSet<ICategory>result=new LinkedHashSet<ICategory>();
		for (ICategory c:categories){
			String title = c.getTitle();
			if (title.toLowerCase().equals(documentByTitle.getTitle().toLowerCase())){
				ICategory[] parentCategories = c.getParentCategories();
				for(ICategory q:parentCategories){
					result.add(q);
				}
			}
			else{
				result.add(c);
			}
		}
		return result.toArray(new ICategory[result.size()]);
	}

}
