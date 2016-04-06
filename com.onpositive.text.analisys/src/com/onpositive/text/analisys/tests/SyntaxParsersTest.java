package com.onpositive.text.analisys.tests;

import java.util.List;

import com.onpositive.semantic.wordnet.composite.CompositeWordnet;
import com.onpositive.text.analysis.IToken;
import com.onpositive.text.analysis.lexic.scalar.UnaryScalarCompositionParser;
import com.onpositive.text.analysis.syntax.SyntaxParser;

public class SyntaxParsersTest extends ParserTest{

//	private Unit kilometerPerHourUnit = new Unit("километр в час",UnitKind.SPEED,1);
	
	public SyntaxParsersTest() {
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
		this.composition = syntaxParser;
	}
	
	public void test000(){
		String str = "Идёт в большой магазин.";		
		List<IToken> processed = process(str);
		assertTestTokenPrint( processed,
			"CLAUSE("
		        + "<subject>no subject"
		        + "<predicate>VERB_NOUN_PREP("
				     + "<main>WORD_FORM иду([ГЛ, неперех, несов])"
				     + "WORD_FORM в([ПР])"
				     + "NOUN_ADJECTIVE("
					    + "WORD_FORM большой([кач, ПРИЛ])"
					    + "<main>WORD_FORM магазин([мр, СУЩ, неод])  )  )  )");
		
		assertTestTokenPrint( processed, "SYMBOL .");
		assertEquals(processed.size(), 2);
	}
	
	public void test001(){
		String str = "Потом он красной ручкой расписался в дневнике.";		
		List<IToken> processed = process(str);
		assertTestTokenPrint( processed,
			"CLAUSE("
		        + "<subject>WORD_FORM он([мр, Анаф, МС, 3л])"
		        + "<predicate>VERB_ADVERB("
			        + "WORD_FORM потом([Н])"
			        + "<main>VERB_NOUN("
				        + "NOUN_ADJECTIVE("
					        + "WORD_FORM *([красный([ПРИЛ]), красный([кач, ПРИЛ])])"
					        + "<main>WORD_FORM ручка([жр, СУЩ, неод])  )"
				        + "<main>VERB_NOUN_PREP("
					        + "<main>WORD_FORM расписался([сов, ГЛ, неперех])"
					        + "WORD_FORM в([ПР])"
						    + "WORD_FORM дневник([мр, СУЩ, неод])  )  )  )  )");
		
		assertTestTokenPrint( processed, "SYMBOL .");
		assertEquals(processed.size(), 2);
	}
	
	public void test002(){//TODO assert output size
		String str = "Петя в красивой вязаной шапке идёт в большой магазин.";		
		List<IToken> processed = process(str);
		assertTestTokenPrint( processed,""
		+ "CLAUSE("
		        + "<subject>WORD_FORM петя([имя, мр, од, СУЩ])"
		        + "<predicate>VERB_NOUN_PREP("
		          + "<main>VERB_NOUN_PREP("
		            + "WORD_FORM в([ПР])"
		            + "NOUN_ADJECTIVE("
		              + "UNIFORM_ADJECTIVE("
		                + "WORD_FORM красивый([кач, ПРИЛ])"
		                + "<main>WORD_FORM вязаный([ПРИЛ])  )"
		              + "<main>WORD_FORM шапка([жр, СУЩ, неод])  )"
		            + "<main>WORD_FORM иду([ГЛ, неперех, несов])  )"
		          + "WORD_FORM в([ПР])"
		          + "NOUN_ADJECTIVE("
		            + "WORD_FORM большой([кач, ПРИЛ])"
		            + "<main>WORD_FORM магазин([мр, СУЩ, неод])  )  )  )");
	}
	
	public void test003(){//TODO assert output size
		String str = "Петя в красивой красной шапке идёт в большой магазин.";		
		List<IToken> processed = process(str);
		assertTestTokenPrint( processed,
			"CLAUSE("
		        + "<subject>WORD_FORM петя([имя, мр, од, СУЩ])"
		        + "<predicate>VERB_NOUN_PREP("
		          + "<main>VERB_NOUN_PREP("
		            + "WORD_FORM в([ПР])"
		            + "NOUN_ADJECTIVE("
		              + "UNIFORM_ADJECTIVE("
		                + "WORD_FORM красивый([кач, ПРИЛ])"
		                + "<main>WORD_FORM *([красный([ПРИЛ]), красный([кач, ПРИЛ])])  )"
		              + "<main>WORD_FORM шапка([жр, СУЩ, неод])  )"
		            + "<main>WORD_FORM иду([ГЛ, неперех, несов])  )"
		          + "WORD_FORM в([ПР])"
		          + "NOUN_ADJECTIVE("
		            + "WORD_FORM большой([кач, ПРИЛ])"
		            + "<main>WORD_FORM магазин([мр, СУЩ, неод])  )  )  )");
	}
	
	public void test004(){
		String str = "Тот дом, своя улица.";		
		List<IToken> processed = process(str);
		assertTestTokenPrint( processed,
			"CLAUSE("
		        + "<subject>UNIFORM_NOUN("
		          + "NOUN_ADJECTIVE("
		            + "WORD_FORM тот([мест-п, Анаф, субст?, ПРИЛ])"
		            + "<main>WORD_FORM дом([мр, СУЩ, неод])  )"
		          + "SYMBOL ,"
		          + "<main>NOUN_ADJECTIVE("
		            + "WORD_FORM свой([мест-п, Анаф, ПРИЛ])"
		            + "<main>WORD_FORM улица([жр, СУЩ, неод])  )  )"
		        + "<predicate>no predicate  )");
		
		assertTestTokenPrint( processed, "SYMBOL .");
		assertEquals(processed.size(), 2);
	}
	
	/**
	 *  INVALID TEST !!!
	 **/
	public void test005(){
		String str = "Но я люблю смотреть кино.";		
		List<IToken> processed = process(str);
		
		assertTestTokenPrint( processed, "WORD_FORM но([СОЮЗ])");
		assertTestTokenPrint( processed,
			"CLAUSE("
		        + "<subject>WORD_FORM я([МС, 1л])"
		        + "<predicate>COMPOSITE_VERB("
			          + "<main>WORD_FORM люблю([ГЛ, перех, несов])"
			          + "DIRECT_OBJECT_NAME("
				            + "<main>WORD_FORM смотреть([перех, несов, ИНФ])"
				            + "WORD_FORM кино([СУЩ, ср, 0, неод])  )  )  )");
		
		assertTestTokenPrint( processed, "SYMBOL .");
		assertEquals(processed.size(), 3);
	}
	/**
	 *  INVALID TEST !!!
	 **/
	public void test006(){
		String str = "Я смотрю сериал каждую неделю.";		
		List<IToken> processed = process(str);
		assertTestTokenPrint( processed,
			"CLAUSE("
		        + "<subject>WORD_FORM я([МС, 1л])"
		        + "<predicate>DIRECT_OBJECT_NAME("
		          + "<main>DIRECT_OBJECT_NAME("
		            + "<main>WORD_FORM смотрю([ГЛ, перех, несов])"
		            + "WORD_FORM сериал([мр, СУЩ, неод])  )"
		          + "NOUN_ADJECTIVE("
		            + "WORD_FORM каждый([мест-п, ПРИЛ])"
		            + "<main>WORD_FORM неделя([жр, СУЩ, неод])  )  )  )");
		
		assertTestTokenPrint( processed, "SYMBOL .");
		assertEquals(processed.size(), 2);
	}
	
