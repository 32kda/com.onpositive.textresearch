package com.onpositive.semantic.wordnet;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;

public class Grammem {
	
	public final byte intId;
	public final String id;
	
	public final String alias;
	public final String description;
	static byte lastId=0;
	static Grammem[]idToGrammem=new Grammem[124];
	static HashMap<String, Grammem>stringMap=new HashMap<String, Grammem>();
	public static final HashSet<Grammem>all=new HashSet<Grammem>();
	public final Grammem parent;
	static HashSet<Class<? extends Grammem>>allGrammemClasses=new HashSet<Class<? extends Grammem>>();
	
	static final Grammem NO_GRAMEM=new Grammem(0,"VOID","VOID","пусто");

	public static LinkedHashSet<Grammem> getGrammemSet(Grammem... a) {
		return new LinkedHashSet<Grammem>(Arrays.asList(a));
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + intId;
		return result;
	}
	@Override
	public String toString() {
		return alias;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		Grammem other = (Grammem) obj;
		if (intId != other.intId)
			return false;
		return true;
	}
	
	public static Grammem get(String  id){
		return stringMap.get(id);
	}
	
	public int getInitialId() {
		return 0;
	}
	
	Grammem(int ordinal,String id, String alias, String description,Grammem parent) {
		super();
		this.intId = (byte) ordinal;
		all.add(this);
		if (idToGrammem[ordinal]!=null){
			throw new IllegalStateException();
		}
		idToGrammem[ordinal]=this;
		if (lastId>123){
			throw new IllegalStateException();
		}
		this.id = id;
		this.alias = alias;
		Grammem put = stringMap.put(id, this);
		if (put!=null){
			throw new IllegalStateException();
		}
		this.description = description;
		this.parent=parent;
	}
	
	public boolean containedIn(GrammarRelation... rels){
		for (GrammarRelation a:rels){
			if (a.owner.getGrammemSet(a.relation).contains(this)){
				return true;
			}
		}
		return false;
	}
	
	Grammem(int ordinal,String id, String alias, String description) {
		super();
		this.parent=null;
		this.intId = (byte) ordinal;
		all.add(this);
		if (idToGrammem[ordinal]!=null){
			throw new IllegalStateException();
		}
		idToGrammem[ordinal]=this;
		if (lastId>123){
			throw new IllegalStateException();
		}
		Grammem put = stringMap.put(id, this);
		if (put!=null){
			throw new IllegalStateException();
		}
		this.id = id;
		this.alias = alias;
		this.description = description;
	}
	static{
		 allGrammemClasses.add(PartOfSpeech.class);
		 PartOfSpeech.init();
		 allGrammemClasses.add(Gender.class);
		 Gender.init();
		 allGrammemClasses.add(AnimateProperty.class);
		 AnimateProperty.init();
		 allGrammemClasses.add(SingularPlural.class);
		 SingularPlural.init();
		 allGrammemClasses.add(Case.class);
		 Case.init();
		 allGrammemClasses.add(SemanGramem.class);
		 SemanGramem.init();
		 allGrammemClasses.add(FeaturesGramem.class);
		 FeaturesGramem.init();
		 allGrammemClasses.add(VerbKind.class);
		 VerbKind.init();
		 allGrammemClasses.add(TransKind.class);
		 TransKind.init();
		 allGrammemClasses.add(StrangeVerbStuff.class);
		 StrangeVerbStuff.init();
		 allGrammemClasses.add(Personality.class);
		 Personality.init();
		 allGrammemClasses.add(Time.class);
		 Time.init();
		 allGrammemClasses.add(Mood.class);
		 Mood.init();
		 allGrammemClasses.add(Invl.class);
		 Invl.init();
		 allGrammemClasses.add(VOic.class);
		 VOic.init();
		 allGrammemClasses.add(Extras.class);
		 Extras.init();
	}
	public static class PartOfSpeech extends Grammem{
		
		private static final int FIRST = 1;

		PartOfSpeech(int ordinal,String id, String alias,
				String description) {
			super( ordinal,id, alias, description);
			all.add(this);
		}
		public static void init() {			
		}
		public static final HashSet<PartOfSpeech>all=new HashSet<PartOfSpeech>();
		
