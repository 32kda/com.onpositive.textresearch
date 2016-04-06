package com.onpositive.text.analysis.syntax;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import com.carrotsearch.hppc.IntIntMap;
import com.carrotsearch.hppc.IntObjectOpenHashMap;
import com.carrotsearch.hppc.cursors.IntCursor;
import com.onpositive.semantic.wordnet.AbstractWordNet;
import com.onpositive.text.analysis.AbstractRelationEvaluator;
import com.onpositive.text.analysis.BasicParser;
import com.onpositive.text.analysis.CompositToken;
import com.onpositive.text.analysis.DefaultRelationEvaluator;
import com.onpositive.text.analysis.EuristicAnalyzingParser;
import com.onpositive.text.analysis.IParser;
import com.onpositive.text.analysis.IToken;
import com.onpositive.text.analysis.IToken.Direction;
import com.onpositive.text.analysis.ParserComposition;
import com.onpositive.text.analysis.SentenceTreeBuilder;
import com.onpositive.text.analysis.SentenceTreeRuleFactory;
import com.onpositive.text.analysis.StructureInspectingCleaner;
import com.onpositive.text.analysis.WordRelationEvaluator;
import com.onpositive.text.analysis.lexic.IndexAttachingPasrser;
import com.onpositive.text.analysis.lexic.PrimitiveTokenizer;
import com.onpositive.text.analysis.lexic.SentenceSplitter;
import com.onpositive.text.analysis.lexic.WordFormParser;
import com.onpositive.text.analysis.lexic.dates.DateCombineParser;
import com.onpositive.text.analysis.lexic.dates.DateParser;
import com.onpositive.text.analysis.lexic.dates.LongNameParser;
import com.onpositive.text.analysis.lexic.dimension.DimensionParser;
import com.onpositive.text.analysis.lexic.dimension.UnitGroupParser;
import com.onpositive.text.analysis.lexic.dimension.UnitParser;
import com.onpositive.text.analysis.lexic.disambig.ILexicLevelDisambiguator;
import com.onpositive.text.analysis.lexic.scalar.ScalarParser;
import com.onpositive.text.analysis.lexic.scalar.UnaryScalarCompositionParser;

public class SyntaxParser extends ParserComposition {
	
	public class TreeBuilder extends SentenceTreeBuilder{

		private TokenIdProvider tip;
		
		public TreeBuilder(TokenIdProvider tip) {
			super();
			this.tip = tip;
			SentenceTreeRuleFactory factory = new SentenceTreeRuleFactory();
			this.setRules(factory.getDetectionRules());
			this.setDecisionRules(factory.getRules());
		}
		
//		@Override
//		public List<IToken> gatherTree(List<IToken> tokens) {
//			this.tip = new TokenIdProvider();
//			tip.prepare(tokens);
//			return super.gatherTree(tokens);
//		}

		public void setTokenIdProvider(TokenIdProvider tip) {
			this.tip = tip;
		}

		@Override
		protected IntObjectOpenHashMap<List<IToken>> produceResultToken(SentenceNode node, IntIntMap parentIndices, DecisionRule rule, List<IToken> regionTokens, TokenArrayBuffer buffer) {
			IntObjectOpenHashMap<List<IToken>> result = new IntObjectOpenHashMap<List<IToken>>();
			for(IntCursor i : parentIndices.keys()){
				int id = i.value;
				int ind = parentIndices.get(id);
				IToken parent = buffer.get(ind);
				int startPosition = Math.min(parent.getStartPosition(), regionTokens.get(0).getStartPosition());
				int endPosition = Math.max(parent.getEndPosition(), regionTokens.get(regionTokens.size()-1).getEndPosition());
				SyntaxToken newToken = new SyntaxToken(parent.getType(), (SyntaxToken) parent, null, startPosition, endPosition);
				if(parent.getStartPosition() < regionTokens.get(0).getStartPosition()){
					newToken.addChild(parent);
					newToken.addChildren(regionTokens);
				}
				else{
					newToken.addChildren(regionTokens);
					newToken.addChild(parent);
				}
				newToken.setId(tip.getVacantId());
				ArrayList<IToken> list = new ArrayList<IToken>();
				list.add(newToken);
				result.put(id, list);
			}
			return result;
		}

