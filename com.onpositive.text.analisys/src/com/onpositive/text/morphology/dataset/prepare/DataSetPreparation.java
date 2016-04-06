package com.onpositive.text.morphology.dataset.prepare;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.onpositive.semantic.wordnet.AbstractWordNet;
import com.onpositive.semantic.wordnet.GrammarRelation;
import com.onpositive.semantic.wordnet.Grammem;
import com.onpositive.semantic.wordnet.Grammem.Case;
import com.onpositive.semantic.wordnet.Grammem.PartOfSpeech;
import com.onpositive.semantic.wordnet.TextElement;
import com.onpositive.semantic.wordnet.WordNetProvider;
import com.onpositive.text.analisys.tests.ParsedTokensLoader;
import com.onpositive.text.analisys.tests.euristics.SimplifiedToken;
import com.onpositive.text.analisys.tests.neural.ui.SOMGuesser;
import com.onpositive.text.morphology.neural.SinglePartPOSSelector;

public abstract class DataSetPreparation {

	private static final String LEARNING_SET_PATH = "C:\\WORK\\science\\annot.opcorpora.xml.byfile";
	
	private String dataPath = LEARNING_SET_PATH;
	
	protected static boolean USE_HALF_DATA = true;

	protected static int count = 0;
	protected static int goodCount=0;
	
	protected HashSet<String>text=new HashSet<String>();
	protected ArrayList<DataSetTestSample>list=new ArrayList<>();
	
	protected boolean ignorePrepConj = true;
	
	protected void prepareTestingData() {
		File dir = new File(dataPath);
		File[] listedFiles = dir.listFiles();
		AbstractWordNet wordNet = WordNetProvider.getInstance();
		int size = listedFiles.length;
		if (USE_HALF_DATA) {
			size /= 2;
		}
		for (int i = 0; i < size; i++) {
			File curFile = listedFiles[i];
			prepareForFile(wordNet, curFile);
		}
	}

	public static void main(String[] args) {
		DataSetPreparation dataSetPreparation = new RegularDataSetPreparation();
		if (args.length > 0) {
			dataSetPreparation.setDataPath(args[0]);
		}
		dataSetPreparation.setIgnorePrepConj(false);
		dataSetPreparation.prepareTestingData();
		SinglePartPOSSelector prepSelector = new SinglePartPOSSelector(PartOfSpeech.PREP);
		List<DataSetTestSample> preps = prepSelector.selectSamples(dataSetPreparation.list).collect(Collectors.toList());
		SinglePartPOSSelector conjSelector = new SinglePartPOSSelector(PartOfSpeech.CONJ);
		List<DataSetTestSample> conjs = conjSelector.selectSamples(dataSetPreparation.list).collect(Collectors.toList());
		System.out.println("предл " + preps.size() + " союз " + conjs.size());
		MultiMap<String, DataSetTestSample>ss=MultiMap.withList();
		for (DataSetTestSample t:dataSetPreparation.list){
			ss.add(t.getOptions().toString()+t.correctOption,t);
			
		}
		System.out.println(goodCount+":"+count);
		System.out.println(dataSetPreparation.text.size());
		ArrayList<MultiMap<String, DataSetTestSample>.StatEntry<String>> stat = ss.toStat();
		Collections.sort(stat);
		stat.stream().filter(x->x.count()>10).forEach(x->System.out.println(x));
	}

