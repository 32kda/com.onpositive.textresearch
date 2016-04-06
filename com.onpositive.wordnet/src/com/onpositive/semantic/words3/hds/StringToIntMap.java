package com.onpositive.semantic.words3.hds;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.FileChannel;

import com.carrotsearch.hppc.ByteArrayList;

public final class StringToIntMap extends StringCoder2{
	protected IntBuffer offsets;
	protected int usedCount = 0;
	private ByteBuffer allocateDirect;
	
	public String[] getAllKeys(){
		String[] s=new String[usedCount];
		int k=0;
		int capacity = offsets.capacity();
		for (int a=0;a<capacity;a++){
			int v=offsets.get(a);
			if (v!=0){
				s[k++]=decode(a);
			}
		}		
		return s;		
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
	
	public void write(FileChannel ch) throws IOException{
		trim();
		writeHeader(ch);
		writeData(ch);
		writeOffsets(ch);
	}

	protected void writeHeader(FileChannel ch) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		DataOutputStream header=new DataOutputStream(out);
		header.writeInt(offsets.capacity());
		header.writeInt(byteBuffer.length);
		header.writeInt(usedCount);
		header.writeInt(usedBytes);
		for (int a=0;a<byteToCharTable.length;a++){
			header.writeChar(byteToCharTable[a]);
		}
		header.close();
		ch.write(ByteBuffer.wrap(out.toByteArray()));
	}
	
	public void read(FileChannel ch) throws IOException{
		readHeader(ch);
		ch.read(ByteBuffer.wrap(byteBuffer));
		ch.read(allocateDirect);
	}

	protected void readHeader(FileChannel ch) throws IOException {
		ByteBuffer allocate = ByteBuffer.allocate(16+byteToCharTable.length*2);
		ch.read(allocate);
		allocate.position(0);
		IntBuffer asIntBuffer = allocate.asIntBuffer();
		allocateDirect = ByteBuffer.allocateDirect(asIntBuffer.get(0)*4);
		offsets=allocateDirect.asIntBuffer();
		byteBuffer=new byte[asIntBuffer.get(1)];
		usedCount=asIntBuffer.get(2);
		usedBytes=asIntBuffer.get(3);
		for (int a=0;a<byteToCharTable.length;a++){
			byteToCharTable[a]=allocate.getChar(16+a*2);
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
		ByteBuffer wrap = ByteBuffer.wrap(bs.toByteArray());
		IntBuffer asIntBuffer = wrap.asIntBuffer();
		for (int a=0;a<asIntBuffer.capacity();a++){
			if (asIntBuffer.get(a)!=offsets.get(a)){
				throw new IllegalStateException();
			}
		}
		ch.write(wrap);
	}
	
	public StringToIntMap(int sz) {
		this.offsets = IntBuffer.allocate(sz*3/2);
		this.byteBuffer = new byte[sz * 10];
	}


	public static void writeIntArray(int[] offsets2, DataOutputStream ds)
			throws IOException {
		ds.writeInt(offsets2.length);
		for (int a = 0; a < offsets2.length; a++) {
			ds.writeInt(offsets2[a]);
		}
	}
	public int store(String string, int data) {
		int hashCode = hashCode(string);
		if (hashCode < 0) {
			hashCode = -hashCode;
		}
		int len = offsets.capacity();
		int pos = hashCode % len;
		int i = offsets.get(pos);
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
			store(string, data);
		}
		usedCount++;
		// ids[pos] = usedCount;
		int ll = encode(string, data);
		offsets.put(pos,ll);
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

	public void test(StringToIntMap map) {
		for (int a=0;a<map.byteBuffer.length;a++){
			if (map.byteBuffer[a]!=map.byteBuffer[a]){
				throw new IllegalStateException();
			}
		}
		for (int a=0;a<map.offsets.capacity();a++){
			if (this.offsets.get(a)!=map.offsets.get(a)){
				throw new IllegalStateException();
			}
		}
	}
}