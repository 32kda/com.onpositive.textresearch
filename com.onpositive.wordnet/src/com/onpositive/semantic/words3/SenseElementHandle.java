package com.onpositive.semantic.words3;

import com.onpositive.semantic.wordnet.AbstractRelation;
import com.onpositive.semantic.wordnet.MeaningElement;
import com.onpositive.semantic.wordnet.SemanticRelation;
import com.onpositive.semantic.wordnet.TextElement;
import com.onpositive.semantic.words3.ReadOnlyWordNet.WordStore;
import com.onpositive.semantic.words3.dto.ConceptInfo;
import com.onpositive.semantic.words3.hds.StringCoder;

public class SenseElementHandle extends TextElement{
	
	public final class EmbeddedConcept extends MeaningElement {
		private final byte count;

		public EmbeddedConcept(byte count) {
			super(SenseElementHandle.this.owner);
			this.count = count;
		}

		@Override
		public int id() {
			return SenseElementHandle.this.id();					
		}

		@Override
		public AbstractRelation<MeaningElement>[] getAllRelations() {
			return basicGetRelations(count);
		}

		@Override
		public TextElement getParentTextElement() {
			return SenseElementHandle.this;
		}

		@Override
		public short getGrammemCode() {
			int msk = -((int) count);
			msk += WordStore.CUSTOM_CASE_OFFSET;
			int pos=address+4;
			boolean smallKind = (msk & WordStore.SMALL_KIND) != 0;
			if (smallKind) {
				short b = store.buffer()[pos];
				if (b<0){
					return (short) (256+b);
				}
				return b;												
			} else {
				return ReadOnlyWordNet.WordStore.makeShort(store.buffer()[pos+1],store.buffer()[pos]);						
			}					
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + address;
			result = prime * result + store.hashCode();
			return result;
		}
		protected final int addr(){
			return address;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			EmbeddedConcept other = (EmbeddedConcept) obj;
			if (address != other.addr())
				return false;
			if (this.owner!=other.owner){
				return false;
			}
			return true;
		}
		
		
	}

	private static final SemanticRelation[] WORD_RELATIONS = new SemanticRelation[0];

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
		SenseElementHandle other = (SenseElementHandle) obj;
		if (address != other.address)
			return false;
		if (this.store!=other.store){
			return false;
		}
		return true;
	}
	
	public SenseElementHandle(int address, WordStore store) {
		super(store.getWordNet());
		this.address=address;
		this.store=store;
	}
	
	public int id(){
		int address2 = address;
		byte[] block2 = this.store.buffer();
		return StringCoder.makeInt(block2[address2+2], block2[address2+1], block2[address2]);		
	}


	@Override
	public String getBasicForm() {
		return this.store.getStringBeforeData(store.buffer(), address);
	}

	@Override
	public MeaningElement[] getConcepts() {
		final byte count = this.store.buffer()[address + 3];
		if (count<0){
			return new MeaningElement[]{new EmbeddedConcept(count)};
		}
		else{
			MeaningElement[] result=new MeaningElement[count];
			int sPos=address + 4+1/*This is because of decrease*/;
			byte[] block2 = this.store.buffer();
			for (int a=0;a<count;a++){
				result[a]=new SeparateConceptHandle(sPos,store);
				sPos+=ConceptInfo.getConceptLength(sPos-1, block2);
			}
			return result;
		}
	}

	protected AbstractRelation<MeaningElement>[] basicGetRelations(byte count) {
		byte[] buffer = store.buffer();
		int msk = -((int) count);
		msk += WordStore.CUSTOM_CASE_OFFSET;
		int pos=address+4;
		boolean smallKind = (msk & WordStore.SMALL_KIND) != 0;
		boolean noRelations = (msk & WordStore.NO_RELATIONS) != 0;
		if (smallKind) {
			pos++;
		} else {
			pos += 2;
		}
		AbstractRelation<MeaningElement>[] rels = null;
		if (noRelations) {
			rels = WORD_RELATIONS;
		} else
			rels = CodingUtils.decodeMeaningRelations(pos, buffer);
		for (AbstractRelation<MeaningElement> q:rels){
			AbstractRelation<?> g=(AbstractRelation<?>) q;
			g.setOwner(store.getWordNet());
		}
		return rels;
	}

	@Override
	public boolean isMultiWord() {
		return false;
	}

	@Override
	public TextElement[] getParts() {
		return new TextElement[]{this};
	}
}