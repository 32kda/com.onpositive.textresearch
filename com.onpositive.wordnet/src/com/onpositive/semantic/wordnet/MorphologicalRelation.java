package com.onpositive.semantic.wordnet;

public class MorphologicalRelation extends AbstractRelation<MeaningElement>{


	public static final int MORPHOLOGICAL_OFFSET=40;
	
	public static final int BACK_LINK_OFFSET=30;
	
	public static final int ADJF_ADJS=1+MORPHOLOGICAL_OFFSET;
	public static final int ADJF_COMP=2+MORPHOLOGICAL_OFFSET;
	public static final int INFN_VERB=3+MORPHOLOGICAL_OFFSET;
	public static final int INFN_PRTF=4+MORPHOLOGICAL_OFFSET;
	public static final int INFN_GRND=5+MORPHOLOGICAL_OFFSET;
	public static final int PRTF_PRTS=6+MORPHOLOGICAL_OFFSET;
	public static final int NAME_PATR=7+MORPHOLOGICAL_OFFSET;
	public static final int PATR_MASC_PATR_FEMN=8+MORPHOLOGICAL_OFFSET;
	public static final int SURN_MASC_SURN_FEMN=9+MORPHOLOGICAL_OFFSET;
	public static final int SURN_MASC_SURN_PLUR=10+MORPHOLOGICAL_OFFSET;
	public static final int PERF_IMPF=11+MORPHOLOGICAL_OFFSET;
	public static final int ADJF_SUPR_ejsh=12+MORPHOLOGICAL_OFFSET;
	public static final int PATR_MASC_FORM_PATR_MASC_INFR=13+MORPHOLOGICAL_OFFSET;
	public static final int PATR_FEMN_FORM_PATR_FEMN_INFR=14+MORPHOLOGICAL_OFFSET;
	public static final int ADJF_eish_SUPR_nai_eish=15+MORPHOLOGICAL_OFFSET;
	public static final int ADJF_SUPR_ajsh=16+MORPHOLOGICAL_OFFSET;
	public static final int ADJF_aish_SUPR_nai_aish=17+MORPHOLOGICAL_OFFSET;
	public static final int ADJF_SUPR_suppl=18+MORPHOLOGICAL_OFFSET;
	public static final int ADJF_SUPR_nai=19+MORPHOLOGICAL_OFFSET;
	public static final int ADJF_SUPR_slng=20+MORPHOLOGICAL_OFFSET;
	public static final int FULL_CONTRACTED=21+MORPHOLOGICAL_OFFSET;
	public static final int ORPHOVAR=22+MORPHOLOGICAL_OFFSET;

	public String getDescription(){
		return "Код отношения:"+this.relation;
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public MorphologicalRelation(AbstractWordNet owner2, int conceptId,
			int relationId) {
		super(owner2, conceptId, relationId);		
	}

	public MorphologicalRelation(AbstractWordNet owner,
			MeaningElement conceptId, int relation) {
		super(owner, conceptId, relation);
	}

	public MeaningElement getWord() {
		MeaningElement result = owner.getConceptInfo(conceptId);
		return (MeaningElement) result;
	}
	
	@Override
	public String toString() {
		return getWord().toString()+":"+relation;
	}
}
