package com.onpositive.text.analysis;

import java.io.*;
import java.util.*;

import com.carrotsearch.hppc.*;
import com.onpositive.text.analysis.lexic.*;
import com.onpositive.semantic.wordnet.Grammem.PartOfSpeech;

public class TripletRelationEvaluator extends AbstractRelationEvaluator {
	
	ObjectIntOpenHashMap<PartOfSpeech> parts;
	IntObjectOpenHashMap<IntObjectOpenHashMap<IntIntOpenHashMap>> tripletsv = new IntObjectOpenHashMap<IntObjectOpenHashMap<IntIntOpenHashMap>>();	
	
	public TripletRelationEvaluator() {
		parts = new ObjectIntOpenHashMap<PartOfSpeech>();
		
		parts.put(PartOfSpeech.NOUN,1);
		parts.put(PartOfSpeech.ADJF,2);
		parts.put(PartOfSpeech.ADJS,4);
		parts.put(PartOfSpeech.COMP,8);
		parts.put(PartOfSpeech.VERB,16);
		parts.put(PartOfSpeech.INFN,32);
		parts.put(PartOfSpeech.PRTF,64);
		parts.put(PartOfSpeech.PRTS,128);
		parts.put(PartOfSpeech.GRND,256);
		parts.put(PartOfSpeech.NUMR,512);
		parts.put(PartOfSpeech.ADVB,1024);
		parts.put(PartOfSpeech.NPRO,2048);
		parts.put(PartOfSpeech.PRED,8192);
		parts.put(PartOfSpeech.PREP,16384);
		parts.put(PartOfSpeech.CONJ,32768);
		parts.put(PartOfSpeech.PRCL,65536);
		parts.put(PartOfSpeech.INTJ,131072);
	
		String fname = System.getProperty("engineConfigDir") + "/triplets.dat";
		try {
			readTriplets(fname);
		} catch (IOException e) {
			triplets = null;
		}
	}
	
	LongIntOpenHashMap triplets = new LongIntOpenHashMap();
	
	long fullcount = 0;
	
	void put(int a, int b, int c, int value) {
		if (!tripletsv.containsKey(a)) tripletsv.put(a, new IntObjectOpenHashMap<IntIntOpenHashMap>());
		IntObjectOpenHashMap<IntIntOpenHashMap> f = tripletsv.get(a); 
		if (!f.containsKey(b)) f.put(b, new IntIntOpenHashMap());
		IntIntOpenHashMap s = f.get(b);
		int val = s.containsKey(c) ? s.get(c) : 0;
		s.put(c, val + value);
	}
	
