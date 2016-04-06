package com.onpositive.semantic.words3.hds;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.carrotsearch.hppc.ByteArrayList;
import com.carrotsearch.hppc.CharArrayList;
import com.carrotsearch.hppc.CharByteOpenHashMap;

public abstract class StringTrie<T> extends StringStorage<T> {
	
	protected int count = 0;
	private TrieBuilder builder;
	private boolean useESwitch = true;

	public StringTrie() {
	
	}

	public TrieBuilder newBuilder() {
		return new TrieBuilder();
	}

	public void commit(TrieBuilder builder) {
		this.byteBuffer = builder.store();
		this.byteToCharTable = builder.byteToCharTable;
		this.charToByteMap = builder.charToByteMap;
		this.count = builder.count;
	}

	public T get(String element) {
		try{
		return find(element, 0, 0);
		} catch (Exception e){
			return null;
		}
	}

	private T findRecursive(String search, int position, int cAddress) {
		int length = search.length();
		int childCount = byteBuffer[cAddress];
		
		if (childCount == 0 && position < length) { //if we have no children, but still have chars to search - return null
			return null;
		}
		
		int i = cAddress + 1;
		int dataPos = i;
		if (childCount < 0) { //Skip associated data
			i = i + getDataSize(dataPos);
		}
		
		if (position==length){
			//we need to decode value;
			if (childCount<=0){
				if (childCount < 0) {
					i = i - getDataSize(dataPos);
				}
				return decodeValue(i);
			}
			return null;
		}
		char nextChar = search.charAt(position);
		if (childCount<0){
			childCount=-childCount;
		}
		int currentItem=0;
		l1:while (true) {
			byte b = byteBuffer[i];
			boolean onChar=false;
			if (b<0){
				b=(byte) -b;
				onChar=true;
			}
			char currentChar = byteToCharTable[b];
			if (charEquals(nextChar, currentChar)) {
				if (onChar){
					//read next address;
					i++;
					i = makeShift(i);
					return findRecursive(search,position+1,i);
				}
				else{
					i++;
					int charIndex=position+1;
					
					while (true){
						b = byteBuffer[i++];
						if (b<0){
							b=(byte) -b;
							onChar=true;
						}
						currentChar = byteToCharTable[b];
						if (charIndex>=length|| !charEquals(currentChar,search.charAt(charIndex))){
							//no we can skip to end of string and go next;
							while (i < byteBuffer.length && byteBuffer[i]>=0){
								i++;//skip string
							}
							if (!onChar){
								i++;
							}
							if (i >= byteBuffer.length) {
								return null;
							}
							if (currentItem==childCount-1){
								return null;
							}
							currentItem++;
							//decode address
							byte vl=byteBuffer[i];
							if (vl==Byte.MIN_VALUE){
								i = makeMultibyteShift(i);
								continue l1;
							}
							else{
								int next=vl;
								i=i+next;
								continue l1;
							}
						}
						else{
							charIndex++;
							if (charIndex==length){
								//we are at the end of string
								if (onChar){
									//end of constant segment
									if (byteBuffer[i] == Byte.MIN_VALUE) {
										return decodeValue(i+5);
									} else {  
										return decodeValue(i+2);
									}
								}
								return null;
							}
							if (onChar){
								i = makeShift(i);
								return findRecursive(search,charIndex,i);			
							}
						}
					}
				}
			}
			else{
				if (currentItem==childCount-1 || childCount == 0){
					return null;
				}
				currentItem++;
				if (onChar){
					//read next address;
					i++;
					byte vl=byteBuffer[i];
					if (vl==Byte.MIN_VALUE){
						i = makeMultibyteShift(i);
						continue l1;
					}
					else{
						int next=vl;
						i=i+next;
						continue l1;
					}
				}
				else{
					i++;
					while (i < byteBuffer.length && byteBuffer[i++]>=0); //skip the rest of string
					if (i >= byteBuffer.length) {
						return null;
					}
					//decode address
					byte vl=byteBuffer[i];
					if (vl==Byte.MIN_VALUE){
						i = makeMultibyteShift(i);
						continue l1;
					}
					else{
						int next=vl;
						i=i+next;
						continue l1;
					}
					
				}
				//we need to read next sibling position;				
			}
		}
		
	}

