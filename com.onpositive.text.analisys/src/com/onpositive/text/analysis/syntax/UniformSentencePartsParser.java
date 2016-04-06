package com.onpositive.text.analysis.syntax;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import com.onpositive.semantic.wordnet.AbstractWordNet;
import com.onpositive.semantic.wordnet.Grammem;
import com.onpositive.semantic.wordnet.Grammem.PartOfSpeech;
import com.onpositive.text.analysis.IToken;
import com.onpositive.text.analysis.lexic.SymbolToken;
import com.onpositive.text.analysis.lexic.WordFormToken;
import com.onpositive.text.analysis.rules.matchers.HasGrammem;
import com.onpositive.text.analysis.rules.matchers.UnaryMatcher;
import com.onpositive.text.analysis.syntax.SyntaxToken.GrammemSet;

public abstract class UniformSentencePartsParser extends AbstractSyntaxParser {
	
	private static final Set<String> noCommaConjunctions = new HashSet<String>(Arrays.asList(
		"и", "да", "или", "либо"
	));
		
		
	private static final Set<String> commaConjunctions = new HashSet<String>(Arrays.asList(
		"да", "но", "а" , "однако", "хотя", "и"
	));
		
		
	private static final Set<String> acceptedConjunctions = new HashSet<String>();
	static{
		acceptedConjunctions.addAll(noCommaConjunctions);
		acceptedConjunctions.addAll(commaConjunctions);
	}
	
	
	public UniformSentencePartsParser(AbstractWordNet wordNet, int tokenType, Collection<PartOfSpeech> acceptedParts) {
		this(wordNet, tokenType, acceptedParts.toArray(new PartOfSpeech[acceptedParts.size()]));
	}

	public UniformSentencePartsParser(AbstractWordNet wordNet, int tokenType, PartOfSpeech... acceptedParts) {
		super(wordNet);
		this.acceptedParts = acceptedParts;
		this.tokenType = tokenType;
		initMatchers();
	}
	
	private int tokenType;

	private PartOfSpeech[] acceptedParts;

	@Override
	protected void combineTokens(Stack<IToken> sample, ProcessingData processingData)
	{
		List<SyntaxToken> parts = collectParts(sample);
		if(parts.size()<2){
			return;
		}
		if(!checkParents(null,sample)){
			return;
		}
		HashSet<Grammem> occuredGrammems = new HashSet<Grammem>();
		List<GrammemSet> grammemSets = matchMembers(parts, occuredGrammems);
		int count = parts.size();
		if(count<2){
			return;
		}
		
		boolean isReliable = isReliable(grammemSets,occuredGrammems);
		
		SyntaxToken last = (SyntaxToken) parts.get(count-1);
		int startPosition = sample.get(0).getStartPosition();
		int endPosition = last.getEndPosition();

		if(isReliable){
			SyntaxToken newToken = new SyntaxToken(tokenType, last, grammemSets, startPosition, endPosition);
			processingData.addReliableToken(newToken);
		}
		else{
			SyntaxToken newToken = new SyntaxToken(tokenType, last, grammemSets, startPosition, endPosition, true);
			processingData.addDoubtfulToken(newToken);
		}
	}

	protected boolean isReliable(List<GrammemSet> grammemSets, Set<Grammem> occuredGrammems) {
		return true;
	}

	private List<GrammemSet> matchMembers(List<SyntaxToken> parts, Set<Grammem> grammems) {
		
		SyntaxToken token0 = (SyntaxToken)parts.get(0);
		List<GrammemSet> grammemSets = new ArrayList<SyntaxToken.GrammemSet>(token0.getGrammemSets());
		for(GrammemSet gs : grammemSets){
			grammems.addAll(gs.grammems());
		}
		List<GrammemSet> acceptedGrammemSets = new ArrayList<SyntaxToken.GrammemSet>();
		List<GrammemSet> tmpList;
		int size = parts.size();
		int i = 1;
		for( ; i < size ; i++ ){
			SyntaxToken token = parts.get(i);
			for(GrammemSet gs0 : grammemSets){
				for(GrammemSet gs1 : token.getGrammemSets()){
					grammems.addAll(gs1.grammems());
					GrammemSet matchedSet = checkGrammemSetCorrespondence(gs0,gs1);
					if(matchedSet!=null){
						acceptedGrammemSets.add(matchedSet);
						break;
					}
				}
			}
			if(acceptedGrammemSets.isEmpty()){
				break;
			}
			else{
				tmpList = grammemSets;
				grammemSets = acceptedGrammemSets;
				acceptedGrammemSets = tmpList;
				acceptedGrammemSets.clear();
			}
		}
		for(int j = size ; j > i ; j--){
			parts.remove(j-1);
		}
		return grammemSets;
	}

