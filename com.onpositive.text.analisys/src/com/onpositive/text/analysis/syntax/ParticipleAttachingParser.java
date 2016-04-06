package com.onpositive.text.analysis.syntax;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import com.carrotsearch.hppc.IntOpenHashSet;
import com.onpositive.semantic.wordnet.AbstractWordNet;
import com.onpositive.semantic.wordnet.MeaningElement;
import com.onpositive.text.analysis.BaseArrayInspector;
import com.onpositive.text.analysis.IToken;
import com.onpositive.text.analysis.IToken.Direction;

public class ParticipleAttachingParser extends AbstractSyntaxParser {

	public ParticipleAttachingParser(AbstractWordNet wordNet) {
		super(wordNet);
	}
	
	private BaseArrayInspector baseArrayInspector = new BaseArrayInspector() {
		
		int commaCount = 0;
		
		@Override
		protected ArrayProcessingReesult match(IToken token, Direction dir) {		
			if(nounMatch.match(token)){
				return ACCEPT;
			}
			else if(verbMatch.match(token)){
				return BREAK;
			}			
			MeaningElement conjToken = ParticipleAttachingParser.this.getConjugation(token);			
			if(conjToken!=null){
					
			}
			if(ParticipleAttachingParser.this.isComma(token)){
				if(dir==Direction.END){
					return BREAK;
				}
				commaCount++;
				if(dir==Direction.START){
					if(commaCount>1){
						return BREAK;
					}
				}
			}
			return CONTINUE;
		}

		@Override
		protected void prepare() {
			commaCount = 0;
		}
	};
	
	private SyntaxTokenAttacher attacher = new SyntaxTokenAttacher();

	@Override
	protected void combineTokens(Stack<IToken> sample,ProcessingData processingData)
	{
		IToken token = sample.peek();
		List<IToken> baseTokens = getBaseTokens();
		IntOpenHashSet baseTokenIDs = getBaseTokenIDs();
		List<IToken> lNouns = baseArrayInspector.findToken(token, Direction.START, baseTokens,baseTokenIDs);
		List<IToken> abjustedLNouns = adjust(lNouns,token);
		List<IToken> rNouns = baseArrayInspector.findToken(token, Direction.END, baseTokens,baseTokenIDs);
		List<IToken> abjustedRNouns = adjust(rNouns,token);
		if(!abjustedLNouns.isEmpty()&&!abjustedRNouns.isEmpty()){
			return;
		}
		if(!abjustedLNouns.isEmpty()){
			for(IToken t : abjustedLNouns){
				attacher.attachToken(token, t, IToken.TOKEN_TYPE_NOUN_PARTICIPLE);
			}
		}
		if(!abjustedRNouns.isEmpty()){
			for(IToken t : abjustedRNouns){
				attacher.attachToken(token, t, IToken.TOKEN_TYPE_NOUN_PARTICIPLE);
			}
		}
	}

	private List<IToken> adjust(List<IToken> list, IToken token) {
		ArrayList<IToken> result = new ArrayList<IToken>();
		for(IToken t : list){
			SyntaxToken combined = combineNames((SyntaxToken)token, (SyntaxToken)t, IToken.TOKEN_TYPE_NOUN_PARTICIPLE);
			if(combined != null){
				result.add(t);
			}
		}
		return result;
	}

	@Override
	protected ProcessingResult checkToken(IToken newToken) {
		if(prepMatch.match(newToken)){
			return DO_NOT_ACCEPT_AND_BREAK;
		}
		if(participleMatch.match(newToken)){
			return ACCEPT_AND_BREAK;
		}		
		return DO_NOT_ACCEPT_AND_BREAK;
	}
	
	@Override
	public boolean isHandleBounds() {
		return false;
	}

}
