package com.onpositive.semantic.words2.builder;

import java.util.LinkedHashSet;

import com.onpositive.semantic.wordnet.Grammem;

public abstract class AdjectiveRelations{
	public static final int nom_sg_m_code=1;
	public static final int nom_sg_n_code=2;
	public static final int nom_sg_f_code=3;
	public static final int nom_pl_code=4;
	
	public static final int gen_sg_m_code=5;
	public static final int gen_sg_n_code=6;
	public static final int gen_sg_f_code=7;
	public static final int gen_pl_code=8;
	
	public static final int dat_sg_m_code=9;
	public static final int dat_sg_n_code=10;
	public static final int dat_sg_f_code=11;
	public static final int dat_pl_code=12;
	
	public static final int acc_sg_m_a_code=13;
	public static final int acc_sg_m_n_code=14;
	public static final int acc_sg_n_code=15;
	public static final int acc_sg_f_code=16;
	public static final int acc_pl_a_code=17;
	public static final int acc_pl_n_code=18;
	
	public static final int ins_sg_m_code=19;
	public static final int ins_sg_n_code=20;
	public static final int ins_sg_f_code=21;
	public static final int ins_pl_code=22;
	
	public static final int prp_sg_m_code=23;
	public static final int prp_sg_n_code=24;
	public static final int prp_sg_f_code=25;
	public static final int prp_pl_code=26;
	
	public static final int srt_sg_m_code=27;
	public static final int srt_sg_n_code=28;
	public static final int srt_sg_f_code=29;
	public static final int srt_pl_code=30;
	
