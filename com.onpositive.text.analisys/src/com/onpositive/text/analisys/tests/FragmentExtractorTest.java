package com.onpositive.text.analisys.tests;

import java.util.List;

import com.onpositive.semantic.wordnet.composite.CompositeWordnet;
import com.onpositive.text.analysis.IToken;
import com.onpositive.text.analysis.lexic.dimension.Unit;
import com.onpositive.text.analysis.lexic.dimension.UnitKind;
import com.onpositive.text.analysis.syntax.SyntaxParser;

public class FragmentExtractorTest extends ParserTest{

	private Unit kilometerPerHourUnit = new Unit("километр в час",UnitKind.SPEED,1);
	
	public FragmentExtractorTest() {
		super();
		CompositeWordnet wn=new CompositeWordnet();
		wn.addUrl("/numerics.xml");
		wn.addUrl("/dimensions.xml");
		wn.addUrl("/modificator-adverb.xml");
		wn.addUrl("/prepositions.xml");
		wn.addUrl("/conjunctions.xml");
		wn.addUrl("/modalLikeVerbs.xml");
		wn.addUrl("/participles.xml");
		wn.prepare();
		SyntaxParser syntaxParser = new SyntaxParser(wn);
		setLogger(syntaxParser);
		this.composition = syntaxParser;
	}
	
	public void test_brackets000(){
		String str = "Петя (он работает (в институте) программистом) является моим другом.";		
		List<IToken> processed = process(str);
		assertTestTokenPrint( processed,
			"CLAUSE("
		        + "<subject>WORD_FORM("
		          + "<main>WORD_FORM петя([имя, мр, од, СУЩ])"
		          + "BRACKETS("
		            + "REGION_BOUND("
		              + "SYMBOL (  )"
		            + "CLAUSE("
		              + "<subject>WORD_FORM он([мр, Анаф, МС, 3л])"
		              + "<predicate>VERB_NOUN("
		                + "<main>WORD_FORM("
		                  + "<main>WORD_FORM работаю([ГЛ, неперех, несов])"
		                  + "BRACKETS("
		                    + "REGION_BOUND("
		                      + "SYMBOL (  )"
		                    + "WORD_FORM в([ПР])"
		                    + "WORD_FORM институт([мр, СУЩ, неод])"
		                    + "REGION_BOUND("
		                      + "SYMBOL )  )  )  )"
		                + "WORD_FORM программист([мр, од, СУЩ])  )  )"
		            + "REGION_BOUND("
		              + "SYMBOL )  )  )  )"
		        + "<predicate>VERB_NOUN("
		          + "<main>WORD_FORM являюсь([ГЛ, неперех, несов])"
		          + "NOUN_ADJECTIVE("
		            + "WORD_FORM мой([мест-п, ПРИЛ])"
		            + "<main>WORD_FORM друг([мр, од, СУЩ])  )  )  )");
	}
	
	public void test_brackets001(){
		String str = "Пушкин (26 мая [6 июня] 1799, Москва — 29 января [10 февраля] 1837, Санкт-Петербург) является русским поэтом, драматургом и прозаиком.";		
		List<IToken> processed = process(str);
		assertTestTokenPrint( processed,
			"CLAUSE("
				  + "<subject>WORD_FORM("
					+ "<main>WORD_FORM *([пушкин([sg, мр, од, СУЩ, фам]), пушкин([гео])])"
					+ "BRACKETS("
					  + "REGION_BOUND("
						+ "SYMBOL (  )"
					  + "DATE 26.0 май([sg, мр, СУЩ, неод]) "
					  + "SYMBOL ["
					  + "DATE 6.0 июнь([мр, СУЩ, неод]) "
					  + "SYMBOL ]"
					  + "SCALAR 1799.0"
					  + "SYMBOL ,"
					  + "CLAUSE("
						+ "<subject>WORD_FORM москва([sg, жр, СУЩ, гео, неод])"
						+ "<predicate>no predicate  )"
					  + "SYMBOL —"
					  + "DATE 29.0 январь([мр, СУЩ, неод]) "
					  + "SYMBOL ["
					  + "DATE 10.0 февраль([мр, СУЩ, неод]) "
					  + "SYMBOL ]"
					  + "SCALAR 1837.0"
					  + "SYMBOL ,"
					  + "WORD_FORM санкт-петербург([мр, СУЩ, гео, неод])"
					  + "CLAUSE("
						+ "<subject>WORD_FORM петербург([мр, СУЩ, гео, неод])"
						+ "<predicate>no predicate  )"
					  + "REGION_BOUND("
						+ "SYMBOL )  )  )  )"
				  + "<predicate>VERB_NOUN("
					+ "<main>WORD_FORM являюсь([ГЛ, неперех, несов])"
					+ "UNIFORM_NOUN("
					  + "NOUN_ADJECTIVE("
						+ "WORD_FORM русский([субст?, ПРИЛ])"
						+ "<main>WORD_FORM поэт([мр, од, СУЩ])  )"
					  + "SYMBOL ,"
					  + "WORD_FORM драматург([мр, од, СУЩ])"
					  + "WORD_FORM и([СОЮЗ])"
					  + "<main>WORD_FORM прозаик([мр, од, СУЩ])  )  )  )");
	}
	