	public void test007(){//TODO assert output size
		String str = "Петя любит петь.";		
		List<IToken> processed = process(str);
		assertTestTokenPrint( processed,
			"CLAUSE("
				+ "<subject>WORD_FORM петя([имя, мр, од, СУЩ])"
				+ "<predicate>COMPOSITE_VERB("
					+ "<main>WORD_FORM люблю([ГЛ, перех, несов])"
					+ "WORD_FORM петь([перех, несов, ИНФ])  )  )");
	}
	
	public void test008(){
		String str = "Мы очень быстро едем.";		
		List<IToken> processed = process(str);
		assertTestTokenPrint( processed,
			"CLAUSE("
		        + "<subject>WORD_FORM мы([МС, 1л])"
		        + "<predicate>VERB_ADVERB("
		          + "ADVERB_WITH_MODIFICATOR("
		            + "WORD_FORM *([очень([]), очень([Н])])"
		            + "<main>WORD_FORM быстро([Н])  )"
		          + "<main>WORD_FORM еду([ГЛ, неперех, несов])  )  )");
		
		assertTestTokenPrint( processed, "SYMBOL .");
		assertEquals(processed.size(), 2);
	}
	
	public void test009(){//TODO assert output size
		String str = "Мы быстро и комфортно едем на автобусе.";		
		List<IToken> processed = process(str);
		assertTestTokenPrint( processed,
			"CLAUSE("
			     + "<subject>WORD_FORM мы([МС, 1л])"
			     + "<predicate>VERB_NOUN_PREP("
			       + "<main>VERB_ADVERB("
			         + "UNIFORM_ADVERB("
			           + "WORD_FORM быстро([Н])"
			           + "WORD_FORM и([СОЮЗ])"
			           + "<main>WORD_FORM комфортно([Н])  )"
			         + "<main>WORD_FORM еду([ГЛ, неперех, несов])  )"
			       + "WORD_FORM на([ПР])"
				   + "WORD_FORM автобус([мр, СУЩ, неод])  )  )");
	}
	
	public void test010(){
		String str = "Мы очень быстро и комфортно едем на автобусе.";		
		List<IToken> processed = process(str);
		assertTestTokenPrint( processed,
			"CLAUSE("
		        + "<subject>WORD_FORM мы([МС, 1л])"
		        + "<predicate>VERB_NOUN_PREP("
		          + "<main>VERB_ADVERB("
		            + "UNIFORM_ADVERB("
		              + "ADVERB_WITH_MODIFICATOR("
		                + "WORD_FORM *([очень([]), очень([Н])])"
		                + "<main>WORD_FORM быстро([Н])  )"
		              + "WORD_FORM и([СОЮЗ])"
		              + "<main>WORD_FORM комфортно([Н])  )"
		            + "<main>WORD_FORM еду([ГЛ, неперех, несов])  )"
		          + "WORD_FORM на([ПР])"
		          + "WORD_FORM автобус([мр, СУЩ, неод])  )  )");
		
		assertTestTokenPrint( processed, "SYMBOL .");
		assertEquals(processed.size(), 2);
	}
	
	public void test011(){//TODO стоять - стоить, лес - лёса
		String str = "В лесу стоит очень красивое дерево.";		
		List<IToken> processed = process(str);
		assertTestTokenPrint( processed,
			" CLAUSE("
			     + "<subject>NOUN_ADJECTIVE("
			       + "ADJECTIVE_ADVERB("
			         + "WORD_FORM *([очень([]), очень([Н])])"
			         + "<main>WORD_FORM красивый([кач, ПРИЛ])  )"
			       + "<main>WORD_FORM дерево([СУЩ, ср, неод])  )"
			     + "<predicate>VERB_NOUN_PREP("
			       + "WORD_FORM в([ПР])"
				   + "WORD_FORM лес([мр, СУЩ, неод])"
			       + "<main>WORD_FORM стою([ГЛ, неперех, несов])  )  )");
		assertEquals(processed.size(), 5);
	}
	
	public void test012(){
		String str = "Мы встретили красивую Машу, умного Петю и весёлого Васю.";		
		List<IToken> processed = process(str);
		assertTestTokenPrint( processed,
			"CLAUSE("
				+ "<subject>WORD_FORM мы([МС, 1л])"
				+ "<predicate>DIRECT_OBJECT_NAME("
					+ "<main>WORD_FORM встретил([сов, ГЛ, перех])"
						+ "UNIFORM_NOUN("
							+ "NOUN_ADJECTIVE("
								+ "WORD_FORM красивый([кач, ПРИЛ])"
								+ "<main>WORD_FORM маша([имя, жр, од, СУЩ])  )"
							+ "SYMBOL ,"
							+ "NOUN_ADJECTIVE("
								+ "WORD_FORM умный([кач, ПРИЛ])"
								+ "<main>WORD_FORM петя([имя, мр, од, СУЩ])  )"
							+ "WORD_FORM и([СОЮЗ])"
							+ "<main>NOUN_ADJECTIVE("
								+ "WORD_FORM весёлый([кач, ПРИЛ])"
								+ "<main>WORD_FORM вася([имя, мр, од, СУЩ])  )  )  )  )");
		
		assertTestTokenPrint( processed, "SYMBOL .");
		assertEquals(processed.size(), 2);
	}
	
	public void test013(){
		String str = "Вчера чинил суровый злой сантехник водопровод.";		
		List<IToken> processed = process(str);
		assertTestTokenPrint( processed,
			"CLAUSE("
		        + "<subject>NOUN_ADJECTIVE("
		          + "UNIFORM_ADJECTIVE("
		            + "WORD_FORM суровый([кач, ПРИЛ])"
		            + "<main>WORD_FORM злой([кач, ПРИЛ])  )"
		          + "<main>WORD_FORM сантехник([мр, од, СУЩ])  )"
		        + "<predicate>DIRECT_OBJECT_NAME("
		          + "<main>VERB_ADVERB("
		            + "WORD_FORM вчера([Н])"
		            + "<main>WORD_FORM чиню([ГЛ, перех, несов])  )"
		          + "WORD_FORM водопровод([мр, СУЩ, неод])  )  )");
		
		assertTestTokenPrint( processed, "SYMBOL .");
		assertEquals(processed.size(), 2);
	}
	
