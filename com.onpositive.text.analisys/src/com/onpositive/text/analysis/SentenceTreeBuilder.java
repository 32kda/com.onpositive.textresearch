package com.onpositive.text.analysis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;

import com.carrotsearch.hppc.IntIntMap;
import com.carrotsearch.hppc.IntIntOpenHashMap;
import com.carrotsearch.hppc.IntObjectOpenHashMap;
import com.carrotsearch.hppc.IntOpenHashSet;
import com.carrotsearch.hppc.cursors.IntCursor;
import com.onpositive.text.analysis.IToken.Direction;
import com.onpositive.text.analysis.basic.matchers.BasicRule;
import com.onpositive.text.analysis.basic.matchers.ITokenArrayMatcher;
import com.onpositive.text.analysis.basic.matchers.ITokenMatcher;

public abstract class SentenceTreeBuilder {
	
	protected abstract IntObjectOpenHashMap<List<IToken>> produceResultToken(SentenceNode node, IntIntMap parents, DecisionRule rule, List<IToken> regionTokens, TokenArrayBuffer buffer);
	
	protected abstract List<IToken> processContent(List<IToken> tokens, DecisionRule rule);
	
	protected abstract List<IToken> processBound(List<IToken> tokens, DecisionRule rule, Direction dir);
	
	protected abstract List<IToken> produceRegionToken(List<IToken> content, List<IToken> startBound, List<IToken> endBound, DecisionRule rule);
	
	public static class DecisionRule extends BasicRule{
		
		public DecisionRule(ITokenArrayMatcher startMatcher,
				ITokenArrayMatcher endMatcher) {
			super(startMatcher, endMatcher);
		}
		
		public DecisionRule(ITokenMatcher startMatcher,
				ITokenMatcher endMatcher) {
			super(startMatcher, endMatcher);
		}
		
		public DecisionRule(ITokenMatcher startMatcher,
				ITokenArrayMatcher endMatcher) {
			super(startMatcher, endMatcher);
		}
		
		public DecisionRule(String startStr, String endStr){
			super(startStr, endStr);
		}

		private int resultTokenType;
		
		private Direction parentDirection = Direction.START;

		public int getResultTokenType() {
			return resultTokenType;
		}

		public void setResultTokenType(int resultTokenType) {
			this.resultTokenType = resultTokenType;
		}

		public Direction getParentDirection() {
			return parentDirection;
		}

		public void setParentDirection(Direction parentDirection) {
			this.parentDirection = parentDirection;
		}

		public void setAcceptStart(boolean value) {
			this.startMatcher.setAcceptStart(value);
		}
		
		public void setAcceptEnd(boolean value) {
			this.endMatcher.setAcceptEnd(value);
		}
		
		public boolean isAcceptStart() {
			return this.startMatcher.isAcceptStart();
		}
		
		public boolean isAcceptEnd() {
			return this.endMatcher.isAcceptEnd();
		}
	}
	
	public static class TokenArrayBuffer{
		
		public TokenArrayBuffer(List<IToken> tokens) {
			super();
			this.tokens = new ArrayList<IToken>(tokens);
		}

		private ArrayList<IToken> tokens;
		
		private ArrayList<IToken> buf = new ArrayList<IToken>();
		
		private HashSet<TokenArrayBufferListener> listeners;
		
		public void replace(List<IToken> list, int start, int end){

			int newLength = list.size();
			
			buf.clear();
			buf.addAll(tokens.subList(0, start));
			buf.addAll(list);
			buf.addAll(tokens.subList(end, tokens.size()));
			
			removeRangeFromNeighbours(start, end, list);
			
			ArrayList<IToken> tmp = this.tokens;
			this.tokens = buf;
			buf = tmp;
			buf.clear();
			
			fireChanges(start,end-start,newLength);
		}