	public void test_direct_speech000(){
		String str = "\"Здравствуйте вам ?\" - сказал он.";		
		List<IToken> processed = process(str);
		assertTestTokenPrint( processed,
			"CLAUSE("
			        + "<subject>WORD_FORM он([мр, Анаф, МС, 3л])"
			        + "<predicate>WORD_FORM("
			          + "DIRECT_SPEACH("
			            + "REGION_BOUND("
			              + "SYMBOL \"  )"
			            + "CLAUSE("
			              + "<subject>no subject"
			              + "<predicate>VERB_NOUN("
			                + "<main>WORD_FORM здравствую([ГЛ, неперех, несов])"
			                + "WORD_FORM вы([МС, 2л])  )  )"
			            + "SYMBOL ?"
			            + "REGION_BOUND("
			              + "SYMBOL \""
			              + "SYMBOL -  )  )"
			          + "<main>WORD_FORM сказал([сов, ГЛ, перех])  )  )");
	}
	
	public void test_direct_speech001(){
		String str = "\"Здравствуйте вам\", - сказал он.";		
		List<IToken> processed = process(str);
		assertTestTokenPrint( processed,
			"CLAUSE("
		        + "<subject>WORD_FORM он([мр, Анаф, МС, 3л])"
		        + "<predicate>WORD_FORM("
		          + "DIRECT_SPEACH("
		            + "REGION_BOUND("
		              + "SYMBOL \"  )"
		            + "CLAUSE("
		              + "<subject>no subject"
		              + "<predicate>VERB_NOUN("
		                + "<main>WORD_FORM здравствую([ГЛ, неперех, несов])"
		                + "WORD_FORM вы([МС, 2л])  )  )"
		            + "REGION_BOUND("
		              + "SYMBOL \""
		              + "SYMBOL ,"
		              + "SYMBOL -  )  )"
		          + "<main>WORD_FORM сказал([сов, ГЛ, перех])  )  )");
	}
	
	public void test_direct_speech002(){
		String str = "Он сказал: \"Здравствуйте вам!\"";		
		List<IToken> processed = process(str);
		assertTestTokenPrint( processed,
			"CLAUSE("
		        + "<subject>WORD_FORM он([мр, Анаф, МС, 3л])"
		        + "<predicate>WORD_FORM("
		          + "<main>WORD_FORM сказал([сов, ГЛ, перех])"
		          + "DIRECT_SPEACH("
		            + "REGION_BOUND("
		              + "SYMBOL :"
		              + "SYMBOL \"  )"
		            + "CLAUSE("
		              + "<subject>no subject"
		              + "<predicate>VERB_NOUN("
		                + "<main>WORD_FORM здравствую([ГЛ, неперех, несов])"
		                + "WORD_FORM вы([МС, 2л])  )  )"
		            + "SYMBOL !"
		            + "REGION_BOUND("
		              + "SYMBOL \"  )  )  )  )");
	}
	
