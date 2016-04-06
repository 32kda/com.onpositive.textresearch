package com.onpositive.text.morphology.neural;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.onpositive.semantic.wordnet.Grammem.PartOfSpeech;
import com.onpositive.text.analysis.utils.Stats;
import com.onpositive.text.morphology.AnalisysResult;
import com.onpositive.text.morphology.Result;
import com.onpositive.text.morphology.dataset.prepare.DataSetPreparation;
import com.onpositive.text.morphology.dataset.prepare.DataSetTestSample;
import com.onpositive.text.morphology.dataset.prepare.POSSelectorBuilder;

public class NeuralEstimator {

	private static final double MIN_SCORE = 0.05;
	private static final int MIN_TRUST = 15;
	private static final double MAX_DIST = 0.01;
	protected ArrayList<POSSelector> selectors = new ArrayList<>();

	public NeuralEstimator(){
		selectors=POSSelectorBuilder.buildSelectors();
	}
	
	public ArrayList<AnalisysResult> analyze(String s){
		return analize(new ArrayList<>(Arrays.asList(s.split(" "))));
	}
	public ArrayList<AnalisysResult> analize(List<String> words){
		Stats.startNeural();
		ArrayList<AnalisysResult>rs=new ArrayList<>();
		String prev=null;
		String next=null;
		boolean start = true;
		boolean end = false;
		int i=0;
		for (String s:words){
			if (i<words.size()-1){
				next=words.get(i+1);
				if (i == words.size() - 2) {
					end = true;
				}
			}
			else{
				next=null;
			}
			rs.add(estimate(s, prev, next, start, end));
			if (prev != null) {
				start = false;
			}
			prev=s;
			i++;
		}
		Stats.finishNeural();
		return rs;
	}
	
	
	
	public AnalisysResult estimate(String toEstimate, String prev, String next, boolean sentenceStart, boolean sentenceEnd) {
		Set<PartOfSpeech> possiblePartsOfSpeach = DataSetPreparation.possiblePartsOfSpeech(toEstimate);
		if (possiblePartsOfSpeach.size() == 1) {
			return AnalisysResult.createConstant(toEstimate, possiblePartsOfSpeach.iterator().next());
		}
		DataSetTestSample smpl = new DataSetTestSample(possiblePartsOfSpeach, null, prev, next, toEstimate,
				DataSetPreparation.possiblePartsOfSpeech(prev).isEmpty(),
				DataSetPreparation.possiblePartsOfSpeech(next).isEmpty(), sentenceStart, sentenceEnd);
		ArrayList<Result>rs=new ArrayList<>();
		ArrayList<Result>abandonedResults=new ArrayList<>();
		double maxScore = 0;
		for (POSSelector sa:selectors){
			if (sa.match(smpl.getOptions())){
				double score = sa.score(smpl);
				if (score > maxScore) {
					maxScore = score;
				}
				Result e = new Result(sa.correct,score);
				e.trust=sa.count();
				rs.add(e);
				//System.out.println(toEstimate+":"+sa+":"+score);
			}
		}
		Collections.sort(rs);
		Double lastResult=null;
		ArrayList<Result>actualResult=new ArrayList<>();
		for (int a=0;a<rs.size();a++){
			Result result = rs.get(a);
			if (result.trust<MIN_TRUST || maxScore - result.score > MAX_DIST){
				abandonedResults.add(result);
				continue;
			}
			if (result.score < MIN_SCORE){
				abandonedResults.add(result);
				break;
			}
			if (lastResult==null){
				lastResult=result.score;
				actualResult.add(result);
				continue;
			}
			actualResult.add(result);
		}
		AnalisysResult ar=new AnalisysResult();
		ar.content=toEstimate;
		ar.results=actualResult;
		ar.chooseFrom=possiblePartsOfSpeach;
		ar.abandonedResults = abandonedResults;
		return ar;
	}
	
	public static void main(String[] args) {
		ArrayList<AnalisysResult> analize = new NeuralEstimator().analyze("с семью нулями");
		System.out.println(analize);
		analize = new NeuralEstimator().analyze("большая сорока прилетела");
		System.out.println(analize);
		analize = new NeuralEstimator().analyze("шестьдесят минут прошло");
		System.out.println(analize);
		analize = new NeuralEstimator().analyze("Маша белила стену");
		System.out.println(analize);
		analize = new NeuralEstimator().analyze("стоял перед строем");
		System.out.println(analize);
		analize = new NeuralEstimator().analyze("не покупать белил совсем");
		System.out.println(analize);
		System.exit(0);
	}
}