		protected void removeRangeFromNeighbours(int start, int end, List<IToken> list) {
			
			IntOpenHashSet neighboursToRemove = new IntOpenHashSet();
			for(int i = start; i < end ; i++){
				neighboursToRemove.add(tokens.get(i).id());
			}			
			List<IToken> startNeighbours = removeFromSide(start, neighboursToRemove, Direction.START);
			List<IToken> endNeighbours = removeFromSide(end-1, neighboursToRemove, Direction.END);
			
			if(list.isEmpty()){
				gluePieces(startNeighbours, endNeighbours);
			}
			else{
				int size = list.size();
				List<IToken> leftSide = list.subList(0, 1);				
				List<IToken> rightSide = list.subList(size-1, size);
				gluePieces(startNeighbours, leftSide);
				gluePieces(rightSide, endNeighbours);
			}
		}

		private void gluePieces(List<IToken> left,	List<IToken> right) {
			
			for(IToken l : left){
				for(IToken r : right){
					l.addNextToken(r);
					r.addPreviousToken(l);
				}
			}
		}

		protected List<IToken> removeFromSide(int start, IntOpenHashSet neighboursToRemove, Direction dir)
		{
			List<IToken> startRangeNeighbours = collectNeighbours(start, dir);			
			for(IToken t : startRangeNeighbours){
				IToken neighbour = t.getNeighbour(dir.opposite());
				if(neighbour!=null&&neighboursToRemove.contains(neighbour.id())){
					t.removeNeighbour(dir.opposite(), neighbour);
				}
				else{
					List<IToken> neighbours = t.getNeighbours(dir.opposite());
					if(neighbours!=null){
						neighbours = new ArrayList<IToken>(neighbours);
						for(IToken n : neighbours){
							if(neighboursToRemove.contains(n.id())){
								t.removeNeighbour(dir.opposite(), n);
								n.removeNeighbour(dir, t);
							}							
						}
					}
				}
			}
			return startRangeNeighbours;
		}

		private List<IToken> collectNeighbours(int start, Direction dir) {
			
			ArrayList<IToken> result = new ArrayList<IToken>();
			IToken token = tokens.get(start);
			IToken nbr = token.getNeighbour(dir);
			if(nbr != null){
				result.add(nbr);
			}
			else{
				List<IToken> neighbours = token.getNeighbours(dir);
				if(neighbours!=null){
					result.addAll(neighbours);
				}
			}
			return result;
		}

		private void fireChanges(int start, int oldLength, int newLength) {
			if(this.listeners==null){
				return;
			}
			for(TokenArrayBufferListener l : listeners){
				l.onChange(start, oldLength, newLength);
			}
		}
		
		public void addListener(TokenArrayBufferListener l){
			if(this.listeners==null){
				this.listeners = new HashSet<TokenArrayBufferListener>();
			}
			listeners.add(l);
		}
		
		public void removeListener(TokenArrayBufferListener l){
			if(this.listeners==null){
				return;
			}
			listeners.remove(l);
		}

		public ArrayList<IToken> getTokens() {
			return tokens;
		}
		
		public List<IToken> getRange(int start, int end){
			if(start<0||end>tokens.size()){
				throw new ArrayIndexOutOfBoundsException();
			}
			return tokens.subList(start, end);
		}
		
		public IToken get(int index){
			if(index<0||index>=tokens.size()){
				throw new ArrayIndexOutOfBoundsException();
			}
			return tokens.get(index);
		}
		
		public int length(){
			return tokens.size();
		}
		
		@Override
		public String toString() {
			return tokens.toString();
		}
	}
	
	public interface TokenArrayBufferListener{		
		void onChange(int start, int oldLength, int newLength);		
	}
	
	
	public static class SentenceNode implements TokenArrayBufferListener{		
		
		
		public SentenceNode(int id, boolean collapse) {
			this(id);
			this.collapse = collapse;
		}

		public SentenceNode(int id) {
			super();
			this.id=id;
		}
		
		private final int id;

		private int startTokenIndex;
		
		private int endTokenIndex;
		
		private int contentStartIndex;
		
