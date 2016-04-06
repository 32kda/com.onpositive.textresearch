package com.onpositive.renderer.wordnet;

import com.onpositive.text.webview.DemoComponent;

public class WordNetComponent extends DemoComponent {

	@Override
	public String getDescription() {
		return "Просмотр семантико-морфологической сети. Введите слово для просмотра имеющейся информации:";
	}

	@Override
	public String getTitle() {
		return "Словосеть";
	}

	@Override
	public String getId() {
		return "wordnet";
	}

	@Override
	public String getOutput() {
		return new WordInformationRenderer(this.getText()).render();
	}

}
