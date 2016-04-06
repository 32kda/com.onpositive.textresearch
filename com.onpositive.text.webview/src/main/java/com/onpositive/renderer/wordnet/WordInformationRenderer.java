package com.onpositive.renderer.wordnet;

import java.util.Arrays;
import java.util.Set;

import com.onpositive.semantic.wordnet.AbstractRelation;
import com.onpositive.semantic.wordnet.GrammarRelation;
import com.onpositive.semantic.wordnet.Grammem;
import com.onpositive.semantic.wordnet.MeaningElement;
import com.onpositive.semantic.wordnet.TextElement;
import com.onpositive.semantic.wordnet.WordNetProvider;
import com.onpositive.text.webview.ui.BasicComponent;
import com.onpositive.text.webview.ui.Container;

public class WordInformationRenderer extends Container {

	protected TextElement element;
	private GrammarRelation[] possibleGrammarForms;

	public WordInformationRenderer(String element) {
		super();
		element=element.toLowerCase();
		this.element = WordNetProvider.getInstance().getWordElement(element);
		possibleGrammarForms = WordNetProvider.getInstance().getPossibleGrammarForms(element);
		this.createChildren();
	}

	void createChildren() {

		if (this.element == null && (possibleGrammarForms == null || possibleGrammarForms.length == 0)) {
			this.add(h3("Слово не найдено"));
			return;
		}
		for (GrammarRelation x : this.possibleGrammarForms) {
			BasicComponent li = li("");
			li.add(a(x.getWord().toString()+x.getGrammems().toString(), "wordnet?text=" + ((TextElement) x.getWord()).getBasicForm()));
			this.add(li);
		}
		if (this.element==null){
			return;
		}
		this.add(createSummary(this.element));
		Arrays.asList(this.element.getConcepts()).forEach(x -> {
			this.add(createConceptUI(x));
		});
	}

	private BasicComponent createConceptUI(MeaningElement x) {
		Container res = new Container();
		res.add(renderGrammems(x.getGrammems()));
		res.add(renderRelations(x.getMorphologicalRelations(), "Морфологические отношения"));
		res.add(renderRelations(x.getSemanticRelations(), "Семантические отношения"));
		res.add(tag("hr", ""));
		return res;
	}

	private Container renderGrammems(Set<Grammem> gr) {
		Container result = new Container();
		result.add(h4("Граммемы"));
		gr.forEach(x -> result.add(tag("span", x.toString() + " ")));
		return result;
	}

	private Container renderRelations(AbstractRelation<?>[] rels, String title) {
		Container cnt = new Container();
		cnt.add(h4(title));
		if (rels.length == 0) {
			cnt.add(span("Записи отсутсвуют"));
		}
		Arrays.sort(rels, (x, y) -> x.relation - y.relation);
		Arrays.asList(rels).forEach(x -> {

			BasicComponent li = li("");
			li.add(a(x.getWord().toString(),
					"wordnet?text=" + ((MeaningElement) x.getWord()).getParentTextElement().getBasicForm()));
			li.add(span("(" + x.getDescription() + ")"));
			cnt.add(li);
		});
		return cnt;
	}

	private BasicComponent createSummary(TextElement element2) {
		return h3(element.getBasicForm());
	}

}
