package com.onpositive.text.morphology.dataset.prepare;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import org.encog.neural.networks.BasicNetwork;

import com.onpositive.semantic.wordnet.Grammem.PartOfSpeech;
import com.onpositive.text.morphology.neural.Solver;
import com.onpositive.text.morphology.neural.Trainer;

public class SmallTest {

	static int badCount=0;
	static int goodCount=0;
	public static void main(String[] args) {
		List<DataSetTestSample> list = DataSetProvider.getInstance();
		Collections.shuffle(list, new Random(323232));
		Trainer tr = new Trainer();
		Stream<DataSetTestSample> filter = mm(list.subList(0, list.size() / 2));
		BasicNetwork train = tr.train(filter, PartOfSpeech.VERB);
		Solver ms = new Solver(train);
		mm(list.subList(list.size() / 2, list.size())).forEach(x -> {
			double value = ms.score(x.textList(), x.isSentenceStart(), x.isSentenceEnd());
			if (x.getCorrect() != PartOfSpeech.VERB) {
				if (value > 0.5) {
					badCount++;
					System.out.println(x + ":" + value);
				}
				else{
					goodCount++;
				}
			}
			if (x.getCorrect() == PartOfSpeech.VERB) {
				if (value < 0.5) {
					badCount++;
					System.out.println(x + ":" + value);
				}
				else{
					goodCount++;
				}
			}
		});
		System.out.println(goodCount*1.0/(badCount+goodCount)+"("+goodCount+badCount+")");
		// ArrayList<MultiMap<String, DataSetTestSample>.StatEntry<String>> stat
		// = ss.toStat();
		// Collections.sort(stat);
		// stat.stream().filter(x -> x.count() > 10).forEach(x ->
		// System.out.println(x));
	}

	private static Stream<DataSetTestSample> mm(List<DataSetTestSample> list) {
		return list.stream().filter(x -> x.isFullSet() && x.getOptions().toString().equals("[СУЩ, ГЛ]"));
	}
}
