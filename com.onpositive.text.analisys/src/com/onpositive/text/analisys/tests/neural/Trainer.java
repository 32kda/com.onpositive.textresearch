package com.onpositive.text.analisys.tests.neural;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.encog.engine.network.activation.ActivationSigmoid;
import org.encog.mathutil.randomize.ConsistentRandomizer;
import org.encog.ml.data.MLDataPair;
import org.encog.ml.data.MLDataSet;
import org.encog.ml.data.basic.BasicMLDataPair;
import org.encog.ml.data.basic.BasicMLDataSet;
import org.encog.ml.train.MLTrain;
import org.encog.neural.data.basic.BasicNeuralData;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.training.propagation.resilient.ResilientPropagation;
import org.encog.neural.pattern.FeedForwardPattern;
import org.encog.persist.EncogDirectoryPersistence;

import com.onpositive.semantic.wordnet.AbstractWordNet;
import com.onpositive.semantic.wordnet.GrammarRelation;
import com.onpositive.semantic.wordnet.Grammem;
import com.onpositive.semantic.wordnet.Grammem.PartOfSpeech;
import com.onpositive.semantic.wordnet.WordNetProvider;
import com.onpositive.text.analisys.tests.ParsedTokensLoader;
import com.onpositive.text.analisys.tests.euristics.SimplifiedToken;
import com.onpositive.text.analysis.neural.BinaryFlagDataSetGenerator;
import com.onpositive.text.analysis.neural.IDataSetGenerator;

import static com.onpositive.text.analysis.neural.NeuralConstants.*;

public class Trainer {
	
	private static final String LEARNING_SET_PATH = "D:\\tmp\\corpora";

	private static final double THRESHOLD = 0.055;
	
	private IDataSetGenerator dataSetGenerator = new BinaryFlagDataSetGenerator();

	private interface IVisitor {
		void visit(double[] inputs, double[] desired);
	}
	
//	private class DataSetVisitor implements IVisitor  {
//		
//		private DataSet dataSet = new DataSet(TOKEN_WINDOW_SIZE * dataSetGenerator.getDatasetSize(), 1);
//
//		@Override
//		public void visit(double[] inputs, double[] desired) {
//			dataSet.addRow(new DataSetRow(inputs, desired));
//		}
//
//		public DataSet getDataSet() {
//			return dataSet;
//		}
//		
//	}
	
	private class EncogDataSetVisitor implements IVisitor  {
		
		private List<Input> rightList = new ArrayList<Input>();
		private List<Input> wrongList = new ArrayList<Input>();
		
		private List<MLDataPair> pairs = new ArrayList<MLDataPair>();

		@Override
		public void visit(double[] inputs, double[] desired) {
			if (desired[0] > 0.1) {
				rightList.add(new Input(inputs));
			} else {
				wrongList.add(new Input(inputs));
			}
//			pairs.add(createData(inputs, desired));
		}

		private BasicMLDataPair createData(double[] inputs, double[] desired) {
			return new BasicMLDataPair(new BasicNeuralData(inputs), new BasicNeuralData(desired));
		}

		public List<MLDataPair> getPairs() {
			for (Input input : rightList) {
				pairs.add(createData(input.getData(), new double[] {1}));
			}
			Set<Input> rightSet = new HashSet<Input>(rightList);
			wrongList.removeAll(rightSet);
			for (Input input : wrongList) {
				pairs.add(createData(input.getData(), new double[] {0}));
			}
			return pairs;
		}
		
	}
	
//	public void train() {
//		int inputSize = TOKEN_WINDOW_SIZE * dataSetGenerator.getDatasetSize();
//		MultiLayerPerceptron perceptron1 = new MultiLayerPerceptron(inputSize, inputSize, 1);
//		DataSetVisitor visitor = new DataSetVisitor();
//		prepareLearningData(visitor);
//		perceptron1.learn(visitor.getDataSet());
//	}
	
	public void trainEncog() {
		int inputSize = TOKEN_WINDOW_SIZE * dataSetGenerator.getDatasetSize();
		BasicNetwork network = simpleFeedForward(inputSize, inputSize * 4,  inputSize * 4, 1);
		
		// randomize consistent so that we get weights we know will converge
		(new ConsistentRandomizer(-1,1,100)).randomize(network);
		
		EncogDataSetVisitor visitor = new EncogDataSetVisitor();
		
		prepareLearningData(visitor);

		MLDataSet trainingSet = new BasicMLDataSet(visitor.getPairs());

		// train the neural network
		final MLTrain train = new ResilientPropagation(network, trainingSet);

		long millis = System.currentTimeMillis();
		double minError = 5;
		int epoch = 1;
		double lastSaveError = -1;
		do {
			train.iteration();
			System.out.println (
					"Epoch #" + epoch + "  Error : " + train.getError () ) ;
			epoch++;
			double curError = train.getError();
			minError = Math.min(minError, curError);
			if (curError < THRESHOLD && curError - lastSaveError > 0.01) {
				EncogDirectoryPersistence.saveObject(new File(String.format("morphology.%1$,4f.nnet", curError)), network);	
				lastSaveError = curError;
			}
		} while (train.getError() > 0.01);

		double e = network.calculateError(trainingSet);
		System.out.println("Network trained to error: " + e);
		
		System.out.println("Training took, seconds: " + ((System.currentTimeMillis() - millis) / 1000));

		System.out.println("Saving network");
		EncogDirectoryPersistence.saveObject(new File("morphology.nnet"), network);
	}
	
