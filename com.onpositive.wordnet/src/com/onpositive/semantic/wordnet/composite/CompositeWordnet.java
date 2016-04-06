package com.onpositive.semantic.wordnet.composite;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import com.carrotsearch.hppc.IntObjectOpenHashMap;
import com.onpositive.semantic.wordnet.AbstractWordNet;
import com.onpositive.semantic.wordnet.GrammarRelation;
import com.onpositive.semantic.wordnet.Grammem;
import com.onpositive.semantic.wordnet.MeaningElement;
import com.onpositive.semantic.wordnet.TextElement;
import com.onpositive.semantic.wordnet.WordNetProvider;
import com.onpositive.semantic.wordnet.edit.WordNetPatch;
import com.onpositive.semantic.words2.SimpleWordNet;
import com.onpositive.semantic.words3.MetaLayer;

public class CompositeWordnet extends AbstractWordNet{

	protected AbstractWordNet original;
	public CompositeWordnet(AbstractWordNet original, SimpleWordNet additions) {
		super();
		this.original = original;
		this.additions = additions;
		this.loadGrammems(original);
		lock(original, additions);
	}
	public void prepare(){
		lock(original,additions);
	}

	public void lock(AbstractWordNet original, SimpleWordNet additions) {
		for (MetaLayer<?>z: original.getMetaLayers().getAll()){
				@SuppressWarnings({ "rawtypes", "unchecked" })
				CompositeLayer l=new CompositeLayer(z, additions.getMetaLayers().getLayer(z.getId()), this);
				meta.registerLayer(l);
			
		}
		for (MetaLayer<?>z: additions.getMetaLayers().getAll()){
			if(original.getMetaLayers().getLayer(z.getId())==null){
				@SuppressWarnings({ "rawtypes", "unchecked" })
				CompositeLayer l=new CompositeLayer(null,z, this);
				meta.registerLayer(l);
			}
		}
	}
	
	public CompositeWordnet(){
		this(WordNetProvider.getInstance(),new SimpleWordNet());
	}
	
	public void addUrl(String url){
		InputStream k=CompositeWordnet.class.getResourceAsStream(url);
		add(k);
	}

	public void add(InputStream k) {
		WordNetPatch wordNetPatch;
		try {
			wordNetPatch = WordNetPatch.parse(new BufferedReader(new InputStreamReader(k,"UTF-8")));
			wordNetPatch.execute(WordNetProvider.editable(additions));
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	protected SimpleWordNet additions;
	//protected IntObjectOpenHashMap<TextElement>originalToComposite=new IntObjectOpenHashMap<TextElement>();
	//protected IntObjectOpenHashMap<TextElement>additionToComposite=new IntObjectOpenHashMap<TextElement>();
	protected IntObjectOpenHashMap<TextElement>idToText=new IntObjectOpenHashMap<TextElement>();
	protected IntObjectOpenHashMap<MeaningElement>idTomeaning=new IntObjectOpenHashMap<MeaningElement>();
	
	@Override
	public GrammarRelation[] getPossibleGrammarForms(String wordForm) {
		GrammarRelation[] possibleGrammarForms = original.getPossibleGrammarForms(wordForm);
		GrammarRelation[] possibleGrammarForms2 = additions.getPossibleGrammarForms(wordForm);
		ArrayList<GrammarRelation>result=new ArrayList<GrammarRelation>();
		processRelations(possibleGrammarForms, result, original);
		processRelations(possibleGrammarForms2, result, additions);			
		return result.toArray(new GrammarRelation[result.size()]);
	}

	public void processRelations(GrammarRelation[] possibleGrammarForms,
			ArrayList<GrammarRelation> result,AbstractWordNet oener) {
		if (possibleGrammarForms!=null){
			for (GrammarRelation q:possibleGrammarForms){
				result.add(convertRelation(q,oener));
			}
		}
	}

	private GrammarRelation convertRelation(GrammarRelation q, final AbstractWordNet oener) {
		
		return new GrammarRelation(this, convertElement(q.conceptId,oener), q.relation){
			
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public HashSet<Grammem> getGrammems() {				
				return oener.getGrammemSet(relation);
			}
		};
	}

	private int convertElement(int conceptId, AbstractWordNet oener) {
		TextElement wordElement = oener.getWordElement(conceptId);
		return getWordElement(wordElement.getBasicForm()).id();
	}
	protected HashMap<String, TextElement>mm=new HashMap<String, TextElement>();

	@Override
	public TextElement getWordElement(String basicForm) {
		TextElement textElement = mm.get(basicForm);
		if (textElement!=null){
			return textElement;
		}
		TextElement t1= original.getWordElement(basicForm);
		TextElement t2=additions.getWordElement(basicForm);
		if( t1!=null||t2!=null){
		CompositeTextElement r=new CompositeTextElement(this,this.idToText.size()+1, t1,t2);
		idToText.put(idToText.size()+1, r);
		mm.put(basicForm, r);
		return r;
		}
		return null;
	}

	@Override
	public TextElement[] getPossibleContinuations(TextElement startOfSequence) {
		String basicForm = startOfSequence.getBasicForm();
		TextElement t1= original.getWordElement(basicForm);
		TextElement t2=additions.getWordElement(startOfSequence.getBasicForm());
		TextElement[] possibleContinuations = t1!=null?original.getPossibleContinuations(t1):null;
		TextElement[] additionElements=t2!=null?additions.getPossibleContinuations(t2):null;
		ArrayList<TextElement>sss=new ArrayList<TextElement>();
		if(possibleContinuations!=null){
			sss.addAll(convertElements(possibleContinuations, original));
		}
		if (additionElements!=null){
			sss.addAll(convertElements(additionElements, additions));
		}
		return sss.toArray(new TextElement[sss.size()]);
	}

	public List<TextElement> convertElements(TextElement[] possibleContinuations,AbstractWordNet ww) {
		ArrayList<TextElement>rs=new ArrayList<TextElement>();
		for (TextElement t:possibleContinuations){
			rs.add(getWordElement(t.getBasicForm()));
		}
		return rs;
	}

	@Override
	public boolean hasContinuations(TextElement te) {
		if(original.hasContinuations(te)){
			return true;
		}
		return additions.hasContinuations(te);
	}

	@Override
	public int wordCount() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int conceptCount() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int grammarFormsCount() {
		throw new UnsupportedOperationException();
	}

	@Override
	public TextElement getWordElement(int wordId) {
		return idToText.get(wordId);
	}

	@Override
	public MeaningElement getConceptInfo(int conceptId) {
		
		return idTomeaning.get(conceptId);
	}

	@Override
	public String[] getAllGrammarKeys() {
		throw new UnsupportedOperationException();
	}
	
	HashMap<MeaningElement, MeaningElement>proxy=new HashMap<MeaningElement, MeaningElement>();

	MeaningElement convertMeaning(MeaningElement q) {
		if (q==null){
			return null;
		}
		MeaningElement meaningElement = proxy.get(q);
		if (meaningElement!=null){
			return meaningElement;
		}
		CompositeMeaning m=new CompositeMeaning(this, getWordElement(q.getParentTextElement().getBasicForm()), q, this.idTomeaning.size()+1);
		idTomeaning.put(idTomeaning.size()+1, m);
		proxy.put(q, m);
		return m;
	}

	public int remapMeaning(MeaningElement word) {
		MeaningElement convertMeaning = convertMeaning(word);
		if (convertMeaning==null){
			return -1;
		}
		return convertMeaning.id();
	}
	
	public AbstractWordNet getOriginal() { return this.original; }
	public AbstractWordNet getAdditions() { return this.additions; }
}
