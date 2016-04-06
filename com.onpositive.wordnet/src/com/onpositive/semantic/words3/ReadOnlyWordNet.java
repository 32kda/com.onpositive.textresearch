package com.onpositive.semantic.words3;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;
import com.carrotsearch.hppc.ByteArrayList;
import com.carrotsearch.hppc.IntIntOpenHashMapSerialable;
import com.carrotsearch.hppc.IntOpenHashSetSerializable;
import com.onpositive.semantic.wordnet.AbstractRelation;
import com.onpositive.semantic.wordnet.AbstractWordNet;
import com.onpositive.semantic.wordnet.GrammarRelation;
import com.onpositive.semantic.wordnet.MeaningElement;
import com.onpositive.semantic.wordnet.RelationTarget;
import com.onpositive.semantic.wordnet.SemanticRelation;
import com.onpositive.semantic.wordnet.TextElement;
import com.onpositive.semantic.words2.SimpleSequence;
import com.onpositive.semantic.words2.SimpleWordNet;
import com.onpositive.semantic.words2.Word;
import com.onpositive.semantic.words3.dto.ConceptInfo;
import com.onpositive.semantic.words3.dto.SenseElementInfo;
import com.onpositive.semantic.words3.dto.SequenceInfo;
import com.onpositive.semantic.words3.dto.WordInfo;
import com.onpositive.semantic.words3.hds.StringCoder;
import com.onpositive.semantic.words3.hds.StringStorage;
import com.onpositive.semantic.words3.hds.StringToDataHashMap;

public abstract class ReadOnlyWordNet extends AbstractWordNet {

	protected StringStorage<GrammarRelation[]> relations;

	protected WordStore wordsData;
	protected IntIntOpenHashMapSerialable sequences = new IntIntOpenHashMapSerialable();
	protected byte[] store;
	protected String version = "0";
	
	protected String descripton = "Russian";

	public final class WordStore extends StringToDataHashMap<SenseElementInfo> {
		public static final int CUSTOM_CASE_OFFSET = -50;

		public static final int SMALL_KIND = 8;
		public static final int NO_RELATIONS = 4;

		protected int[] wordIdToOffset;
		protected int[] conceptIdToOffet;

		int smallPath;

		int smallPathLength;

		int otherPath;

		int otherPathLength;

		protected final byte[] buffer() {
			return this.byteBuffer;
		}

		@Override
		public void write(OutputStream stream) throws IOException {
			super.write(stream);
			DataOutputStream ds = (stream instanceof DataOutputStream) ? (DataOutputStream) stream
					: new DataOutputStream(stream);
			writeIntArray(wordIdToOffset, ds);
			writeIntArray(conceptIdToOffet, ds);
		}

		@Override
		public void read(InputStream stream) throws IOException {
			super.read(stream);
			DataInputStream is = (stream instanceof DataInputStream) ? (DataInputStream) stream
					: new DataInputStream(stream);
			wordIdToOffset = readIntArray(is);
			conceptIdToOffet = readIntArray(is);
		}

		public WordStore(int sz, int conceptCount) {
			super(sz * 4 / 3 + 10);
			wordIdToOffset = new int[sz];
			conceptIdToOffet = new int[conceptCount];
			for (int a = 0; a < wordIdToOffset.length; a++) {
				wordIdToOffset[a] = Integer.MIN_VALUE;
			}
			for (int a = 0; a < conceptIdToOffet.length; a++) {
				conceptIdToOffet[a] = Integer.MIN_VALUE;
			}
		}

		public SenseElementInfo getInfo(int index) {
			int addr = wordIdToOffset[index];
			if (addr == Integer.MIN_VALUE) {
				return null;
			}
			if (addr < 0) {
				addr = -addr;
			}
			return decodeValue(this.byteBuffer, addr - 1);
		}

