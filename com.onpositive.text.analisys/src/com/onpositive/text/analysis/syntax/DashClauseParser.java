package com.onpositive.text.analysis.syntax;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;

import com.onpositive.semantic.wordnet.AbstractWordNet;
import com.onpositive.semantic.wordnet.GrammarRelation;
import com.onpositive.semantic.wordnet.Grammem;
import com.onpositive.semantic.wordnet.Grammem.Case;
import com.onpositive.semantic.wordnet.Grammem.PartOfSpeech;
import com.onpositive.semantic.wordnet.Grammem.TransKind;
import com.onpositive.semantic.wordnet.Grammem.VerbKind;
import com.onpositive.semantic.wordnet.MeaningElement;
import com.onpositive.text.analysis.IToken;
import com.onpositive.text.analysis.lexic.WordFormToken;
import com.onpositive.text.analysis.rules.matchers.UnaryMatcher;

public class DashClauseParser extends AbstractSyntaxParser {
	
	private static final UnaryMatcher<SyntaxToken> isNoun = hasAny( PartOfSpeech.NOUN, PartOfSpeech.NPRO);

	private static final UnaryMatcher<SyntaxToken> acceptedNomn	= hasAny(caseMatchMap.get(Case.NOMN));
	
	private static final Grammem[] checkEstGrammems = new Grammem[]{PartOfSpeech.VERB, TransKind.intr, VerbKind.IMPERFECT};
	
	private static final UnaryMatcher<SyntaxToken> checkEst	= hasAny(checkEstGrammems);
	
	private static final UnaryMatcher<SyntaxToken> checkNoun = and(isNoun, acceptedNomn, not(prepConjMatch));
	
	private static final UnaryMatcher<SyntaxToken> infnMatch
		= hasAll(PartOfSpeech.INFN);
	
	
	private HashSet<String> dashLikeSymbols = new HashSet<String>(Arrays.asList("-","—"));  


	public DashClauseParser(AbstractWordNet wordNet) {
		super(wordNet);
	}

	@Override
	protected void combineTokens(Stack<IToken> sample,ProcessingData processingData)
	{
		if(sample.size()<3){
			return;
		}
		IToken token0 = sample.get(0);
		IToken token1 = sample.get(1);
		IToken token2 = sample.get(2);
		
		SyntaxToken verbToken = null;
		if(token1.getType()==IToken.TOKEN_TYPE_SYMBOL){
			verbToken = getVerb(token1);
			verbToken.addChild(token1);
			token1.addParent(verbToken);
		}
		else{
			verbToken = (SyntaxToken) token1;
		}
		
		SyntaxToken predicateToken = createPredicate(verbToken,token2);
		int startPosition = token0.getStartPosition();
		int endPosition = token2.getEndPosition();//computeEndPosition(token2);
		
		ClauseToken ct = new ClauseToken((SyntaxToken) token0, predicateToken, startPosition, endPosition);
		if(checkParents(ct)){
			ArrayList<IToken> children = new ArrayList<IToken>(Arrays.asList(token0,predicateToken)); 
			ct.addChildren(children);
			for(IToken ch : children){
				ch.addParent(ct);
			}
			processingData.addReliableToken(ct);
		}
		else{
			discardPredicate(predicateToken);
		}
	}

	protected int computeEndPosition(IToken token) {
		
		int endPosition = token.getEndPosition();
		IToken next = token.getNext();
		if(next!=null){
			if(next.getType()==IToken.TOKEN_TYPE_SYMBOL&&next.getStringValue().equals(".")){
				endPosition = next.getEndPosition();
			}
		}
		else{
			List<IToken> nextTokens = token.getNextTokens();
			if(nextTokens!=null){
				for(IToken n : nextTokens){
					if(n.getType()==IToken.TOKEN_TYPE_SYMBOL&&n.getStringValue().equals(".")){
						endPosition = n.getEndPosition();
						break;
					}					
				}
			}
		}
		return endPosition;
	}
	
	private static void discardPredicate(SyntaxToken predicateToken) {
		
		SyntaxToken verbToken = predicateToken.getMainGroup();
		cleanParentship(verbToken);
		cleanParentship(predicateToken);
	}