	protected abstract GrammemSet checkGrammemSetCorrespondence(GrammemSet gs0, GrammemSet gs1);

	private List<SyntaxToken> collectParts(Stack<IToken> tokens) {
		ArrayList<SyntaxToken> result = new ArrayList<SyntaxToken>();
		for(IToken token : tokens){
			if(conjunctionMatcher.match(token)){
				continue;
			}
			if(partOfSpeechMatcher.match(token)){
				result.add((SyntaxToken) token);
			}
		}
		return result;
	}
	
	@Override
	protected ProcessingResult continuePush(Stack<IToken> sample,IToken newToken)
	{
		IToken token0 = sample.peek();
		if(commaMatcher.match(token0)){
			return checkToken(newToken);
		}
		else if(conjunctionMatcher.match(token0)){
			return checkToken(newToken);
		}
		else if(partOfSpeechMatcher.match(token0)){
			if(conjunctionMatcher.match(newToken)){
				return CONTINUE_PUSH;
			}
			if(commaMatcher.match(newToken)){
				return CONTINUE_PUSH;
			}
			else if(noComma()&&partOfSpeechMatcher.match(newToken)){
				return ACCEPT_AND_BREAK;
			}
			else{	
				return DO_NOT_ACCEPT_AND_BREAK;
			}
		}		
		else{
			return DO_NOT_ACCEPT_AND_BREAK;
		}
	}

	protected boolean noComma() {
		return true;
	}

	@Override	
	protected ProcessingResult checkToken(IToken newToken) {
		
		if(!(newToken instanceof SyntaxToken)){
			return DO_NOT_ACCEPT_AND_BREAK;
		}
		
		SyntaxToken token1 = (SyntaxToken) newToken;
		if(token1.hasGrammem(PartOfSpeech.PREP)){
			return DO_NOT_ACCEPT_AND_BREAK;
		}
		
		if(or(partOfSpeechMatcher,conjunctionMatcher).match(newToken)){
			return CONTINUE_PUSH;
		}
		return DO_NOT_ACCEPT_AND_BREAK;
	}
	
	protected UnaryMatcher<SymbolToken> commaMatcher;
	
	protected UnaryMatcher<SyntaxToken> conjunctionMatcher;
	
	protected UnaryMatcher<SyntaxToken> partOfSpeechMatcher;
	
	private void initMatchers(){
		commaMatcher = new UnaryMatcher<SymbolToken>(SymbolToken.class) {
		
			@Override
			protected boolean innerMatch(SymbolToken token) {
				int type = token.getType();
				if(type != IToken.TOKEN_TYPE_SYMBOL){
					return false;
				}
				boolean result = token.getStringValue().equals(",");			
				return result;
			}
		};
		
		conjunctionMatcher = new UnaryMatcher<SyntaxToken>(SyntaxToken.class) {
		
			@Override
			protected boolean innerMatch(SyntaxToken token) {
				
				if(!(token instanceof WordFormToken)){
					return false;
				}
				
				String stringValue = token.getBasicForm();
				if(new HasGrammem(PartOfSpeech.CONJ).match(token)){			
					if(acceptedConjunctions.contains(stringValue)){
						return true;
					}
				}
				return false;
			}
		};
	
		partOfSpeechMatcher = hasAny(acceptedParts);
	}
	
	public boolean isRecursive() {
		return false;
	}
}