		@Override
		protected List<IToken> processContent(List<IToken> tokens,DecisionRule rule) {			
			return SyntaxParser.this.parseSyntax(tokens);
		}

		@Override
		protected List<IToken> processBound(List<IToken> tokens,DecisionRule rule, Direction dir)
		{
			int startPosition = tokens.get(0).getStartPosition();
			int endPosition = tokens.get(tokens.size()-1).getEndPosition();			
			CompositToken newToken = new CompositToken(tokens,IToken.TOKEN_TYPE_REGION_BOUND,startPosition,endPosition);
			newToken.addChildren(tokens);
			newToken.setId(tip.getVacantId());
			ArrayList<IToken> result = new ArrayList<IToken>();
			result.add(newToken);
			return result;
		}

		@Override
		protected List<IToken> produceRegionToken(List<IToken> content, List<IToken> startBound, List<IToken> endBound, DecisionRule rule) {
			int startPosition = !startBound.isEmpty()
					? startBound.get(0).getStartPosition()
					: content.get(0).getStartPosition();
					
			int endPosition = !endBound.isEmpty()
					? endBound.get(endBound.size()-1).getEndPosition()
					: content.get(content.size()-1).getEndPosition();
			
			ArrayList<IToken> tokens = new ArrayList<IToken>();
			tokens.addAll(startBound);
			tokens.addAll(content);
			tokens.addAll(endBound);
			
			CompositToken newToken = new CompositToken(tokens,rule.getResultTokenType(),startPosition,endPosition);
			newToken.setId(tip.getVacantId());
			newToken.addChildren(tokens);
			ArrayList<IToken> result = new ArrayList<IToken>();
			result.add(newToken);
			return result;
		}

		public TokenIdProvider getTokenIdProvider() {
			return tip;
		}
		
	}
	
	protected BiConsumer<Integer,Integer> onProcess;
	
	public void setOnProcess(BiConsumer<Integer, Integer> onProcess) { this.onProcess = onProcess; }
	
	private TokenIdProvider tip;
	
	private TreeBuilder treeBuilder = new TreeBuilder(tip);
	
	private static final Class<?>[] initialParsersArray = new Class<?>[]{
		WordFormParser.class
	};
	
	private static final Class<?>[] lexicParsersArray = new Class<?>[]{
		ScalarParser.class,
		UnaryScalarCompositionParser.class,
		UnitParser.class,
		UnitGroupParser.class,
		DimensionParser.class,
		//NumericsParser.class,
		DateParser.class,
		DateCombineParser.class,
		LongNameParser.class,
		IndexAttachingPasrser.class
	};
	
	private static final Class<?>[] euristicParsersArray = new Class<?>[]{
		EuristicAnalyzingParser.class,
	};
	
	private static final Class<?>[] nameSyntaxParsersArray = new Class<?>[]{
		AdverbModificatorParser.class,
		UniformAdverbParser.class,
		AdjectiveAdverbParser.class,		
		UniformAdjectivesParser.class,
		NounAdjectiveParser.class,
		NounDimensionParser.class,
		UniformNounsParser.class,
//		GenitiveChainParser.class//,
//		PrepositionGroupParser.class
	};
	
	private static final Class<?>[] nameSyntaxRecursiveParsersArray = new Class<?>[]{
		GenitiveChainParser.class
	};
	
	private static final Class<?>[] verbGroupSyntaxParsersArray = new Class<?>[]{
		VerbParticleParser.class,
		VerbAdverbParser.class,
		//DirectObjectParser.class,
		VerbNameComposition.class,		
		VerbGerundParser.class,
		VerbNameComposition.class,
		UniformVerbsParser.class
	};
	
	private static final Class<?>[] participleParserArray = new Class<?>[]{
		ParticipleAttachingParser.class
	};
	
	public SyntaxParser(AbstractWordNet wordnet) {
		super();
		this.wordNet = wordnet;
		initParsers();
	}

	private AbstractWordNet wordNet;
	
	protected ParserComposition lexicParsers;
	
	private ParserComposition nameSyntaxParsers;
	
	private ParserComposition nameRecursiveSyntaxParsers;
	
	private ParserComposition verbGroupSyntaxParsers;
	
	private ParserComposition participleParsers;
	
