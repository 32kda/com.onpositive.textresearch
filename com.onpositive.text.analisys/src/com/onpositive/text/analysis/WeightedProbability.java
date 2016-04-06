package com.onpositive.text.analysis;

public class WeightedProbability {
	public double probability;
	public double weight;
	
	public WeightedProbability(double probability, double weight) {
		this.probability = probability;
		this.weight = weight;
	}
	
	public static WeightedProbability True = new WeightedProbability(1.0, 1.0);
	public static WeightedProbability False = new WeightedProbability(0.0, 1.0);
}