		private int contentEndIndex;
		
		private List<SentenceNode> children;
		
		private boolean collapse = true;		

		@Override
		public void onChange(int start, int oldLength, int newLength) {
			
			if(start<endTokenIndex)
			{
				int dif = newLength-oldLength;
				this.endTokenIndex += dif;
				this.contentEndIndex += dif;
				
				int oldEnd = start + oldLength;				
				if(oldEnd<=startTokenIndex){
					this.startTokenIndex += dif;
					this.contentStartIndex += dif;
				}
			}
		}

		public int getStartTokenIndex() {
			return startTokenIndex;
		}

		public void setStartTokenIndex(int startTokenIndex) {
			this.startTokenIndex = startTokenIndex;
		}

		public int getEndTokenIndex() {
			return endTokenIndex;
		}

		public void setEndTokenIndex(int endTokenIndex) {
			this.endTokenIndex = endTokenIndex;
		}

		public List<SentenceNode> getChildren() {
			return children;
		}
		
		public void addChild(SentenceNode ch){
			if(this.children==null){
				this.children = new ArrayList<SentenceNode>();
			}
			this.children.add(ch);
		}

		public boolean isCollapse() {
			return collapse;
		}

		public void setCollapse(boolean collapse) {
			this.collapse = collapse;
		}

		public int getContentStartIndex() {
			return contentStartIndex;
		}

		public void setContentStartIndex(int contentStartIndex) {
			this.contentStartIndex = contentStartIndex;
		}

		public int getContentEndIndex() {
			return contentEndIndex;
		}

		public void setContentEndIndex(int contentEndIndex) {
			this.contentEndIndex = contentEndIndex;
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + id;
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
			SentenceNode other = (SentenceNode) obj;
			if (id != other.id)
				return false;
			return true;
		}
	}
	
	public static class BufferIndex implements TokenArrayBufferListener{

		public BufferIndex(int value) {
			super();
			this.value = value;
		}

		private int value;
		
		@Override
		public void onChange(int start, int oldLength, int newLength) {
			if(this.value<start){
				return;
			}
			if(this.value >= start + oldLength){
				this.value += newLength - oldLength;
			}
		}

		public int value() {
			return value;
		}
		
	}
	
	private List<BasicRule> rules;
	
	private List<DecisionRule> decisionRules;
	
	
	public List<BasicRule> getRules() {
		return rules;
	}

	public void setRules(List<BasicRule> rules) {
		this.rules = rules;
	}

	public List<DecisionRule> getDecisionRules() {
		return decisionRules;
	}

	public void setDecisionRules(List<DecisionRule> decisionRules) {
		this.decisionRules = decisionRules;
	}

	public List<IToken> gatherTree (List<IToken> tokens){
		

		TokenArrayBuffer buffer = new TokenArrayBuffer(tokens);
		
		SentenceNode rootNode = buildTree(tokens, buffer);
		
		collapseNode(rootNode, buffer);
		
		ArrayList<IToken> result = buffer.getTokens();
		return result;		
	}	


	private SentenceNode buildTree(List<IToken> tokens, TokenArrayBuffer buffer) {
		
		int lastNodeId = 0;
		SentenceNode result = new SentenceNode(lastNodeId++,false);
		result.setStartTokenIndex(0);
		result.setContentStartIndex(0);
		result.setEndTokenIndex(tokens.size());
		result.setContentEndIndex(tokens.size());
		
		Stack<SentenceNode> nodes = new Stack<SentenceNode>();
		nodes.push(result);
		
		Stack<BasicRule> ruleStack = new Stack<BasicRule>();
		int size = tokens.size();
l0:		for(int i = 0 ; i < size ; i++){

			if(!ruleStack.isEmpty()){
				BasicRule rule = ruleStack.peek();
				boolean matched = rule.matchEnd(tokens, i);
				if(matched){
					ruleStack.pop();					
					SentenceNode node = nodes.pop();
					node.setContentEndIndex(i);
					i += rule.getEndBound().getLength();
					node.setEndTokenIndex(i);				
					continue l0;
				}
			}
			
			for(BasicRule rule : rules){
				boolean matched = rule.matchStart(tokens,i);
				if(matched){
					ruleStack.push(rule);
					SentenceNode node = new SentenceNode(lastNodeId++);
					buffer.addListener(node);
					node.setStartTokenIndex(i);
					if(!nodes.isEmpty()){
						nodes.peek().addChild(node);
					}
					nodes.push(node);
					i += rule.getStartBound().getLength();
					node.setContentStartIndex(i);
					node.setEndTokenIndex(tokens.size());
					node.setContentEndIndex(tokens.size());
					continue l0;
				}
			}
		}
		return result;
	}
	