		public static final PartOfSpeech NOUN=new PartOfSpeech(1,"NOUN", "СУЩ", "имя существительное");
		public static final PartOfSpeech ADJF=new PartOfSpeech(2,"ADJF", "ПРИЛ", "имя прилагательное (полное)");
		public static final PartOfSpeech ADJS=new PartOfSpeech(3, "ADJS", "КР_ПРИЛ", "имя прилагательное (краткое)");
		public static final PartOfSpeech COMP=new PartOfSpeech(4, "COMP", "КОМП", "компаратив");
		public static final PartOfSpeech VERB=new PartOfSpeech(5, "VERB", "ГЛ", "глагол (личная форма)");
		public static final PartOfSpeech INFN=new PartOfSpeech(6, "INFN", "ИНФ", "глагол (инфинитив)");
		public static final PartOfSpeech PRTF=new PartOfSpeech(7, "PRTF", "ПРИЧ", "причастие (полное)");
		public static final PartOfSpeech PRTS=new PartOfSpeech(8, "PRTS", "КР_ПРИЧ", "причастие (краткое)");
		public static final PartOfSpeech GRND=new PartOfSpeech(9, "GRND", "ДЕЕПР", "деепричастие");
		public static final PartOfSpeech NUMR=new PartOfSpeech(10, "NUMR", "ЧИСЛ", "числительное");
		public static final PartOfSpeech ADVB=new PartOfSpeech(11, "ADVB", "Н", "наречие");
		public static final PartOfSpeech NPRO=new PartOfSpeech(12, "NPRO", "МС", "местоимение-существительное");
		public static final PartOfSpeech PRED=new PartOfSpeech(13, "PRED", "ПРЕДК", "предикатив");
		public static final PartOfSpeech PREP=new PartOfSpeech(14, "PREP", "ПР", "предлог");
		public static final PartOfSpeech CONJ=new PartOfSpeech(15, "CONJ", "СОЮЗ", "союз");
		public static final PartOfSpeech PRCL=new PartOfSpeech(16, "PRCL", "ЧАСТ", "частица");
		public static final PartOfSpeech INTJ=new PartOfSpeech(17, "INTJ", "МЕЖД", "междометие");
		
		public final boolean isDefinitelyThisPartOfSpech(TextElement e){
			return e.hasOnlyGrammemOfKind(this); 
		}
		public final boolean mayBeThisPartOfSpech(TextElement e){
			return e.hasGrammem(this); 
		}
		public final boolean mayBeThisPartOfSpech(GrammarRelation[] e){
			for (GrammarRelation q:e){
				if (q==null){
					continue;
				}
				if (q.getWord()==null){
					continue;
				}
				if (q.getWord().hasGrammem(this)){
					return true;
				}
			}
			return false;
		}
		public final boolean isDefinitelyThisPartOfSpech(GrammarRelation[] e){
			for (GrammarRelation q:e){
				if (q.getWord().hasOnlyGrammemOfKind(this)){
					return true;
				}
			}
			return false;
		}
		
		public int getInitialId() {
			return FIRST;
		}
	}
	
	public static class AnimateProperty extends Grammem{
		
		private static final int FIRST = 18;

		AnimateProperty(int ordinal,String id, String alias, String description) {
			super(ordinal,id, alias, description);
			all.add(this);
		}
		public static void init() {
			
			
		}
		public static final HashSet<AnimateProperty>all=new HashSet<AnimateProperty>();
		
		public static final AnimateProperty ANim=new AnimateProperty(18,"ANim", "Од-неод", "одушевлённость / одушевлённость не выражена");
		public static final AnimateProperty anim=new AnimateProperty(19,"anim", "од", "одушевлённое");
		public static final AnimateProperty inan=new AnimateProperty(20,"inan", "неод", "неодушевлённое");
		
		public int getInitialId() {
			return FIRST;
		}

	}

	
	public static class Gender extends Grammem{
		
		private static final int FIRST = 21;

		public static final HashSet<Gender>all=new HashSet<Gender>();
		
		Gender(int ordinal,String id, String alias, String description) {
			super(ordinal,id, alias, description);
			all.add(this);
		}
		
		public static void init() {
			
		}

		public static final Gender UNKNOWN=new Gender(21,"GNdr", "хр", "род / род не выражен");
		public static final Gender MASC=new Gender(22,"masc", "мр", "мужской род");
		public static final Gender FEMN=new Gender(23,"femn", "жр", "женский род");
		public static final Gender NEUT=new Gender(24,"neut", "ср", "средний род");
		public static final Gender COMMON=new Gender(25,"Ms-f", "ор", "общий род");
		
		public int getInitialId() {
			return FIRST;
		}

	}
	
	public static class SingularPlural extends Grammem{
		
		private static final int FIRST = 26;

		public static final HashSet<SingularPlural>all=new HashSet<SingularPlural>();
		