	private T find(String search, int position, int cAddress) {
		int i = cAddress;
		outerLoop:while (position <= search.length()) {
			int childCount = byteBuffer[i];
			if (childCount == 0 && position < search.length()) { //if we have no children, but still have chars to search - return null
				return null;
			}
			i++;
			int dataPos = i;
			if (childCount < 0) { //Skip associated data
				i = i + getDataSize(dataPos);
			}
			
			int length = search.length();
			if (position==length){
				//we need to decode value;
				if (childCount<=0){
					if (childCount < 0) {
						i = i - getDataSize(dataPos);
					}
					return decodeValue(i);
				}
				return null;
			}
			char nextChar = search.charAt(position);
			if (childCount<0){
				childCount=-childCount;
			}
			int currentItem=0;
			l1:while (true) {
				byte b = byteBuffer[i];
				boolean onChar=false;
				if (b<0){
					b=(byte) -b;
					onChar=true;
				}
				char currentChar = byteToCharTable[b];
				if (charEquals(nextChar, currentChar)) {
					if (onChar){
						//read next address;
						i++;
						i = makeShift(i);
						position++;
						continue outerLoop;
//						return find(search,position+1,i);
					}
					else{
						i++;
						int charIndex=position+1;
						
						while (true){
							b = byteBuffer[i++];
							if (b<0){
								b=(byte) -b;
								onChar=true;
							}
							currentChar = byteToCharTable[b];
							if (charIndex>=length||  !charEquals(currentChar,search.charAt(charIndex))){
								//now we can skip to end of string and go next;
								while (i < byteBuffer.length && byteBuffer[i]>=0) {
									i++;//skip string
								}
								if (!onChar) {
									i++;
								}
								if (i >= byteBuffer.length) {
									return null;
								}

								if (currentItem==childCount-1){
									return null;
								}
								currentItem++;
								//decode address
								byte vl=byteBuffer[i];
								if (vl==Byte.MIN_VALUE){
									i = makeMultibyteShift(i);
									continue l1;
								}
								else{
									int next=vl;
									i=i+next;
									continue l1;
								}
							}
							else{
								charIndex++;
								if (charIndex==length){
									//we are at the end of string
									if (onChar){
										//end of constant segment
										if (byteBuffer[i] == Byte.MIN_VALUE) {
											return decodeValue(i+5);
										} else {  
											return decodeValue(i+2);
										}
									}
									return null;
								}
								if (onChar){
									i = makeShift(i);
									position = charIndex;
									continue outerLoop;
//									return find(search,charIndex,i);			
								}
							}
						}
					}
				}
				else{
					if (currentItem==childCount-1 || childCount == 0){
						return null;
					}
					currentItem++;
					if (onChar){
						//read next address;
						i++;
						byte vl=byteBuffer[i];
						if (vl==Byte.MIN_VALUE){
							i = makeMultibyteShift(i);
							continue l1;
						}
						else{
							int next=vl;
							i=i+next;
							continue l1;
						}
					}
					else{
						i++;
						while (i<byteBuffer.length && byteBuffer[i++]>=0); //skip the rest of string
						if (i >= byteBuffer.length) {
							return null;
						}
						//decode address
						byte vl=byteBuffer[i];
						if (vl==Byte.MIN_VALUE){
							i = makeMultibyteShift(i);
							continue l1;
						}
						else{
							int next=vl;
							i=i+next;
							continue l1;
						}
						
					}
					//we need to read next sibling position;				
				}
			}
		}
		return null;
	}

	
	public List<String> getStrings(String search) {
		CollectingVisitor<T> visitor = new CollectingVisitor<T>();
		visitSubtree(search, 0, 0, visitor);
		return visitor.getWords();
	}
	
