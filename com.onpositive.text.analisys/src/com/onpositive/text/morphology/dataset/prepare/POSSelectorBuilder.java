package com.onpositive.text.morphology.dataset.prepare;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import com.onpositive.semantic.wordnet.Grammem.PartOfSpeech;
import com.onpositive.text.morphology.neural.AbstractPOSSelector;
import com.onpositive.text.morphology.neural.POSSelector;

public class POSSelectorBuilder {

	private static final int TRESHOLD = 100;

	public static void main(String[] args) {
		ArrayList<POSSelector> result = buildSelectors();
		for (AbstractPOSSelector s : result) {
			//if (s.toString().equals("[СУЩ, ГЛ]->СУЩ")) {
				s.prepare();
			//}
			// System.out.println(s+":"+s.rate());
		}
		for (AbstractPOSSelector s : result) {
			// s.prepare();
			//if (s.toString().equals("[СУЩ, ГЛ]->СУЩ")) {
				System.out.println(s + ":" + s.rate()+": count:"+s.count());
			//}
		}
		System.out.println(result.size());
		System.exit(0);
	}

	public static ArrayList<POSSelector> buildSelectors() {
		List<DataSetTestSample> list = DataSetProvider.getInstance();
		ArrayList<POSSelector> result = new ArrayList<>();
		MultiMap<Set<PartOfSpeech>, DataSetTestSample> smpl = MultiMap.withList();
		list.forEach(x -> smpl.add(x.getOptions(), x));
		ArrayList<MultiMap<Set<PartOfSpeech>, DataSetTestSample>.StatEntry<Set<PartOfSpeech>>> stat = smpl
				.toStat();
		Collections.sort(stat);
		stat.forEach(x -> {
			if (x.count() > TRESHOLD) {
				Stream<DataSetTestSample> set = subset(list, x);
				Stream<PartOfSpeech> distinct = set.map(y -> y.getCorrect()).distinct();
				distinct.forEach(targetPart -> {
					POSSelector selector = new POSSelector(x.value, targetPart);
					result.add(selector);
				});
			}
		});
		return result;
	}

	private static Stream<DataSetTestSample> subset(List<DataSetTestSample> list,
			MultiMap<Set<PartOfSpeech>, DataSetTestSample>.StatEntry<Set<PartOfSpeech>> x) {
		return list.stream().filter(y -> y.getOptions().equals(x.value));
	}
}