		public ConceptInfo getConceptInfo(int conceptIndex) {
			int addr = conceptIdToOffet[conceptIndex];
			if (addr == Integer.MIN_VALUE) {
				return null;
			}
			if (addr < 0) {
				SenseElementInfo decodeValue = decodeValue(byteBuffer,
						-addr - 1);
				return decodeValue.senses[0];
			}
			return ConceptInfo.getFromBytes(byteBuffer, addr);
		}

		@Override
		public int store(String string, SenseElementInfo data) {
			int store = super.store(string, data);
			int addr = store + string.length();

			wordIdToOffset[data.elementId] = data instanceof WordInfo ? addr
					: -addr;
			int conceptStart = addr + 4;
			if (data.senses.length == 1&&data.senses[0].senseId==data.elementId) {
				conceptIdToOffet[data.senses[0].senseId] = -addr;
			} else {
				for (ConceptInfo infs : data.senses) {
					conceptIdToOffet[infs.senseId] = conceptStart;
					SeparateConceptHandle separateConceptHandle = new SeparateConceptHandle(
							conceptStart, wordsData);
					if (separateConceptHandle.id() != infs.senseId&&data.senses.length!=1) {
						System.out.println("Error");
					}
					short features = separateConceptHandle.getGrammemCode();
					if (features != infs.gcode) {
						System.out.println("Error");
					}
					int tid = separateConceptHandle.textElementId();
					if (tid != data.elementId) {
						System.out.println("Error");
					}
					AbstractRelation<MeaningElement>[] semanticRelations = separateConceptHandle
							.getAllRelations();
					if (!Arrays.equals(semanticRelations, infs.relations)) {
						System.out.println("Error");
					}
					byte[] byteArray = infs.toByteArray();
					int conceptLength = ConceptInfo.getConceptLength(
							conceptStart - 1, wordsData.byteBuffer);
					if (conceptLength != byteArray.length) {
						System.out.println(conceptLength + ":"
								+ byteArray.length);
						System.out.println("Error");
					}
					conceptStart += byteArray.length;
				}
				SenseElementHandle wordElement = (SenseElementHandle) getWordElement(data.elementId);
				MeaningElement[] concepts = wordElement.getConcepts();
				for (MeaningElement q : concepts) {
					TextElement parentTextElement = q.getParentTextElement();
					if (parentTextElement == null) {
						q.getParentTextElement();
					}
					if (parentTextElement.id() != data.elementId) {
						System.out.println("Error");
					}
				}
			}

			return store;
		}

		final int getDataAddress2(String string) {
			return super.getDataAddress(string);
		}

		public void store(RelationTarget t, IntOpenHashSetSerializable set) {
			if (t instanceof Word) {
				String basicForm = ((Word) t).getBasicForm();
				if (basicForm.length() == 0&&!set.contains(t.id())) {
					return;
				}
				for (int a = 0; a < basicForm.length(); a++) {
					char charAt = basicForm.charAt(a);
					if (!Character.isLetter(charAt)
							&& !Character.isWhitespace(charAt) && charAt != '-' && charAt != '/') {

						MeaningElement[] concepts = ((Word) t).getConcepts();
						boolean sr = false;
						if (set.contains(t.id())) {
							sr = true;
						} else {
							for (MeaningElement q : concepts) {
								SemanticRelation[] semanticRelations = q
										.getSemanticRelations();
								if (semanticRelations != null
										&& semanticRelations.length > 0) {
									sr = true;
								}
							}
						}
						if (!sr) {
							return;
						}
					}
				}
				store(basicForm, toWordInfo((Word) t));
			}
			if (t instanceof SimpleSequence) {
				SimpleSequence basicForm = ((SimpleSequence) t);
				store(basicForm.getBasicForm(), toWordInfo(basicForm));
			}
		}

