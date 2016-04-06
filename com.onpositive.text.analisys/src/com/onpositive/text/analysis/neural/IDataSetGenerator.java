package com.onpositive.text.analysis.neural;

import java.util.Collection;

import com.onpositive.semantic.wordnet.Grammem;

public interface IDataSetGenerator {
	
	public int getDatasetSize();
	
	public double[] generateDataset(Collection<Grammem> grammems);

}
