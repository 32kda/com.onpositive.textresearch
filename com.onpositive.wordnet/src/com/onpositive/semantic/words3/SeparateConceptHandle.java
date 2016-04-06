package com.onpositive.semantic.words3;

import com.onpositive.semantic.wordnet.AbstractRelation;
import com.onpositive.semantic.wordnet.MeaningElement;
import com.onpositive.semantic.wordnet.TextElement;
import com.onpositive.semantic.words3.ReadOnlyWordNet.WordStore;
import com.onpositive.semantic.words3.hds.StringCoder;

public class SeparateConceptHandle extends MeaningElement{
	
	protected final int address;
	
	protected final WordStore store;
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + address;
		result = prime * result + store.hashCode();
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
		SeparateConceptHandle other = (SeparateConceptHandle) obj;
		if (address != other.address)
			return false;
		if (this.store!=other.store){
			return false;
		}
		return true;
	}
	
	public SeparateConceptHandle(int address, WordStore store) {
		super(store.getWordNet());
		this.address=address-1;
		this.store=store;
	}
	
	public int id(){
		int address2 = address;
		byte[] block2 = this.store.buffer();
		return StringCoder.makeInt(block2[address2+2], block2[address2+1], block2[address2]);		
	}
	
	public int textElementId(){
		int address2 = address;
		byte[] block2 = this.store.buffer();
		return StringCoder.makeInt(block2[address2+7], block2[address2+6], block2[address2+5]);		
	}
	
	@Override
	public short getGrammemCode() {	
		int address2 = address;
		byte[] block2 = this.store.buffer();
		return StringCoder.makeShort(block2[address2+4], block2[address2+3]);
	}

	@Override
	public AbstractRelation<MeaningElement>[] getAllRelations() {
		AbstractRelation<MeaningElement>[] decodeSemanticRelations = CodingUtils.decodeMeaningRelations(address+8, this.store.buffer());
		ReadOnlyWordNet wordNet = store.getWordNet();		
		for (AbstractRelation<MeaningElement> e:decodeSemanticRelations){
			e.setOwner(wordNet);
		}
		return decodeSemanticRelations;
	}

	@Override
	public TextElement getParentTextElement() {
		return this.store.getWordNet().getWordElement(textElementId());
	}
}