	public void test_direct_speech003(){
		String str = "Он сказал: \"Здравствуйте вам!\" - радостным голосом.";
		List<IToken> processed = process(str);
		assertTestTokenPrint( processed,
			"CLAUSE("
				+ "<subject>WORD_FORM он([мр, Анаф, МС, 3л])"
				+ "<predicate>VERB_NOUN("
				  + "<main>WORD_FORM("
					+ "<main>WORD_FORM сказал([сов, ГЛ, перех])"
					+ "DIRECT_SPEACH("
					  + "REGION_BOUND("
						+ "SYMBOL :"
						+ "SYMBOL \"  )"
					  + "CLAUSE("
						+ "<subject>no subject"
						+ "<predicate>VERB_NOUN("
						  + "<main>WORD_FORM здравствую([ГЛ, неперех, несов])"
						  + "WORD_FORM вы([МС, 2л])  )  )"
					  + "SYMBOL !"
					  + "REGION_BOUND("
						+ "SYMBOL \""
						+ "SYMBOL -  )  )  )"
				  + "NOUN_ADJECTIVE("
					+ "WORD_FORM радостный([кач, ПРИЛ])"
					+ "<main>WORD_FORM голос([мр, СУЩ, неод])  )  )  )");
	}
	
	public void test_direct_speech004(){
		String str = "Он сказал: \"Здравствуйте вам\".";		
		List<IToken> processed = process(str);
		assertTestTokenPrint( processed,
			"CLAUSE("
		        + "<subject>WORD_FORM он([мр, Анаф, МС, 3л])"
		        + "<predicate>WORD_FORM("
		          + "<main>WORD_FORM сказал([сов, ГЛ, перех])"
		          + "DIRECT_SPEACH("
		            + "REGION_BOUND("
		              + "SYMBOL :"
		              + "SYMBOL \"  )"
		            + "CLAUSE("
		              + "<subject>no subject"
		              + "<predicate>VERB_NOUN("
		                + "<main>WORD_FORM здравствую([ГЛ, неперех, несов])"
		                + "WORD_FORM вы([МС, 2л])  )  )"
		            + "REGION_BOUND("
		              + "SYMBOL \"  )  )  )  )");
	}
	
	public void test_direct_speech005(){
		String str = "Он сказал: \"Здравствуйте вам\", - радостным голосом.";
		List<IToken> processed = process(str);
		assertTestTokenPrint( processed,
			"CLAUSE("
				+ "<subject>WORD_FORM он([мр, Анаф, МС, 3л])"
				+ "<predicate>VERB_NOUN("
				  + "<main>WORD_FORM("
					+ "<main>WORD_FORM сказал([сов, ГЛ, перех])"
					+ "DIRECT_SPEACH("
					  + "REGION_BOUND("
						+ "SYMBOL :"
						+ "SYMBOL \"  )"
					  + "CLAUSE("
						+ "<subject>no subject"
						+ "<predicate>VERB_NOUN("
						  + "<main>WORD_FORM здравствую([ГЛ, неперех, несов])"
						  + "WORD_FORM вы([МС, 2л])  )  )"
					  + "REGION_BOUND("
						+ "SYMBOL \""
						+ "SYMBOL ,"
						+ "SYMBOL -  )  )  )"
				  + "NOUN_ADJECTIVE("
					+ "WORD_FORM радостный([кач, ПРИЛ])"
					+ "<main>WORD_FORM голос([мр, СУЩ, неод])  )  )  )");
	}
	
	public void test_title000(){
		String str = "Поставим систему \"Окна\".";		
		List<IToken> processed = process(str);
		assertTestTokenPrint( processed,
			"CLAUSE("
		        + "<subject>no subject"
		        + "<predicate>DIRECT_OBJECT_NAME("
		          + "<main>WORD_FORM поставил([сов, ГЛ, перех])"
		          + "WORD_FORM("
		            + "<main>WORD_FORM система([жр, СУЩ, неод])"
		            + "TITLE("
		              + "REGION_BOUND("
		                + "SYMBOL \"  )"
		              + "CLAUSE("
		                + "<subject>WORD_FORM окно([СУЩ, ср, неод])"
		                + "<predicate>no predicate  )"
		              + "REGION_BOUND("
		                + "SYMBOL \"  )  )  )  )  )");
	}
	