	private IParser dashClauseParser;
	
	private ClauseParser clauseParser;
	
	private IncompleteClauseParser incompleteClauseParser;
	
	private ComplexClauseParser complexClauseParser;

	protected PrimitiveTokenizer primitiveTokenizer = new PrimitiveTokenizer();
	
	protected SentenceSplitter sentenceSplitter = new SentenceSplitter();
	
	private ArrayList<IParser> syntaxParsers;

	private ParserComposition euristicParsers;
	
	private boolean useEuristics = true;

	private ParserComposition initialParsers;

	private boolean useEstimator = true;
	
	
	public List<IToken> parse(String str){
		setText(str);
		List<IToken> primitiveTokens = primitiveTokenizer.tokenize(str);
		if (this.onProcess != null) onProcess.accept(1, 3);
		resetTokenIdProvider(primitiveTokens);
		List<IToken> initialProcessed = initialParsers.process(primitiveTokens);
		List<IToken> currentResult = useEuristics?euristicParsers.process(initialProcessed):initialProcessed; 
		List<IToken> lexicProcessed = lexicParsers.process(currentResult);
		if (this.onProcess != null) onProcess.accept(2, 3);
		List<IToken> sentences = sentenceSplitter.split(lexicProcessed);
		if (this.onProcess != null) onProcess.accept(3, 3);

		AbstractRelationEvaluator evaluator;
		if (useEstimator) {
			evaluator = AbstractRelationEvaluator.getInstance(WordRelationEvaluator.class);
		} else {
			evaluator = AbstractRelationEvaluator.getInstance(DefaultRelationEvaluator.class);
		}
		sentences.forEach(x->evaluator.process(x, false));
		
		int processed = 0;
		int sentlen = sentences.size();
		for(IToken sentence : sentences){
			try {
				List<IToken> initialTokens = new ArrayList<IToken>(sentence.getChildren());			
				List<IToken> tokens = treeBuilder.gatherTree(initialTokens);
				List<IToken> tokens1 = parseSyntax(tokens);
				List<IToken> tokens2 = complexClauseParser.process(tokens1);
				sentence.setChildren(new StructureInspectingCleaner().clean(tokens2));
				processed += 1;
				if (this.onProcess != null) onProcess.accept(processed, sentlen);
			}
			catch(Exception e){
				
				this.errorLogger.writeString(e.getMessage()).writelnString(
						getText().substring(sentence.getStartPosition(), sentence.getEndPosition())
					);
			}
		}
		sentences.forEach(x->evaluator.process(x, true));
		return sentences;
	}


	private void resetTokenIdProvider(List<IToken> tokens) {
		this.tip = new TokenIdProvider();
		tip.prepare(tokens);
		this.tip.block();
		this.treeBuilder.setTokenIdProvider(tip);
		setTokenIdProvider(tip);
	}


	protected List<IToken> parseSyntax(List<IToken> initialTokens) {
		for(IParser p : syntaxParsers){
			p.setBaseTokens(initialTokens);
		}
		List<IToken> namesProcessed = nameSyntaxParsers.process(initialTokens);
		List<IToken> recNamesProcessed1 = nameRecursiveSyntaxParsers.process(namesProcessed);
		List<IToken> verbsProcessed1 = verbGroupSyntaxParsers.process(recNamesProcessed1);
		List<IToken> participlesProcessed = participleParsers.process(verbsProcessed1);
		List<IToken> dashClausesFormed = dashClauseParser.process(participlesProcessed);
		List<IToken> clausesFormed = clauseParser.process(dashClausesFormed);
		List<IToken> recNamesProcessed2 = nameRecursiveSyntaxParsers.process(clausesFormed);
		List<IToken> verbsProcessed2 = verbGroupSyntaxParsers.process(recNamesProcessed2);
		List<IToken> incompleteClausesFormed = incompleteClauseParser.process(verbsProcessed2);
		List<IToken> tokens = incompleteClausesFormed;
		return tokens;
	}
	
	WordFormParser wd;
	
	public void setDisambiguator(ILexicLevelDisambiguator disambiguator) {
		wd.setDisambiguator(disambiguator);
	}


	public void setIgnoreCombinations(boolean ignoreCombinations) {
		wd.setIgnoreCombinations(ignoreCombinations);
	}


