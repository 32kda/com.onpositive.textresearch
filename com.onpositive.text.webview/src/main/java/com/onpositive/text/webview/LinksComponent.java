package com.onpositive.text.webview;

import java.util.ArrayList;
import java.util.List;

import com.onpositive.text.webview.ui.annotations.View;

@View("page.html")
public class LinksComponent extends DemoComponent {

	private List<DemoComponent> submenu = new ArrayList<>();
	public LinksComponent()
	{
		submenu.add(new PageComponent("materials/rules.html","Список правил"));
		submenu.add(new PageComponent("materials/tagsets.html","Набор тэгов словаря"));
		submenu.add(new PageComponent("materials/filters.html","Список слов с предопределённой частью речи"));
		submenu.add(new PageComponent("materials/flare.html","Категоризация"));
		submenu.add(new PageComponent("materials/cats.html","Категоризация (+)"));
	}
	@Override
	public String getDescription() {
		return "Ссылки на дополнительные материалы";
	}

	@Override
	public String getTitle() {
		return "Материалы";
	}

	@Override
	public String getId() {
		return "materials";
	}

	@Override
	public String getOutput() {
		StringBuilder builder = new StringBuilder();
		builder.append("<p>\n");
		builder.append("<a href=\"../materials/rules.html\">Список правил</a>\n");
		builder.append("</p>\n");
		builder.append("<p>\n");
		builder.append("<a href=\"../materials/tagsets.html\">Набор тэгов словаря</a>\n");
		builder.append("</p>\n");
		builder.append("<p>\n");
		builder.append("<a href=\"../materials/filters.html\">Список слов с предопределённой частью речи</a>\n");
		builder.append("</p>\n");
		builder.append("<p>\n");
		builder.append("<a href=\"../materials/flare.html\">Категоризация</a>\n");
		builder.append("</p>\n");
		builder.append("<p>\n");
		builder.append("<a href=\"../materials/cats.html\">Категоризация (+)</a>\n");
		builder.append("</p>\n");
		return builder.toString();
	}
	
	@Override
	public DemoComponent[] getSubmenu() {
		return submenu.toArray(new DemoComponent[0]);
	}

}