	public void test014(){
		String str = "Вчера смотрели наши коллеги фильм.";		
		List<IToken> processed = process(str);
		assertTestTokenPrint( processed,
			"CLAUSE("
				+ "<subject>NOUN_ADJECTIVE("
					+ "WORD_FORM наш([мест-п, ПРИЛ])"
					+ "<main>WORD_FORM коллега([жр, од, СУЩ, ор])  )"
				+ "<predicate>DIRECT_OBJECT_NAME("
					+ "<main>VERB_ADVERB("
						+ "WORD_FORM вчера([Н])"
						+ "<main>WORD_FORM смотрю([ГЛ, перех, несов]) )"
					+ "WORD_FORM фильм([мр, СУЩ, неод])  )  )");
		
		assertTestTokenPrint( processed, "SYMBOL .");
		assertEquals(processed.size(), 2);
	}
	
	
	public void test015(){
		String str = "Я занял денег.";		
		List<IToken> processed = process(str);
		assertTestTokenPrint( processed,"CLAUSE("
			+ "<subject>WORD_FORM я([МС, 1л])"
			+ "<predicate>DIRECT_OBJECT_NAME("
				+ "<main>WORD_FORM занял([сов, ГЛ, перех])"
				+ "WORD_FORM деньга([жр, СУЩ, неод])))");
		
		assertTestTokenPrint( processed, "SYMBOL .");
		assertEquals(processed.size(), 2);
	}
	
	
	public void test016(){//TODO assert output size
		String str = "Он быстро бежал за красивой девушкой.";		
		List<IToken> processed = process(str);
		assertTestTokenPrint( processed,
			"CLAUSE("
		     + "<subject>WORD_FORM он([мр, Анаф, МС, 3л])"
		     + "<predicate>VERB_NOUN_PREP("
		       + "<main>VERB_ADVERB("
		         + "WORD_FORM быстро([Н])"
		         + "<main>WORD_FORM бегу([ГЛ, неперех, несов])  )"
		       + "WORD_FORM за([ПР])"
			   + "NOUN_ADJECTIVE("
			     + "WORD_FORM красивый([кач, ПРИЛ])"
			     + "<main>WORD_FORM девушка([жр, од, СУЩ])  )  )  )");
	}
	
	//FIXME ACHTUNG !!!!
	public void test017(){
		String str = "Длина составляет 200 километров.";		
		List<IToken> processed = process(str);
		assertTestTokenPrint( processed,
			"CLAUSE("
				+ "<subject>WORD_FORM длина([жр, СУЩ, неод])"
				+ "<predicate>DIRECT_OBJECT_NAME("
					+ "<main>WORD_FORM составляю([ГЛ, перех, несов])"
					+ "DIMENSION 200.0 километр(SIZE)  )  )");
	}
	
	//FIXME ACHTUNG !!!!
	public void test018(){
		String str = "Длина составляет 200 км.";		
		List<IToken> processed = process(str);
		assertTestTokenPrint( processed,
			"CLAUSE("
				+ "<subject>WORD_FORM длина([жр, СУЩ, неод])"
				+ " <predicate>DIRECT_OBJECT_NAME("
					+ "<main>WORD_FORM составляю([ГЛ, перех, несов])"
					+ "DIMENSION 200.0 километр(SIZE)  )  )");
	}
	
	//FIXME ACHTUNG !!!!
	public void test019(){
		String str = "Дайте 5 апельсинов.";		
		List<IToken> processed = process(str);
		assertTestTokenPrint( processed,
			"CLAUSE("
				+ "<subject>no subject"
				+ "<predicate>DIRECT_OBJECT_NAME("
					+ "<main>WORD_FORM дал([сов, ГЛ, перех])"
					+ "NOUN_ADJECTIVE("
						+ "SCALAR 5.0"
						+ "<main>WORD_FORM апельсин([мр, СУЩ, неод])  )  )  )");
		
		assertTestTokenPrint( processed, "SYMBOL .");
		assertEquals(processed.size(), 2);
	}

	//FIXME ACHTUNG !!!!
	public void test020(){
		String str = "Взвесьте 5 кг апельсинов.";		
		List<IToken> processed = process(str);
		assertTestTokenPrint( processed,
			"CLAUSE("
				+ "<subject>no subject"
				+ "<predicate>DIRECT_OBJECT_NAME("
					+ "<main>WORD_FORM взвесил([сов, ГЛ, перех])"
					+ "MEASURED_NOUN("
						+ "DIMENSION 5.0 килограмм(WEIGHT)"
						+ "<main>WORD_FORM апельсин([мр, СУЩ, неод])  )  )  )");
	}
	
	//FIXME ACHTUNG !!!!
	public void test021(){
		String str = "Взвесьте 5 кг зелёных яблок.";		
		List<IToken> processed = process(str);
		assertTestTokenPrint( processed,
			"CLAUSE("
		        + "<subject>no subject"
		        + "<predicate>DIRECT_OBJECT_NAME("
			        + "<main>WORD_FORM взвесил([сов, ГЛ, перех])"
			        + "MEASURED_NOUN("
				        + "DIMENSION 5.0 килограмм(WEIGHT)"
				        + "<main>NOUN_ADJECTIVE("
					        + "WORD_FORM зелёный([кач, ПРИЛ])"
					        + "<main>WORD_FORM яблоко([СУЩ, ср, неод])  )  )  )  )");
	}
	
	
	public void test022(){
		String str = "Читаем книгу мы быстро.";		
		List<IToken> processed = process(str);
		assertTestTokenPrint( processed,
			"CLAUSE("
			+ "<subject>WORD_FORM мы([МС, 1л])"
			+ "<predicate>VERB_ADVERB("
				+ "<main>DIRECT_OBJECT_NAME("
					+ "<main>WORD_FORM читаю([ГЛ, перех, несов])"
					+ "WORD_FORM книга([жр, СУЩ, неод])  )"
				+ "WORD_FORM быстро([Н])  )  )");
		
		assertTestTokenPrint( processed, "SYMBOL .");
		assertEquals(processed.size(), 2);
	}
	
	public void test023(){
		String str = "Читаем быстро мы книгу.";		
		List<IToken> processed = process(str);
		assertTestTokenPrint( processed,
			"CLAUSE("
	        + "<subject>WORD_FORM мы([МС, 1л])"
	        + "<predicate>DIRECT_OBJECT_NAME("
		        + "<main>VERB_ADVERB("
		        	+ "<main>WORD_FORM читаю([ГЛ, перех, несов])"
		        	+ "WORD_FORM быстро([Н])  )"
		        + "WORD_FORM книга([жр, СУЩ, неод])  )  )");
		
		assertTestTokenPrint( processed, "SYMBOL .");
		assertEquals(processed.size(), 2);
	}
	
	public void test024(){
		String str = "Быстро читаем мы книгу.";		
		List<IToken> processed = process(str);
		assertTestTokenPrint( processed,
			"CLAUSE("
				+ "<subject>WORD_FORM мы([МС, 1л])"
	        + "<predicate>DIRECT_OBJECT_NAME("
		        + "<main>VERB_ADVERB("
			        + "WORD_FORM быстро([Н])"
			        + "<main>WORD_FORM читаю([ГЛ, перех, несов])  )"
		        + "WORD_FORM книга([жр, СУЩ, неод])  )  )");
		
		assertTestTokenPrint( processed, "SYMBOL .");
		assertEquals(processed.size(), 2);
	}
	
