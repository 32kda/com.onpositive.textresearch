package com.onpositive.text.morphology;

import com.onpositive.semantic.wordnet.Grammem.PartOfSpeech;

public class Result implements Comparable<Result>{
	public PartOfSpeech speach;
	public Result(PartOfSpeech speach, double score) {
		super();
		this.speach = speach;
		this.score = score;
	}

	public double score;
	public int trust;
	
	@Override
	public int compareTo(Result o) {
		if (o.score-this.score>0){
			return 1;
		}
		return -1;
	}
	public String toString(){
		return this.speach+":"+ String.format("%1$,.4f", this.score)+"("+trust+")";
	}
}