		@Override
		protected SenseElementInfo decodeValue(byte[] buffer, int addr) {
			int id = makeInt((byte) 0, buffer[2 + addr], buffer[1 + addr],
					buffer[addr]);
			byte count = buffer[addr + 3];
			ConceptInfo[] infos = null;
			int pos = addr + 4;
			if (count > 0) {
				infos = new ConceptInfo[count];
				for (int a = 0; a < count; a++) {
					ConceptInfo fromBytes = ConceptInfo.getFromBytes(buffer,
							pos);
					infos[a] = fromBytes;
					pos += fromBytes.sizeInBytes();
				}
			} else {
				int msk = -((int) count);
				msk += CUSTOM_CASE_OFFSET;
				boolean smallKind = (msk & SMALL_KIND) != 0;
				boolean noRelations = (msk & NO_RELATIONS) != 0;
				int kind = 0;
				if (smallKind) {
					kind = buffer[pos++];
				} else {
					kind = makeInt((byte) 0, (byte) 0, buffer[pos + 1],
							buffer[pos]);
					pos += 2;
				}

				AbstractRelation<MeaningElement>[] rels = null;
				if (noRelations) {
					rels = new SemanticRelation[0];
				} else
					rels = CodingUtils.decodeMeaningRelations(pos, buffer);
				ConceptInfo conceptInfo = new ConceptInfo(id, id, (short) kind,
						rels);
				infos = new ConceptInfo[] { conceptInfo };
				if (!noRelations) {
					pos += CodingUtils.length(rels);
				}
			}
			byte c = buffer[pos++];
			String wordName = getStringBeforeData(buffer, addr);
			if (c == -1) {
				return new WordInfo(id, wordName, infos);
			} else {
				int[] wids = new int[c];
				for (int p = 0; p < c; p++) {
					byte c0 = buffer[pos++];
					byte c1 = buffer[pos++];
					byte c2 = buffer[pos++];
					int wid = makeInt((byte) 0, c2, c1, c0);
					wids[p] = wid;
				}
				return new SequenceInfo(id, wordName, infos, wids);
			}
		}

		public String getStringBeforeData(byte[] buffer, int addr) {
			int stOff = 0;
			for (int a = addr - 1; a >= 0; a--) {
				if (buffer[a] < 0 && a != addr - 1) {
					stOff = a + 1;
					break;
				}
			}
			String wordName = decode(stOff + 1);
			return wordName;
		}

		@Override
		protected byte[] encodeValue(SenseElementInfo value) {
			ByteArrayList ll = new ByteArrayList();
			ll.add(int0(value.elementId));
			ll.add(int1(value.elementId));
			ll.add(int2(value.elementId));
			int length = value.senses.length;
			if (length == 1&&value.senses[0].senseId==value.elementId) {
				ConceptInfo tt = value.senses[0];
				byte vl = CUSTOM_CASE_OFFSET;
				// no features

				// no relations
				if (tt.relations.length == 0) {
					vl -= NO_RELATIONS;

				}
				// simple kind
				if (tt.gcode < 255) {
					vl -= SMALL_KIND;
				}

				ll.add(vl);
				ll.add(tt.encodeWordInfoSmall(tt.gcode, tt.relations));
				byte[] array = ll.toArray();
				smallPath++;
				smallPathLength += array.length;
				// return array;
				// features
			} else {
				ll.add((byte) length);
				for (ConceptInfo q : value.senses) {
					byte[] byteArray = q.toByteArray();
					ll.add(byteArray, 0, byteArray.length);
				}
			}
			if (value instanceof SequenceInfo) {
				SequenceInfo si = (SequenceInfo) value;
				ll.add((byte) si.words.length);
				for (int q : si.words) {
					ll.add(int0(q));
					ll.add(int1(q));
					ll.add(int2(q));
				}
				ll.add((byte) -1);
			} else {
				ll.add((byte) -1);
			}
			byte[] array = ll.toArray();
			otherPath++;
			otherPathLength += array.length;
			return array;
		}

		public SenseElementInfo toWordInfo(Word w) {
			MeaningElement[] concepts = w.getConcepts();
			ConceptInfo[] wordSenseInfo = conceptsConvert(w, concepts);
			SenseElementInfo data = new WordInfo(w.id(), w.getBasicForm(),
					wordSenseInfo);
			return data;
		}

