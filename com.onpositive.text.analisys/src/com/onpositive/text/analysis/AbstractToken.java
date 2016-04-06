package com.onpositive.text.analysis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.carrotsearch.hppc.DoubleArrayList;

public abstract class AbstractToken implements IToken {
	
	protected AbstractToken(int tokenType, int startPosition, int endPosition)
	{
		this.tokenType = tokenType;
		this.startPosition = startPosition;
		this.endPosition = endPosition;
		
		//setParserName();
				
	}
	@Override
	public String getStableStringValue() {
		return getStringValue();
	}
	
	private void setParserName() {
		parserName = CreatedByParserRegistry.getParserName();
	}
	
	protected AbstractToken(int tokenType, int startPosition, int endPosition, boolean isDoubtful)
	{
		this.tokenType = tokenType;
		this.startPosition = startPosition;
		this.endPosition = endPosition;
		this.isDoubtful = isDoubtful;
		
		setParserName();
	}
	
	private String parserName;
	
	public String getParserName() { return parserName; }
	
	private int id;
	
	private IToken next;
	
	private IToken previous;
	
	private ArrayList<IToken> nextTokens;
	
	private ArrayList<IToken> previousTokens;
	
	private ArrayList<IToken> conflictTokens;
		
	private final int tokenType;
	
	private int startPosition;
	
	private DoubleArrayList corrValues = new DoubleArrayList();  
	private DoubleArrayList corrWeights = new DoubleArrayList();
	
	public void setStartPosition(int startPosition) {
		this.startPosition = startPosition;
	}

	public void setEndPosition(int endPosition) {
		this.endPosition = endPosition;
	}

	private int endPosition;
	
	protected List<IToken> children;
	
	private List<IToken> parents;
	
	private String link;
	
	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	private boolean isDoubtful = false;
	