	protected void prepareForFile(AbstractWordNet wordNet, File curFile) {
		try {
			BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(curFile));
			ParsedTokensLoader loader = new ParsedTokensLoader(inputStream);
			List<List<SimplifiedToken>> chains = loader.getChains();
			for (List<SimplifiedToken> chain : chains) {
				SimplifiedToken start=new SimplifiedToken(".", new HashSet<>());
				chain.add(0,start);
				SimplifiedToken end=new SimplifiedToken(".", new HashSet<>());
				chain.add(end);
				int i=0;
				for (SimplifiedToken t : chain) {
					PartOfSpeech partOfSpeech = t.getPartOfSpeech();
					i++;
					if (partOfSpeech != null) {
						String word = t.getWord();
						Set<PartOfSpeech> ps = possiblePartsOfSpeech(word);
						if (ps.size()==0){
							continue;
						}
						if (!ps.contains(partOfSpeech)) {
							count++;
						}
						else if (ps.size() > 1) {
							
							if (ps.contains(PartOfSpeech.VERB)&&ps.contains(PartOfSpeech.INFN)){
								ps.remove(PartOfSpeech.VERB);
								//continue;
							}
							if (!select(ps)) {
								continue;
							}
							if (ps.size()==1){
								continue;
							}
							SimplifiedToken prev=chain.get(i-2);
							SimplifiedToken next=chain.get(i);
//							if (prev.getPartOfSpeech()==null){
//								continue;
//							}
//							if (next.getPartOfSpeech()==null){
//								continue;
//							}
							if (t.getWord().equals(next.getWord())){
								System.out.println(chain);
							}
							goodCount++;
							text.add(t.getWord());
							DataSetTestSample e = new DataSetTestSample(ps, partOfSpeech, prev.getWord(), next.getWord(), t.getWord(),prev.getPartOfSpeech()==null,next.getPartOfSpeech()==null, i == 1, i == chain.size()-2);
							
							list.add(e);
						}
					}
					
				}
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	protected abstract boolean select(Set<PartOfSpeech> ps);
	
	public static <T extends Grammem> Set<T> possibleGrammems(String word, Class<T> type) {
		if (word==null){
			return new HashSet<>();
		}
		String lowerCase = word.toLowerCase();
		HashSet<T> ps = new HashSet<>();
		GrammarRelation[] possibleGrammarForms = WordNetProvider.getInstance()
				.getPossibleGrammarForms(lowerCase);
		TextElement wordElement = WordNetProvider.getInstance().getWordElement(lowerCase);
		if (wordElement != null) {
			for (Grammem g : wordElement.allGrammems()) {
				if (type.isAssignableFrom(g.getClass())) {
					ps.add((T) g);
				}
			}
		}
		if (possibleGrammarForms != null) {
			for (GrammarRelation c : possibleGrammarForms) {
				HashSet<Grammem> grammems = c.getGrammems();
				for (Grammem g : grammems) {
					if (type.isAssignableFrom(g.getClass())) {
						ps.add((T) g);
					}
				}
			}
		}
		return ps;
	}
	
	public static Set<Case> possibleCases(String word) {
		if (word==null){
			return new HashSet<>();
		}		
		return possibleGrammems(word, Case.class);
	}

	public static Set<PartOfSpeech> possiblePartsOfSpeech(String word) {
		if (word==null || word.trim().isEmpty()){
			return Collections.emptySet();
		}		
		word = word.trim();
		char firstChar = word.charAt(0);
		if (!Character.isLetter(firstChar) && !Character.isDigit(firstChar)) {
			return Collections.emptySet();
		}
		Set<PartOfSpeech> ps = new HashSet<>();
		if (isDigit(word)) {
			ps.add(PartOfSpeech.NUMR);
			return ps;
		}
		ps = possibleGrammems(word, PartOfSpeech.class);
		if (ps.isEmpty()) {
			return SOMGuesser.getInstance().guessPartsOfSpeech(word);
		}
//		if (ps.isEmpty()) { //Let's try to guess if nothing was found...
////			GrammarRelation[] forms = PredictionUtil.getPredictionHelper().getForms(word);
////			for (GrammarRelation grammarRelation : forms) {
////				HashSet<Grammem> grammems = grammarRelation.getGrammems();
////				for (Grammem grammem : grammems) {
////					if (grammem instanceof PartOfSpeech) {
////						ps.add((PartOfSpeech) grammem);
////					}
////				}
////			}
		return ps;
	}

	private static boolean isDigit(String word) {
		if (word.length() > 0 && (Character.isDigit(word.charAt(0)) || word.charAt(0) == '-')) {
			try {
				Double.parseDouble(word);
				return true;
			} catch (NumberFormatException e) {
				return false;
			}
		}
		return false;
	}

	public boolean isIgnorePrepConj() {
		return ignorePrepConj;
	}

	public void setIgnorePrepConj(boolean ignorePrepConj) {
		this.ignorePrepConj = ignorePrepConj;
	}

	public String getDataPath() {
		return dataPath;
	}

	public void setDataPath(String dataPath) {
		this.dataPath = dataPath;
	}
}