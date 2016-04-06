package com.onpositive.semantic.words3.hds;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.FileChannel;

import com.carrotsearch.hppc.ByteArrayList;

public final class StringVocabulary extends StringCoder2{
	protected IntBuffer offsets;
	protected IntBuffer backLinks=IntBuffer.allocate(40000);
	protected int backLinksSize=1;
	protected int usedCount = 0;
	private ByteBuffer allocateDirect;
	
	public String[] getAllKeys(){
		String[] s=new String[usedCount];
		int k=0;
		int capacity = offsets.capacity();
		for (int a=0;a<capacity;a++){
			int v=offsets.get(a);
			if (v!=0){
				s[k++]=decode(v);
			}
		}		
		return s;		
	}
	public String get(int id){
		return decode(backLinks.get(id));		
	}
	
	public int get(String string) {
		int actualAddress = getDataAddress(string);
		if (actualAddress>=0){
			int decodeRelations = decodeValue(this.byteBuffer,actualAddress);
			return decodeRelations;
		}
		return Integer.MIN_VALUE;
	}
	
	
	protected byte[] internalEncode(String string, int relations) {
		byte[] encodeRelations = encodeValue(relations);
		ByteArrayList rs2 = new ByteArrayList();
		encodeString(string, rs2);
		rs2.add((byte) 0);
		rs2.add((byte) 0);
		rs2.add((byte) 0);
		rs2.add((byte) 0);		
		byte[] rs = rs2.toArray();
		int ps = rs.length-4;
		for (int a=0;a<encodeRelations.length;a++){
			rs[ps+a]=encodeRelations[a];
		}
		return rs;
	}
	
	protected int decodeValue(byte[] buffer,int addr){
		return makeInt(buffer[addr+3], buffer[addr+2], buffer[addr+1], buffer[addr]);
	}
	protected byte[] encodeValue(int value){
		byte[] rs=new byte[4];
		rs[0]=int0(value);
		rs[1]=int1(value);
		rs[2]=int2(value);
		rs[3]=int3(value);
		return rs;
	}
	@Override
	public void trim() {
		super.trim();
		
	}
	
	public void write(FileChannel ch) throws IOException{
		trim();
		writeHeader(ch);
		writeData(ch);
		writeOffsets(ch);
		writeBackLinks(ch);
	}

	private void writeBackLinks(FileChannel ch) throws IOException {
		ByteArrayOutputStream bs=new ByteArrayOutputStream();
		DataOutputStream ww=new DataOutputStream(bs);
		int capacity = backLinksSize;
		for (int a=0;a<capacity;a++){
			ww.writeInt(backLinks.get(a));			
		}
		ww.close();
		ch.write(ByteBuffer.wrap(bs.toByteArray()));
	}
	protected void writeHeader(FileChannel ch) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		DataOutputStream header=new DataOutputStream(out);
		header.writeInt(offsets.capacity());
		header.writeInt(byteBuffer.length);
		header.writeInt(usedCount);
		header.writeInt(usedBytes);
		header.writeInt(backLinksSize);
		ByteArrayOutputStream charTables=new ByteArrayOutputStream();
		DataOutputStream ds=new DataOutputStream(charTables);
		char[] array = charToByteMap.keys().toArray();
		ds.writeInt(array.length);
		
