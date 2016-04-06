package com.onpositive.text.analysis.syntax;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import com.onpositive.semantic.wordnet.AbstractWordNet;
import com.onpositive.semantic.wordnet.Grammem.PartOfSpeech;
import com.onpositive.text.analysis.IToken;
import com.onpositive.text.analysis.IToken.Direction;
import com.onpositive.text.analysis.TokenVisitor;
import com.onpositive.text.analysis.lexic.WordFormToken;
import com.onpositive.text.analysis.rules.matchers.UnaryMatcher;

public class VerbGerundParser extends AbstractSyntaxParser {

	public VerbGerundParser(AbstractWordNet wordNet) {
		super(wordNet);
	}

	
	protected static final UnaryMatcher<SyntaxToken> gerundMatch = hasAny(PartOfSpeech.GRND);
	
	@Override
	protected void combineTokens(Stack<IToken> sample, ProcessingData processingData) {
		
		List<SyntaxToken> words = extractWords(sample);
		
		if (words.size() < 2) {
			return;
		}

		SyntaxToken[] orderedTokens = new SyntaxToken[3];
		
		if(gerundMatch.match(words.get(0))){
			orderedTokens[1] = words.get(0);
			orderedTokens[2] = words.get(1);
		}
		else if(gerundMatch.match(words.get(1))){
			orderedTokens[0] = words.get(0);
			orderedTokens[1] = words.get(1);
			if(words.size()>2){
				orderedTokens[2] = words.get(2);
			}
		}
		else if(words.size()>2&&gerundMatch.match(words.get(2))){
			orderedTokens[0] = words.get(1);
			orderedTokens[1] = words.get(2);
		}
		else{
			return;
		}
		
		SyntaxToken newToken = appendGerund( orderedTokens, sample, processingData);
		if(newToken==null){
			
			return;
		}
		else if (checkParents(newToken, sample)) {
			processingData.addReliableToken(newToken);
		}
		else{
			processingData.setStop(true);
		}
	}

	private SyntaxToken appendGerund(SyntaxToken[] orderedTokens,Stack<IToken> sample, ProcessingData pd) {

		VisitorDump lDump = prepareStructure(orderedTokens[0], Direction.END);
		VisitorDump rDump = prepareStructure(orderedTokens[2], Direction.START);
		
		SyntaxToken gerundToken = orderedTokens[1];
		int gInd = sample.indexOf(gerundToken);
		
		int gStartPos = gerundToken.getStartPosition();
		if(gInd>0){
			IToken prev = sample.get(gInd-1);
			if(prev.getType()==IToken.TOKEN_TYPE_SYMBOL){
				gStartPos = prev.getStartPosition();
			}			
		}
		
		int gEndPos = gerundToken.getEndPosition();
		if(gInd<sample.size()-1){
			IToken next = sample.get(gInd+1);
			if(next.getType()==IToken.TOKEN_TYPE_SYMBOL){
				gEndPos = next.getEndPosition();
			}			
		}
		
		SyntaxToken result = null;		
		if(selectLeftVerb(lDump,rDump)){
			result = doAppendGerund(gerundToken,lDump, gStartPos, gEndPos, pd);
		}
		else{
			result = doAppendGerund(gerundToken,rDump, gStartPos, gEndPos, pd);
		}
		return result;
	}

	private SyntaxToken doAppendGerund(SyntaxToken gerundToken, VisitorDump dump, int sp, int ep, ProcessingData pd) {
		
		VerbSet vs = dump.getNonModalVerb();
		if(vs==null){
			vs = dump.getModalVerb();
		}
		
		SyntaxToken verbToken = vs.getVerb();
		SyntaxToken parent = vs.getParent();
		ClauseToken clause = dump.getClause();
		
		if ((verbToken.childrenCount() > 0 && verbToken.getChildren().contains(gerundToken)) || (parent != null && parent.childrenCount() > 0 && parent.getChildren().contains(gerundToken))) 
			return null;
		
		int startPosition = Math.min(verbToken.getStartPosition(), sp);
		int endPosition = Math.max(verbToken.getEndPosition(), ep);
		SyntaxToken newToken = new SyntaxToken(IToken.TOKEN_TYPE_VERB_GERUND, verbToken, null, startPosition, endPosition);
		if(parent!=null){
//			parent.replaceChild(verbToken,newToken);
//			newToken.addChild(verbToken);
//			newToken.addChild(gerundToken);
//			verbToken.replaceParent(parent, newToken);
//			gerundToken.addParent(newToken);
			
			pd.addChangedToken(newToken);
			newToken.setId(getTokenIdProvider().getVacantId());
			return null;
		}
		else if(clause != null){
			clause.setPredicate(newToken);
			newToken.setId(getTokenIdProvider().getVacantId());
			return null;
		}
		else{
			return newToken;
		}
	}

	private boolean selectLeftVerb(VisitorDump lDump, VisitorDump rDump) {
		if(lDump==null||lDump.getNonModalVerb()==null){
			return false;
		}
		else if(rDump==null||rDump.getNonModalVerb()==null){
			return true;
		}
		
		return false;
	}

	protected VisitorDump prepareStructure(SyntaxToken token, Direction dir)
	{
		if(token==null){
			return null;
		}
		SyntaxToken verb = null;
		ClauseToken clauseToken = null;
		if(token instanceof ClauseToken){		
			clauseToken = (ClauseToken) token;
			verb = clauseToken.getPredicate();
		}
		else{
			verb = token;
		}
		VerbVisitor visitor = new VerbVisitor();
		visitor.visit(verb, null, dir);
		VisitorDump dump = visitor.getDump();
		dump.setClause(clauseToken); 
		return dump;
	}

