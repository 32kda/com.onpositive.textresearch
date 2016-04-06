package com.onpositive.semantic.words3.prediction;

import java.io.InputStream;
import java.io.OutputStream;

public interface IPredictionDataProvider {
	
	public InputStream getOriginalWordNetData();
	
	public InputStream getPredictionData();
	
	public OutputStream getPredictionDataOutputStream();
	
}
