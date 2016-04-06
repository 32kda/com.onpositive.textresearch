package com.onpositive.text.morphology.dataset.prepare;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.onpositive.semantic.wordnet.Grammem.PartOfSpeech;
import com.onpositive.text.analysis.IToken;
import com.onpositive.text.analysis.filtering.AdditionalPartsPresetFilter;
import com.onpositive.text.analysis.filtering.CompositeSelector;
import com.onpositive.text.analysis.filtering.InfinitiveSelector;
import com.onpositive.text.analysis.lexic.PrimitiveTokenizer;
import com.onpositive.text.analysis.lexic.scalar.ScalarParser;
import com.onpositive.text.morphology.AnalisysResult;
import com.onpositive.text.morphology.neural.NeuralEstimator;
import com.onpositive.text.morphology.Result;

public class DemoEstimationData {

	protected static CompositeSelector compositeSelector = new CompositeSelector(new AdditionalPartsPresetFilter(), new InfinitiveSelector());

	public static class EstimatedToken {

		public IToken primitiveToken;

		public String string;

		public Set<PartOfSpeech> originalParts = new HashSet<>();

		public HashSet<PartOfSpeech> neural = new HashSet<>();

		public HashSet<PartOfSpeech> heuristic = new HashSet<>();

		public AnalisysResult neuralAnalysisResult;

		public String toString() {
			return string + ":" + originalParts.toString();
		}

		public HashSet<PartOfSpeech> doSelect() {
			HashSet<PartOfSpeech> result = new HashSet<>(this.originalParts);

			if (!neuralAnalysisResult.results.isEmpty()) {
				result.retainAll(neural);
			}
			if (heuristic != null) {
				result.retainAll(heuristic);
			}
			String val = primitiveToken.getStringValue().toLowerCase().trim();
			Collection<PartOfSpeech> filteredParts = compositeSelector.select(val, result);
			if (filteredParts != null && !filteredParts.isEmpty()) {
				result = new HashSet<>();
				if (filteredParts.size() == 1) {
					return new HashSet<PartOfSpeech>(filteredParts);
				} else {
					result.addAll(filteredParts);
				}
			}
			if (result.isEmpty()) {
				result = new HashSet<>(originalParts);
			}

			return result;
		}
	}

	public final ArrayList<EstimatedToken> results = new ArrayList<>();
	protected HashMap<Integer, EstimatedToken> ts = new HashMap<>();
	protected String originalString;
	static NeuralEstimator ess = new NeuralEstimator();

	public DemoEstimationData(String toEstimate) {
		this.originalString = toEstimate;
		PrimitiveTokenizer tk = new PrimitiveTokenizer();
		List<IToken> tokenized = tk.tokenize(toEstimate);
		tokenized = new ScalarParser().process(tokenized);
		ArrayList<String> str = new ArrayList<>();
		for (IToken t : tokenized) {
			EstimatedToken es = new EstimatedToken();
			es.primitiveToken = t;
			es.string = t.getStringValue();
			Set<PartOfSpeech> hashSet = es.originalParts = DataSetPreparation.possiblePartsOfSpeech(es.string);
			es.originalParts = hashSet;
			str.add(es.string);
			this.results.add(es);
			ts.put(t.getStartPosition(), es);
		}

		ArrayList<AnalisysResult> analize = ess.analize(str);
		int pos = 0;
		for (AnalisysResult r : analize) {
			this.results.get(pos).neuralAnalysisResult = r;
			for (Result ra : r.results) {
				this.results.get(pos).neural.add(ra.speach);
			}
			pos++;
		}
		try {
			HeuristicBasedEstimator h = new HeuristicBasedEstimator();
			h.process(this);
		} catch (Exception e) {
			for (EstimatedToken t : this.results) {
				t.heuristic = null;
			}
		}
	}
}
