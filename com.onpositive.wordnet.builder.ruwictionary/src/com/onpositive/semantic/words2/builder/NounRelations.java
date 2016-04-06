package com.onpositive.semantic.words2.builder;

import java.util.LinkedHashSet;

import com.onpositive.semantic.wordnet.Grammem;

public abstract class NounRelations {

	public static final int NOM_SG = 1;

	public static final int NOM_PL = 2;

	public static final int GEN_SG = 3;

	public static final int GEN_PL = 4;

	public static final int DAT_SG = 5;

	public static final int DAT_PL = 6;

	public static final int ACC_SG = 7;

	public static final int ACC_PL = 8;

	public static final int INS_SG = 9;

	public static final int INS_PL = 10;

	public static final int PRP_SG = 11;

	public static final int PRP_PL = 12;
	
	
	public static LinkedHashSet<Grammem>getRelations(int code){
		switch (code) {
		case NOM_SG:
			return Grammem.getGrammemSet(Grammem.PartOfSpeech.NOUN,Grammem.Case.NOMN,Grammem.SingularPlural.SINGULAR);
		case NOM_PL:
			return Grammem.getGrammemSet(Grammem.PartOfSpeech.NOUN,Grammem.Case.NOMN,Grammem.SingularPlural.PLURAL);				
		case GEN_SG:
			return Grammem.getGrammemSet(Grammem.PartOfSpeech.NOUN,Grammem.Case.GENT,Grammem.SingularPlural.SINGULAR);
		case GEN_PL:
			return Grammem.getGrammemSet(Grammem.PartOfSpeech.NOUN,Grammem.Case.GENT,Grammem.SingularPlural.PLURAL);
		case DAT_SG:
			return Grammem.getGrammemSet(Grammem.PartOfSpeech.NOUN,Grammem.Case.DATV,Grammem.SingularPlural.SINGULAR);
		case DAT_PL:
			return Grammem.getGrammemSet(Grammem.PartOfSpeech.NOUN,Grammem.Case.DATV,Grammem.SingularPlural.PLURAL);
		case ACC_SG:
			return Grammem.getGrammemSet(Grammem.PartOfSpeech.NOUN,Grammem.Case.ACCS,Grammem.SingularPlural.SINGULAR);
		case ACC_PL:
			return Grammem.getGrammemSet(Grammem.PartOfSpeech.NOUN,Grammem.Case.ACCS,Grammem.SingularPlural.PLURAL);
		case INS_SG:
			return Grammem.getGrammemSet(Grammem.PartOfSpeech.NOUN,Grammem.Case.ABLT,Grammem.SingularPlural.SINGULAR);
		case INS_PL:
			return Grammem.getGrammemSet(Grammem.PartOfSpeech.NOUN,Grammem.Case.ABLT,Grammem.SingularPlural.PLURAL);
		case PRP_SG:
			return Grammem.getGrammemSet(Grammem.PartOfSpeech.NOUN,Grammem.Case.LOCT,Grammem.SingularPlural.SINGULAR);
		case PRP_PL:
			return Grammem.getGrammemSet(Grammem.PartOfSpeech.NOUN,Grammem.Case.LOCT,Grammem.SingularPlural.PLURAL);				
		default:
			break;
		}
		return null;			
	}
    
}