	public static LinkedHashSet<Grammem>getRelations(int code){
		switch (code) {
		case nom_sg_m_code:
			return Grammem.getGrammemSet(Grammem.PartOfSpeech.ADJF,Grammem.Case.NOMN,Grammem.SingularPlural.SINGULAR,Grammem.Gender.MASC);
		case nom_sg_n_code:
			return Grammem.getGrammemSet(Grammem.PartOfSpeech.ADJF,Grammem.Case.NOMN,Grammem.SingularPlural.SINGULAR,Grammem.Gender.NEUT);
		case nom_sg_f_code:
			return Grammem.getGrammemSet(Grammem.PartOfSpeech.ADJF,Grammem.Case.NOMN,Grammem.SingularPlural.SINGULAR,Grammem.Gender.FEMN);
		case nom_pl_code:
			return Grammem.getGrammemSet(Grammem.PartOfSpeech.ADJF,Grammem.Case.NOMN,Grammem.SingularPlural.PLURAL);				
		case gen_sg_m_code:
			return Grammem.getGrammemSet(Grammem.PartOfSpeech.ADJF,Grammem.Case.GENT,Grammem.SingularPlural.SINGULAR,Grammem.Gender.MASC);
		case gen_sg_n_code:
			return Grammem.getGrammemSet(Grammem.PartOfSpeech.ADJF,Grammem.Case.GENT,Grammem.SingularPlural.SINGULAR,Grammem.Gender.NEUT);
		case gen_sg_f_code:
			return Grammem.getGrammemSet(Grammem.PartOfSpeech.ADJF,Grammem.Case.GENT,Grammem.SingularPlural.SINGULAR,Grammem.Gender.FEMN);
		case gen_pl_code:
			return Grammem.getGrammemSet(Grammem.PartOfSpeech.ADJF,Grammem.Case.GENT,Grammem.SingularPlural.PLURAL);
		case dat_sg_m_code:
			return Grammem.getGrammemSet(Grammem.PartOfSpeech.ADJF,Grammem.Case.DATV,Grammem.SingularPlural.SINGULAR,Grammem.Gender.MASC);
		case dat_sg_n_code:
			return Grammem.getGrammemSet(Grammem.PartOfSpeech.ADJF,Grammem.Case.DATV,Grammem.SingularPlural.SINGULAR,Grammem.Gender.NEUT);
		case dat_sg_f_code:
			return Grammem.getGrammemSet(Grammem.PartOfSpeech.ADJF,Grammem.Case.DATV,Grammem.SingularPlural.SINGULAR,Grammem.Gender.FEMN);
		case dat_pl_code:
			return Grammem.getGrammemSet(Grammem.PartOfSpeech.ADJF,Grammem.Case.DATV,Grammem.SingularPlural.PLURAL);
		case ins_sg_m_code:
			return Grammem.getGrammemSet(Grammem.PartOfSpeech.ADJF,Grammem.Case.ABLT,Grammem.SingularPlural.SINGULAR,Grammem.Gender.MASC);
		case ins_sg_n_code:
			return Grammem.getGrammemSet(Grammem.PartOfSpeech.ADJF,Grammem.Case.ABLT,Grammem.SingularPlural.SINGULAR,Grammem.Gender.NEUT);
		case ins_sg_f_code:
			return Grammem.getGrammemSet(Grammem.PartOfSpeech.ADJF,Grammem.Case.ABLT,Grammem.SingularPlural.SINGULAR,Grammem.Gender.FEMN);
		case ins_pl_code:
			return Grammem.getGrammemSet(Grammem.PartOfSpeech.ADJF,Grammem.Case.ABLT,Grammem.SingularPlural.PLURAL);
		case prp_sg_m_code:
			return Grammem.getGrammemSet(Grammem.PartOfSpeech.ADJF,Grammem.Case.LOCT,Grammem.SingularPlural.SINGULAR,Grammem.Gender.MASC);
		case prp_sg_n_code:
			return Grammem.getGrammemSet(Grammem.PartOfSpeech.ADJF,Grammem.Case.LOCT,Grammem.SingularPlural.SINGULAR,Grammem.Gender.NEUT);
		case prp_sg_f_code:
			return Grammem.getGrammemSet(Grammem.PartOfSpeech.ADJF,Grammem.Case.LOCT,Grammem.SingularPlural.SINGULAR,Grammem.Gender.FEMN);
		case prp_pl_code:
			return Grammem.getGrammemSet(Grammem.PartOfSpeech.ADJF,Grammem.Case.LOCT,Grammem.SingularPlural.PLURAL);
		case acc_sg_m_a_code:
			return Grammem.getGrammemSet(Grammem.PartOfSpeech.ADJF,Grammem.Case.ACCS,Grammem.SingularPlural.SINGULAR,Grammem.Gender.MASC,Grammem.AnimateProperty.anim);
		case acc_sg_m_n_code:
			return Grammem.getGrammemSet(Grammem.PartOfSpeech.ADJF,Grammem.Case.ACCS,Grammem.SingularPlural.SINGULAR,Grammem.Gender.MASC,Grammem.AnimateProperty.inan);
		case acc_sg_n_code:
			return Grammem.getGrammemSet(Grammem.PartOfSpeech.ADJF,Grammem.Case.ACCS,Grammem.SingularPlural.SINGULAR,Grammem.Gender.NEUT);
		case acc_sg_f_code:
			return Grammem.getGrammemSet(Grammem.PartOfSpeech.ADJF,Grammem.Case.ACCS,Grammem.SingularPlural.SINGULAR,Grammem.Gender.FEMN);
		case acc_pl_a_code:
			return Grammem.getGrammemSet(Grammem.PartOfSpeech.ADJF,Grammem.Case.ACCS,Grammem.SingularPlural.PLURAL,Grammem.AnimateProperty.anim);
		case acc_pl_n_code:
			return Grammem.getGrammemSet(Grammem.PartOfSpeech.ADJF,Grammem.Case.ACCS,Grammem.SingularPlural.PLURAL,Grammem.AnimateProperty.inan);
		case srt_sg_m_code:
			return Grammem.getGrammemSet(Grammem.PartOfSpeech.ADJS,Grammem.SingularPlural.SINGULAR,Grammem.Gender.MASC);
		case srt_sg_n_code:
			return Grammem.getGrammemSet(Grammem.PartOfSpeech.ADJS,Grammem.SingularPlural.SINGULAR,Grammem.Gender.NEUT);
		case srt_sg_f_code:
			return Grammem.getGrammemSet(Grammem.PartOfSpeech.ADJS,Grammem.SingularPlural.SINGULAR,Grammem.Gender.FEMN);
		case srt_pl_code:
			return Grammem.getGrammemSet(Grammem.PartOfSpeech.ADJS,Grammem.SingularPlural.PLURAL);				
		default:
			break;
		}
		return null;			
	}
}