	private void visitSubtree(String prefix, int position, int fromAddress, IStringTrieVisitor<T> visitor) {
		StringBuilder builder = new StringBuilder();
		int i = fromAddress;
		int length = prefix.length();
		outerLoop:while (position <= prefix.length()) {
			int childCount = byteBuffer[i];
			if (childCount == 0 && position < prefix.length()) { //if we have no children, but still have chars to search - return null
				T value = decodeValue(i + 1);
				visitor.visit(builder.toString(), value);
				return;
			}
			i++;
			int dataPos = i;
			if (childCount < 0) { //Skip associated data
				i = i + getDataSize(dataPos);
			}
			if (position==length){
				//we need to decode value;
				if (childCount<=0) {
					if (childCount < 0) {
						i = i - getDataSize(dataPos);
					}
					T value = decodeValue(i);
					visitor.visit(prefix, value);
				}
				visitItemChildren(i,childCount,builder.toString(), visitor);
				return;
			}
			char nextChar = prefix.charAt(position);
			if (childCount<0){
				childCount=-childCount;
			}
			int prevBuilderPos = builder.length();
			int currentItem=0;
			l1:while (true) {
				byte b = byteBuffer[i];
				boolean onChar=false;
				if (b<0){
					b=(byte) -b;
					onChar=true;
				}
				char currentChar = byteToCharTable[b];
				if (charEquals(nextChar, currentChar)) {
					builder.append(currentChar);
					if (onChar){
						//read next address;
						i++;
						i = makeShift(i);
						position++;
						continue outerLoop;
					}
					else{
						i++;
						int charIndex=position+1;
						
						while (true){
							b = byteBuffer[i++];
							if (b<0){
								b=(byte) -b;
								onChar=true;
							}
							currentChar = byteToCharTable[b];
							if (charIndex>=length || !charEquals(currentChar,prefix.charAt(charIndex))){
								//now we can skip to end of string and go next;
								int prevI = i;
								if (!onChar) {
									while (i < byteBuffer.length && byteBuffer[i]>=0){
										i++;
									}
								}
								if (i >= byteBuffer.length) {
									return;
								}
								if (i > prevI) { //if at least one character was matched
									builder.append(currentChar);
									visitRest(visitor, builder,prevI);
									return;
								}

								if (currentItem==childCount-1){ //XXX should we try this actually?
									builder.append(currentChar);
									visitRest(visitor, builder,prevI);
									return;
								}
								builder.delete(prevBuilderPos, builder.length());
								currentItem++;
								//decode address
								byte vl=byteBuffer[i];
								if (vl==Byte.MIN_VALUE){
									i = makeMultibyteShift(i);
									continue l1;
								}
								else{
									int next=vl;
									i=i+next;
									continue l1;
								}
							}
							else{
								builder.append(currentChar);
								charIndex++;
								if (charIndex==length){
									//we are at the end of string
									if (onChar){
										//end of constant segment
										byte shiftValue = byteBuffer[i];
										T value = (shiftValue == Byte.MIN_VALUE)?decodeValue(i+5):decodeValue(i+2);
										visitor.visit(builder.toString(), value);										
									} else {
										visitRest(visitor, builder, i);
									}
									return;
								}
								if (onChar){
									i = makeShift(i);
									position = charIndex;
									continue outerLoop;
								}
							}
						}
					}
				} else {
					if (currentItem==childCount-1 || childCount == 0){
						if (position > 0) {
							visitRest(visitor, builder, i);
						}
						return;
					}
					currentItem++;
					if (onChar){
						//read next address;
						i++;
						byte vl=byteBuffer[i];
						if (vl==Byte.MIN_VALUE){
							i = makeMultibyteShift(i);
							continue l1;
						}
						else{
							int next=vl;
							i=i+next;
							continue l1;
						}
					}
					else{
						i++;
						while (i<byteBuffer.length && byteBuffer[i++]>=0); //skip the rest of string
						if (i >= byteBuffer.length) {
							return;
						}
						//decode address
						byte vl=byteBuffer[i];
						if (vl==Byte.MIN_VALUE){
							i = makeMultibyteShift(i);
							continue l1;
						}
						else{
							int next=vl;
							i=i+next;
							continue l1;
						}
						
					}
					//we need to read next sibling position;				
				}
			}
		}
		return;
	}