		private ConceptInfo[] conceptsConvert(TextElement par,
				MeaningElement[] concepts) {
			ConceptInfo[] result = new ConceptInfo[concepts.length];
			for (int a = 0; a < concepts.length; a++) {
				MeaningElement el = concepts[a];
				result[a] = new ConceptInfo(par.id(), el.id(),
						el.getGrammemCode(), el.getAllRelations());
			}
			return result;
		}

		public SenseElementInfo toWordInfo(SimpleSequence w) {
			ConceptInfo[] wordSenseInfo = conceptsConvert(w, w.getConcepts());
			Word[] words = w.getWords();
			int[] wIds = new int[words.length];
			for (int a = 0; a < wIds.length; a++) {
				wIds[a] = words[a].id();
			}
			SenseElementInfo data = new SequenceInfo(w.id(), w.getBasicForm(),
					wordSenseInfo, wIds);
			return data;
		}

		public ReadOnlyWordNet getWordNet() {
			return ReadOnlyWordNet.this;
		}

		@Override
		public int size() {
			// TODO Auto-generated method stub
			return 0;
		}
	}

	public String[] getAllGrammarKeys() {
		throw new UnsupportedOperationException();
	}

	public ReadOnlyWordNet(SimpleWordNet original) {
		super(original);
		initRelations(original);
		HashMap<Integer, ArrayList<Integer>> sequenceMap = original
				.getSequenceMap();
		ByteArrayList mm = new ByteArrayList();

		for (Integer q : sequenceMap.keySet()) {
			sequences.put(q, mm.size());
			ArrayList<Integer> intArrayList = sequenceMap.get(q);
			int size = intArrayList.size();
			if (size > 120) {
				if (size > Short.MAX_VALUE) {
					throw new IllegalStateException();
				}
				mm.add((byte) -1);
				mm.add(StringCoder.int0(size));
				mm.add(StringCoder.int1(size));
			} else {
				mm.add((byte) size);
			}
			for (Integer v : intArrayList) {
				mm.add(StringCoder.int0(v));
				mm.add(StringCoder.int1(v));
				mm.add(StringCoder.int2(v));
			}

		}
		this.store = mm.toArray();
	}

	protected void initRelations(SimpleWordNet original) {
		Set<String> formsSet = original.getFormsSet();
		this.iset = original.iset();
		this.set = original.gset();
		relations = createStorage(formsSet.size() * 4 / 3 + 10);
		WordStore store = new WordStore(original.size(),
				original.conceptCount());
		this.wordsData = store;
		IntOpenHashSetSerializable sm = new IntOpenHashSetSerializable();
		for (RelationTarget t : original) {
			if (t instanceof SimpleSequence) {
				SimpleSequence sa = (SimpleSequence) t;
				Word[] words = sa.getWords();
				for (Word w : words) {
					sm.add(w.id());
				}
			}
		}
		for (RelationTarget t : original) {
			store.store(t, sm);
		}
		for (String q : original.getFormsSet()) {
			relations.store(q, original.getPosibleWords(q));
		}
	}

	public ReadOnlyWordNet(DataInputStream is) throws IOException {
		version = is.readUTF();
		descripton = is.readUTF();
		relations = createStorage(10);
		relations.read(is);
		wordsData = new WordStore(5, 5);
		wordsData.read(is);
		int readInt = is.readInt();
		this.store = new byte[readInt];
		is.readFully(this.store);
		sequences = StringToDataHashMap.readMap(is);
		readGrammems(is);
		meta.load(is);
	}

	protected ReadOnlyWordNet() {
	}

	protected abstract StringStorage<GrammarRelation[]> createStorage(int size);

	public void store(String fl) throws FileNotFoundException, IOException {

		store(new File(fl));
	}