		SingularPlural(int ordinal,String id, String alias, String description) {
			super(ordinal,id, alias, description);
			all.add(this);
		}
		
		public static void init() {
			
		}

		public SingularPlural(int ordinal,String id, String alias, String description,
				Grammem parent) {
			super(ordinal,id, alias, description, parent);
			all.add(this);			
		}

		public static final SingularPlural SINGULAR=new SingularPlural(26,"sing", "ед", "единственное число");
		public static final SingularPlural PLURAL=new SingularPlural(27,"plur", "мн", "множественное число");
		public static final SingularPlural UNCHANGABLE=new SingularPlural(28,"Fixd", "0", "неизменяемое");
		
		public static final SingularPlural SG1=new SingularPlural(29,"Sgtm", "sg", "singularia tantum",SINGULAR);
		public static final SingularPlural Pl1=new SingularPlural(30,"Pltm", "pl", "pluralia tantum",PLURAL);
		
		public int getInitialId() {
			return FIRST;
		}

	}

	public static class Case extends  Grammem{
		
		private static final int FIRST = 31;
		
		public static final HashSet<Case>all=new HashSet<Case>();
		Case(int ordinal,String id, String alias, String description) {
			super(ordinal,id, alias, description);
			all.add(this);
		}
		
		public static void init() {			
		}

		Case(int ordinal,String id, String alias, String description, Grammem parent) {
			super(ordinal,id, alias, description, parent);
			all.add(this);
		}

		public static final Case NOMN=new Case(31,"nomn", "им", "именительный падеж");
		public static final Case GENT=new Case(32,"gent", "рд", "родительный падеж");
		public static final Case DATV=new Case(33,"datv", "дт", "дательный падеж");
		public static final Case ACCS=new Case(34,"accs", "вн", "винительный падеж");
		public static final Case ABLT=new Case(35,"ablt", "тв", "творительный падеж");
		public static final Case LOCT=new Case(36,"loct", "пр", "предложный падеж");
		public static final Case VOCT=new Case(37,"voct", "зв", "звательный падеж",NOMN);
		public static final Case GEN1=new Case(38,"gen1", "рд1", "первый родительный падеж",GENT);
		public static final Case GEN2=new Case(39,"gen2", "рд2", "второй родительный (частичный) падеж",GENT);
		public static final Case ACC2=new Case(40,"acc2", "вн2", "второй винительный падеж",ACCS);
		public static final Case LOC1=new Case(41,"loc1", "пр1", "первый предложный падеж",LOCT);
		public static final Case LOC2=new Case(42,"loc2", "пр2", "второй предложный (местный) падеж",LOCT);
		
		public int getInitialId() {
			return FIRST;
		}

	}
	public static class SemanGramem extends Grammem{
		
		private static final int FIRST = 43;
		
		public static final HashSet<SemanGramem>all=new HashSet<SemanGramem>();
		SemanGramem(int ordinal,String id, String alias, String description) {
			super(ordinal,id, alias, description);
			all.add(this);
		}
		public static void init() {
			
		}
		public static final SemanGramem ABBR=new SemanGramem(43,"Abbr", "аббр","аббревиатура");
		public static final SemanGramem NAME=new SemanGramem(44,"Name", "имя","имя");
		public static final SemanGramem SURN=new SemanGramem(45,"Surn", "фам","фамилия");
		public static final SemanGramem PATR=new SemanGramem(46,"Patr", "отч","отчество");
		public static final SemanGramem TOPONIM=new SemanGramem(47,"Geox", "гео","топоним");
		public static final SemanGramem ORGN=new SemanGramem(48,"Orgn", "орг","организация");
		public static final SemanGramem TRADE_MARK=new SemanGramem(49,"Trad", "tm","торговая марка");
		
		public int getInitialId() {
			return FIRST;
		}

	}
	
	public static class FeaturesGramem extends Grammem{
		
		private static final int FIRST = 50;
		
		public static final HashSet<FeaturesGramem>all=new HashSet<FeaturesGramem>();
		
