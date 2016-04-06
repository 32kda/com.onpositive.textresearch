package com.onpositive.text.analisys.tests.euristics;

import com.onpositive.semantic.wordnet.Grammem.PartOfSpeech;

public class OmonimyPair {
	
	public final PartOfSpeech part1;
	public final PartOfSpeech part2;
	private int rightCount;
	
	public OmonimyPair(PartOfSpeech part1, PartOfSpeech part2) {
		super();
		if (part1.intId < part2.intId) {
			this.part1 = part1;
			this.part2 = part2;
		} else {
			this.part2 = part1;
			this.part1 = part2;
		}
	}

	public int getRightCount() {
		return rightCount;
	}
	
	public void incRightCount() {
		rightCount++;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((part1 == null) ? 0 : part1.hashCode());
		result = prime * result + ((part2 == null) ? 0 : part2.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		OmonimyPair other = (OmonimyPair) obj;
		if (part1 == null) {
			if (other.part1 != null)
				return false;
		} else if (!part1.equals(other.part1))
			return false;
		if (part2 == null) {
			if (other.part2 != null)
				return false;
		} else if (!part2.equals(other.part2))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "[" + part1 + ", " + part2 + "]";
	}
}