	private void initParsers() {
		this.initialParsers = createParsers(initialParsersArray,false);
		wd=(WordFormParser)this.initialParsers.getParsers()[0];
		this.euristicParsers = createParsers(euristicParsersArray,false);
		this.lexicParsers = createParsers(lexicParsersArray,false);
		this.verbGroupSyntaxParsers = createParsers(verbGroupSyntaxParsersArray,true);
		this.nameSyntaxParsers = createParsers(nameSyntaxParsersArray, false);
		this.nameRecursiveSyntaxParsers = createParsers(nameSyntaxRecursiveParsersArray, false);
		this.participleParsers = createParsers(participleParserArray, false);
		this.clauseParser = new ClauseParser(this.wordNet);
		this.complexClauseParser = new ComplexClauseParser(wordNet);
		this.dashClauseParser = new DashClauseParser(this.wordNet);
		this.incompleteClauseParser = new IncompleteClauseParser(this.wordNet);
		this.syntaxParsers = new ArrayList<IParser>();
		
		this.syntaxParsers.add(nameSyntaxParsers);
		this.syntaxParsers.add(nameRecursiveSyntaxParsers);
		this.syntaxParsers.add(verbGroupSyntaxParsers);
		this.syntaxParsers.add(clauseParser);
		this.syntaxParsers.add(incompleteClauseParser);
		this.syntaxParsers.add(participleParsers);
		this.syntaxParsers.add(complexClauseParser);
		
		this.parsers = new IParser[]{
				lexicParsers,
				verbGroupSyntaxParsers,
				nameSyntaxParsers,
				nameRecursiveSyntaxParsers,
				participleParsers,
				dashClauseParser,
				clauseParser,
				incompleteClauseParser,
				complexClauseParser };
	}

	private ParserComposition createParsers(Class<?>[] array,boolean isGloballyRecursive) {
		
		ArrayList<IParser> list = new ArrayList<IParser>();
		for(Class<?> clazz : array){
			IParser parser = createParser(clazz);
			if(parser != null){
				list.add(parser);
			}
		}
		IParser[] arr = list.toArray(new IParser[list.size()]);
		ParserComposition result = new ParserComposition(isGloballyRecursive, arr);
		return result;
	}

	private IParser createParser(Class<?> clazz) {
		
		boolean isParser = extendsClass(clazz,BasicParser.class) || hasInterface(clazz, IParser.class);
		if(!isParser){
			return null;
		}
		Constructor<?> constr = null;
		Constructor<?>[] constructors = clazz.getConstructors();
		for(Constructor<?> c : constructors){
			Class<?>[] params = c.getParameterTypes();
			if(params.length==0){
				constr = c;
			}
			else if(params.length==1){
				Class<?> paramClass = params[0];
				if(extendsClass(paramClass, AbstractWordNet.class)){
					constr = c;
					break;
				}
			}
		}
		if(constr==null){
			return null;
		}
		IParser instance = null;
		try{
			if(constr.getParameterTypes().length==0){
				instance = (IParser) constr.newInstance();
			}
			else{
				instance = (IParser) constr.newInstance(this.wordNet);
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return instance;
	}


	private boolean extendsClass(Class<?> clazz, Class<?> parent) {
		boolean isParser = false;
		for(Class<?> cl = clazz ; cl != null ; cl = cl.getSuperclass()){
			isParser = (cl==parent);
			if(isParser){
				break;
			}
		}
		return isParser;
	}

	private boolean hasInterface(Class<?> clazz, Class<IParser> iClass) {
		boolean isParser = false;
		for(Class<?> cl = clazz ; cl != null ; cl = cl.getSuperclass()){
			
			if(cl==iClass){
				return true;
			}
			
			Class<?>[] interfaces = cl.getInterfaces();
			if(interfaces==null){
				continue;
			}
			for(Class<?> i : interfaces){
				if(hasInterface(i, iClass)){
					return true;
				}
			}
		}
		return isParser;
	}


	public boolean isUseEuristics() {
		return useEuristics;
	}


	public void setUseEuristics(boolean useEuristics) {
		this.useEuristics = useEuristics;
	}


	public void setUseEstimator(boolean useEstimator) {
		this.useEstimator = useEstimator;
	}


}