	@Override
	protected ProcessingResult continuePush(Stack<IToken> sample, IToken newToken) {
		
		List<SyntaxToken> words = extractWords(sample);
		IToken last = sample.peek();
		if(isComma(newToken)){
			if(isComma(last)){
				return DO_NOT_ACCEPT_AND_BREAK;
			}
			if(gerundMatch.match(last)){
				return words.size() < 3 ? CONTINUE_PUSH : ACCEPT_AND_BREAK;
			}
			boolean hasNextGerund = hasNextGerund(newToken);
			return hasNextGerund ? CONTINUE_PUSH : DO_NOT_ACCEPT_AND_BREAK;
		}
		
		int verbCount = 0;
		int gerundCount = 0;
		for(IToken t : words){
			if(verbMatch.match(t)){
				verbCount++ ;
			}
			else{
				gerundCount++ ;
			}
		}
		
		if(verbMatch.match(newToken)){
			if(verbCount < 2){
				return CONTINUE_PUSH;
			}
		}
		
		if(gerundMatch.match(newToken)){
			if(gerundCount < 1){
				return CONTINUE_PUSH;
			}
		}
		return DO_NOT_ACCEPT_AND_BREAK;
	}

	private List<SyntaxToken> extractWords(Stack<IToken> sample) {
		
		ArrayList<SyntaxToken> words = new ArrayList<SyntaxToken>();
		for(IToken t : sample){
			if(t instanceof SyntaxToken){
				words.add((SyntaxToken) t);
			}
		}		
		return words;
	}

	protected boolean matchTokensCouple(Stack<IToken> sample) {
		return true;
	}

	@Override
	protected ProcessingResult checkToken(IToken newToken) {
		
		if(prepMatch.match(newToken)){
			return DO_NOT_ACCEPT_AND_BREAK;
		}
		
		if(isComma(newToken)){
			boolean hasNextGerund = hasNextGerund(newToken);
			return hasNextGerund ? CONTINUE_PUSH : DO_NOT_ACCEPT_AND_BREAK;
		}		
		
		if (verbMatch.match(newToken)||gerundMatch.match(newToken)) {
			return CONTINUE_PUSH;
		}
		if(newToken.getType()==IToken.TOKEN_TYPE_CLAUSE){
			return CONTINUE_PUSH;
		}
		return DO_NOT_ACCEPT_AND_BREAK;
	}

	protected boolean hasNextGerund(IToken newToken) {
		boolean result = false;
		IToken next = newToken.getNext();
		if(next!=null&&gerundMatch.match(next)){
			result = true;
		}
		else{
			List<IToken> nextTokens = newToken.getNextTokens();
			if(nextTokens!=null){
				for(IToken n : nextTokens){
					if(gerundMatch.match(n)){
						result = true;
						break;
					}						
				}
			}
		}
		return result;
	}

	
	private class VerbVisitor extends TokenVisitor{

		private VisitorDump dump = new VisitorDump();
		
		@Override
		protected int inspectNode(IToken token, IToken parent) {
			
			if(!(token instanceof SyntaxToken)){
				return CONTINUE;
			}
			
			if(verbMatch.match(token)){
				if(isModalLikeVerb((SyntaxToken) token)){
					if(dump.getNonModalVerb().getVerb()==null){
						updateDump((SyntaxToken)token,(SyntaxToken)parent,dump.getModalVerb());						
					}
				}
				else{
					if(!updateDump((SyntaxToken)token,(SyntaxToken)parent,dump.getNonModalVerb())){
						return STOP;
					}
				}
			}			
			return CONTINUE;
		}
		
		private boolean updateDump(SyntaxToken verb, SyntaxToken parent, VerbSet vs) {
			
			SyntaxToken verbMainGroup = verb.getMainGroup();
			if(vs.getVerb()==null||vs.getMainGroup()==verbMainGroup){
				vs.setVerb(verb);
				vs.setParent(parent);
				vs.setMainGroup(verbMainGroup);
				return true;
			}
			return false;
		}

		@Override
		protected boolean needVisitChildren(IToken token, IToken parent) {
			boolean result = (!(token instanceof WordFormToken)&&token instanceof SyntaxToken);
			return result;
		}

		public VisitorDump getDump() {
			return dump;
		}

		
	}
	
	
	private class VisitorDump{
		
		private VerbSet nonModalVerb = new VerbSet();
		
		private VerbSet modalVerb = new VerbSet();
		
		private ClauseToken clause;

		public VerbSet getNonModalVerb() {
			return nonModalVerb;
		}

		public VerbSet getModalVerb() {
			return modalVerb;
		}

		public ClauseToken getClause() {
			return clause;
		}

		public void setClause(ClauseToken clause) {
			this.clause = clause;
		}		
	}
	
	private class VerbSet{
		
		private SyntaxToken verb;
		
		private SyntaxToken parent;
		
		private SyntaxToken mainGroup;

		public SyntaxToken getVerb() {
			return verb;
		}

		public void setVerb(SyntaxToken verb) {
			this.verb = verb;
		}

		public SyntaxToken getParent() {
			return parent;
		}

		public void setParent(SyntaxToken parent) {
			this.parent = parent;
		}

		public SyntaxToken getMainGroup() {
			return mainGroup;
		}

		public void setMainGroup(SyntaxToken mainGroup) {
			this.mainGroup = mainGroup;
		}
	}

}
