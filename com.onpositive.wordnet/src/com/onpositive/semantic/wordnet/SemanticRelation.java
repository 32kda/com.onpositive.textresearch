package com.onpositive.semantic.wordnet;

import java.io.Serializable;
import java.util.HashMap;

public class SemanticRelation extends AbstractRelation<MeaningElement> implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * Semantic relations
	 */
	public static final int SYNONIM = 1;
	public static final int SPECIALIZATION = 2;
	public static final int GENERALIZATION = 3;
	public static final int GENERALIZATION_BACK_LINK = 5;
	public static final int SYNONIM_BACK_LINK = 6;
	public static final int ANTONIM = 4;
	public static final int SPECIALIZATION_BACK_LINK = 7;
	public static final int MERONIM = 8;
	public static final int ANTONIM_BACKLINK = 9;
	public static final int MERONIM_BACKLINK = 10;
	
	private static final HashMap<Integer,Integer> backlinkMap = new HashMap<Integer, Integer>();
	
	private static final HashMap<Integer,Integer> backlinkAnalogueMap = new HashMap<Integer, Integer>();
	
	static{
		backlinkMap.put(SYNONIM, SYNONIM_BACK_LINK);
		backlinkMap.put(SPECIALIZATION, SPECIALIZATION_BACK_LINK);
		backlinkMap.put(GENERALIZATION, GENERALIZATION_BACK_LINK);
		backlinkMap.put(ANTONIM, ANTONIM_BACKLINK);
		backlinkMap.put(MERONIM, MERONIM_BACKLINK);
		
		backlinkAnalogueMap.put(SYNONIM, SYNONIM_BACK_LINK);
		backlinkAnalogueMap.put(SPECIALIZATION, SPECIALIZATION_BACK_LINK);
		backlinkAnalogueMap.put(GENERALIZATION, GENERALIZATION_BACK_LINK);
		backlinkAnalogueMap.put(ANTONIM, ANTONIM_BACKLINK);
	}
	public String getDescription(){
		return this.relationCode(this.relation);
	}
	
	public static Integer getBacklink(int relation){
		return backlinkMap.get(relation);
	}
	
	public static Integer getBacklinkAnalogue(int relation){
		return backlinkAnalogueMap.get(relation);
	}
	
	public SemanticRelation(AbstractWordNet owner, MeaningElement conceptId, int relation) {
		super(owner,conceptId,relation);
	}

	@Override
	public String toString() {
		return getWord() + ":" + relationCode(relation);
	}

	private String relationCode(int relation) {
		if (relation==1){
			return "SYNONIM";
		}
		if (relation==2){
			return "SPECIALIZATION";
		}
		if (relation==3){
			return "GENERALIZATION";
		}
		if (relation==5){
			return "GENERALIZATION_BACK_LINK";
		}
		if (relation==6){
			return "SYNONIM_BACK_LINK";
		}
		if (relation==4){
			return "ANTONIM";
		}
		if (relation==7){
			return "SPECIALIZATION_BACK_LINK";
		}
		return null;
	}

	public SemanticRelation(AbstractWordNet owner2, int conceptId, int relationId) {
		super(owner2,conceptId,relationId);		
	}
	
	public MeaningElement getWord() {
		MeaningElement result = owner.getConceptInfo(conceptId);
		return (MeaningElement) result;
	}
}