		for (char c:array){
			ds.writeChar(c);
			ds.writeShort(charToByteMap.get(c));
		}
		ds.close();
		byte[] byteArray = charTables.toByteArray();
		header.writeInt(byteArray.length);
		header.write(byteArray);
		header.close();
		ch.write(ByteBuffer.wrap(out.toByteArray()));
		
	}
	
	public void read(FileChannel ch) throws IOException{
		readHeader(ch);
		ch.read(ByteBuffer.wrap(byteBuffer));
		ch.read(allocateDirect);
		ByteBuffer allocateDirect2 = ByteBuffer.allocateDirect(backLinksSize*4);
		ch.read(allocateDirect2);
		allocateDirect2.position(0);
		backLinks=allocateDirect2.asIntBuffer();
	}

	protected void readHeader(FileChannel ch) throws IOException {
		ByteBuffer allocate = ByteBuffer.allocate(24);
		ch.read(allocate);
		allocate.position(0);
		IntBuffer asIntBuffer = allocate.asIntBuffer();
		allocateDirect = ByteBuffer.allocateDirect(asIntBuffer.get(0)*4);
		offsets=allocateDirect.asIntBuffer();
		byteBuffer=new byte[asIntBuffer.get(1)];
		usedCount=asIntBuffer.get(2);
		usedBytes=asIntBuffer.get(3);
		backLinksSize=asIntBuffer.get(4);
		int charTables=asIntBuffer.get(5);
		byte[]charTable=new byte[charTables];
		ch.read(ByteBuffer.wrap(charTable));
		ByteArrayInputStream bi=new ByteArrayInputStream(charTable);
		DataInputStream is=new DataInputStream(bi);
		int readInt = is.readInt();
		for (int a=0;a<readInt;a++){
			char c=is.readChar();
			short v=is.readShort();
			charToByteMap.put(c, v);
			byteToCharTable[v]=c;
		}
	}

	protected int writeData(FileChannel ch) throws IOException {
		return ch.write(ByteBuffer.wrap(this.byteBuffer));
	}

	protected void writeOffsets(FileChannel ch) throws IOException {
		ByteArrayOutputStream bs=new ByteArrayOutputStream();
		DataOutputStream ww=new DataOutputStream(bs);
		int capacity = offsets.capacity();
		for (int a=0;a<capacity;a++){
			ww.writeInt(offsets.get(a));			
		}
		ww.close();
		ch.write(ByteBuffer.wrap(bs.toByteArray()));
	}
	
	public StringVocabulary(int sz) {
		this.offsets = IntBuffer.allocate(sz*3/2);
		this.byteBuffer = new byte[sz * 10];
	}


	
	public StringVocabulary(File createTempFile) throws IOException {
		read(new FileInputStream(createTempFile).getChannel());
	}
	public int store(String string) {
		int hashCode = hashCode(string);
		if (hashCode < 0) {
			hashCode = -hashCode;
		}
		int len = offsets.capacity();
		int pos = hashCode % len;
		int i = offsets.get(pos);
		int data=usedCount+1;
		while (i != 0) {
			String m = decode(i);
			if (!m.equals(string)) {
				pos++;
				if (pos == len) {
					pos = 0;
				}
				i = offsets.get(pos);
				continue;
			} else {
				return offsets.get(pos)-1;
			}
		}
		if (usedCount > (len * 3) / 4) {
			repack();
			return store(string);			
		}
		usedCount++;
		int k = usedBytes + 1;
		// ids[pos] = usedCount;
		int ll = encode(string, data);
		offsets.put(pos,ll);
		if (data>=backLinks.capacity()){
			int[] array = backLinks.array();
			int[] newArray=new int[array.length*3/2];
			System.arraycopy(array, 0, newArray, 0, array.length);
			backLinks=IntBuffer.wrap(newArray);
		}
		backLinks.put(data,k);
		return ll;
	}
	
	public int allwaysStore(String string) {
		int hashCode = hashCode(string);
		if (hashCode < 0) {
			hashCode = -hashCode;
		}
		int len = offsets.capacity();
		int pos = hashCode % len;
		int i = offsets.get(pos);
		int data=backLinksSize++;
		while (i != 0) {
			String m = decode(i);
			if (!m.equals(string)) {
				pos++;
				if (pos == len) {
					pos = 0;
				}
				i = offsets.get(pos);
				continue;
			} else {
				int j = offsets.get(pos)-1;
				if (data>=backLinks.capacity()){
					int[] array = backLinks.array();
					int[] newArray=new int[array.length*3/2];
					System.arraycopy(array, 0, newArray, 0, array.length);
					backLinks=IntBuffer.wrap(newArray);					
				}
				backLinks.put(data,j+1);
				return j;
			}
		}
		if (usedCount > (len * 3) / 4) {
			repack();
			return store(string);			
		}
		usedCount++;
		int k = usedBytes + 1;
		// ids[pos] = usedCount;
		int ll = encode(string, data);
		offsets.put(pos,ll);
		if (data>=backLinks.capacity()){
			int[] array = backLinks.array();
			int[] newArray=new int[array.length*3/2];
			System.arraycopy(array, 0, newArray, 0, array.length);
			backLinks=IntBuffer.wrap(newArray);
		}
		backLinks.put(data,k);
		return ll;
	}
	/**
	 * this hashcode makes ё and е same
	 * @param string
	 * @return
	 */
	private int hashCode(String string) {
		int h=0;
		int length = string.length();
		for (int i = 0; i < length; i++) {
             char charAt = string.charAt(i);             
             h = 31 * h + charAt;
        }
		return h;
	}
	protected final int encode(String string, int relations) {
		int k = usedBytes + 1;
		byte[] bytes = internalEncode(string, relations);
		storeData(bytes);
		String decode = decode(k);
		if (!decode.equals(string)){
			throw new IllegalStateException();
		}
		return k;
	}
	
	protected final int getDataAddress(String string) {
		int hashCode = hashCode(string);
		if (hashCode < 0) {
			hashCode = -hashCode;
		}
		int len = offsets.capacity();
		int pos = hashCode % len;
		int i = offsets.get(pos);
		int actualAddress=-1;
		while (i != 0) {
			int equals = equals(string, i);
			if (equals==-1) {
				pos++;
				if (pos == len) {
					pos = 0;
				}
				i = offsets.get(pos);
				continue;
			} else {	
				int i2 = equals;
				actualAddress=i2;
				break;
			}
		}
		return actualAddress;
	}
	protected final void repack() {
		int e = offsets.capacity() * 3 / 2;
		int[] offf=new int[e];
		int capacity = offsets.capacity();
		for (int a = 0; a < capacity; a++) {
			int k = offsets.get(a);
			if (k != 0) {
				String decode = decode(k);
				int hc = hashCode(decode);
				if (hc < 0) {
					hc = -hc;
				}
				int pos = hc % e;
				int i = offf[pos];
				while (i != 0) {
					pos++;
					if (pos == e) {
						pos = 0;
					}
					i = offf[pos];
				}
				offf[pos] = k;
			}
		}
		this.offsets = IntBuffer.wrap(offf);
	}
	
	public static void main(String[] args) {
		StringVocabulary vv=new StringVocabulary(100);
		int store = vv.allwaysStore("Aaa");
		System.out.println(store);
		store = vv.allwaysStore("Aaa");
		String string = vv.get(1);
		String string2 = vv.get(2);
		System.out.println(string);
		System.out.println(string2);
	}
	public void store(File createTempFile) throws IOException {
		FileOutputStream fileOutputStream = new FileOutputStream(createTempFile);
		write(fileOutputStream.getChannel());
		fileOutputStream.close();
	}
}