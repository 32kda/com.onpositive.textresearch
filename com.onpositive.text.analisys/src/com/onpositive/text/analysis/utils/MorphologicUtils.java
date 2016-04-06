package com.onpositive.text.analysis.utils;

import java.util.ArrayList;
import java.util.List;

import com.onpositive.text.analysis.IToken;

public class MorphologicUtils {

	public static List<IToken> getWithNoConflicts(List<IToken> source) {
		List<IToken> workingCopy = new ArrayList<IToken>(source);
		for (int i = 0; i < workingCopy.size(); i++) {
			if (workingCopy.get(i).getConflicts() != null || !workingCopy.get(i).getConflicts().isEmpty()) {
				List<IToken> conflicts = new ArrayList<IToken>(workingCopy.get(i).getConflicts());
				conflicts.add(0, workingCopy.get(i));
				while (i+1 < workingCopy.size() && conflicts.contains(workingCopy.get(i+1))) {
					workingCopy.remove(i+1);
				}
			}
		}
		return workingCopy;
	}

	
}
