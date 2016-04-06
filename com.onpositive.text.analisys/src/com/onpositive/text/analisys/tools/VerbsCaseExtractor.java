package com.onpositive.text.analisys.tools;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.onpositive.semantic.wordnet.Grammem;
import com.onpositive.semantic.wordnet.Grammem.Case;
import com.onpositive.semantic.wordnet.Grammem.PartOfSpeech;
import com.onpositive.semantic.wordnet.composite.CompositeWordnet;
import com.onpositive.semantic.wordnet.MeaningElement;
import com.onpositive.semantic.wordnet.MorphologicalRelation;
import com.onpositive.semantic.wordnet.TextElement;
import com.onpositive.text.analisys.tools.data.FileSystemVisitor;
import com.onpositive.text.analisys.tools.data.FileTextLoader;
import com.onpositive.text.analisys.tools.data.HtmlRemover;
import com.onpositive.text.analisys.tools.data.LogWriter;
import com.onpositive.text.analysis.IToken;
import com.onpositive.text.analysis.TokenVisitor;
import com.onpositive.text.analysis.lexic.WordFormToken;
import com.onpositive.text.analysis.syntax.SyntaxParser;
import com.onpositive.text.analysis.syntax.SyntaxToken;
import com.onpositive.text.analysis.syntax.SyntaxToken.GrammemSet;
import com.onpositive.text.analysis.utils.ILogger;

public class VerbsCaseExtractor extends FileSystemVisitor {
	
	private static final HashSet<Integer> validNodes = new HashSet<Integer>(Arrays.asList(
			
			IToken.TOKEN_TYPE_SENTENCE,
			IToken.TOKEN_TYPE_VERB_ADVERB,			
			IToken.TOKEN_TYPE_DIRECT_OBJECT_NAME,			
			IToken.TOKEN_TYPE_DIRECT_OBJECT_INF,			
			IToken.TOKEN_TYPE_VERB_NOUN,			
			IToken.TOKEN_TYPE_VERB_ADJECTIVE,			
			IToken.TOKEN_TYPE_VERB_NOUN_PREP,			
			IToken.TOKEN_TYPE_VERB_ADJECTIVE_PREP,			
			IToken.TOKEN_TYPE_VERB_ADVERB_PREP,			
			IToken.TOKEN_TYPE_VERB_GERUND,			
			IToken.TOKEN_TYPE_COMPOSITE_VERB,			
			IToken.TOKEN_TYPE_CLAUSE,			
			IToken.TOKEN_TYPE_COMPLEX_CLAUSE,			
			IToken.TOKEN_TYPE_UNIFORM_PREDICATIVE,			
			IToken.TOKEN_TYPE_UNIFORM_VERB,			
			IToken.TOKEN_TYPE_BRACKETS,			
			IToken.TOKEN_TYPE_ENUMERATION,			
			IToken.TOKEN_TYPE_DIRECT_SPEACH
		));
	
	private static final HashSet<Integer> targetNodes = new HashSet<Integer>(Arrays.asList(
			IToken.TOKEN_TYPE_DIRECT_OBJECT_NAME,			
			IToken.TOKEN_TYPE_VERB_NOUN,			
			IToken.TOKEN_TYPE_VERB_ADJECTIVE			
		));
	private static final HashSet<Integer> infnRelations = new HashSet<Integer>(Arrays.asList(
			MorphologicalRelation.INFN_VERB,
			MorphologicalRelation.INFN_PRTF,
			MorphologicalRelation.INFN_GRND,
			MorphologicalRelation.INFN_VERB + MorphologicalRelation.BACK_LINK_OFFSET,
			MorphologicalRelation.INFN_PRTF + MorphologicalRelation.BACK_LINK_OFFSET,
			MorphologicalRelation.INFN_GRND + MorphologicalRelation.BACK_LINK_OFFSET
		));
	