	public void test025(){
		String str = "Быстро мы читаем книгу.";		
		List<IToken> processed = process(str);
		assertTestTokenPrint( processed,
			"CLAUSE("
	        + "<subject>WORD_FORM мы([МС, 1л])"
	        + "<predicate>VERB_ADVERB("
		        + "WORD_FORM быстро([Н])"
		        + "<main>DIRECT_OBJECT_NAME("
			        + "<main>WORD_FORM читаю([ГЛ, перех, несов])"
			        + "WORD_FORM книга([жр, СУЩ, неод])  )  )  )",
			"CLAUSE("
	        + "<subject>WORD_FORM мы([МС, 1л])"
	        + "<predicate>DIRECT_OBJECT_NAME("
			    + "<main>VERB_ADVERB("
			        + "WORD_FORM быстро([Н])"
			        + "<main>WORD_FORM читаю([ГЛ, перех, несов])  )"
			    + "WORD_FORM книга([жр, СУЩ, неод])  )  )");
		
		assertTestTokenPrint( processed, "SYMBOL .");
		assertEquals(processed.size(), 2);
	}
	
	public void test026(){
		String str = "Книгу мы читаем быстро.";		
		List<IToken> processed = process(str);
		assertTestTokenPrint( processed,
			"CLAUSE("
	        + "<subject>WORD_FORM мы([МС, 1л])"
	        + "<predicate>DIRECT_OBJECT_NAME("
		        + "WORD_FORM книга([жр, СУЩ, неод])"
		        + "<main>VERB_ADVERB("
			        + "<main>WORD_FORM читаю([ГЛ, перех, несов])"
			        + "WORD_FORM быстро([Н])  )  )  )");
		
		assertTestTokenPrint( processed, "SYMBOL .");
		assertEquals(processed.size(), 2);
	}
	
	public void test027(){//TODO assert output size
		String str = "Я занял глебу денег.";		
		List<IToken> processed = process(str);
		assertTestTokenPrint( processed,
			"CLAUSE("
		        + "<subject>WORD_FORM я([МС, 1л])"
		        + "<predicate>DIRECT_OBJECT_NAME("
			        + "<main>VERB_NOUN("
				        + "<main>WORD_FORM занял([сов, ГЛ, перех])"
				        + "WORD_FORM глеб([имя, мр, од, СУЩ])  )"
			        + "WORD_FORM деньга([жр, СУЩ, неод])  )  )");
	}

	public void test028(){//TODO assert output size
		String str = "Ученые узнали о влиянии пальцев владельцев смартфонов на мозг.";		
		List<IToken> processed = process(str);
		assertTestTokenPrint( processed,
			"CLAUSE("
				+ "<subject>WORD_FORM учёный([мр, од, СУЩ])"
		        + "<predicate>VERB_NOUN_PREP("
			        + "<main>WORD_FORM узнал([сов, ГЛ, перех])"
			        + "WORD_FORM о([ПР])"
			        + "NOUN_NAME_PREP("
				        + "<main>GENITIVE_CHAIN("
					        + "<main>WORD_FORM влияние([СУЩ, ср, неод])"
					        + "WORD_FORM палец([мр, СУЩ, неод])"
					        + "WORD_FORM владелец([мр, од, СУЩ])"
					        + "WORD_FORM смартфон([мр, СУЩ, неод])  )"
				        + "WORD_FORM на([ПР])"
				        + "WORD_FORM мозг([мр, СУЩ, неод])  )  )  )");
	}
	
	public void test030(){
		String str = "Российские следователи ответили на упреки украинских силовиков.";		
		List<IToken> processed = process(str);
		assertTestTokenPrint( processed,
			"CLAUSE("
		        + "<subject>NOUN_ADJECTIVE("
			        + "WORD_FORM российский([ПРИЛ])"
			        + "<main>WORD_FORM следователь([мр, од, СУЩ])  )"
		        + "<predicate>VERB_NOUN_PREP("
			        + "<main>WORD_FORM ответил([сов, ГЛ, неперех])"
			        + "WORD_FORM на([ПР])"
				    + "GENITIVE_CHAIN("
					    + "<main>WORD_FORM упрёк([мр, СУЩ, неод])"
					    + "NOUN_ADJECTIVE("
					       + "WORD_FORM украинский([ПРИЛ, гео])"
					       + "<main>WORD_FORM силовик([мр, од, СУЩ])  )  )  )  )");
		
		assertTestTokenPrint( processed, "SYMBOL .");
		assertEquals(processed.size(), 2);
	}
	
	public void test031(){//TODO assert output size
		String str = "В Якутии составят электронную родословную всего населения.";		
		List<IToken> processed = process(str);
		assertTestTokenPrint( processed,
			"CLAUSE("
		        + "<subject>no subject"
		        + "<predicate>DIRECT_OBJECT_NAME("
		          + "<main>VERB_NOUN_PREP("
		            + "WORD_FORM в([ПР])"
		            + "WORD_FORM якутия([sg, жр, СУЩ, гео, неод])"
		            + "<main>WORD_FORM составил([сов, ГЛ, перех])  )"
		          + "GENITIVE_CHAIN("
		            + "<main>NOUN_ADJECTIVE("
		              + "WORD_FORM электронный([ПРИЛ])"
		              + "<main>WORD_FORM родословная([жр, СУЩ, неод])  )"
		            + "NOUN_ADJECTIVE("
		              + "WORD_FORM весь([мест-п, ПРИЛ])"
		              + "<main>WORD_FORM население([СУЩ, ср, неод])  )  )  )  )");
	}
	
	public void test032(){//TODO assert output size
		String str = "Президент Гамбии опроверг попытку государственного переворота.";		
		List<IToken> processed = process(str);
		assertTestTokenPrint( processed,
			"CLAUSE("
		        + "<subject>GENITIVE_CHAIN("
			        + "<main>WORD_FORM президент([мр, од, СУЩ])"
			        + "WORD_FORM гамбия([sg, жр, СУЩ, гео, неод])  )"
		        + "<predicate>DIRECT_OBJECT_NAME("
			        + "<main>WORD_FORM опроверг([сов, ГЛ, перех])"
			        + "GENITIVE_CHAIN("
				        + "<main>WORD_FORM попытка([жр, СУЩ, неод])"
				        + "NOUN_ADJECTIVE("
					        + "WORD_FORM государственный([ПРИЛ])"
					        + "<main>WORD_FORM переворот([мр, СУЩ, неод])  )  )  )  )");
	}
	