	private boolean checkParents(ClauseToken clauseToken) {
		
		SyntaxToken subjToken = clauseToken.getSubject();
		SyntaxToken predicateToken = clauseToken.getPredicate();
		WordFormToken verbToken = predicateToken.getMainWord();
		IToken objectToken = null;
		List<IToken> children = predicateToken.getChildren();
		for(IToken ch : children){
			if(ch==verbToken){
				continue;
			}
			objectToken = ch;
			break;
		}
		
		List<IToken> parents = subjToken.getParents();
		if(parents!=null){
			for(IToken parent : parents){
				if(parent instanceof ClauseToken) {
					ClauseToken pClauseToken = (ClauseToken) parent;
					if(pClauseToken.getSubject()==subjToken){
						SyntaxToken pPredToken = pClauseToken.getPredicate();
						SyntaxToken pVerbToken = pPredToken.getMainGroup();
						List<IToken> pChildren = pPredToken.getChildren();
						IToken pObjectToken = null;
						for(IToken ch : pChildren){							
							if(ch==pVerbToken){
								continue;
							}
							pObjectToken = ch;
							break;
						}
						if(pObjectToken==objectToken){
							return false;
						}
					}
				}
			}
		}
		return true;
	}

	private SyntaxToken createPredicate(SyntaxToken verbToken, IToken objectToken) {
		
		int startPosition = Math.min(verbToken.getStartPosition(), objectToken.getStartPosition());
		int endPosition = Math.max(computeEndPosition(verbToken), objectToken.getEndPosition());
		
		SyntaxToken predicateToken = new SyntaxToken(IToken.TOKEN_TYPE_VERB_NOUN, verbToken, null, startPosition, endPosition);
		predicateToken.setId(getTokenIdProvider().getVacantId());
		predicateToken.addChild(verbToken);
		predicateToken.addChild(objectToken);
		verbToken.addParent(predicateToken);
		objectToken.addParent(predicateToken);
		
		return predicateToken;
	}

	private MeaningElement estElement = null;
	
	private SyntaxToken getVerb(IToken token1) {
		
		if(estElement == null){
			GrammarRelation[] possibleGrammarForms = this.wordNet.getPossibleGrammarForms("есть");
l0:			for(GrammarRelation gr : possibleGrammarForms){
				MeaningElement[] concepts = gr.getWord().getConcepts();
l1:				for(MeaningElement me : concepts){
					for(Grammem g : checkEstGrammems){
						if(!me.getGrammems().contains(g)){
							continue l1;
						}
					}
					estElement = me;
					break l0;
				}
			}
		}
		WordFormToken result = new WordFormToken(
				estElement.getParentTextElement(),
				new MeaningElement[]{estElement}, token1.getStartPosition(), token1.getEndPosition());
		result.setId(getTokenIdProvider().getVacantId());
		return result;
	}

	@Override
	protected ProcessingResult continuePush(Stack<IToken> sample,IToken newToken)
	{
		if(sample.size()==1){
			int type = newToken.getType();
			if(type == IToken.TOKEN_TYPE_SYMBOL){
				String val = newToken.getStringValue();
				if(dashLikeSymbols.contains(val)){
					return CONTINUE_PUSH;
				}
				else{
					return DO_NOT_ACCEPT_AND_BREAK;
				}
			}
			else if(newToken instanceof SyntaxToken){
				SyntaxToken sToken = (SyntaxToken) newToken;
				WordFormToken mainWord = sToken.getMainWord();
				if(mainWord==null){
					return DO_NOT_ACCEPT_AND_BREAK;
				}
				String val = mainWord.getStringValue();
				if(val.equals("есть")&&checkEst.match(newToken)){
					return CONTINUE_PUSH;
				}
				else{
					return DO_NOT_ACCEPT_AND_BREAK;
				}
			}
		}
		else if(sample.size()==2){
			if(checkNoun.match(newToken)){
				return ACCEPT_AND_BREAK;
			}
			
			if(infnMatch.match(newToken)){
				return ACCEPT_AND_BREAK;
			}
		}
		return DO_NOT_ACCEPT_AND_BREAK;
	}

	@Override
	protected ProcessingResult checkToken(IToken newToken) {
		
		if(checkNoun.match(newToken)){
			return CONTINUE_PUSH;
		}
		
		if(infnMatch.match(newToken)){
			return CONTINUE_PUSH;
		}
		
		return DO_NOT_ACCEPT_AND_BREAK;
	}

}
