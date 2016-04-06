package com.onpositive.text.morphology.neural;

import java.io.File;
import java.util.List;
import java.util.stream.Stream;

import org.encog.engine.network.activation.ActivationSigmoid;
import org.encog.mathutil.randomize.ConsistentRandomizer;
import org.encog.ml.data.MLDataPair;
import org.encog.ml.data.MLDataSet;
import org.encog.ml.data.basic.BasicMLDataSet;
import org.encog.ml.train.MLTrain;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.training.propagation.resilient.ResilientPropagation;
import org.encog.neural.pattern.FeedForwardPattern;
import org.encog.persist.EncogDirectoryPersistence;

import com.onpositive.semantic.wordnet.Grammem.PartOfSpeech;
import com.onpositive.text.morphology.dataset.prepare.DataSetTestSample;

public class Trainer {

	private static final double THRESHOLD = 0.05;

	public static BasicNetwork simpleFeedForward(final int... neurons) {
		if (neurons.length < 2) {
			throw new IllegalArgumentException();
		}
		final FeedForwardPattern pattern = new FeedForwardPattern();
		pattern.setInputNeurons(neurons[0]);
		pattern.setOutputNeurons(neurons[neurons.length - 1]);
		pattern.setActivationFunction(new ActivationSigmoid());
		for (int i = 1; i < neurons.length - 1; i++) {
			pattern.addHiddenLayer(neurons[i]);
		}
		final BasicNetwork network = (BasicNetwork) pattern.generate();
		network.reset();
		return network;
	}
	public BasicNetwork train(Stream<DataSetTestSample>samples,PartOfSpeech correct){
		EncogDataSetVisitor encogDataSetVisitor = new EncogDataSetVisitor();
		BinaryFlagDataSetGenerator flg=new BinaryFlagDataSetGenerator();
		samples.forEach(t->{
			List<String> textList = t.textList();
			double[] dataSet = flg.getDataSet(textList, t.isSentenceStart(), t.isSentenceEnd());
			encogDataSetVisitor.visit(dataSet, t.getCorrect()==correct);
		});
		return train(flg.getDatasetSize()*NeuralConstants.TOKEN_WINDOW_SIZE + flg.getMetadataOverhead(), encogDataSetVisitor);
	}
	
	public BasicNetwork train(int inputSize,EncogDataSetVisitor visitor){
		BasicNetwork network = simpleFeedForward(inputSize, inputSize , inputSize, 1);
		// randomize consistent so that we get weights we know will converge
		(new ConsistentRandomizer(-1, 1, 100)).randomize(network);
		
		List<MLDataPair> pairs = visitor.getPairs();
		if (pairs.size()==0){
			return network;
		}
		MLDataSet trainingSet = new BasicMLDataSet(pairs);

		// train the neural network
		final MLTrain train = new ResilientPropagation(network, trainingSet);

		long millis = System.currentTimeMillis();
		double minError = 5;
		int epoch = 1;
		double lastSaveError = -1;
		do {
			train.iteration();
			//System.out.println("Epoch #" + epoch + "  Error : " + train.getError());
			epoch++;
			double curError = train.getError();
			minError = Math.min(minError, curError);
//			if (curError < THRESHOLD && curError - lastSaveError > 0.01) {
//				EncogDirectoryPersistence.saveObject(new File(String.format("morphologyT.nnet", curError)),
//						network);
//				lastSaveError = curError;
//			}
			if (epoch>500){
				break;
			}
		} while (train.getError() > 0.01);

		double e = network.calculateError(trainingSet);
		System.out.println("Network trained to error: " + e);
		return network;
	}
}
