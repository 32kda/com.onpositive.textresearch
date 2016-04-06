package com.onpositive.text.analysis.neural;

import java.util.Collection;

import static com.onpositive.text.analysis.neural.NeuralConstants.*;

import com.onpositive.semantic.wordnet.Grammem;

public class BinaryDataSetGenerator implements IDataSetGenerator {
	
	private int bitsCount;

	public BinaryDataSetGenerator() {
		bitsCount = NeuralConstants.BITS_PER_PROP.values().stream().mapToInt(val -> val).sum();
	}
	
	@Override
	public int getDatasetSize() {
		return bitsCount;
	}

	@Override
	public double[] generateDataset(Collection<Grammem> grammems) {
		double[] result = new double[bitsCount];
		int dataSetIdx = 0;
		for (int i = 0; i < USED_PROPS.length; i++) {
			Class<?> propClass = USED_PROPS[i];
			Integer bits = BITS_PER_PROP.get(propClass);
			boolean found = false;
			for (Grammem grammem : grammems) {
				if (propClass.isAssignableFrom(grammem.getClass())) {
					found = true;
					double[] doubleSet = toDoubleSet(grammem.intId - grammem.getInitialId() + 1, bits);
					for (int j = 0; j < doubleSet.length; j++) {
						result[dataSetIdx++] = doubleSet[j];
					}
					break;
				}
			}
			if (!found) {
				dataSetIdx += bits;
			}
		}
		return result;
	}
	
	private static double[] toDoubleSet(int number, int base) {
	    final double[] ret = new double[base];
	    for (int i = 0; i < base; i++) {
	        ret[base - 1 - i] = Math.abs(Math.signum((1 << i & number)));
	    }
	    return ret;
	}

}
