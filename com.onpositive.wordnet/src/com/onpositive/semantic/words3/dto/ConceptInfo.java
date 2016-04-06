package com.onpositive.semantic.words3.dto;

import static com.onpositive.semantic.words3.hds.StringCoder.int0;
import static com.onpositive.semantic.words3.hds.StringCoder.int1;
import static com.onpositive.semantic.words3.hds.StringCoder.int2;
import static com.onpositive.semantic.words3.hds.StringCoder.makeInt;

import java.util.Arrays;

import com.carrotsearch.hppc.ByteArrayList;
import com.onpositive.semantic.wordnet.AbstractRelation;
import com.onpositive.semantic.wordnet.MeaningElement;
import com.onpositive.semantic.words3.CodingUtils;

public final class ConceptInfo {

	public int parentTextElement;
	public final int senseId;
	public final int gcode;
	
	public final AbstractRelation<MeaningElement>[] relations;
	private int sizeB=-1;
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + gcode;
		result = prime * result + Arrays.hashCode(relations);
		result = prime * result + senseId;
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
		ConceptInfo other = (ConceptInfo) obj;
		if (gcode != other.gcode)
			return false;
		if (!Arrays.equals(relations, other.relations))
			return false;
		if (senseId != other.senseId)
			return false;
		return true;
	}

	public ConceptInfo(int parentTextElement,int senseId, int gcode,
			AbstractRelation<MeaningElement>[] relations) {
		super();
		this.parentTextElement=parentTextElement;
		this.senseId = senseId;
		this.gcode=gcode;
		this.relations = relations;
	}	
	
	public byte[] toByteArray(){
		byte[] encodeWordInfo = encodeWordInfo(parentTextElement,senseId, gcode,  relations);
		ConceptInfo fromBytes = ConceptInfo.getFromBytes(encodeWordInfo, 0);
		if (!fromBytes.equals(this)){
			throw new IllegalStateException();
		}
		return encodeWordInfo;
	}
	
	public static ConceptInfo getFromBytes(byte[] b,int position){
		int id=makeInt((byte)0, b[2+position], b[1+position], b[0+position]);
		short gkind=(short) makeInt((byte)0, (byte)0, b[4+position], b[3+position]);
		//short features=(short) makeInt((byte)0, (byte)0, b[6+position], b[5+position]);
		int pid=makeInt((byte)0, b[7+position], b[6+position], b[5+position]);
		AbstractRelation<MeaningElement>[] relations=CodingUtils.decodeMeaningRelations(8+position, b);
		int length = CodingUtils.length(relations);
		//not actually clear how to fill it;
		ConceptInfo wordSenseInfo = new ConceptInfo(pid,id, gkind, relations);
		wordSenseInfo.sizeB=8+length;
		return wordSenseInfo;		
	}
	protected byte[] encodeWordInfo(int pel,int id,int gcode,AbstractRelation<?>[] relations){
		if (gcode>Short.MAX_VALUE){
			throw new IllegalStateException();
		}
		byte[] encodeRelations = CodingUtils.encodeRelations(relations);
		int estimateRelationsLength = CodingUtils.estimateRelationsLength(0,encodeRelations);
		if (estimateRelationsLength!=encodeRelations.length){
			System.out.println("Error");
		}
		byte[] totalResult=new byte[8+encodeRelations.length];
		totalResult[0]=int0(id);
		totalResult[1]=int1(id);
		totalResult[2]=int2(id);
		totalResult[3]=int0(gcode);
		totalResult[4]=int1(gcode);		
		totalResult[5]=int0(pel);
		totalResult[6]=int1(pel);
		totalResult[7]=int2(pel);
		for (int a=0;a<encodeRelations.length;a++){
			totalResult[a+8]=encodeRelations[a];
		}
		return totalResult;
	}
	
	public static int getConceptLength(int addr,byte[] buf){
		return CodingUtils.estimateRelationsLength(addr+8,buf)+8;
	}
	
	public byte[] encodeWordInfoSmall(int kind,AbstractRelation<?>[] relations){
		ByteArrayList rs=new ByteArrayList();
		if (kind<255){
			rs.add((byte) kind);
		}
		else{
			rs.add(int0(kind));
			rs.add(int1(kind));
		}
		if (relations.length==0){
			return rs.toArray();
		}
		byte[] encodeRelations = CodingUtils.encodeRelations(relations);
		rs.add(encodeRelations);
		return rs.toArray();
	}

	public int sizeInBytes() {
		return sizeB;
	}
}