	public void test033(){
		String str = "Инфляция в России достигла 10 процентов.";		
		List<IToken> processed = process(str);
		assertTestTokenPrint( processed,
			"CLAUSE("
		        + "<subject>WORD_FORM инфляция([жр, СУЩ, неод])"
		        + "<predicate>DIRECT_OBJECT_NAME("
		          + "<main>VERB_NOUN_PREP("
		            + "WORD_FORM в([ПР])"
		            + "WORD_FORM россия([sg, жр, СУЩ, гео, неод])"
		            + "<main>WORD_FORM достиг([сов, ГЛ, перех])  )"
		          + "NOUN_ADJECTIVE("
		            + "SCALAR 10.0"
		            + "<main>WORD_FORM процент([мр, СУЩ, неод])  )  )  )");
		
		assertTestTokenPrint( processed, "SYMBOL .");
		assertEquals(processed.size(), 2);
	}
	
	public void test034(){
		String str = "Я люблю, напевая, гулять.";		
		List<IToken> processed = process(str);
		assertTestTokenPrint( processed,
			"CLAUSE("
				+ "<subject>WORD_FORM я([МС, 1л])"
		        + "<predicate>COMPOSITE_VERB("
			        + "<main>WORD_FORM люблю([ГЛ, перех, несов])"
			        + "VERB_GERUND("
			            + "SYMBOL ,"
			            + "WORD_FORM напевая([ДЕЕПР, перех, несов])"
			            + "SYMBOL ,"
			            + "<main>WORD_FORM гулять([неперех, несов, ИНФ])  )  )  )");
		
		assertTestTokenPrint( processed, "SYMBOL .");
		assertEquals(processed.size(), 2);
	}
	
	public void test035(){
		String str = "Я иду с Машей.";		
		List<IToken> processed = process(str);
		assertTestTokenPrint( processed,
			"CLAUSE("
				+ "<subject>WORD_FORM я([МС, 1л])"
		        + "<predicate>VERB_NOUN_PREP("
			        + "<main>WORD_FORM иду([ГЛ, неперех, несов])"
			        + "WORD_FORM с([ПР])"
			        + "WORD_FORM маша([имя, жр, од, СУЩ])  )  )");
		
		assertTestTokenPrint( processed, "SYMBOL .");
		assertEquals(processed.size(), 2);
	}
	
	//FIXME ACHTUNG !!!!
	//TODO UNIFORM VERBS
	public void test036(){
		String str = "Полёт длился 254 с и завершился удачно.";		
		List<IToken> processed = process(str);
		assertTestTokenPrint( processed,
			"CLAUSE("
				+ "<subject>WORD_FORM я([МС, 1л])"
		        + "<predicate>VERB_NOUN_PREP("
			        + "<main>WORD_FORM иду([ГЛ, неперех, несов])"
			        + "WORD_FORM с([ПР])"
			        + "WORD_FORM маша([имя, жр, од, СУЩ])  )  )");
	}
	
	//FIXME ACHTUNG !!!!
	public void test037(){
		String str = "Время полёта составило 254 с.";		
		List<IToken> processed = process(str);
		assertTestTokenPrint( processed,
			"CLAUSE("
		        + "<subject>GENITIVE_CHAIN("
		          + "<main>WORD_FORM время([СУЩ, ср, неод])"
		          + "WORD_FORM полёт([мр, СУЩ, неод])  )"
		        + "<predicate>VERB_NOUN("
		          + "<main>WORD_FORM составил([сов, ГЛ, перех])"
		          + "DIMENSION 254.0 секунда(TIME)  )  )");
	}
	
	public void test038(){//TODO kill invalid choice
		String str = "В 1960 году сформировалась новая граница Москвы.";		
		List<IToken> processed = process(str);
		assertTestTokenPrint( processed,
			"CLAUSE("
				+ "<subject>GENITIVE_CHAIN("
					+ "<main>NOUN_ADJECTIVE("
						+ "WORD_FORM новый([кач, ПРИЛ])"
						+ "<main>WORD_FORM граница([жр, СУЩ, неод])  )"
					+ "WORD_FORM москва([sg, жр, СУЩ, гео, неод])  )"
		        + "<predicate>VERB_NOUN_PREP("
			        + "WORD_FORM в([ПР])"
				    + "DATE 1960.0 год([мр, СУЩ, неод])" 
			        + "<main>WORD_FORM сформировался([сов, ГЛ, неперех])  )  )");
		
		assertTestTokenPrint( processed, "SYMBOL .");
		assertEquals(processed.size(), 3);
	}
	
	
	public void test039(){//TODO assert output size
		String str = "С победой большевиков в 1920 году в войне началась новая, советская эпоха в развитии города";		
		List<IToken> processed = process(str);
		assertTestTokenPrint( processed,
			"CLAUSE("
		        + "<subject>NOUN_NAME_PREP("
			        + "<main>NOUN_ADJECTIVE("
				        + "UNIFORM_ADJECTIVE("
					        + "WORD_FORM новый([кач, ПРИЛ])"
					        + "SYMBOL ,"
					        + "<main>WORD_FORM советский([ПРИЛ])  )"
				        + "<main>WORD_FORM эпоха([жр, СУЩ, неод])  )"
			        + "WORD_FORM в([ПР])"
			        + "GENITIVE_CHAIN("
				        + "<main>WORD_FORM развитие([СУЩ, ср, неод])"
				        + "WORD_FORM город([мр, СУЩ, неод])  )  )"
		        + "<predicate>VERB_NOUN_PREP("
			        + "WORD_FORM с([ПР])"
			        + "NOUN_NAME_PREP("
				        + "<main>GENITIVE_CHAIN("
					        + "<main>WORD_FORM победа([жр, СУЩ, неод])"
					        + "WORD_FORM большевик([мр, од, СУЩ])  )"
					    + "WORD_FORM в([ПР])"
					    + "DATE 1920.0 год([мр, СУЩ, неод])   )"
					+ "<main>VERB_NOUN_PREP("
				        + "WORD_FORM в([ПР])"
				        + "WORD_FORM война([жр, СУЩ, неод])"
				        + "<main>WORD_FORM начался([сов, ГЛ, неперех])  )  )  )",
			"CLAUSE("
					+ "<subject>NOUN_NAME_PREP("
						+ "<main>NOUN_ADJECTIVE("
							+ "UNIFORM_ADJECTIVE("
								+ "WORD_FORM новый([кач, ПРИЛ])"
								+ "SYMBOL ,"
								+ "<main>WORD_FORM советский([ПРИЛ])  )"
							+ "<main>WORD_FORM эпоха([жр, СУЩ, неод])  )"
						+ "WORD_FORM в([ПР])"
						+ "GENITIVE_CHAIN("
							+ "<main>WORD_FORM развитие([СУЩ, ср, неод])"
							+ "WORD_FORM город([мр, СУЩ, неод])  )  )"
					+ "<predicate>VERB_NOUN_PREP("
						+ "WORD_FORM с([ПР])"
						+ "NOUN_NAME_PREP("
							+ "<main>NOUN_NAME_PREP("
								+ "<main>GENITIVE_CHAIN("
									+ "<main>WORD_FORM победа([жр, СУЩ, неод])"
									+ "WORD_FORM большевик([мр, од, СУЩ])  )"
								+ "WORD_FORM в([ПР])"
								+ "DATE 1920.0 год([мр, СУЩ, неод])   )"
							+ "WORD_FORM в([ПР])"
							+ "WORD_FORM война([жр, СУЩ, неод])  )"
						+ "<main>WORD_FORM начался([сов, ГЛ, неперех])  )  )");
	}
	