		FeaturesGramem(int ordinal,String id, String alias, String description) {
			super(ordinal,id, alias, description);
			all.add(this);
		}
		public static void init() {
			
		}
		public static final FeaturesGramem Subx=new FeaturesGramem(50,"Subx", "субст?", "возможна субстантивация");
		public static final FeaturesGramem Supr=new FeaturesGramem(51,"Supr", "превосх", "превосходная степень");
		public static final FeaturesGramem Qual=new FeaturesGramem(52,"Qual", "кач", "качественное");
		public static final FeaturesGramem Apro=new FeaturesGramem(53,"Apro", "мест-п", "местоименное");
		public static final FeaturesGramem Anum=new FeaturesGramem(54,"Anum", "числ-п", "порядковое");
		public static final FeaturesGramem Poss=new FeaturesGramem(55,"Poss", "притяж", "притяжательное");
		public static final FeaturesGramem V_ey=new FeaturesGramem(56,"V-ey", "*ею", "форма на -ею");
		public static final FeaturesGramem V_oy=new FeaturesGramem(57,"V-oy", "*ою", "форма на -ою");
		public static final FeaturesGramem Cmp2=new FeaturesGramem(58,"Cmp2", "сравн2", "сравнительная степень на по-");
		public static final FeaturesGramem V_ej=new FeaturesGramem(59,"V-ej", "*ей", "форма компаратива на -ей");
		
		public int getInitialId() {
			return FIRST;
		}

	}
	
	public static class VerbKind extends Grammem{
		
		private static final int FIRST = 60;
		
		public static final HashSet<VerbKind>all=new HashSet<VerbKind>();
		VerbKind(int ordinal,String id, String alias, String description) {
			super(ordinal,id, alias, description);
			all.add(this);
		}
		public static void init() {
			
		}
		public static final VerbKind PERFECT=new VerbKind(60,"perf", "сов", "совершенный вид");
		public static final VerbKind IMPERFECT=new VerbKind(61,"impf", "несов", "несовершенный вид");
		
		public int getInitialId() {
			return FIRST;
		}

	}
	
	
	public static class TransKind extends Grammem{
		
		private static final int FIRST = 62;
		
		public static final HashSet<TransKind>all=new HashSet<TransKind>();
		TransKind(int ordinal,String id, String alias, String description) {
			super(ordinal,id, alias, description);
			all.add(this);
		}
		public static void init() {
			
		}
		public static final TransKind tran=new TransKind(62,"tran", "перех", "переходный");
		public static final TransKind intr=new TransKind(63,"intr", "неперех", "непереходный");
		
		public int getInitialId() {
			return FIRST;
		}

	}
	
	
	
	public static class StrangeVerbStuff extends Grammem{
		
		private static final int FIRST = 64;
		
		public static final HashSet<StrangeVerbStuff>all=new HashSet<StrangeVerbStuff>();
		StrangeVerbStuff(int ordinal,String id, String alias, String description) {
			super(ordinal,id, alias, description);
			all.add(this);
		}
		public static void init() {
			
		}
		public static final StrangeVerbStuff Impe=new StrangeVerbStuff(64,"Impe", "безл", "безличный");
		public static final StrangeVerbStuff Impx=new StrangeVerbStuff(65,"Impx", "безл?", "возможно безличное употребление");
		public static final StrangeVerbStuff Mult=new StrangeVerbStuff(66,"Mult", "мног", "многократный");
		public static final StrangeVerbStuff Refl=new StrangeVerbStuff(67,"Refl", "возвр", "возвратный");
		
		public int getInitialId() {
			return FIRST;
		}

	}
	
	
	public static class Personality extends Grammem{
		
		private static final int FIRST = 68;
		
		public static final HashSet<Personality>all=new HashSet<Personality>();
		Personality(int ordinal,String id, String alias, String description) {
			super(ordinal,id, alias, description);
			all.add(this);
		}
		public static void init() {
			
		}
		public static final Personality PERS1=new Personality(68,"1per", "1л", "1 лицо");
		public static final Personality PERS2=new Personality(69,"2per", "2л", "2 лицо");
		public static final Personality PERS3=new Personality(70,"3per", "3л", "3 лицо");

		public int getInitialId() {
			return FIRST;
		}

	}
	
	public static class Time extends Grammem{
		
		private static final int FIRST = 71;

		public static final HashSet<Time>all=new HashSet<Time>();
		Time(int ordinal,String id, String alias, String description) {
			super(ordinal,id, alias, description);
			all.add(this);
		}
		public static void init() {
			
		}
		public static final Time PRESENT=new Time(71,"pres", "наст", "настоящее время");
		public static final Time PAST=new Time(72,"past", "прош", "прошедшее время");
		public static final Time FUTURE=new Time(73,"futr", "буд", "будущее время");
		
		public int getInitialId() {
			return FIRST;
		}

	}
	
	public static class Mood extends Grammem{
		
		private static final int FIRST = 74;
		
