package com.onpositive.text.analysis;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import com.carrotsearch.hppc.ShortObjectOpenHashMap;
import com.onpositive.text.analysis.lexic.WordFormToken;

public class MarkovChain {
	
	private short tag = -1;
	private int count = 0;
	private MarkovChain parent = null;
	private ShortObjectOpenHashMap<MarkovChain> nexts = new ShortObjectOpenHashMap<>();
	
	public int getCount(short tag) {
		MarkovChain ch = get(tag);
		return ch == null ? 0 : ch.getCount();
	}
	
	public int getCount() { return count; }
	
	public double getWeight() {
		if (parent == null) return 1.0;
		else return (double) count / parent.count;
	}
	
	public short getTag() { return tag; }
	public MarkovChain get(short tag) { return nexts.get(tag); } 
	
	public double P(WordFormToken wft, short ... tags) {
		MarkovChain ch = this;
		for (int i = 0; i < tags.length; i++) {
			ch = ch.get(tags[i]);
			if (ch == null) return 0.0;
		}
		long count = 0;
		for (IToken c : wft.getConflicts()) count += ch.getCount(((WordFormToken)c).getGrammemCode());
		
		double curr = ch.getCount(wft.getGrammemCode());
		
		return count == 0 ? (curr == 0 ? 0.0 : 1.0) : curr / (count + curr);
	}
	
	public static MarkovChain load(File input) {
		try {
			DataInputStream in = new DataInputStream(new FileInputStream(input));
			MarkovChain res = load(in);
			in.close();
			return res;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private static MarkovChain load(DataInputStream in) {
		try {
			short tag = in.readShort();
			int elCount = in.readInt();
			int chCount = in.readInt();
			
			MarkovChain result = new MarkovChain();
			result.tag = tag;
			result.count = elCount;
			for (int i = 0; i < chCount; i++) {
				MarkovChain child = MarkovChain.load(in);
				child.parent = result;
				result.nexts.put(child.tag, child);
			}
			return result;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}
