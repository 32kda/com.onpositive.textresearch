package com.onpositive.text.analysis.neural;

import java.util.Collection;

import com.onpositive.semantic.wordnet.Grammem;

public class SimpleDataSetGenerator implements IDataSetGenerator {

	@Override
	public int getDatasetSize() {
		return NeuralConstants.USED_PROPS.length;
	}

	@Override
	public double[] generateDataset(Collection<Grammem> grammems) {
		double[] result = new double[NeuralConstants.USED_PROPS.length];
		for (int i = 0; i < result.length; i++) {
			Class<?> propClass = NeuralConstants.USED_PROPS[i];
			for (Grammem grammem : grammems) {
				if (propClass.isAssignableFrom(grammem.getClass())) {
					result[i] = grammem.intId - grammem.getInitialId() + 1;
				}
			}
		}
		return result;
	}

}
