package com.onpositive.text.morphology.neural;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import com.onpositive.semantic.wordnet.GrammarRelation;
import com.onpositive.semantic.wordnet.Grammem;
import com.onpositive.semantic.wordnet.WordNetProvider;

@SuppressWarnings("unchecked")
public class BinaryFlagDataSetGenerator implements IDataSetGenerator {

	private static final int SENTENCE_BOUND_FLAGS = 4;
	private static List<Grammem> usedGrammems = new ArrayList<Grammem>();

	static {
		try {
			for (Class<? extends Grammem> clazz : NeuralConstants.USED_PROPS) {
				Field allField = clazz.getDeclaredField("all");
				usedGrammems.addAll((Collection<Grammem>) allField.get(clazz));
			}
		} catch (NoSuchFieldException e) {
			// Shouldn't happen
			e.printStackTrace();
		} catch (SecurityException e) {
			// Shouldn't happen
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// Shouldn't happen
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// Shouldn't happen
			e.printStackTrace();
		}
	}

	private Grammem[] grammemsArray = usedGrammems.toArray(new Grammem[0]);

	@Override
	public int getDatasetSize() {
		return grammemsArray.length;
	}

	@Override
	public double[] generateDataset(Collection<Grammem> grammems) {
		double[] dataset = new double[grammemsArray.length];
		for (int i = 0; i < grammemsArray.length; i++) {
			if (grammems.contains(grammemsArray[i])) {
				dataset[i] = 1;
			}
		}
		return dataset;
	}

	public double[] getDataSet(List<String> tokens, boolean isSentenceStart, boolean isSentenceEnd) {
		if (tokens.size() < 2) {
			return new double[0];
		}
		if (tokens.size() == 2) {
			if (isSentenceStart) {
				tokens.add(0,null);	
			} else {
				tokens.add(null);
			}
		}		
		if (NeuralConstants.TOKEN_WINDOW_SIZE < tokens.size()) {
			throw new IllegalArgumentException(
					"tokens list too large, should be " + NeuralConstants.TOKEN_WINDOW_SIZE + " tokens at most");
		}
		int i = 0;
		int datasetSize = this.getDatasetSize();
		double[] result = new double[NeuralConstants.TOKEN_WINDOW_SIZE * datasetSize + getMetadataOverhead()];
		for (String simplifiedToken : tokens) {
			GrammarRelation[] possibleGrammarForms = WordNetProvider.getInstance()
					.getPossibleGrammarForms(simplifiedToken);
			if (possibleGrammarForms != null) {
				for (GrammarRelation g : possibleGrammarForms) {
					HashSet<Grammem> grammems = g.getGrammems();
					double[] dataset = this.generateDataset(grammems);
					for (int j = 0; j < dataset.length; j++) {
						result[i * datasetSize + j] += dataset[j];
					}
				}
			}
			i++;
		}
		result[i++] = isSentenceStart ? 1 : 0; 
		result[i++] = tokens.get(0) != null ? 1 : 0;
		result[i++] = tokens.get(2) != null ? 1 : 0;
		result[i++] = isSentenceEnd ? 1 : 0;
		return result;
	}

	public int getMetadataOverhead() {
		return SENTENCE_BOUND_FLAGS; //Additional data for sentence start/end tagging
	}
}
