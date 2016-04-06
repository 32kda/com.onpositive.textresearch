package com.onpositive.text.analysis;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;

import com.carrotsearch.hppc.ObjectIntOpenHashMap;
import com.carrotsearch.hppc.ShortIntOpenHashMap;
import com.carrotsearch.hppc.ShortObjectOpenHashMap;
import com.carrotsearch.hppc.cursors.ObjectIntCursor;
import com.onpositive.text.analysis.lexic.StringToken;
import com.onpositive.text.analysis.lexic.SymbolToken;
import com.onpositive.text.analysis.lexic.WordFormToken;

public class WordRelationEvaluator extends AbstractRelationEvaluator {

	public static final String WORDDATA_FILE_NAME = "worddata.dat";
	public static final String CONFIG_DIR = System.getProperty("engineConfigDir");
	
	public WordRelationEvaluator() {
		load(CONFIG_DIR + "/" + WORDDATA_FILE_NAME);
	}
	
	void load(String filename) {
		try {
			List<String> lines = Files.readAllLines(Paths.get(filename));
			for (String line : lines) {
				String[] parts = line.split("\t");
				if (parts.length != 5) continue;
				String fw = parts[0],
					   sw = parts[1];
				short ft = Short.parseShort(parts[2]),
					  st = Short.parseShort(parts[3]);
				
				int count = Integer.parseInt(parts[4]);
				
				ShortObjectOpenHashMap<HashMap<String, ShortIntOpenHashMap>> P1 = data.get(fw);
				if (P1 == null) {
					P1 = new ShortObjectOpenHashMap<>();
					data.put(fw, P1);
				}
				HashMap<String, ShortIntOpenHashMap> P2 = P1.get(ft);
				if (P2 == null) {
					P2 = new HashMap<>();
					P1.put(ft, P2);
				}
				ShortIntOpenHashMap P3 = P2.get(sw);
				if (P3 == null) {
					P3 = new ShortIntOpenHashMap();
					P2.put(sw, P3);
				}
				P3.put(st,  count);
			}
		} catch (IOException e) {
			data = null;
		}
	}
	
	HashMap<String, ShortObjectOpenHashMap<HashMap<String, ShortIntOpenHashMap>>> data = new HashMap<>();
	
	@Override
	public void clear() {}

	private int count(WordFormToken first, WordFormToken second) {
		try {
			return data.get(first.getBasicForm()).get(first.getGrammemCode()).get(second.getBasicForm()).get(second.getGrammemCode());
		} catch (NullPointerException e) {
			return 0;
		}
	}
	
	private int count(WordFormToken wft) {
		WordFormToken[] ps = prevs(wft),
				ns = nexts(wft);

		int res = 0;
		
		for (WordFormToken p : ps) 
			res += count(p, wft);
		
		for (WordFormToken n: ns)
			res += count(wft, n);

		return res;
	}
	
	@Override
	protected WeightedProbability calculate(IToken token) {
		if (data == null || (token instanceof StringToken || token instanceof SymbolToken) || (token instanceof WordFormToken && token.getConflicts().size() == 0))
			return WeightedProbability.True;

		if (token instanceof WordFormToken) {
			WordFormToken wft = (WordFormToken) token;
			ObjectIntOpenHashMap<WordFormToken> cdata = new ObjectIntOpenHashMap<>();
			int ac = 0;
			int dcount = 0;
			for (IToken ch : wft.getConflicts()) {
				if (!(ch instanceof WordFormToken)) continue;
				WordFormToken cw = (WordFormToken) ch;
				
				int d = count(cw);
				cdata.put(cw, d);
				ac += d;
				if (d > 0) dcount += 1;
			}
			int d = count(wft);
			cdata.put(wft, d);
			ac += d;
			if (d > 0) dcount += 1;
			
			if (ac > 0) {
				for (ObjectIntCursor<WordFormToken> cd : cdata) {
					if (cd.value > 0)
						save(cd.key, new WeightedProbability((double) cd.value / ac, dcount > 1 ? 1.0 : 10.0)); 
				}
				return new WeightedProbability((double) d / ac, dcount > 1 ? 1.0 : 10.0);
			} else return WeightedProbability.False;
		} else {
			if (token.getChildren() == null) return WeightedProbability.False;
			double P = 1;
			for (IToken ch : token.getChildren()) P *= ch.getCorrelation();
			return new WeightedProbability(P, 1.0);
		}

	}

}
