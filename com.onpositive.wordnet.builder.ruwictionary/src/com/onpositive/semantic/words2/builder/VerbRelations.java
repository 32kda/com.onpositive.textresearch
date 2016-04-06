package com.onpositive.semantic.words2.builder;

import java.util.LinkedHashSet;

import com.onpositive.semantic.wordnet.Grammem;

public abstract class VerbRelations {
	public static final int IA = 1;
	public static final int IA_PAST = 2;
	
	public static final int WE = 3;
	public static final int WE_PAST = 4;

	public static final int YOU = 5;
	public static final int YOU_PAST = 6;
	public static final int YOU_DIRECT = 7;

	public static final int ON_ONA_ON = 8;
	public static final int ON_ONA_ONO_PAST = 9;

	public static final int ONI = 10;
	public static final int ONI_PAST = 11;

	public static final int FUTURE = 12;
	public static final int INFINITIVE = 13;
	
	public static final int THOU = 14;
	public static final int THOU_PAST = 15;
	public static final int THOU_DIRECT = 16;
	
	public static LinkedHashSet<Grammem>getRelations(int code){
		switch (code) {
		case IA:
			return Grammem.getGrammemSet(Grammem.PartOfSpeech.VERB,Grammem.Time.PRESENT,Grammem.Personality.PERS1,Grammem.SingularPlural.SINGULAR);
		case IA_PAST:
			return Grammem.getGrammemSet(Grammem.PartOfSpeech.VERB,Grammem.Time.PAST,Grammem.Personality.PERS1,Grammem.SingularPlural.SINGULAR);
		case WE:
			return Grammem.getGrammemSet(Grammem.PartOfSpeech.VERB,Grammem.Time.PRESENT,Grammem.Personality.PERS1,Grammem.SingularPlural.PLURAL);
		case WE_PAST:
			return Grammem.getGrammemSet(Grammem.PartOfSpeech.VERB,Grammem.Time.PAST,Grammem.Personality.PERS1,Grammem.SingularPlural.PLURAL);
		case YOU:
			return Grammem.getGrammemSet(Grammem.PartOfSpeech.VERB,Grammem.Time.PRESENT,Grammem.Personality.PERS2,Grammem.SingularPlural.PLURAL);
		case YOU_PAST:
			return Grammem.getGrammemSet(Grammem.PartOfSpeech.VERB,Grammem.Time.PAST,Grammem.Personality.PERS2,Grammem.SingularPlural.PLURAL);
		case YOU_DIRECT:
			return Grammem.getGrammemSet(Grammem.PartOfSpeech.VERB,Grammem.Time.PRESENT,Grammem.Mood.impr,Grammem.Personality.PERS2,Grammem.SingularPlural.PLURAL);				
		case THOU:
			return Grammem.getGrammemSet(Grammem.PartOfSpeech.VERB,Grammem.Time.PRESENT,Grammem.Personality.PERS2,Grammem.SingularPlural.SINGULAR);
		case THOU_PAST:
			return Grammem.getGrammemSet(Grammem.PartOfSpeech.VERB,Grammem.Time.PAST,Grammem.Personality.PERS2,Grammem.SingularPlural.SINGULAR);				
		case THOU_DIRECT:
			return Grammem.getGrammemSet(Grammem.PartOfSpeech.VERB,Grammem.Time.PRESENT,Grammem.Mood.impr,Grammem.Personality.PERS2,Grammem.SingularPlural.SINGULAR);
		case ON_ONA_ON:
			return Grammem.getGrammemSet(Grammem.PartOfSpeech.VERB,Grammem.Time.PRESENT,Grammem.Personality.PERS3,Grammem.SingularPlural.SINGULAR);
		case ON_ONA_ONO_PAST:
			return Grammem.getGrammemSet(Grammem.PartOfSpeech.VERB,Grammem.Time.PAST,Grammem.Personality.PERS3,Grammem.SingularPlural.SINGULAR);
		case ONI:
			return Grammem.getGrammemSet(Grammem.PartOfSpeech.VERB,Grammem.Time.PRESENT,Grammem.Personality.PERS3,Grammem.SingularPlural.PLURAL);
		case ONI_PAST:
			return Grammem.getGrammemSet(Grammem.PartOfSpeech.VERB,Grammem.Time.PAST,Grammem.Personality.PERS3,Grammem.SingularPlural.PLURAL);				
		case FUTURE:
			return Grammem.getGrammemSet(Grammem.PartOfSpeech.VERB,Grammem.Time.FUTURE);
		case INFINITIVE:
			return Grammem.getGrammemSet(Grammem.PartOfSpeech.INFN);				
			
		default:
			break;
		}
		return null;			
	}
}