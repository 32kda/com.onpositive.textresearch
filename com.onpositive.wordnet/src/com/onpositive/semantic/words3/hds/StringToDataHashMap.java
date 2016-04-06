package com.onpositive.semantic.words3.hds;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;

import com.carrotsearch.hppc.IntIntOpenHashMapSerialable;

public abstract class StringToDataHashMap<T> extends StringStorage<T> {
	protected int[] offsets;
	protected int usedCount = 0;
	
	public String[] getAllKeys(){
		String[] s=new String[usedCount];
		int k=0;
		for (int a:offsets){
			if (a!=0){
				s[k++]=decode(a);
			}
		}
		return s;		
	}
	
	/* (non-Javadoc)
	 * @see com.onpositive.semantic.words3.hds.IStringToDataStorage#get(java.lang.String)
	 */
	@Override
	public T get(String string) {
		int actualAddress = getDataAddress(string);
		if (actualAddress>=0){
			T decodeRelations = decodeValue(this.byteBuffer,actualAddress);
			return decodeRelations;
		}
		return null;
	}
	protected byte[] internalEncode(String string, T relations) {
		byte[] encodeRelations = encodeValue(relations);
		byte[] rs = new byte[string.length() + encodeRelations.length];
		encodeString(string, rs);
		int ps = string.length();
		for (int a=0;a<encodeRelations.length;a++){
			rs[ps+a]=encodeRelations[a];
		}
		return rs;
	}
	
	protected abstract T decodeValue(byte[] buffer,int addr);
	protected abstract byte[] encodeValue(T value);
	
	@Override
	public void write(OutputStream stream) throws IOException {
		DataOutputStream ds = (stream instanceof DataOutputStream) ? (DataOutputStream) stream : new DataOutputStream(stream);
		ds.writeInt(byteBuffer.length);
		ds.write(byteBuffer);
		ds.writeInt(usedCount);
		ds.writeInt(usedBytes);
		writeIntArray(offsets, ds);
		char[] array = charToByteMap.keys().toArray();
		ds.writeInt(array.length);
		for (char c:array){
			ds.writeChar(c);
			ds.write(charToByteMap.get(c));
		}
	}
	@Override
	public void read(InputStream stream) throws IOException {
		DataInputStream is =  (stream instanceof DataInputStream) ? (DataInputStream)stream : new DataInputStream(stream);
		int q = is.readInt();
		byteBuffer = new byte[q];
		is.readFully(byteBuffer);
		usedCount = is.readInt();
		usedBytes = is.readInt();
		offsets = readIntArray(is);
		int readInt = is.readInt();
		for (int a=0;a<readInt;a++){
			char c=is.readChar();
			byte v=is.readByte();
			charToByteMap.put(c, v);
			byteToCharTable[v]=c;
		}
	}
	public static void writeMap(DataOutputStream str, IntIntOpenHashMapSerialable omap)
			throws IOException {
		str.writeFloat(omap.loadFactor);
		try{
		Field field = IntIntOpenHashMapSerialable.class.getDeclaredField("perturbation");
		field.setAccessible(true);
		str.writeInt(field.getInt(omap));
		}catch (Exception e) {
			throw new IllegalStateException();
		}
		writeIntArray(omap.keys, str);
		writeIntArray(omap.values, str);
		str.writeInt(omap.assigned);
		boolean[] allocated = omap.allocated;
		str.writeInt(allocated.length);
		for (int a = 0; a < omap.allocated.length; a++) {
			str.writeBoolean(omap.allocated[a]);
		}
	}
	static boolean[] readBoolArrray(DataInputStream di) throws IOException {
		int k = di.readInt();
		boolean[] rs = new boolean[k];
		for (int a = 0; a < k; a++) {
			rs[a] = di.readBoolean();
		}
		return rs;
	}

	public static IntIntOpenHashMapSerialable readMap(DataInputStream di)
			throws IOException {
		float readFloat = di.readFloat();
		IntIntOpenHashMapSerialable intIntOpenHashMap = new IntIntOpenHashMapSerialable(4,
				readFloat);
		try{
			Field field = IntIntOpenHashMapSerialable.class.getDeclaredField("perturbation");
			field.setAccessible(true);
			field.setInt(intIntOpenHashMap,di.readInt());
			}catch (Exception e) {
				throw new IllegalStateException();
			}		
		intIntOpenHashMap.keys = readIntArray(di);
		intIntOpenHashMap.values = readIntArray(di);
		intIntOpenHashMap.assigned = di.readInt();
		intIntOpenHashMap.allocated = readBoolArrray(di);
		return intIntOpenHashMap;
	}
	
	public static int[] readIntArray(DataInputStream is) throws IOException {
		int l = is.readInt();
		int[] d = new int[l];
		for (int a = 0; a < l; a++) {
			d[a] = is.readInt();
		}
		return d;
	}
	public StringToDataHashMap(int sz) {
		this.offsets = new int[sz];
		this.byteBuffer = new byte[sz * 10];
	}


	public static void writeIntArray(int[] offsets2, DataOutputStream ds)
			throws IOException {
		ds.writeInt(offsets2.length);
		for (int a = 0; a < offsets2.length; a++) {
			ds.writeInt(offsets2[a]);
		}
	}
	/* (non-Javadoc)
	 * @see com.onpositive.semantic.words3.hds.IStringToDataStorage#store(java.lang.String, T)
	 */
	@Override
	public int store(String string, T data) {
		int hashCode = hashCode(string);
		if (hashCode < 0) {
			hashCode = -hashCode;
		}
		int len = offsets.length;
		int pos = hashCode % len;
		int i = offsets[pos];
		while (i != 0) {
			String m = decode(i);
			if (!m.equals(string)) {
				pos++;
				if (pos == len) {
					pos = 0;
				}
				i = offsets[pos];
				continue;
			} else {
				throw new IllegalStateException();
			}
		}
		if (usedCount > (len * 3) / 4) {
			repack();
			store(string, data);
		}
		usedCount++;
		// ids[pos] = usedCount;
		int ll = encode(string, data);
		offsets[pos] = ll;
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
             if (charAt=='ё'){
            	 charAt='е';
             }
			h = 31 * h + charAt;
        }
		return h;
	}
	protected final int encode(String string, T relations) {
		int k = usedBytes + 1;
		byte[] bytes = internalEncode(string, relations);
		storeData(bytes);
		return k;
	}
	
	
	protected final int getDataAddress(String string) {
		int hashCode = hashCode(string);
		if (hashCode < 0) {
			hashCode = -hashCode;
		}
		int len = offsets.length;
		int pos = hashCode % len;
		int i = offsets[pos];
		int actualAddress=-1;
		while (i != 0) {
			if (!equals(string, i)) {
				pos++;
				if (pos == len) {
					pos = 0;
				}
				i = offsets[pos];
				continue;
			} else {	
				int i2 = i + string.length();
				actualAddress=i2;
				break;
			}
		}
		return actualAddress-1;
	}
	protected final void repack() {
		int e = offsets.length * 3 / 2;
		int[] offf=new int[e];
		for (int a = 0; a < offsets.length; a++) {
			int k = offsets[a];
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
		this.offsets = offf;
	}
}
