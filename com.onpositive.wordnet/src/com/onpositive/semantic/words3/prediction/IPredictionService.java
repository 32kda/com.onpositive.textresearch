package com.onpositive.semantic.words3.prediction;

import com.onpositive.semantic.words3.ReadOnlyWordNet;

public interface IPredictionService {
	public IPredictionHelper getPredictionHelper(ReadOnlyWordNet wordNet);
}