	protected void visitRest(IStringTrieVisitor<T> visitor, StringBuilder builder,
			int i) {
		String decoded = decodeRest(i);
		builder.append(decoded);
		int lengthIdx = i + decoded.length();
		int childLength = byteBuffer[lengthIdx];
		int start = lengthIdx + 1;
		if (childLength < 0) {
			childLength = makeInt(byteBuffer[lengthIdx+2], byteBuffer[lengthIdx+1],byteBuffer[lengthIdx]);
			start = lengthIdx + 3;
		}
		visitChildTree(start, childLength, builder.toString(), visitor);
	}
	
	private void visitChildTree(int start, int length, String prefix,
			IStringTrieVisitor<T> visitor) {
		int i = start;
		int childCount = byteBuffer[i];
		
		i++;
		int dataPos = i;
		boolean hasData = childCount <= 0;
		if (hasData) { //Skip associated data
			T value = decodeValue(i);
			i = i + getDataSize(dataPos);
			visitor.visit(prefix, value);
		}
		if (childCount < 0) {
			childCount = -childCount;
		}
		visitItemChildren(i, childCount, prefix, visitor);
		
	}

	protected void visitItemChildren(int i, int childCount, String prefix,
			IStringTrieVisitor<T> visitor) {
		int j = 0;
		while (j < childCount) {
			String rest = decodeRest(i);
			i = i + rest.length();
			int childLength = byteBuffer[i];
			if (childLength < 0) {
				childLength = makeInt(byteBuffer[i+2], byteBuffer[i+1],byteBuffer[i]);
				i = i + 3;
			}
			visitChildTree(i + 1, childLength, prefix + rest, visitor);
			j++;
			i = i + childLength;
		}
	}

	protected final String decodeRest(int i) {
		CharArrayList resList = new CharArrayList();
//		i--;
		int size = byteBuffer.length;
		for (int a = i; a < size; a++) {
			byte b = byteBuffer[a];
			boolean needBreal = false;
			if (b == Byte.MIN_VALUE) {
				return new String(resList.buffer,0, resList.elementsCount);		
			}
			if (b < 0) {
				b = (byte) -b;
				needBreal = true;
			}
			char c = byteToCharTable[b];
			resList.add(c);
			if (needBreal) {
				break;
			}
		}
		return new String(resList.buffer,0, resList.elementsCount);
	}

	protected int makeShift(int i) {
		byte vl=byteBuffer[i];
		if (vl==Byte.MIN_VALUE){
			i+=4;
		}
		else{
			i++;
		}
		return i;
	}

	private int makeMultibyteShift(int i) {
		i++;
		int next=makeInt(byteBuffer[i+2], byteBuffer[i+1],byteBuffer[i]);
		i=i + 2 + next;
		return i;
	}
	
	protected boolean charEquals(char nextChar, char currentChar) {
		if (useESwitch) {
			if (currentChar == 'ё')
				currentChar = 'е';
			if (nextChar == 'ё')
				nextChar = 'е';
		}
		return currentChar == nextChar;
	}

	protected abstract int getDataSize(int i);
	protected abstract T decodeValue(int i);

	protected abstract byte[] encodeValue(T associatedData2);

	public class TrieBuilder extends StringCoder {

		public class TrieNode {
			char ch;
			String s = "";
			ArrayList<TrieNode> children = new ArrayList<TrieNode>();
			T associatedData;

			public TrieNode getOrCreateChild(char c) {
				if (useESwitch && c == 'ё') {
					c = 'е';
				}
				
				for (TrieNode q : children) {
					if (charEquals(q.ch,c)) {
						return q;
					}
				}
				TrieNode newNode = new TrieNode();
				newNode.ch = c;
				children.add(newNode);
				return newNode;
			}

			protected ByteArrayList doStore() {
				ByteArrayList list = new ByteArrayList();
				int size = children.size();
				if (associatedData != null) {
					size = -size;
				}
				list.add((byte) size);
				if (associatedData != null) {
					byte[] vl = encodeValue(associatedData);
					list.add(vl);
				}
				for (TrieNode q : children) {
					byte[] values = q.store();
					q.encodeHeader(list);
					encodeLength(values.length + 1, list);
					list.add(values);
				}
				return list;
			}