	public void test040(){//TODO assert output size
		String str = "Мы наблюдаем рыжего кота, играющего в углу.";
		List<IToken> processed = process(str);
		assertTestTokenPrint( processed,
			"CLAUSE("
				+ "<subject>WORD_FORM мы([МС, 1л])"
				+ "<predicate>DIRECT_OBJECT_NAME("
					+ "<main>WORD_FORM наблюдаю([ГЛ, перех, несов])"					
					+ "NOUN_PARTICIPLE("
						+ "<main>NOUN_ADJECTIVE("
							+ "WORD_FORM  *([рыжий([ПРИЛ]), рыжий([кач, ПРИЛ])])"
							+ "<main>WORD_FORM кот([мр, од, СУЩ])  )"
						+ "VERB_NOUN_PREP("
							+ "<main>WORD_FORM играющий([действ, ПРИЧ, перех, наст, несов])"
							+ "WORD_FORM в([ПР])"
							+ "WORD_FORM угол([мр, СУЩ, неод])  )  )  )  )");
	}
	
	public void test041(){//TODO assert output size
		String str = "Мы наблюдаем рыжего играющего в углу кота.";
		List<IToken> processed = process(str);
		assertTestTokenPrint( processed,
			"CLAUSE("
				+ "<subject>WORD_FORM мы([МС, 1л])"
				+ "<predicate>DIRECT_OBJECT_NAME("
					+ "<main>WORD_FORM наблюдаю([ГЛ, перех, несов])"					
					+ "NOUN_PARTICIPLE("
						+ "<main>NOUN_ADJECTIVE("
							+ "WORD_FORM рыжий([кач, ПРИЛ])"
							+ "<main>WORD_FORM кот([мр, од, СУЩ])  )"
						+ "VERB_NOUN_PREP("
							+ "<main>WORD_FORM играющий([действ, ПРИЧ, перех, наст, несов])"
							+ "WORD_FORM в([ПР])"
							+ "WORD_FORM угол([мр, СУЩ, неод])  )  )  )  )");
	}
	
	public void test042(){
		String str = "Москва - столица России.";
		List<IToken> processed = process(str);
		assertTestTokenPrint( processed,
			"CLAUSE("
		        + "<subject>WORD_FORM москва([sg, жр, СУЩ, гео, неод])"
		        + "<predicate>VERB_NOUN("
		          + "<main>WORD_FORM есть([ГЛ, неперех, несов])"
		          + "GENITIVE_CHAIN("
		            + "<main>WORD_FORM столица([жр, СУЩ, неод])"
		            + "WORD_FORM россия([sg, жр, СУЩ, гео, неод])  )  )  )");
		
		assertTestTokenPrint( processed, "SYMBOL .");
		assertEquals(processed.size(), 2);
	}
	
	
	public void test043(){
		String str = "Александр Сергеевич Пушкин  — русский поэт, драматург и прозаик.";
		List<IToken> processed = process(str);
		assertTestTokenPrint( processed,
			"CLAUSE("
				+ "<subject>LONG_NAME александр([имя, мр, од, СУЩ]) сергеевич([мр, од, СУЩ, отч]) *([пушкин([sg, мр, од, СУЩ, фам]), пушкин([гео])])"
				+ "<predicate>VERB_NOUN("
				  + "<main>WORD_FORM есть([ГЛ, неперех, несов])"
				  + "UNIFORM_NOUN("
					+ "NOUN_ADJECTIVE("
					  + "WORD_FORM русский([субст?, ПРИЛ])"
					  + "<main>WORD_FORM поэт([мр, од, СУЩ])  )"
					+ "SYMBOL ,"
					+ "WORD_FORM драматург([мр, од, СУЩ])"
					+ "WORD_FORM и([СОЮЗ])"
					+ "<main>WORD_FORM прозаик([мр, од, СУЩ])  )  )  )");
		
		assertTestTokenPrint( processed, "SYMBOL .");
		assertEquals(processed.size(), 2);
	}
	
	public void test044(){
		String str = "Александр Сергеевич Пушкин (26 мая [6 июня] 1799, Москва — 29 января [10 февраля] 1837, Санкт-Петербург) — русский поэт, драматург и прозаик.";
		List<IToken> processed = process(str);
		assertTestTokenPrint( processed,
			"CLAUSE("
	          + "<subject>LONG_NAME("
	            + "<main>LONG_NAME александр([имя, мр, од, СУЩ]) сергеевич([мр, од, СУЩ, отч]) *([пушкин([sg, мр, од, СУЩ, фам]), пушкин([гео])])"
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
	            + "<main>WORD_FORM есть([ГЛ, неперех, несов])"
	            + "UNIFORM_NOUN("
	              + "NOUN_ADJECTIVE("
	                + "WORD_FORM русский([субст?, ПРИЛ])"
	                + "<main>WORD_FORM поэт([мр, од, СУЩ])  )"
	              + "SYMBOL ,"
	              + "WORD_FORM драматург([мр, од, СУЩ])"
	              + "WORD_FORM и([СОЮЗ])"
	              + "<main>WORD_FORM прозаик([мр, од, СУЩ])  )  )  )");
		
		assertTestTokenPrint( processed, "SYMBOL .");
		assertEquals(processed.size(), 2);
	}
	
	public void test045(){
		String str = "Купил хлеба";
		List<IToken> processed = process(str);
		assertTestTokenPrint( processed,
			"CLAUSE("
		        + "<subject>no subject"
		        + "<predicate>DIRECT_OBJECT_NAME("
			        + "<main>WORD_FORM купил([сов, ГЛ, перех])"
			        + "WORD_FORM хлеб([мр, СУЩ, неод])  )  )");
		
		assertEquals(processed.size(), 1);		
	}
	
	
	public void test046(){//TODO assert output size
		String str = "Мы смотрим на кота, который лежит на ковре, который лежит на полу.";
		List<IToken> processed = process(str);
		assertTestTokenPrint( processed,
			"COMPLEX_CLAUSE("
		        + "<main>CLAUSE("
		          + "<subject>WORD_FORM мы([МС, 1л])"
		          + "<predicate>VERB_NOUN_PREP("
		            + "<main>WORD_FORM смотрю([ГЛ, перех, несов])"
		            + "WORD_FORM на([ПР])"
		            + "WORD_FORM кот([мр, од, СУЩ])  )  )"
		        + "SYMBOL ,"
		        + "WORD_FORM который([СОЮЗ])"
		        + "CLAUSE("
		          + "<subject>no subject"
		          + "<predicate>VERB_NOUN_PREP("
		            + "<main>WORD_FORM лежу([ГЛ, неперех, несов])"
		            + "WORD_FORM на([ПР])"
		            + "WORD_FORM ковёр([мр, СУЩ, неод])  )  )"
		        + "SYMBOL ,"
		        + "WORD_FORM который([СОЮЗ])"
		        + "CLAUSE("
		          + "<subject>no subject"
		          + "<predicate>VERB_NOUN_PREP("
		            + "<main>WORD_FORM лежу([ГЛ, неперех, несов])"
		            + "WORD_FORM на([ПР])"
		            + "WORD_FORM *([пол([жр, СУЩ, 0, аббр, неод]), пол([имя, мр, од, СУЩ])])  )  )  )");
	}
	
