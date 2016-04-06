package com.onpositive.renderer.wordnet;

import java.util.Collection;
import java.util.HashSet;

import com.onpositive.semantic.wordnet.Grammem.PartOfSpeech;
import com.onpositive.text.analysis.IToken;
import com.onpositive.text.analysis.filtering.AdditionalPartsPresetFilter;
import com.onpositive.text.analysis.filtering.CompositeSelector;
import com.onpositive.text.analysis.filtering.InfinitiveSelector;
import com.onpositive.text.analysis.lexic.WordFormToken;
import com.onpositive.text.morphology.AnalisysResult;
import com.onpositive.text.morphology.AnalysisResultProvider;
import com.onpositive.text.morphology.dataset.prepare.DemoEstimationData;
import com.onpositive.text.morphology.dataset.prepare.DemoEstimationData.EstimatedToken;
import com.onpositive.text.morphology.neural.NeuralEstimator;
import com.onpositive.text.morphology.pairs.PairsEstimator;
import com.onpositive.text.webview.DemoComponent;

public class MorhologyComponent extends DemoComponent {

	private static final String BRACKETS_COLOR = "#7aba7a";
	private static final String NEURAL_COLOR = "#ff9900";
	private static final String EURISTIC_COLOR = "#7a3e48";
	
	private PairsEstimator pairsEstimator = new PairsEstimator();

	@Override
	public String getDescription() {
		return "Снятие частеречной омонимии. Введите строку для анализа:";
	}

	@Override
	public String getTitle() {
		return "Омонимия";
	}

	@Override
	public String getId() {
		return "omonimy";
	}

	static NeuralEstimator est = new NeuralEstimator();

	@Override
	public String getOutput() {
		String text = getText();
		DemoEstimationData demoEstimationData = new DemoEstimationData(text);
		StringBuilder output = new StringBuilder();
		CompositeSelector selector=new CompositeSelector(new AdditionalPartsPresetFilter(), new InfinitiveSelector());
//		AnalysisResultProvider pairsEstimatedProvider = new AnalysisResultProvider(pairsEstimator.analyze(text));
		for (EstimatedToken t : demoEstimationData.results) {
			output.append("<p>");
			output.append(t.string);
//			AnalisysResult pairsResult = pairsEstimatedProvider.getToken(t.string.toLowerCase().trim());
			if (t.originalParts.isEmpty()) {
				continue;
			}
			
			output.append(styleTextFont(t.originalParts.toString(), BRACKETS_COLOR));
			if (t.originalParts.size() > 1) {
				// output.append("<br/>");
				output.append(styleTextFont(" Правила: ", EURISTIC_COLOR));
				output.append(styleHeuristic(t.heuristic));
				HashSet<PartOfSpeech>result=new HashSet<>(t.originalParts);
				if (!t.neuralAnalysisResult.results.isEmpty()) {
					output.append(styleNeural(t.neuralAnalysisResult));
					result.retainAll(t.neural);
				}
				if (t.heuristic!=null){
					result.retainAll(t.heuristic);
				}
				output.append(" ");
				Collection<PartOfSpeech> filteredParts = selector.select(t.string, result);
				if (filteredParts!=null && !filteredParts.equals(result)){
					result=new HashSet<>();
					result.addAll(filteredParts);
					output.append(styleTextFont("(фильтры!!!)","#ffcc00"));
				}
//				if (pairsResult!=null && !pairsResult.chooseFrom.isEmpty() && !pairsResult.chooseFrom.equals(result)){
//					result=new HashSet<>();
//					result.addAll(pairsResult.chooseFrom);
//					output.append(styleTextFont("(словосочетания!)","#ffcc00"));
//				}
				if (result.isEmpty()){
					result=new HashSet<>(t.neural);
					result.addAll(t.heuristic);
				}
				output.append("Итого:"+ styleTextFont(result.toString(),null,true,false));
			}
			output.append("</p>");
		}
		return output.toString();
	}

	private String styleNeural(AnalisysResult na) {
//		neuralAnalysisResult.results
//		String resultStr = neuralAnalysisResult.toString();
//		int bracketIdx = resultStr.indexOf('[');
//		if (bracketIdx > -1) {
//			return styleTextFont("Нейросеть: " + resultStr.substring(0,bracketIdx), NEURAL_COLOR) + styleTextFont(resultStr.substring(bracketIdx),BRACKETS_COLOR);
//		}
		return styleTextFont("Нейросеть: ", NEURAL_COLOR) + styleTextFont(na.results.toString(),BRACKETS_COLOR);
	}
	
	private String styleTextFont(String text, String color, boolean bold, boolean italic) {
		String result = text;
		if (bold)
			result = "<b>" + result + "</b>"; 
		if (italic)
			result = "<i>" + result + "</i>";
		if (color != null)
			result = "<font color=\"" + color + "\">" + result + "</font>";
		return result;
	}
	
	private String styleTextFont(String text, String color) {
		return styleTextFont(text, color, false, false);
	}

	private String styleHeuristic(HashSet<PartOfSpeech> heuristic) {
		return styleTextFont(heuristic.toString(), BRACKETS_COLOR);
	}

	String getTokenValue(IToken t) {
		if (t instanceof WordFormToken) {
			if (t.getChildren().size() == 1) {
				return t.getChildren().get(0).getStringValue();
			}
		}
		return t.getStringValue();
	}
}