	public void test_title001(){
		String str = "\"Окна\" мы поставим.";		
		List<IToken> processed = process(str);
		assertTestTokenPrint( processed,
			"CLAUSE("
		        + "<subject>WORD_FORM("
		          + "TITLE("
		            + "REGION_BOUND("
		              + "SYMBOL \"  )"
		            + "CLAUSE("
		              + "<subject>WORD_FORM окно([СУЩ, ср, неод])"
		              + "<predicate>no predicate  )"
		            + "REGION_BOUND("
		              + "SYMBOL \"  )  )"
		          + "<main>WORD_FORM мы([МС, 1л])  )"
		        + "<predicate>WORD_FORM поставил([сов, ГЛ, перех])  )");
	}
	
	public void test_title002(){
		String str = "Поставим систему \"Окна\" мы.";		
		List<IToken> processed = process(str);
		assertTestTokenPrint( processed,
			"CLAUSE("
		        + "<subject>WORD_FORM мы([МС, 1л])"
		        + "<predicate>DIRECT_OBJECT_NAME("
		          + "<main>WORD_FORM поставил([сов, ГЛ, перех])"
		          + "WORD_FORM("
		            + "<main>WORD_FORM система([жр, СУЩ, неод])"
		            + "TITLE("
		              + "REGION_BOUND("
		                + "SYMBOL \"  )"
		              + "CLAUSE("
		                + "<subject>WORD_FORM окно([СУЩ, ср, неод])"
		                + "<predicate>no predicate  )"
		              + "REGION_BOUND("
		                + "SYMBOL \"  )  )  )  )  )");
	}
		

	
	
	public void test_enumeration000(){
		String str = "Вася купил продукты: сыр, колбасу и мороженое,- в магазине через дорогу.";		
		List<IToken> processed = process(str);
		assertTestTokenPrint( processed,
			"CLAUSE("
		        + "<subject>WORD_FORM вася([имя, мр, од, СУЩ])"
		        + "<predicate>DIRECT_OBJECT_NAME("
		          + "<main>WORD_FORM купил([сов, ГЛ, перех])"
		          + "NOUN_NAME_PREP("
		            + "<main>NOUN_NAME_PREP("
		              + "<main>WORD_FORM("
		                + "<main>WORD_FORM продукт([мр, СУЩ, неод])"
		                + "ENUMERATION("
		                  + "REGION_BOUND("
		                    + "SYMBOL :  )"
		                  + "UNIFORM_NOUN("
		                    + "WORD_FORM сыр([мр, СУЩ, неод])"
		                    + "SYMBOL ,"
		                    + "WORD_FORM колбаса([жр, СУЩ, неод])"
		                    + "WORD_FORM и([СОЮЗ])"
		                    + "<main>WORD_FORM мороженое([СУЩ, ср, неод])  )"
		                  + "WORD_FORM сыр([кач, КР_ПРИЛ])"
		                  + "SYMBOL ,"
		                  + "UNIFORM_NOUN("
		                    + "WORD_FORM колбаса([жр, СУЩ, неод])"
		                    + "WORD_FORM и([СОЮЗ])"
		                    + "<main>WORD_FORM мороженое([СУЩ, ср, неод])  )"
		                  + "WORD_FORM мороженый([ПРИЛ])"
		                  + "REGION_BOUND("
		                    + "SYMBOL ,"
		                    + "SYMBOL -  )  )  )"
		              + "WORD_FORM в([ПР])"
		              + "WORD_FORM магазин([мр, СУЩ, неод])  )"
		            + "WORD_FORM через([ПР])"
		            + "WORD_FORM дорога([жр, СУЩ, неод])  )  )  )");
	}
	