			private void encodeLength(int length, ByteArrayList list) {
				if (length < 128) {
					list.add(int0(length));
				} else {
					list.add(Byte.MIN_VALUE);
					list.add(int0(length));
					list.add(int1(length));
					list.add(int2(length));
				}
			}

			private void encodeHeader(ByteArrayList headers) {
				if (s.length() == 0) {
					s += ch;
				}
				byte[] array = new byte[s.length()];
				encodeString(s, array);
				headers.add(array);
			}

			public byte[] store() {
				ByteArrayList b = doStore();
				return b.toArray();
			}

			public void optimize() {
				for (TrieNode q : children) {
					q.optimize();
				}
				if (children.size() == 1&&this!=rootNode&&this.associatedData==null) {
					TrieNode trieNode = children.get(0);
					this.associatedData=trieNode.associatedData;
					if (this.s.length()==0){
						this.s=""+ch;
					}
					if (trieNode.s.length() > 0) {
						this.s += trieNode.s;
					} else {
						this.s += trieNode.ch;
					}
					this.children = trieNode.children;
				}
			}
			@Override
			public String toString() {
				if (s.length() > 0) {
					return s;
				} else {
					return "" + ch;
				}
			}
		}

		protected TrieNode rootNode = new TrieNode();
		private int count = 0;

		public void append(String s, T value) {
			TrieNode t = rootNode;
			int length = s.length();
			for (int a = 0; a < length; a++) {
				char c = s.charAt(a);
				t = t.getOrCreateChild(c);
			}
			t.associatedData = value;
			count++;
		}

		public byte[] store() {
			rootNode.optimize();
			return rootNode.store();
		}
	}

	public void write(OutputStream stream) throws IOException {
		DataOutputStream dos = (stream instanceof DataOutputStream) ? (DataOutputStream) stream : new DataOutputStream(stream);
		dos.writeInt(count);
		dos.writeInt(usedBytes);
		ByteBuffer buffer = Charset.forName("UTF-8").encode(CharBuffer.wrap(charToByteMap.keys));
		byte[] keys = new byte[buffer.limit()];
		buffer.get(keys);
		dos.writeInt(keys.length);
		dos.write(keys);
		dos.writeInt(charToByteMap.values.length);
		dos.write(charToByteMap.values);
		buffer = Charset.forName("UTF-8").encode(CharBuffer.wrap(byteToCharTable));
		byte[] ba2 = new byte[buffer.limit()];
		buffer.get(ba2);
		dos.writeInt(ba2.length);
		dos.write(ba2);
		dos.writeInt(byteBuffer.length);
		dos.write(byteBuffer);
	}

	public void read(InputStream stream) throws IOException {
		DataInputStream dis =  (stream instanceof DataInputStream) ? (DataInputStream)stream : new DataInputStream(stream);
		count = dis.readInt();
		usedBytes = dis.readInt();
		int length = dis.readInt();
		byte[] charToByteKeys = new byte[length];
		dis.read(charToByteKeys);
		CharBuffer buffer = Charset.forName("UTF-8").decode(ByteBuffer.wrap(charToByteKeys));
		char[] keys = new char[buffer.limit()];
		buffer.get(keys);
		length = dis.readInt();
		byte[] values = new byte[length];
		dis.read(values);
		charToByteMap = CharByteOpenHashMap.from(keys, values);
		length = dis.readInt();
		byte[] byteToChar = new byte[length];
		dis.read(byteToChar);
		buffer = Charset.forName("UTF-8").decode(ByteBuffer.wrap(byteToChar));
		byteToCharTable = new char[buffer.limit()];
		buffer.get(byteToCharTable);
		length = dis.readInt();
		byteBuffer = new byte[length];
		dis.read(byteBuffer);
	}

	public int size() {
		return count;
	}

	@Override
	public int store(String string, T data) {
		if (builder == null) {
			builder = newBuilder();
		}
		builder.append(string, data);
		return 0;
	}
	
	public void commit() {
		if (builder == null) {
			return;
		}
		commit(builder);
		builder = null;
	}
}
