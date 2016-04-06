package com.onpositive.text.morphology.neural;

import java.util.List;

import org.encog.neural.networks.BasicNetwork;

public class Solver {

	
	protected BasicNetwork network;
	
	public Solver(BasicNetwork network) {
		super();
		this.network = network;
	}


	protected BinaryFlagDataSetGenerator flg=new BinaryFlagDataSetGenerator();


	public double score(List<String>input, boolean isSentenceStart, boolean isSentenceEnd){
		double[] vals=flg.getDataSet(input, isSentenceStart, isSentenceEnd);
		double[] output=new double[1];
		network.compute(vals, output);
		return output[0];
	}
}