	public void test_complex_direct_speech000(){
		String str = "\"Это вы насчет большого дома перед  деревней	направо? - спросил я. - Того, что стоит в парке?\" -  \"Вот-вот!  -  ответил он, - я именно так и говорю, чтобы вы знали, что я говорю правду и  ничего	не скрываю. Длинный белый дом с колоннами, возле Бландфордской дороги\".";		
		List<IToken> processed = process(str);
		
	}
	
	
	public void test_complex_direct_speech001(){
		String str = "\"Ничего подобного!\" - возразил он. \"Я-то здесь на что?\" - спросил он немного погодя.";		
		List<IToken> processed = process(str);
		assertTestTokenPrint( processed,
				"CLAUSE("
				        + "<subject>WORD_FORM он([мр, Анаф, МС, 3л])"
				        + "<predicate>WORD_FORM("
				          + "DIRECT_SPEACH("
				            + "REGION_BOUND("
				              + "SYMBOL \"  )"
				            + "ADJECTIVE_ADVERB("
				              + "WORD_FORM ничего([Н])"
				              + "<main>WORD_FORM подобный([субст?, кач, ПРИЛ])  )"
				            + "WORD_FORM ничто([ср, МС])"
				            + "WORD_FORM ничего([ЧАСТ])"
				            + "WORD_FORM подобный([субст?, кач, ПРИЛ])"
				            + "SYMBOL !"
				            + "REGION_BOUND("
				              + "SYMBOL \""
				              + "SYMBOL -  )  )"
				          + "<main>WORD_FORM возразил([сов, ГЛ, неперех])  )  )",
				"CLAUSE("
			        + "<subject>WORD_FORM он([мр, Анаф, МС, 3л])"
			        + "<predicate>VERB_ADVERB("
			          + "<main>WORD_FORM("
			            + "DIRECT_SPEACH("
			              + "REGION_BOUND("
			                + "SYMBOL \"  )"
			              + "CLAUSE("
			                + "<subject>WORD_WITH_INDEX("
			                  + "<main>WORD_FORM я([МС, 1л])"
			                  + "SYMBOL -  )"
			                + "<predicate>no predicate  )"
			              + "ADJECTIVE_ADVERB("
			                + "<main>WORD_FORM тот([мест-п, Анаф, субст?, ПРИЛ])"
			                + "WORD_FORM здесь([Н, предк?])  )"
			              + "WORD_FORM то([СОЮЗ])"
			              + "WORD_FORM здесь([Н, предк?])"
			              + "WORD_FORM на([ПР])"
			              + "CLAUSE("
			                + "<subject>WORD_FORM что([СОЮЗ])"
			                + "<predicate>no predicate  )"
			              + "SYMBOL ?"
			              + "REGION_BOUND("
			                + "SYMBOL \""
			                + "SYMBOL -  )  )"
			            + "<main>WORD_FORM спросил([сов, ГЛ, перех])  )"
			          + "UNIFORM_ADVERB("
			            + "WORD_FORM немного([Н])"
			            + "<main>WORD_FORM погодя([Н])  )  )  )");
		
	}
	
	public void test_complex_direct_speech002(){
		String str = "Секретарь  отдела Нина  Ивановна  возникла  на  пороге его  кабинета и, блеснув толстыми стеклами очков, скрипуче пропела- прокукарекала: -\"Лейтенант Кольцов! К начальнику!\" И потом добавила -\"Саша ! И захвати на доклад проверку на \"Дайкона\" После  малозначительного  разговора  по  линии  работы  и  выслушивания доклада  о  результатах  реализации  материалов  об  изготовлении  оружия  и стрельбе на государственной границе начальник задумался .";		
		//String str = "После выслушивания доклада о результатах реализации и стрельбе на государственной границы начальник задумался .";
		List<IToken> processed = process(str);
		assertTestTokenPrint( processed,
				"");
		
	}
	
	
	public void test_complex_direct_speech003(){		
		//String str = "Каждый  век	открывал   человечеству   все   новые  пласты  знаний,  как  бы	приоткрывая еще один покров тайны с более глубокого слоя смысла	записанного в Торе.";
		String str = "Каждый Век открывал человечеству новые пласты знаний.";		
		List<IToken> processed = process(str);
		assertTestTokenPrint( processed,
				"");
		
	}
	
}