	public void test047(){//TODO need semanics here
		String str = "Власть пытается закрыть рот всем.";
		List<IToken> processed = process(str);
		assertTestTokenPrint( processed,
			"CLAUSE("
		        + "<subject>WORD_FORM власть([жр, СУЩ, неод])"
		        + "<predicate>COMPOSITE_VERB("
		          + "<main>WORD_FORM пытаюсь([ГЛ, неперех, несов])"
		          + "VERB_ADJECTIVE("
		            + "<main>DIRECT_OBJECT_NAME("
		              + "<main>WORD_FORM закрыть([сов, перех, ИНФ])"
		              + "WORD_FORM рот([мр, СУЩ, неод])  )"
		            + "WORD_FORM весь([мест-п, ПРИЛ])  )  )  )");
		
		assertTestTokenPrint( processed, "SYMBOL .");
		assertEquals(processed.size(), 3);
	}
	
	public void test048(){
		String str = "Идёт большой БКТД.";
		List<IToken> processed = process(str);
		assertTestTokenPrint( processed,
			"CLAUSE("
		        + "<subject>NOUN_ADJECTIVE("
		          + "WORD_FORM большой([кач, ПРИЛ])"
		          + "<main>WORD_FORM Unknown(БКТД)  )"
		        + "<predicate>WORD_FORM иду([ГЛ, неперех, несов])  )");
		
		assertTestTokenPrint( processed, "SYMBOL .");
		assertEquals(processed.size(), 2);
	}
	
	public void test049(){
		String str = "Мы воспользовались автоматическим определителем номера.";
		List<IToken> processed = process(str);
		assertTestTokenPrint( processed,
			"CLAUSE("
		        + "<subject>WORD_FORM мы([МС, 1л])"
		        + "<predicate>VERB_NOUN("
			          + "<main>WORD_FORM воспользовался([сов, ГЛ, неперех])"
			          + "WORD_FORM автоматический определитель номера([])  )  )");
		
		assertTestTokenPrint( processed, "SYMBOL .");
		assertEquals(processed.size(), 2);
	}
	
	public void test050(){
		String str = "Мы полетим на ковре-самолёте.";
		List<IToken> processed = process(str);
		assertTestTokenPrint( processed,
			"CLAUSE("
		        + "<subject>WORD_FORM мы([МС, 1л])"
		        + "<predicate>VERB_NOUN("
		          + "<main>VERB_NOUN_PREP("
		            + "<main>WORD_FORM полетел([сов, ГЛ, неперех])"
		            + "WORD_FORM на([ПР])"
		            + "WORD_FORM ковёр-самолёт([])  )"
		          + "WORD_FORM самолёт([мр, СУЩ, неод])  )  )");
		
		assertTestTokenPrint( processed, "SYMBOL .");
		assertEquals(processed.size(), 2);
	}
	
	
	public void test051(){
		String str = "Мы полетим на ковре самолёте.";
		List<IToken> processed = process(str);
		assertTestTokenPrint( processed,
			"CLAUSE("
		        + "<subject>WORD_FORM мы([МС, 1л])"
		        + "<predicate>VERB_NOUN_PREP("
		          + "<main>WORD_FORM полетел([сов, ГЛ, неперех])"
		          + "WORD_FORM на([ПР])"
		          + "WORD_FORM ковёр-самолёт([])  )  )");
		
		assertTestTokenPrint( processed, "SYMBOL .");
		assertEquals(processed.size(), 2);
	}
	
	
	public void test052(){
		String str = "Мы полетим на СУ-27.";
		List<IToken> processed = process(str);
		assertTestTokenPrint( processed,
			"CLAUSE("
		        + "<subject>WORD_FORM мы([МС, 1л])"
		        + "<predicate>VERB_NOUN_PREP("
		          + "<main>WORD_FORM полетел([сов, ГЛ, неперех])"
		          + "WORD_FORM на([ПР])"
		          + "WORD_WITH_INDEX("
		            + "<main>WORD_FORM су([СУЩ, ср, 0, неод])"
		            + "SYMBOL -"
		            + "SCALAR 27.0  )  )  )");
		
		assertTestTokenPrint( processed, "SYMBOL .");
		assertEquals(processed.size(), 2);
	}

	public void test053A() {
		String str = "А вот мой баннер";
		List <IToken> processed = process(str);
		assertTrue(processed != null);
	}
	
	public void test053(){
		String str = "Кампания против повременной оплаты телефона закончена  Кампания против повременной оплаты телефона закончена     Date: 13 марта 1998    МК   сообщил,   что  под  давлением  пенсионеров  планы  ввести  повременку за телефон в Москве в 1998 отложены, как минимум  на  год. Популизм восторжествовал.     Date: 06 Dec 1997   From: (ilia_fantasy@hotmail.com)   To:   Maksim Moshkow    Макс, тут разворачивается кампания против  введения повременной оплаты телефона:    http://www.relis.ru/MEDIA/death.html        Нельзя сдаваться, Макс !  Бороться с МГТС до победного конца !    Вот еще один URL по повремянке:  http://dial.ortv.ru/rgw/TALK/Editor1.htm    А вот мой баннер \"Черная ленточка\"      В  ответ  на  повременную  оплату  телефона...         Проблема  гораздо  шире!  За  наш с Вами счет на нас будет  собираться полная информация когда, кому вы звонили, как  долго  говорили.  Все  будет  храниться  в  компьютере и в любое время  будет доступно. И я думаю не только правоохран. органам.         Это  сбор  сведений  о  личной  жизни  абонентов ни кем не  санкционированное  -  это  противоречит   Конституции.   Но   в  конституционный  Суд  возможно обратиться только после того как  права будут нарушены - заранее это сделать не возможно.";
		//String str = "мой баннер \"Черная ленточка\" В  ответ на повременную";
		List<IToken> processed = process(str);
		assertTrue(processed != null);
	}
	
	public void test054(){
		String str = "Для  этого  понадобилась  бы  Тора!  Под   силу   ли   человеку воздвигнуть   такой   грандиозный   труд?  Не  подобно  ли  это воздвижению Вавилонской  башни? И тем не менее тысячелетия исполины   человеческой  мысли  снова  и  снова  комментировали Писание, раскрывая все более  глубокие  его  слои.";
		List<IToken> processed = process(str);
		assertTrue(processed != null);
	}
	
