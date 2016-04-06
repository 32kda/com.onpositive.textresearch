package com.onpositive.semantic.wikipedia2.catrelations;

import com.onpositive.semantic.wikipedia2.WikiEngine2;

public class SimpleTest {

	public static void main(String[] args) {
		WikiEngine2 eng = new WikiEngine2("D:\\se2\\ruwiki");
		TitleModel titleModel = TitleModel.get("Здания_и_сооружения_по_городам_Израиля", false);
		TitleModel titleModel1 = TitleModel.get("Здания_и_сооружения_Рамат-Гана", false);
		System.out.println(titleModel);
		System.out.println(titleModel1);
		SmallGraphBuilder smallGraphBuilder = new SmallGraphBuilder();
		smallGraphBuilder.build(eng);
		smallGraphBuilder.write();
	}
}