		public static final HashSet<Mood>all=new HashSet<Mood>();
		Mood(int ordinal,String id, String alias, String description) {
			super(ordinal,id, alias, description);
			all.add(this);
		}
		public static void init() {
			
		}
		public static final Mood indc=new Mood(74,"indc", "изъяв", "изъявительное наклонение");
		public static final Mood impr=new Mood(75,"impr", "повел", "повелительное наклонение");
		
		public int getInitialId() {
			return FIRST;
		}
		
	}
	
	public static class Invl extends Grammem{
		
		private static final int FIRST = 76;
		
		public static final HashSet<Invl>all=new HashSet<Invl>();
		Invl(int ordinal,String id, String alias, String description) {
			super(ordinal,id, alias, description);
			all.add(this);
		}
		public static void init() {
			// TODO Auto-generated method stub
			
		}
		public static final Invl incl=new Invl(76,"incl", "вкл", "говорящий включён (идем, идемте) ");
		public static final Invl excl=new Invl(77,"excl", "выкл", "говорящий не включён в действие (иди, идите)");
		
		public int getInitialId() {
			return FIRST;
		}

	}
	public static class VOic extends Grammem{
		
		private static final int FIRST = 78;
		
		public static final HashSet<VOic>all=new HashSet<VOic>();
		VOic(int ordinal,String id, String alias, String description) {
			super(ordinal,id, alias, description);
			all.add(this);
		}
		public static void init() {
			
		}
		public static final VOic actv=new VOic(78,"actv", "действ", "действительный залог");
		public static final VOic pssv=new VOic(79,"pssv", "страд", "страдательный залог");
		
		public int getInitialId() {
			return FIRST;
		}

	}
	
	public static class Extras extends Grammem{
		
		private static final int FIRST = 80;
		
		public static final HashSet<Extras>all=new HashSet<Extras>();
		Extras(int ordinal,String id, String alias, String description) {
			super(ordinal,id, alias, description);
			all.add(this);
		}
		public static void init() {
			
		}
		public static final Extras Infr=new Extras(80,"Infr", "разг", "разговорное");
		public static final Extras Slng=new Extras(81,"Slng", "жарг", "жаргонное");
		public static final Extras Arch=new Extras(82,"Arch", "арх", "устаревшее");
		public static final Extras Litr=new Extras(83,"Litr", "лит", "литературный вариант");
		public static final Extras Erro=new Extras(84,"Erro", "опеч", "опечатка");
		public static final Extras Dist=new Extras(85,"Dist", "искаж", "искажение");
		public static final Extras Ques=new Extras(86,"Ques", "вопр", "вопросительное");
		public static final Extras Dmns=new Extras(87,"Dmns", "указ", "указательное");
		public static final Extras Prnt=new Extras(88,"Prnt", "вводн", "вводное слово");
		public static final Extras V_be=new Extras(89,"V-be", "*ье", "форма на -ье");
		public static final Extras V_en=new Extras(90,"V-en", "енен", "форма на -енен");
		public static final Extras V_ie=new Extras(91,"V-ie", "*ие", "отчество через -ие-");
		public static final Extras V_bi=new Extras(92,"V-bi", "*ьи", "форма на -ьи");
		public static final Extras Fimp=new Extras(93,"Fimp", "*несов", "деепричастие от глагола несовершенного вида");
		public static final Extras Prdx=new Extras(94,"Prdx", "предк?", "может выступать в роли предикатива");
		public static final Extras Coun=new Extras(95,"Coun", "счетн", "счётная форма");
		public static final Extras Coll=new Extras(96,"Coll", "собир", "собирательное числительное");
		
		public static final Extras V_sh=new Extras(97,"V-sh", "*ши", "деепричастие на -ши");
		public static final Extras Af_p=new Extras(98,"Af-p", "*предл", "форма после предлога");
		
		public static final Extras Inmx=new Extras(99,"Inmx", "не/одуш?", "может использоваться как одуш. / неодуш. ");
		public static final Extras Vpre=new Extras(100,"Vpre", "в_предл", "Вариант предлога ( со, подо, ...)");
		public static final Extras Anph=new Extras(101,"Anph", "Анаф", "Анафорическое (местоимение)");
		public static final Extras Init=new Extras(102,"Init", "иниц", "Инициал");
		public static final Extras Adjx=new Extras(103,"Adjx", "прил?", "может выступать в роли прилагательного");
		
		public int getInitialId() {
			return FIRST;
		}

	}
	public static Grammem get(int i) {
		return idToGrammem[i];
	}
}