	public void store(File fl) throws FileNotFoundException, IOException {
		DataOutputStream stream = new DataOutputStream(
				new BufferedOutputStream(new FileOutputStream(fl)));
		try {
			store(stream);
		} finally {
			stream.close();
		}
	}

	public void trim() {
		relations.trim();
		wordsData.trim();
	}

	public void store(DataOutputStream stream) throws IOException {
		stream.writeUTF(version);
		stream.writeUTF(descripton);
		relations.write(stream);
		wordsData.write(stream);
		stream.writeInt(store.length);
		stream.write(store);
		StringToDataHashMap.writeMap(stream, sequences);
		storeGrammems(stream);		
		meta.store(stream);
	}
	
	public String getVersion() {
		return version;
	}

	@Override
	public int wordCount() {
		return wordsData.wordIdToOffset.length;
	}

	@Override
	public int conceptCount() {
		return wordsData.conceptIdToOffet.length;
	}

	@Override
	public int grammarFormsCount() {
		return relations.size();
	}

	@Override
	public MeaningElement getConceptInfo(int conceptId) {
		int i = wordsData.conceptIdToOffet[conceptId];
		if (i==Integer.MIN_VALUE){
			return null;
		}
		if (i < 0) {
			SenseElementHandle senseElementHandle = new SenseElementHandle(
					-i - 1, wordsData);
			return senseElementHandle.getConcepts()[0];
		}
		return new SeparateConceptHandle(i, wordsData);
	}

	@Override
	public TextElement getWordElement(int wordId) {
		try{
		int i = wordsData.wordIdToOffset[wordId];
		if (i == Integer.MIN_VALUE) {
			return null;
		}
		if (i < 0) {
			return new WordSequenceHandle(-i - 1, this.wordsData);
		}
		return new WordHandle(i - 1, wordsData);
		} catch (Exception e){
			return null;
		}
	}

	public TextElement getWordElement(String word) {
		int dataAddress2 = wordsData.getDataAddress2(word);
		if (dataAddress2 >= 0) {
			byte[] buffer = wordsData.buffer();
			int makeInt = StringCoder.makeInt(buffer[dataAddress2 + 2],
					buffer[dataAddress2 + 1], buffer[dataAddress2]);
			return getWordElement(makeInt);
		}
		return null;
	}

	@Override
	public GrammarRelation[] getPossibleGrammarForms(String wordForm) {
		if (wordForm==null){
			return null;
		}
		wordForm = wordForm.toLowerCase().trim();
		GrammarRelation[] wordRelations = relations.get(wordForm);
		if (wordRelations != null) {
			for (AbstractRelation<?> q : wordRelations) {
				GrammarRelation g = (GrammarRelation) q;
				g.setOwner(this);
			}
		} else {
			TextElement wordElement = getWordElement(wordForm);
			if (wordElement != null) {
				return new GrammarRelation[] { new GrammarRelation(this,
						wordElement.id(), 0) };
			}
		}
		return wordRelations;
	}

	@Override
	public TextElement[] getPossibleContinuations(TextElement startOfSequence) {
		if (startOfSequence==null){
			return null;
		}
		int id = startOfSequence.id();
		if (sequences.containsKey(id)) {
			int addr = sequences.get(id);
			int count = store[addr];
			if (count < 0) {
				addr++;
				count = StringCoder.makeInt((byte) 0, store[addr + 1],
						store[addr]);
				addr++;
				addr++;
			} else {
				addr++;
			}
			TextElement[] result = new TextElement[count];
			int a = 0;
			while (a < count) {
				int makeInt = StringCoder.makeInt(store[addr + 2],
						store[addr + 1], store[addr]);
				result[a] = getWordElement(makeInt);
				a++;
				addr += 3;
			}
			return result;
		}
		return null;
	}

	@Override
	public boolean hasContinuations(TextElement te) {
		int id = te.id();
		if (sequences.containsKey(id)) {
			return true;
		}
		return false;
	}

	public void writeWords(OutputStream stream) throws IOException {
		wordsData.write(stream);
		stream.close();
	}

}