	private void collapseNode(SentenceNode node, TokenArrayBuffer buffer) {
		
		List<SentenceNode> children = node.getChildren();
		if(children!=null){
			for(SentenceNode ch : children){
				collapseNode(ch, buffer);
			}
		}
		
		if(!node.isCollapse()){
			return;
		}
		for(DecisionRule rule : decisionRules){
			boolean matchStart = rule.matchStart(buffer.getTokens(), node.getStartTokenIndex());
			if(!matchStart){
				continue;
			}
			boolean matchEnd = rule.matchEnd(buffer.getTokens(), node.getContentEndIndex());
			if(!matchEnd){
				continue;
			}
			IntIntMap parents = findParents(node,rule,buffer);
			IntObjectOpenHashMap<List<IToken>> newTokens = createNewToken(node, rule, parents, buffer);
			replaceTokens(node,rule,parents,newTokens,buffer);
			node.setCollapse(false);
			break;
		}
	}

	private void replaceTokens(SentenceNode node, DecisionRule rule, IntIntMap parents, IntObjectOpenHashMap<List<IToken>> newTokens,TokenArrayBuffer buffer)
	{
		int start = node.getStartTokenIndex() + rule.getStartBound().getBoundOffset();
		start = Math.max(0, start);
		int contentEnd = node.getContentEndIndex() + rule.getEndBound().getBoundOffset();
		int end = contentEnd + rule.getEndBound().getLength();
		end = Math.min(buffer.length(), end);
		
		IntObjectOpenHashMap<BufferIndex> map = new IntObjectOpenHashMap<SentenceTreeBuilder.BufferIndex>();
		for(IntCursor cr : parents.keys()){
			int id = cr.value;
			int ind = parents.get(id);
			BufferIndex bi = new BufferIndex(ind);
			map.put(id, bi);
			buffer.addListener(bi);
		}
		
		buffer.replace(new ArrayList<IToken>(), start, end);
		
		for(IntCursor cr : map.keys()){
			int id = cr.value;
			BufferIndex bi = map.get(id);
			int parentIndex = bi.value();
			List<IToken> replacement = newTokens.get(id);
			buffer.replace(replacement, parentIndex, parentIndex+1);
		}
	}