	public void test055() {
		String str = "что-то как-то";
		List<IToken> processed = process(str);
		assertTestTokenPrint( processed,
				"UNIFORM_ADVERB("
			        + "WORD_FORM что-то([Н])"
			        + "<main>WORD_FORM *([как-то([]), как-то([Н])])  )"
			        );			
	}
	
	// FIXME test doesn't finish now. When it will, check for correctness required.
	public void test056() {
		String str = "Я что-то как-то не понимаю.";
		List<IToken> processed = process(str);

		assertTrue(processed != null);
	}

	
	
	
	// FIXME this test only checks UnaryScalarCompositionParser correctness.  
	public void test057() {
		String str = "Сегодня на улице -5.";
		List<IToken> processed = process(str);
		
		assertTestScalar(-5.0, processed);		
	}
	
	public void test058() {
		String str = "Ты зайди-ка ко мне.";
		List<IToken> processed = process(str);
		
		assertTrue(processed != null);
	}
	
	
	public void test059() {
		String str = "Я не знаю.";
		List<IToken> processed = process(str);
		
		assertTrue(processed != null);
	}
	
	public void test060() {
		String str = "я знал бы, если бы не случилась моя красивая жизнь.";
		List<IToken> processed = process(str);
		
		assertTrue(processed != null);
	}
	
	public void test061() {
		String str = "без труда";
		List<IToken> processed = process(str);
		
		assertTrue(processed != null);
	}	
	
	public void test062() {
		String str = "ты хотел идти бухать";
		List<IToken> processed = process(str);
		
		assertTrue(processed != null);		
	}
	
	public void test063() {
		String str = "я бухаю, плевать я хотел на ваши желания.";
		List<IToken> processed = process(str);
		
		assertTrue(processed != null);		
	}
	
	public void test064() { 
		String str = "я стою на входе, прямо возле дубовой двери, рассматривая твои волосы цвета льна.";
		List<IToken> processed = process(str);
		
		assertTrue(processed != null);
	}

	public void test065() {
		String str = "рьяно комментировали Писание, раскрывая слои.";
		List<IToken> processed = process(str);
		
		assertTrue(processed != null);
	}

	public void test066() {
		String str = "Я сверлю, сверкая";
		List<IToken> processed = process(str);
		
		assertTrue(processed != null);
	}

	public void test067() {
		String str = "Можно было без труда следить за движениями";
		List<IToken> processed = process(str);
		
		assertTrue(processed != null);
	}
	
	public void test068() {
		String str = "Странно будет без очков смотреть на его рукоплескания.";
		List<IToken> processed = process(str);
		
		assertTrue(processed != null);
	}
	
	public void test069() {
		String str = "Акула, чувствуя, что ее вытаскивают, забилась.";
		List<IToken> processed = process(str);
		
		assertTrue(processed != null);
	}
	
	public void test070() {
		String str = "Артура изводила какая-то маета. М-да, чудное это дело -  вся  Галактика перед ним открыта, а ему недостает только  двух  мелочей  -  планеты,  где родился, и женщины, которую любил!\n"
					 + "Да провались оно пропадом, подумал он. Что ему сейчас нужно -  так  это помощь и совет. Он достал \"Путеводитель\". Заглянул  в  раздел  \"Помощь\"  и прочел: \"См. разд. \"СОВЕТЫ\". Заглянул в раздел  \"Советы\"  и  прочел:  \"См. разд. \"ПОМОЩЬ\".  Последнее  время  \"Путеводитель\"  часто  откалывал  такие номера - может, тоже с тоски-кручины?\n"
					 + "Артур направил свои стопы к Манжете Восточного Рукава  Галактики,  где, по слухам, только и можно в наши дни найти мудрость и истину. Особенно  на планете Гавалиус - обители оракулов, ясновидящих, знахарей и  пиццерий  на вынос, ибо по большей  части  оракулы,  ясновидящие  и  знахари  не  умеют готовить сами.\n"
					 + "Что бы там ни говорили про планету,  на  деле  она  оказалась  какой-то ненормально тихой. Улица в деревушке пророков,  по  которой  шагал  Артур, казалось, совершенно вымерла. Единственный пророк, которого он увидел, как раз закрывал свою лавочку, и вид у  него  был  не  очень-то  процветающий.\n"
					 + "Артур направился к нему и спросил, что здесь происходит.\n"
					 + "- Нет спроса, - буркнул  тот,  мрачно  вбивая  гвоздь  в  доску,  чтобы заколотить витрину-окно своей хижины.\n"
					 + "- В самом деле? Но почему?\n"
					 + "- Подержи-ка вон тот конец - покажу.\n"
					 + "Артур придержал еще не приколоченный конец доски. Старый пророк  нырнул в дом  и  вернулся  с  портативным  субэфирным  приемником.  Включил  его, поколдовал с настройкой и поставил на маленькую лавку,  с  которой  обычно пророчествовал, вслед за чем  взял  молоток  и  вернулся  к  заколачиванию окошка.\n"
					 + "Артур сел рядом с приемником и напряг слух.\n"
					 + "-  ...требует  подтверждения,  -  проскрипел  приемник.  -  Завтра,   - продолжал  он,  -  вице-президент  Пофф-фигуса  Рули  Га  Стин  объявит  о намерении баллотироваться на пост  президента.  В  своей  завтрашней  речи он...\n"
					 + "- Переключи диапазон, - буркнул пророк.\n";
		List<IToken> processed = process(str);
		assertTrue(processed.size() > 1);		
	}

	public void test071() {
		String str = "а причина была сурьезнейшая.";
		List<IToken> processed = process(str);
		assertTrue(processed != null && processed.size() > 1);
	}
	
	public void test072() {
		String str = "Фаррелл резко поворотил на Пейдж-стрит";
		
		List<IToken> processed = process(str);
		assertTrue(processed != null && processed.size() > 1);
	}
	
	public void test073() {
		String str = "Второй - Юрка Огарев,  студент-переросток.";
		
		List<IToken> processed = process(str);
		assertTrue(processed != null && processed.size() > 1);
	}
	
	public void test074() {
		String str = "в зале прошумел жидкий аплодисмент";

		List<IToken> processed = process(str);
		assertTrue(processed != null && processed.size() > 1);
	}
	
	public void test075() {
		String str = "Кот крутился и веселился";
		
		List<IToken> processed = process(str);
		assertTrue(processed != null && processed.size() > 1);
	}
	public void test076() {
		//            Ву-у ву-у гррбл ву-у гррббл ву-у ву-у ву-у гррббл ву-у ву-у ву-у гррббл гррббл ву-у гррббл гррббл гррббл ву-у бляппс хр-р-рр веселиться.
		String str = "Ву-у ву-у гррбл ву-у гррббл ву-у ву-у ву-у гррббл ву-у ву-у ву-у гррббл";
		
		List<IToken> processed = process(str);
		assertTrue(processed != null && processed.size() > 1);
	}
}