	public VerbsCaseExtractor(FileTextLoader testLoader, String... extensions) {
		super();
		this.textLoader = testLoader;
		
		wordNet=new CompositeWordnet();
		wordNet.addUrl("/numerics.xml");
		wordNet.addUrl("/dimensions.xml");
		wordNet.addUrl("/modificator-adverb.xml");
		wordNet.addUrl("/prepositions.xml");
		wordNet.addUrl("/conjunctions.xml");
		wordNet.addUrl("/modalLikeVerbs.xml");
		wordNet.addUrl("/participles.xml");
		wordNet.prepare();
				
		for(String ext : extensions){
			if(!ext.startsWith(".")){
				ext = "." + ext;
			}
			this.extensions.add(ext);
		}
	}
	
	private CompositeWordnet wordNet;

	private FileTextLoader textLoader;
	
	private SyntaxParser syntaxParser;
	
	private HashMap<Grammem.Case, HashSet<String>> dump = new HashMap<Grammem.Case, HashSet<String>>();
	
	private ObjectInspectingVisitor visitor = new ObjectInspectingVisitor();
	
	private HashSet<String> extensions = new HashSet<String>();
	
	private LogWriter errLog;
	
	private LogWriter resultLog;
	
	private LogWriter progressLog;

	@Override
	protected void visitFile(File file) {
		
		String name = file.getName();
		int ind = name.lastIndexOf('.');
		if(ind==0){
			return;
		}
		String ext = name.substring(ind);
		if(!extensions.contains(ext)){
			return;
		}
		
		String content = textLoader.load(file);
		content = HtmlRemover.removeHTML(content);
		if(!isCyrillic(content)){
			return;
		}
		
		content = content.replaceAll("[\r\n]", " ");
		List<IToken> tokens = null;
		
		long t0 = System.currentTimeMillis();
		try{
			resetParser();
			tokens = syntaxParser.parse(content);
		}
		catch (Exception e){
			writeErrorLog("Parse error: " + e.getClass().getName() + " " + e.getMessage() + file.getAbsolutePath());
			return;
		}
		finally{
			long t1 = System.currentTimeMillis();
			writeProgressLog("Parse " + file.getAbsolutePath() + " " + new SimpleDateFormat("mm:ss").format(new Date(t1-t0)));
		}
		long t2 = System.currentTimeMillis();
		try{
			visitor.setText(content);
			for(IToken t : tokens){
				visitor.visit(t);
			}
		}
		catch (Exception e){
			writeErrorLog("Token Visitor error: " + e.getClass().getName() + " " + e.getMessage() + file.getAbsolutePath());
			return;
		}
		finally{
			long t3 = System.currentTimeMillis();
			writeProgressLog("Visit " + file.getAbsolutePath() + " " + new SimpleDateFormat("mm:ss").format(new Date(t3-t2)));
		}
	}
	
	private void writeProgressLog(String str) {
		progressLog.writeLn(str);		
	}

	private void writeErrorLog(String str) {
		errLog.writeLn(str);
	}
	
	public void writeResult(String str) {
		resultLog.writeLn(str);		
	}

	private boolean isCyrillic(String content) {
		
		String str = content;//.substring(0, Math.min(1000, content.length()));
		String str1 = str.replaceAll("[a-zA-Z\\s\\,\\.]", "");
		boolean result = str1.length() > str.length()/10;
		return result;
	}

	public class ObjectInspectingVisitor extends TokenVisitor{
		
		public ObjectInspectingVisitor() {
			super(false);
		}
		
		private String text;
		
		private HashSet<Grammem.Case> casesSet = new HashSet<Grammem.Case>();

		@Override
		protected boolean needVisitChildren(IToken token, IToken parent) {			
			int type = token.getType();
			boolean result = validNodes.contains(type);
			return result;
		}

		@Override
		protected int inspectNode(IToken token, IToken parent) {
			int type = token.getType();
			if(targetNodes.contains(type)){
				
				SyntaxToken st = (SyntaxToken) token;
				SyntaxToken verbToken = st.getMainWord();
				SyntaxToken objToken = selectObjectToken(st);
				
				if(areNeighbours(verbToken, objToken)){
					casesSet.clear();
					List<GrammemSet> grammemSets = objToken.getGrammemSets();
					for(GrammemSet gs : grammemSets){
						Set<Case> cases = gs.extractGrammems(Grammem.Case.class);
						casesSet.addAll(cases);
					}
					if(casesSet.size()==1){						
						Case cs = casesSet.iterator().next();
						registerVerbCase(st, cs);
					}
				}
			}
			return CONTINUE;
		}

