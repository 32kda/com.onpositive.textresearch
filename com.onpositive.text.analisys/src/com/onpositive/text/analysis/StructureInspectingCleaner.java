package com.onpositive.text.analysis;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.onpositive.text.analysis.CanonicalComparator.TokenCanonicCode;
import com.onpositive.text.analysis.syntax.SyntaxCanonicalComparator;

public class StructureInspectingCleaner implements ITokenCleaner {

	
	private BasicCleaner bc = new BasicCleaner();
	
	@Override
	public List<IToken> clean(List<IToken> tokens) {
		
		List<IToken> tokens0 = bc.clean(tokens);
		
		CanonicalComparator cc = new SyntaxCanonicalComparator();
		HashSet<CanonicalComparator.TokenCanonicCode> set = new HashSet<CanonicalComparator.TokenCanonicCode>();
		ArrayList<IToken> result = new ArrayList<IToken>();
		for(IToken t : tokens0){
			TokenCanonicCode code = cc.buildTokenCode(t);
			if(set.contains(code)){
				continue;
			}
			set.add(code);
			result.add(t);
		}
		//CanonicalComparator.TokenCanonicCode[] arr = set.toArray(new CanonicalComparator.TokenCanonicCode[set.size()]);
		return result;
	}

}
