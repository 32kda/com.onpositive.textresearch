package com.onpositive.semantic.wordnet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashSet;

import com.carrotsearch.hppc.IntObjectOpenHashMapSerialzable;
import com.carrotsearch.hppc.ObjectIntOpenHashMap;
import com.onpositive.semantic.wordnet.Grammem.PartOfSpeech;
import com.onpositive.semantic.words3.LayersPack;
import com.onpositive.semantic.words3.suggestions.GuessedGrammarRelation;
import com.onpositive.semantic.words3.suggestions.GuessedTextElement;

public abstract class AbstractWordNet extends WordLookupEngine{

	public AbstractWordNet(AbstractWordNet net) {
		meta=net.meta;
	}
	public AbstractWordNet(){
		meta=new LayersPack(this);
	}

	public abstract int wordCount();

	public abstract int conceptCount();
	
	public abstract int grammarFormsCount();
	
	public abstract TextElement getWordElement(int wordId);
	
	public abstract MeaningElement getConceptInfo(int conceptId);
	
	public abstract String[] getAllGrammarKeys();
	
	protected final LayersPack meta;
	
	public final LayersPack getMetaLayers() {
		return meta;
	}

	public GrammarRelation[] getPossibleGrammarFormsWithSuggestions(String wordForm){
		GrammarRelation[] possibleGrammarForms = getPossibleGrammarForms(wordForm);
		if (possibleGrammarForms!=null&&possibleGrammarForms.length>0){
			return possibleGrammarForms;
		}		
		GrammarRelation[] prefixSuggestion = prefixSuggestion(wordForm);
		if (prefixSuggestion==null||prefixSuggestion.length==0){
			//try do suffix based guessing
		}
		return prefixSuggestion;		
	}

	private GrammarRelation[] prefixSuggestion(String wordForm) {
		GrammarRelation[] possibleGrammarForms;
		int i = wordForm.length()-4;
		for (int a=1;a<=i;a++){
			String substring = wordForm.substring(a);
			possibleGrammarForms = getPossibleGrammarForms(substring);
			if (possibleGrammarForms!=null&&possibleGrammarForms.length>0){
				String prefix=wordForm.substring(0,a);
				GuessedGrammarRelation[] gs=new GuessedGrammarRelation[possibleGrammarForms.length];
				int j=0;
				for (GrammarRelation q:possibleGrammarForms){
					TextElement word = q.getWord();
					if (word==null){
						continue;
					}
					String basicForm = word.getBasicForm();
					if (basicForm.length()<5){
						if (Grammem.PartOfSpeech.VERB.mayBeThisPartOfSpech(possibleGrammarForms)){
							return null; 
						}
					}
					GuessedTextElement ts=new GuessedTextElement(this, prefix+basicForm);
					gs[j++]=new GuessedGrammarRelation(this, q.conceptId, q.relation, ts);
				}
				return gs;
			}
		}
		return null;
	}
	
	public abstract GrammarRelation[] getPossibleGrammarForms(String wordForm);
	
	public abstract TextElement getWordElement(String basicForm);
	
	public abstract TextElement[] getPossibleContinuations(TextElement startOfSequence);

	public abstract boolean hasContinuations(TextElement te);
	
	protected IntObjectOpenHashMapSerialzable<LinkedHashSet<Grammem>>set=new IntObjectOpenHashMapSerialzable<LinkedHashSet<Grammem>>();
	protected ObjectIntOpenHashMap<LinkedHashSet<Grammem>>iset=new ObjectIntOpenHashMap<LinkedHashSet<Grammem>>();
	
	protected IntObjectOpenHashMapSerialzable<PartOfSpeech>posset = new IntObjectOpenHashMapSerialzable<PartOfSpeech>();	
	
	
	protected void loadGrammems(AbstractWordNet other){
		this.set=other.set;
		this.iset=other.iset;
	}
	
	protected void storeGrammems(DataOutputStream ds) throws IOException{
		int[] array = set.keys().toArray();
		ds.writeInt(array.length);
		for (int i:array){
			ds.writeShort(i);
			HashSet<Grammem> hashSet = set.get(i);
			ds.writeByte(hashSet.size());
			for (Grammem m:hashSet){
				ds.writeByte(m.intId);
			}
		}
	}
	
	protected void readGrammems(DataInputStream is)throws IOException{
		int count=is.readInt();
		for (int a=0;a<count;a++){
			int code=is.readShort();
			byte readByte = is.readByte();
			LinkedHashSet<Grammem>set=new LinkedHashSet<Grammem>();
			for (int i=0;i<readByte;i++){
				set.add(Grammem.get(is.readByte()));
			}
			this.set.put(code, set);
			this.iset.put(set, code);
		}
	}
	
	protected final int registerGrammemSet(LinkedHashSet<Grammem>gr){
		if (gr==null||gr.isEmpty()){
			return 0;
		}
		if (iset.containsKey(gr)){
			return iset.get(gr);
		}
		int i = iset.size()+1;		
		iset.put(gr, i);
		set.put(i, gr);
		return i;
	}
	private static final LinkedHashSet<Grammem> noGrammemSet = new LinkedHashSet<Grammem>();
	
	public final HashSet<Grammem>getGrammemSet(int code){
		if (code==0){
			return noGrammemSet;
		}
		LinkedHashSet<Grammem> linkedHashSet = set.get(code);
		if (linkedHashSet==null){
			return noGrammemSet;
		}
		return linkedHashSet;
	}
	
	
	public PartOfSpeech getPartOfSpeech(short grammemCode) {
		if (grammemCode == 0) return null;
		
		if (posset.containsKey(grammemCode)) return posset.get(grammemCode);
		
		for (Grammem g: getGrammemSet(grammemCode)) {
			if (g instanceof PartOfSpeech) {
				posset.put(grammemCode, (PartOfSpeech) g);
				return (PartOfSpeech) g;
			}
		}
		return null;
	}
}