		private void registerVerbCase(SyntaxToken verbToken, Case cs) {
			
			WordFormToken mainWord = verbToken.getMainWord();
			
			HashSet<String> set = dump.get(cs);
			if(set == null){
				set = new HashSet<String>();
				dump.put(cs, set);
			}
			String basicForm = getBasicForm(mainWord);
			if(!set.contains(basicForm)){
				int sp = verbToken.getStartPosition();
				int ep = verbToken.getEndPosition();
				String example = text.substring(sp, ep);
				writeResult(basicForm + " " + cs + "| " + example);
			}
			set.add(basicForm);
		}

		protected String getBasicForm(WordFormToken word) {
			String basicForm = null;
			if(word.hasGrammem(PartOfSpeech.INFN)){
				basicForm = word.getBasicForm();
				
			}
			else{
				MeaningElement[] meaningElements = word.getMeaningElements();
				for(MeaningElement me : meaningElements){
					MorphologicalRelation[] morphologicalRelations = me.getMorphologicalRelations();
					for(MorphologicalRelation mr : morphologicalRelations){
						if(infnRelations.contains(mr.relation)){
							MeaningElement w = mr.getWord();
							TextElement te = w.getParentTextElement();
							basicForm = te.getBasicForm();
						}
					}
				}
			}
			if(basicForm==null){
				return word.getBasicForm();
			}
			return basicForm;
		}

		private boolean areNeighbours(IToken t0, IToken t1) {
			
			int sp0 = t0.getStartPosition();
			int sp1 = t1.getStartPosition();

			IToken t0_, t1_;
			if(sp0<sp1){
				t0_ = t0;
				t1_ = t1;
			}
			else{
				t0_ = t1;
				t1_ = t0;
			}
			int ind0 = t0_.getEndPosition();
			int ind1 = t1_.getStartPosition();
			String substring = text.substring(ind0, ind1);
			boolean result = substring.trim().isEmpty();
			return result;
		}

		protected SyntaxToken selectObjectToken(SyntaxToken st) {
			SyntaxToken verbToken = st.getMainGroup();
			SyntaxToken objToken = null;
			List<IToken> children = st.getChildren();
			for(IToken ch : children){
				if(ch != verbToken){
					objToken = (SyntaxToken) ch;
					break;
				}
			}
			return objToken;
		}

		public void setText(String text) {
			this.text = text;
		}
		
	}

	public void setErrLog(LogWriter errLog) {
		this.errLog = errLog;
	}

	public void setResultLog(LogWriter resultLog) {
		this.resultLog = resultLog;
	}
	
	private void resetParser(){
		this.syntaxParser = new SyntaxParser(wordNet);
		this.syntaxParser.setErrorLogger( new ILogger() {
			
			@Override
			public ILogger writelnTokens(IToken... tokens) {
				return this;
			}
			
			@Override
			public ILogger writelnTokens(Iterable<IToken> tokens) {
				return this;
			}
			
			@Override
			public ILogger writelnToken(IToken token) {
				return this;
			}
			
			@Override
			public ILogger writelnString(String str) {
				VerbsCaseExtractor.this.writeErrorLog(str + LogWriter.LINE_SEPARATOR);
				return this;
			}
			
			@Override
			public ILogger writeTokens(Iterable<IToken> tokens) {
				return this;
			}
			
			@Override
			public ILogger writeTokens(IToken... tokens) {
				return this;
			}
			
			@Override
			public ILogger writeToken(IToken token) {
				return this;
			}
			
			@Override
			public ILogger writeString(String str) {
				VerbsCaseExtractor.this.writeErrorLog(str);
				return this;
			}
			
			@Override
			public ILogger newLine() {
				VerbsCaseExtractor.this.writeErrorLog(LogWriter.LINE_SEPARATOR);
				return this;
			}
			
			@Override
			public ILogger newLine(int count) {
				return this;
			}
			
			@Override
			public ILogger clean() {
				return this;
			}
		});
	}

	public void setProgressLog(LogWriter progressLog) {
		this.progressLog = progressLog;
	}

}