	private void readTriplets(String fname) throws IOException {
		DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(fname)));
		int[] k = new int[3];
		try {
			while (true) {
				boolean ok = true;				
				for (int i = 0; i < 3; i++) {
					k[i] = in.readInt();
					ok &= (k[i] != 0) && ((k[i] & (k[i] - 1)) == 0);
				}
				int value = in.readInt();
				if (ok) {
					triplets.put(pack(k[0], k[1], k[2]), value);
					put(k[0], k[1], k[2], value);
					fullcount += value;
				}			
			}
		} catch (EOFException eof) {}
		finally{
			in.close();
		}	
	}
	private IntIntOpenHashMap tvm = new IntIntOpenHashMap();
	
	@Override
	public WeightedProbability calculate(IToken token) {
		if (triplets == null || (token instanceof StringToken || token instanceof SymbolToken) || (token instanceof WordFormToken && token.getConflicts().size() == 0))
			return WeightedProbability.True;
		
		if (token instanceof WordFormToken) {
			tvm.clear();
			int glc = 0;
			
			for (IToken c : token.getConflicts()) { 
				int val = calcWithTriplets((WordFormToken) c);
				tvm.put(c.id(), val);
				glc += val;				
			}
			
			int val = calcWithTriplets((WordFormToken) token);
			tvm.put(token.id(), val);
			glc += val;
			
			double div = 1.0 / glc;
			if (div == Double.NaN || Double.isInfinite(div)) return WeightedProbability.True;
			for (IToken c : token.getConflicts()) save(c, new WeightedProbability(tvm.get(c.id()) * div, 1.0));
			return new WeightedProbability(tvm.get(token.id()) * div, 1.0);
		} else {
			if (token.childrenCount() == 0) return WeightedProbability.True;
			double cor = 1.0;
			for (IToken c : token.getChildren()) cor *= c.getCorrelation();
			return new WeightedProbability(cor, 1.0);
		}
	}
	
	public class Triplet<T> {
		public T first;
		public T second;
		public T third;
		
		public Triplet(T first, T second, T third) {
			this.first = first;
			this.second = second;
			this.third = third;
		}
	}
	
	private long pack(int... x) {
		if (x.length != 3) return -1;
		long res = 0;
		for (int i = 0; i < 3; i++) {		
			res = (res << 18) + x[i];
		}
		return res;
	}
		
	private List<Long> getFirst(WordFormToken wft) {
		List<Long> res = new ArrayList<Long>();
		
		int f = parts.get(wft.getPartOfSpeech());
		
		List<IToken> ns = wft.getNextTokens(); 
		if (ns == null || ns.size() == 0) return res; 		
		
		for (IToken n : ns) {
			if (!(n instanceof WordFormToken)) continue;
			int s = parts.get(((WordFormToken)n).getPartOfSpeech());
			List<IToken> nns = n.getNextTokens(); 
			if (nns == null || nns.size() == 0) continue;
			for (IToken nn : nns) {
				if (!(nn instanceof WordFormToken)) continue;
				int t = parts.get(((WordFormToken)nn).getPartOfSpeech());
				res.add(pack(f,s,t));
			}
		}
		return res;
	}
	
	private List<Long> getSecond(WordFormToken wft) {
		List<Long> res = new ArrayList<Long>();
		
		List<IToken> ns = wft.getNextTokens(); 
		if (ns == null || ns.size() == 0) return res; 		
		WordFormToken[] ans = ns.stream().filter(x->x instanceof WordFormToken).map(x->(WordFormToken)x).toArray(WordFormToken[]::new);
		
		List<IToken> ps = wft.getPreviousTokens(); 
		if (ps == null || ps.size() == 0) return res; 		
		WordFormToken[] aps = ps.stream().filter(x->x instanceof WordFormToken).map(x->(WordFormToken)x).toArray(WordFormToken[]::new);
		
		int s = parts.get(wft.getPartOfSpeech());
		
		for (WordFormToken n : ans)
			for (WordFormToken p : aps) {
				int f = parts.get(p.getPartOfSpeech()),
					t = parts.get(n.getPartOfSpeech()); 
				res.add(pack(f,s,t));
			}
		return res;
	}
	
	private List<Long> getThird(WordFormToken wft) {
		List<Long> res = new ArrayList<Long>();
		
		int t = parts.get(wft.getPartOfSpeech());
		
		List<IToken> ps = wft.getPreviousTokens(); 
		if (ps == null || ps.size() == 0) return new ArrayList<Long>(); 		
		
		for (IToken p : ps) {
			if (!(p instanceof WordFormToken)) continue;
			int s = parts.get(((WordFormToken)p).getPartOfSpeech());
			List<IToken> pps = p.getPreviousTokens(); 
			if (pps == null || pps.size() == 0) continue;
			for (IToken pp : pps) {
				if (!(pp instanceof WordFormToken)) continue;
				int f = parts.get(((WordFormToken)pp).getPartOfSpeech());
				res.add(pack(f,s,t));
			}
		}
		return res;
	}
		
	private int calcWithTriplets(WordFormToken wft) {
		List<Long> res = new ArrayList<Long>();
		res.addAll(getFirst(wft));
		res.addAll(getSecond(wft));
		res.addAll(getThird(wft));
			
		return res.stream().map(x->this.triplets.get(x)).reduce(0, (x,y)->x+y);
	}	

	@Override
	public void clear() {}
}