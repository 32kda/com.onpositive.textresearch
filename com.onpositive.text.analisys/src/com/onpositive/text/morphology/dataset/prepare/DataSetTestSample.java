package com.onpositive.text.morphology.dataset.prepare;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.onpositive.semantic.wordnet.Grammem;
import com.onpositive.semantic.wordnet.Grammem.PartOfSpeech;

public class DataSetTestSample implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private transient Set<PartOfSpeech> options = new HashSet<>();

	protected transient PartOfSpeech correctOption;

	protected String prev;
	
	protected boolean isFirstSeparator;
	protected boolean isLastSerparator;

	private boolean sentenceStart;

	private boolean sentenceEnd;

	protected String next;

	protected String current;
	public boolean isFirstSeparator() {
		return isFirstSeparator;
	}

	
	public PartOfSpeech getCorrect(){
		return this.correctOption;
	}
	
	public void setFirstSeparator(boolean isFirstSeparator) {
		this.isFirstSeparator = isFirstSeparator;
	}

	public boolean isLastSerparator() {
		return isLastSerparator;
	}

	public void setLastSerparator(boolean isLastSerparator) {
		this.isLastSerparator = isLastSerparator;
	}
	
	public List<String>textList(){
		return Arrays.asList(new String[]{this.prev,this.current,this.next});
	}
	
	public boolean isFullSet(){
		return !(this.isFirstSeparator||this.isLastSerparator);
	}

	public DataSetTestSample(Set<PartOfSpeech> ps, PartOfSpeech correctOption, String prev, String next,
			String current,boolean isFS,boolean isLS, boolean sentenceStart, boolean sentenceEnd) {
		super();
		this.setOptions(ps);
		this.correctOption = correctOption;
		this.prev = prev;
		this.next = next;
		this.current = current;
		this.isFirstSeparator=isFS;
		this.isLastSerparator=isLS;
		this.sentenceStart = sentenceStart;
		this.sentenceEnd = sentenceEnd;
	}

	public String toString() {
		String result = this.getOptions() + ":" + this.correctOption + "(" + prev + " <" + current + "> " + next + ")";
		return result;
	}

	private void writeObject(ObjectOutputStream oos) throws IOException {
		// default serialization
		oos.defaultWriteObject();
		oos.writeByte(getOptions().size());
		for (PartOfSpeech s:getOptions()){
			oos.writeByte(s.intId);
		}
		oos.writeByte(correctOption.intId);
	}

	private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
		// default deserialization
		ois.defaultReadObject();
		setOptions(new HashSet<>());
		int max=ois.readByte();
		for (int a=0;a<max;a++){
			getOptions().add((PartOfSpeech) Grammem.get(ois.readByte()));
		}
		correctOption=(PartOfSpeech) Grammem.get(ois.readByte());
	}


	public Set<PartOfSpeech> getOptions() {
		return options;
	}


	public void setOptions(Set<PartOfSpeech> ps) {
		this.options = ps;
	}


	public boolean isSentenceStart() {
		return sentenceStart;
	}


	public void setSentenceStart(boolean sentenceStart) {
		this.sentenceStart = sentenceStart;
	}


	public boolean isSentenceEnd() {
		return sentenceEnd;
	}


	public void setSentenceEnd(boolean sentenceEnd) {
		this.sentenceEnd = sentenceEnd;
	}
}
