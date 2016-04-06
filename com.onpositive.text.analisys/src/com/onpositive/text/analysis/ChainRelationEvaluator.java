package com.onpositive.text.analysis;

import java.io.File;

import com.onpositive.text.analysis.lexic.StringToken;
import com.onpositive.text.analysis.lexic.SymbolToken;
import com.onpositive.text.analysis.lexic.WordFormToken;

public class ChainRelationEvaluator extends AbstractRelationEvaluator {

	public static final String CHAINDATA_FILE_NAME = "chains.dat";
	public static final String CONFIG_DIR = System.getProperty("engineConfigDir");
	
	
	private MarkovChain chains = null;
	
	public ChainRelationEvaluator() {
		chains = MarkovChain.load(new File(CONFIG_DIR + "/" +  CHAINDATA_FILE_NAME));
	}
	
	
	@Override
	public void clear() {}

	
	private double L1 =  0.1, L2 = 0.2, L3 = 0.7;	
		
	private WeightedProbability calcPP(WordFormToken token) {
		if (token == null || token.getPreviousTokens() == null) return WeightedProbability.False;
	
		short k = token.getGrammemCode();
		double W1 = chains.P(token);
		
		double W2 = 0.0, W3 = 0.0;
		int W2c = 0, W3c = 0;
		
		WordFormToken[] ps = prevs(token);
		WordFormToken[] ns = nexts(token);
		
		for (WordFormToken p : ps) {
			short k1 = p.getGrammemCode();
			W2 = (W2 * W2c + chains.P(token, k1)) / (++W2c);
			
			for (WordFormToken n : ns)
				W3 = (W3 * W3c + chains.P(n, k1, k)) / (++W3c);
			
			WordFormToken[] pps = prevs(p);
			for (WordFormToken pp : pps) {
				short k0 = pp.getGrammemCode();
				W3 = (W3 * W3c + chains.P(token, k0, k1)) / (++W3c);
			}
		}
		
		for (WordFormToken n : ns) {
			short k1 = n.getGrammemCode();
			W2 = (W2 * W2c + chains.P(n, k)) / (++W2c);
			
			WordFormToken[] nns = nexts(n);
			for (WordFormToken nn : nns) W3 = (W3 * W3c + chains.P(nn, k, k1)) / (++W3c);
		}
		
		double l21 = L1 / (L1 + L2), l22 = L2 / (L1 + L2);
		
		if (W2c == 0) return new WeightedProbability(W1, 0.2);
		
		else if (W3c == 0) return new WeightedProbability(l21 * W1 + l22 * W2, 0.65);
		else return new WeightedProbability(L1 * W1 + L2 * W2 + L3 * W3, 1.0);
	}
	
	
	@Override
	protected WeightedProbability calculate(IToken token) {
		if (chains == null || (token instanceof StringToken || token instanceof SymbolToken) || (token instanceof WordFormToken && token.getConflicts().size() == 0))
			return WeightedProbability.True;

		if (token instanceof WordFormToken) {
			WordFormToken wft = (WordFormToken) token;
			return calcPP(wft);
		} else {
			if (token.getChildren() == null) return WeightedProbability.False;
			double P = 1;
			for (IToken ch : token.getChildren()) P *= ch.getCorrelation();
			return new WeightedProbability(P, 1.0);
		}
	}

}
