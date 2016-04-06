package com.onpositive.text.morphology.neural;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Stream;

import org.encog.neural.networks.BasicNetwork;

import com.onpositive.semantic.wordnet.Grammem.PartOfSpeech;
import com.onpositive.text.morphology.dataset.prepare.DataSetProvider;
import com.onpositive.text.morphology.dataset.prepare.DataSetTestSample;

public abstract class AbstractPOSSelector {
	
	public static final double ESTIMATION_TRESHOLD=0.5;

	public static final int FRACTION=2;

	protected BasicNetwork network;
	
	protected int badCount=0;
	protected int goodCount=0;
	
	protected PartOfSpeech correct;
	
	protected int samplesCount;

	public AbstractPOSSelector() {
		super();
	}

	public abstract boolean match(Set<PartOfSpeech> set);
	
	public abstract Stream<DataSetTestSample> selectSamples(List<DataSetTestSample> list);

	protected double score(DataSetTestSample sample) {
		if (this.network==null){
			this.prepare();
		}
		Solver ms = new Solver(this.network);
		return ms.score(sample.textList(), sample.isSentenceStart(), sample.isSentenceEnd());
	}

	protected BasicNetwork trainNetwork() {
		List<DataSetTestSample> list = DataSetProvider.getInstance();
		Collections.shuffle(list, new Random(323232));
		Trainer tr = new Trainer();
		Stream<DataSetTestSample> filter = selectSamples(list.subList(0, list.size() / FRACTION));
		
		BasicNetwork train = tr.train(filter, this.correct);
		this.network=train;
		return train;
	}

	public void prepare() {
		trainNetwork();
		estimate();
	}

	public double rate() {
		return this.goodCount*1.0/(badCount+goodCount);
	}

	public int count() {
		return this.goodCount+this.badCount;
	}

	void estimate() {
		Solver ms = new Solver(this.network);
		List<DataSetTestSample> list = DataSetProvider.getInstance();
		Collections.shuffle(list, new Random(323232));
		selectSamples(list.subList(list.size() / 2, list.size())).forEach(x -> {
			double value = ms.score(x.textList(), x.isSentenceStart(), x.isSentenceEnd());
			if (x.getCorrect() != this.correct) {
				if (value >ESTIMATION_TRESHOLD) {
					badCount++;
					//System.out.println(x + ":" + value);
				}
				else{
					goodCount++;
				}
			}
			if (x.getCorrect() == this.correct) {
				if (value < ESTIMATION_TRESHOLD) {
					badCount++;
					//System.out.println(x + ":" + value);
				}
				else{
					goodCount++;
				}
			}
		});
	}

}