	public int id() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
		TokenRegistry.put((IToken) this);
	}

	public int getType() {
		return tokenType;
	}

	public int getStartPosition() {
		return startPosition;
	}

	public int getEndPosition() {
		return endPosition;
	}
	
	@Override
	public int getBoundPosition(Direction dir) {
		if(dir == null){
			throw new RuntimeException("Bound position for 'null' direction is undefined");
		}
		if(dir == Direction.END){
			return getEndPosition();
		}
		else {
			return getStartPosition();
		}
	}

	public int getLength() {
		return endPosition - startPosition;
	}

	public IToken getNext() {
		return next;
	}

	public IToken getPrevious() {
		return previous;
	}
	
	public void setNext(IToken unit) {
		this.next = unit;
	}

	public void setPrevious(IToken unit) {
		this.previous = unit;
	}

	public List<IToken> getNextTokens() {
		return nextTokens;
	}
	
	public void addConflict(IToken token) {
		if (token.getStartPosition() > this.getEndPosition() || token.getEndPosition() < this.getStartPosition()) return;
		if (conflictTokens == null) conflictTokens = new ArrayList<IToken>();
		if (!conflictTokens.contains(token)) conflictTokens.add(token); 
	}
	
	public void removeConflict(IToken token) {
		if (conflictTokens == null || !conflictTokens.contains(token)) return;
		conflictTokens.remove(token);		
	}
	
	public List<IToken> getConflicts() {
		if (conflictTokens == null) conflictTokens = new ArrayList<IToken>();
		return conflictTokens;
	}
	
	@Override
	public boolean hasConflicts() {
		return (conflictTokens != null && !conflictTokens.isEmpty());
	}

	public void addNextToken(IToken token) {
		if(this.next == null){
			if( this.nextTokens==null||this.nextTokens.isEmpty()){		
				this.next = token;
				return;
			}
		}
		else{
			if(this.nextTokens==null){
				this.nextTokens = new ArrayList<IToken>();
				this.nextTokens.add(this.next);
				this.next = null;
			}
		}
		if(!this.nextTokens.contains(token)){
			this.nextTokens.add(token);
		}
	}
	
	public void removeNextToken(IToken token) {
		if(this.next == null){
			if( this.nextTokens==null){		
				return;
			}
			else{
				this.nextTokens.remove(token);
			}
		}
		else{
			if(this.next==token){
				this.next = null;
			}
		}
	}
	
	public void removePreviousToken(IToken token) {
		if(this.previous == null){
			if( this.previousTokens==null||this.previousTokens.isEmpty()){		
				return;
			}
			else{
				this.previousTokens.remove(token);
			}
		}
		else{
			if(this.previous==token){
				this.previous = null;
			}
		}
	}
	
	public void removeNeighbour(Direction direction, IToken token) {
		if(direction == Direction.END){
			removeNextToken(token);
		}
		else{
			removePreviousToken(token);
		}
	}

	public List<IToken> getPreviousTokens() {
		return previousTokens;
	}

	public void addPreviousToken(IToken token) {
		if(this.previous == null){
			if( this.previousTokens==null){		
				this.previous = token;
				return;
			}
		}
		else{
			if(this.previousTokens==null){
				this.previousTokens = new ArrayList<IToken>();
				this.previousTokens.add(this.previous);
				this.previous = null;
			}
		}
		if(!this.previousTokens.contains(token)){
			this.previousTokens.add(token);
		}
	}
	

	@Override
	public IToken getNeighbour(Direction direction) {
		if(direction == Direction.END){
			return getNext();
		}
		else{
			return getPrevious();
		}
	}

	@Override
	public void setNeighbour(IToken token, Direction direction) {
		if(direction == Direction.END){
			setNext(token);
		}
		else{
			setPrevious(token);
		}		
	}

	@Override
	public List<IToken> getNeighbours(Direction direction) {
		if(direction == Direction.END){
			return getNextTokens();
		}
		else{
			return getPreviousTokens();
		}
	}

	@Override
	public void addNeighbour(IToken token, Direction direction) {
		if(direction == Direction.END){
			addNextToken(token);
		}
		else{
			addPreviousToken(token);
		}
	}
	
	@Override
	public void cleanNextNeighbours() {
		this.next=null;
		this.nextTokens=null;
	}
	
	@Override
	public void cleanPreviousNeighbours() {
		this.previous = null;
		this.previousTokens = null;
	}
	
	@Override
	public void cleanNeighbours(Direction dir) {
		if(dir==Direction.END){
			cleanNextNeighbours();
		}
		else if(dir==Direction.START){
			cleanPreviousNeighbours();
		}
		
	}
	
	
	public List<IToken> getChildren(){
		if (this.children==null){
			return Collections.emptyList();
		}
		return this.children;
	}	
	
	public void addChild(IToken child){
		if(this.children==null){
			this.children = new ArrayList<IToken>();
		}
		this.children.add(child);
	}
	
	public void addChildren(Collection<IToken> children){
		
		if(children==null){
			return;
		}
		
		if(this.children==null){
			this.children = new ArrayList<IToken>();
		}
		this.children.addAll(children);
	}
	
	public void setChildren(Collection<IToken> children){
		if(this.children!=null){
			this.children.clear();
		}
		this.addChildren(children);
	}
	
	public List<IToken> getParents(){
		return this.parents;
	}
	
	public void addParent(IToken parent){
		if (parent == null) return;
		if(this.parents==null){
			this.parents = new ArrayList<IToken>();
		}
		this.parents.add(parent);
	}
	
	@Override
	public void removeParent(IToken token) {
		if(this.parents == null){
			return;
		}
		this.parents.remove(token);
	}
	
	public IToken getFirstChild(Direction direction){
		if(children==null){
			return null;
		}
		if(direction==Direction.START){
			return children.get(0);
		}
		else{
			return children.get(children.size()-1);
		}
	}
	
	public IToken getChild(int pos, Direction direction){
		
		int size = children.size();
		if(size<pos){
			return null;
		}
		if(direction==Direction.START){
			return children.get(pos);
		}
		else{
			return children.get(size-1-pos);
		}
	}
	
	public boolean hasSpaceAfter(){
		
		int startOfNext = 0;
		if(next!=null){
			startOfNext = next.getStartPosition();
		}
		else if(nextTokens!=null&&!nextTokens.isEmpty()){
			startOfNext = nextTokens.get(0).getStartPosition();
		}
		else{
			return false;
		}
		return this.endPosition < startOfNext;
	}
	
	public boolean hasSpaceBefore(){
		int endOfPrevious = 0;
		if(previous!=null){
			endOfPrevious = previous.getEndPosition();
		}
		else if(previousTokens!=null&&!previousTokens.isEmpty()){
			endOfPrevious = previousTokens.get(0).getEndPosition();
		}
		else{
			return false;
		}
		return this.startPosition > endOfPrevious;
	}
	
	public boolean isDoubtful() {
		return isDoubtful;
	}
	
	@Override
	public int childrenCount() {
		return children == null ? 0 : children.size();
	}
	


	public void replaceChild(IToken oldChild, IToken newChild) {
		
		int ind = children.indexOf(oldChild);
		if(ind<0){
			return;
		}
		children.set(ind, newChild);
		
		int sp = newChild.getStartPosition();
		if(sp<this.getStartPosition()){
			this.setStartPosition(sp);
		}
		
		int ep = newChild.getEndPosition();
		if(ep>this.getEndPosition()){
			this.setEndPosition(ep);
		}
	}
	
	public void replaceParent(IToken oldParent, IToken newParent) {
		
		int ind = parents.indexOf(oldParent);
		if (ind < 0)
			parents.add(newParent);			
		else
			parents.set(ind, newParent);		
	}
	
	@Override
	public void adjustStartPosition(int startPosition) {
		this.startPosition = Math.min(startPosition, this.startPosition);
	}
	
	@Override
	public void adjustEndPosition(int endPosition) {
		this.endPosition = Math.max(endPosition, this.endPosition);		
	}

	@Override
	public String toString() {
		return getStringValue();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + endPosition;
		result = prime * result + startPosition;
		result = prime * result + tokenType;
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
		AbstractToken other = (AbstractToken) obj;
		if (endPosition != other.endPosition)
			return false;
		if (startPosition != other.startPosition)
			return false;
		if (tokenType != other.tokenType)
			return false;
		return true;
	}

	@Override
	public double getCorrelation() {
		double cor = 0.0;
		double weight = 0.0;
		for (int i = 0; i < corrValues.size(); i++) {
			if (Double.isInfinite(corrWeights.get(i))) {
				return corrValues.get(i);
			}
			cor += corrValues.get(i) * corrWeights.get(i);
			weight += corrWeights.get(i);
		}
		
		return weight == 0 ? 0.0 : cor / weight;
	}
	
	@Override
	public void setCorrelation(double corellation, double weight) {
		if (Double.isFinite(corellation) == false) return;
		corrValues.add(corellation);
		corrWeights.add(weight);		
	}
	
	@Override
	public boolean hasCorrelation() {
		return !corrValues.isEmpty();
	}
	
}