	protected IntObjectOpenHashMap<List<IToken>> createNewToken(
			SentenceNode node, DecisionRule rule, IntIntMap parents, TokenArrayBuffer buffer) {

		int start = node.getStartTokenIndex() + rule.getStartBound().getBoundOffset();
		int contentStart = start + rule.getStartBound().getLength();				
		int contentEnd = node.getContentEndIndex() + rule.getEndBound().getBoundOffset();
		int end = contentEnd + rule.getEndBound().getLength();
		if(rule.isAcceptEnd()){
			end = Math.min(end, buffer.length());
		}
		
		
		List<IToken> toProcess = buffer.getRange(contentStart, contentEnd);
		List<IToken> processed = processContent(toProcess,rule);
		
		List<IToken> startBoundTokens = buffer.getRange(start, contentStart);
		List<IToken> startBound = processBound(startBoundTokens, rule, Direction.START);
		handleParents(startBound);		
		
		List<IToken> endBoundTokens = buffer.getRange(contentEnd, end);
		List<IToken> endBound = processBound(endBoundTokens, rule, Direction.START);
		handleParents(endBound);
		
		ArrayList<IToken> list = new ArrayList<IToken>();		
		list.addAll(startBound);
		list.addAll(processed);
		list.addAll(endBound);
		List<IToken> toHandle = new ArrayList<IToken>();
		toHandle.addAll(startBound);
		toHandle.addAll(endBound);
		
		handleBounds(list,toHandle,false);
		
		List<IToken> regionTokens = produceRegionToken(processed, startBound, endBound, rule);
		handleParents(regionTokens);
		handleBounds(regionTokens,regionTokens,false);
		
		IntObjectOpenHashMap<List<IToken>> resultTokens = produceResultToken(node, parents, rule, regionTokens, buffer);
		ArrayList<IToken> producedTokens = new ArrayList<IToken>(); 
		for(IntCursor i : parents.keys()){
			List<IToken> lst = resultTokens.get(i.value);
			producedTokens.addAll(lst);
		}
		
		handleParents(producedTokens);
		handleBounds(producedTokens,producedTokens,true);

		return resultTokens;
	}
	
	private void handleParents(List<IToken> tokens) {
		for(IToken t : tokens){
			handleParents(t);
		}		
	}

	private void handleParents(IToken token) {
		
		List<IToken> children = token.getChildren();
		if(children==null){
			return;
		}
		for(IToken t : children){
			t.addParent(token);
		}
		
	}

	private void handleBounds(List<IToken> tokens, List<IToken> newTokensList,boolean append)
	{
		IntObjectOpenHashMap<IToken> resultTokens = new IntObjectOpenHashMap<IToken>();
		IntObjectOpenHashMap<IToken> newTokens = new IntObjectOpenHashMap<IToken>();

		for(IToken t : tokens){
			resultTokens.put(t.id(), t);
		}
		for(IToken t : newTokensList){
			newTokens.put(t.id(), t);
		}
		TokenBoundsHandler tbh = new TokenBoundsHandler();
		tbh.setNewTokens(newTokens);
		tbh.setResultTokens(resultTokens);
		tbh.handleBounds(tokens,append);
	}
	
	private static final HashSet<Integer> parentTokenTypes = new HashSet<Integer>(Arrays.asList(
			IToken.TOKEN_TYPE_LETTER,
			IToken.TOKEN_TYPE_WORD_FORM,
			IToken.TOKEN_TYPE_LONG_NAME
		));

	private IntIntMap findParents(SentenceNode node, DecisionRule rule, TokenArrayBuffer buffer) {
		
		Direction dir = rule.getParentDirection();
		int sp = -1;
		int ep = -1;
		ArrayList<IToken> tokens = buffer.getTokens();
		if(dir == null || dir==Direction.START){
			for(int i = node.getStartTokenIndex(); i > 0 ; i--){
				int ind = i-1;
				IToken token = tokens.get(ind);
				int type = token.getType();
				if(parentTokenTypes.contains(type)){
					sp = token.getStartPosition();
					ep = token.getEndPosition();
					break;
				}
			}
		}
		if((dir == null || dir==Direction.END) && sp<0 ){
			int length = buffer.length();
			for(int i = node.getEndTokenIndex(); i < length ; i++){
				IToken token = tokens.get(i);
				int type = token.getType();
				if(parentTokenTypes.contains(type)){
					sp = token.getStartPosition();
					ep = token.getEndPosition();
					break;
				}
			}
		}
		IntIntOpenHashMap result = new IntIntOpenHashMap();
		for(int i = 0 ; i < tokens.size() ; i ++){
			IToken t = tokens.get(i);
			int sp0 = t.getStartPosition();
			int ep0 = t.getEndPosition();
			if(sp0!=sp || ep0 != ep){
				continue;
			}
			result.put(t.id(),i);
		}
		return result;
	}

}
