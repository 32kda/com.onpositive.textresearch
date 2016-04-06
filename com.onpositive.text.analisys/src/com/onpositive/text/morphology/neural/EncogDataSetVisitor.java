package com.onpositive.text.morphology.neural;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.encog.ml.data.MLDataPair;
import org.encog.ml.data.basic.BasicMLDataPair;
import org.encog.neural.data.basic.BasicNeuralData;

import com.onpositive.text.analisys.tests.neural.Input;

public class EncogDataSetVisitor  {

	private List<Input> rightList = new ArrayList<Input>();
	private List<Input> wrongList = new ArrayList<Input>();

	private List<MLDataPair> pairs = new ArrayList<MLDataPair>();

	public void visit(double[] inputs, boolean desired) {
		if (desired) {
			rightList.add(new Input(inputs));
		} else {
			wrongList.add(new Input(inputs));
		}
	}

	private BasicMLDataPair createData(double[] inputs, double[] desired) {
		return new BasicMLDataPair(new BasicNeuralData(inputs), new BasicNeuralData(desired));
	}

	public List<MLDataPair> getPairs() {
		for (Input input : rightList) {
			pairs.add(createData(input.getData(), new double[] { 1 }));
		}
		Set<Input> rightSet = new HashSet<Input>(rightList);
		wrongList.removeAll(rightSet);
		for (Input input : wrongList) {
			pairs.add(createData(input.getData(), new double[] { 0 }));
		}
		return pairs;
	}

}