	public void testEncog() {
		BasicNetwork network = (BasicNetwork) EncogDirectoryPersistence.loadObject(new File("preved.nnet"));
		EncogDataSetVisitor visitor = new EncogDataSetVisitor();
		prepareTestingData(visitor);
		MLDataSet trainingSet = new BasicMLDataSet(visitor.getPairs());
		double e = network.calculateError(trainingSet);
		System.out.println("Network trained to error: " + e);

	}
	
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
		final BasicNetwork network = (BasicNetwork)pattern.generate();
		network.reset();
		return network;
	}

	protected void prepareLearningData(IVisitor visitor) {
		
		File dir = new File(LEARNING_SET_PATH);
		File[] listedFiles = dir.listFiles();
		AbstractWordNet wordNet = WordNetProvider.getInstance();
		
		for (int i = 0; i < listedFiles.length / 2; i++) {
			File curFile = listedFiles[i];
			prepareForFile(visitor, wordNet, curFile);
			
		}
	}
	
	protected void prepareTestingData(IVisitor visitor) {
		
		File dir = new File(LEARNING_SET_PATH);
		File[] listedFiles = dir.listFiles();
		AbstractWordNet wordNet = WordNetProvider.getInstance();
		
		for (int i = listedFiles.length / 2; i < listedFiles.length; i++) {
			File curFile = listedFiles[i];
			prepareForFile(visitor, wordNet, curFile);
			
		}
	}

	protected void prepareForFile(IVisitor visitor, AbstractWordNet wordNet, File curFile) {
		try {
			BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(curFile));
			ParsedTokensLoader loader = new ParsedTokensLoader(inputStream);
			List<List<SimplifiedToken>> chains = loader.getChains();
			for (List<SimplifiedToken> chain : chains) {
				chain = chain.stream().filter(token -> token.hasValidGrammemSet()).collect(Collectors.toList());
				for (int j = 0; j < chain.size(); j++) {
					if (chain.isEmpty()) {
						continue;
					}
					List<List<SimplifiedToken>> trigrams = getTrigrams(chain, j, wordNet);
					for (int k = 0; k < trigrams.size(); k++) {
						List<SimplifiedToken> trigram = trigrams.get(k);
						double desired = k == 0 ? 1: 0;
						visitor.visit(getDataSet(trigram, j == 0 && trigram.size() == 2), new double[]{desired});
					}
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	private List<List<SimplifiedToken>> getTrigrams(List<SimplifiedToken> tokens, int j, AbstractWordNet wordNet) {
		List<List<SimplifiedToken>> result = new ArrayList<List<SimplifiedToken>>();
		int subListStart = j - 1;
		if (j == 0) {
			subListStart = 0;
		}
		if (j == tokens.size() - 1) {
			subListStart = Math.max(0,tokens.size() - TOKEN_WINDOW_SIZE);
		}
		int subListEnd = Math.min(tokens.size(), subListStart + TOKEN_WINDOW_SIZE);
		if (subListEnd - subListStart < 2) {
			return Collections.emptyList();
		}
		List<SimplifiedToken> correctList = tokens.subList(subListStart, subListEnd);
		result.add(correctList);
		int interestingToken = j - subListStart;
		SimplifiedToken correctToken = correctList.get(interestingToken);
		GrammarRelation[] forms = wordNet.getPossibleGrammarForms(correctToken.getWord());
		if (forms != null &&  forms.length > 1) {
			PartOfSpeech correctPart = correctToken.getPartOfSpeech();
			for (GrammarRelation grammarRelation : forms) {
				if (!grammarRelation.hasGrammem(correctPart)) {
					List<SimplifiedToken> incorrectList = new ArrayList<SimplifiedToken>(correctList);
					incorrectList.remove(interestingToken);
					incorrectList.add(interestingToken, new SimplifiedToken(correctToken.getWord(), grammarRelation.getGrammems()));
					result.add(incorrectList);
				}
			}
		}
		return result;
	}

	private double[] getDataSet(List<SimplifiedToken> tokens, boolean first) {
		if (TOKEN_WINDOW_SIZE < tokens.size()) {
			throw new IllegalArgumentException("tokens list too large, should be " + TOKEN_WINDOW_SIZE + " tokens at most");
		}
		int i = first ? 1:0;
		int datasetSize = dataSetGenerator.getDatasetSize();
		double[] result = new double[TOKEN_WINDOW_SIZE * dataSetGenerator.getDatasetSize()];
		for (SimplifiedToken simplifiedToken : tokens) {
			Collection<Grammem> grammems = simplifiedToken.getGrammems();
			double[] dataset = dataSetGenerator.generateDataset(grammems);
			for (int j = 0; j < dataset.length; j++) {
				result[i * datasetSize + j] = dataset[j];
			}
			i++;
		}
		return result;
	}
	
	public static void main(String[] args) {
		new Trainer().trainEncog();
	}
	
}
