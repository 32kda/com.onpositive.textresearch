package com.onpositive.text.morphology.neural;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.encog.neural.networks.BasicNetwork;
import org.encog.persist.EncogDirectoryPersistence;

import com.onpositive.semantic.wordnet.Grammem.PartOfSpeech;
import com.onpositive.text.morphology.dataset.prepare.DataSetTestSample;

public class POSSelector extends AbstractPOSSelector {

	protected Set<PartOfSpeech> toSelect=new HashSet<>();
	
	public POSSelector(Set<PartOfSpeech> value, PartOfSpeech correct) {
		super();
		this.toSelect = value;
		this.correct = correct;
		
	}
	
	public String toString(){
		return toSelect+"->"+this.correct;
	}
	
	public Stream<DataSetTestSample> selectSamples(List<DataSetTestSample> list) {
		return list.stream().filter(x -> x.isFullSet() && x.getOptions().equals(toSelect));
	}
	
	public boolean match(Set<PartOfSpeech> set) {
		if (set.containsAll(toSelect)){
			return true;
		}
		return false;
	}
	
	@Override
	public void prepare() {
		String dirPath = System.getProperty("java.io.tmpdir");
		File tempDir = new File(dirPath,"neural");
		tempDir.mkdirs();
		if (tempDir.exists()) {
			File file = new File(tempDir, getNetworkFileName());
			if (file.exists()) {
				this.network = (BasicNetwork) EncogDirectoryPersistence.loadObject(file);	
			}
		}
		if (network == null) {
			trainNetwork();
			if (tempDir.exists()) {
				File file = new File(tempDir, getNetworkFileName());
				EncogDirectoryPersistence.saveObject(file, this.network);
			}
		}
		estimate();
	}
	
	protected String getNetworkFileName() {
		String collected = toSelect.stream().map(grammem -> (grammem.intId + "")).collect(Collectors.joining("_"));
		return "neural" + correct.intId + "a" + collected + ".dat";
	}

}
