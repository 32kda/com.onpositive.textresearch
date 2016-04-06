package com.onpositive.compactdata;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import sun.nio.cs.ArrayDecoder;

/**
 * extremly simple write only string dictionary;
 * 
 * @author kor
 * 
 */
public class ByteStringTable implements Serializable {

	private static final long serialVersionUID = 1L;
	protected int[] offsets;
	protected int[] ids;
	protected int[] idsToPositions;
	protected int   usedCount = 0;
	protected byte[] ls;
	
	public void write(DataOutputStream ds) throws IOException {
		ds.writeInt(ls.length);
		ds.write(ls);
		ds.writeInt(usedCount);
		ds.writeInt(ub);
		writeIntArray(offsets,ds);
		writeIntArray(ids,ds);
		writeIntArray(idsToPositions,ds);
	}
	public void read(DataInputStream is)throws IOException{
		int q=is.readInt();
		ls=new byte[q];
		is.readFully(ls);
		usedCount=is.readInt();
		ub=is.readInt();
		offsets=readIntArray(is);
		ids=readIntArray(is);
		idsToPositions=readIntArray(is);
	}

	private int[] readIntArray(DataInputStream is) throws IOException {
		int l=is.readInt();
		int[] d=new int[l];
		for (int a=0;a<l;a++){
			d[a]=is.readInt();
		}
		return d;
	}
	private void writeIntArray(int[] offsets2, DataOutputStream ds) throws IOException {
		ds.writeInt(offsets2.length);
		for (int a=0;a<offsets2.length;a++){
			ds.writeInt(offsets2[a]);
		}
	}

	public ByteStringTable(int sz) {
		this.offsets = new int[sz];
		this.ids = new int[sz];
		this.idsToPositions = new int[sz];
		this.ls = new byte[sz * 10];
	}

	

	public int id(String string) {
		int hashCode = string.hashCode();
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
				return ids[pos] - 1;
			}
		}
		return -1;
	}
	
	public int id2(String str){
		int id = id(str);
		if (id>=0){
			return id+1;
		}
		return id;
	}

	public int allocateId(String string) {
		int hashCode = string.hashCode();
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
				return ids[pos]-1;
			}
		}
		if (usedCount > (len * 3) / 4) {
			repack();
			return allocateId(string);
		}
		usedCount++;
		ids[pos] = usedCount;
		int ll = encode(string);
		offsets[pos] = ll;
		if (idsToPositions.length<=usedCount-1){
			int[] m = new int[idsToPositions.length*4/3];
			System.arraycopy(idsToPositions, 0, m, 0, idsToPositions.length);
			this.idsToPositions = m;
		}
		idsToPositions[usedCount - 1] = ll;
		return usedCount-1;
		// mm.put(id, offsets[pos]);

	}
	
	public int put(String string,int id) {
		int hashCode = string.hashCode();
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
				return ids[pos]-1;
			}
		}
		if (usedCount > (len * 3) / 4) {
			repack();
			return put(string,id);
		}
		usedCount++;
		ids[pos] = id;
		int ll = encode(string);
		offsets[pos] = ll;
		if (idsToPositions.length<=usedCount-1){
			int[] m = new int[idsToPositions.length*4/3];
			System.arraycopy(idsToPositions, 0, m, 0, idsToPositions.length);
			this.idsToPositions = m;
		}
		idsToPositions[usedCount - 1] = ll;
		return usedCount-1;
		// mm.put(id, offsets[pos]);

	}

	public String getName(int id) {
		int i = idsToPositions[id];
		if (i==0){
			return "";
		}
		return decode(i);
	}

	static transient Charset forName = Charset.forName("UTF-8");
	static CharsetDecoder cd = forName.newDecoder();
	static sun.nio.cs.ArrayDecoder decoder=(ArrayDecoder) cd;
	int ub;

	private int encode(String string) {

		int k = ub + 1;
		byte[] bytes = internalEncode(string);
		if (ub + bytes.length + 1 >= ls.length) {
			try{
			byte[] dd = new byte[ls.length * 3 / 2];
			System.arraycopy(ls, 0, dd, 0, ls.length);
			this.ls = dd;
			}catch (OutOfMemoryError e) {
				byte[] dd = new byte[(int) (((long)ls.length * 9) / 8)];
				System.arraycopy(ls, 0, dd, 0, ls.length);
				this.ls = dd;
			}
		}
		for (int a = 0; a < bytes.length; a++) {
			ls[ub] = bytes[a];
			ub++;
		}
		ls[ub] = 0;
		ub++;
		return k;
	}

	protected byte[] internalEncode(String string) {
		return string.getBytes(forName);
	}

	protected void repack() {
		int e = offsets.length * 3 / 2;
		ByteStringTable tm = new ByteStringTable(e);
		for (int a = 0; a < offsets.length; a++) {
			int k = offsets[a];
			if (k != 0) {
				String decode = decode(k);
				int hc = decode.hashCode();
				if (hc < 0) {
					hc = -hc;
				}
				int pos = hc % e;
				int i = tm.offsets[pos];
				while (i != 0) {
					pos++;
					if (pos == e) {
						pos = 0;
					}
					i = tm.offsets[pos];
				}
				tm.ids[pos] = ids[a];
				tm.offsets[pos] = k;
			}
		}
		this.ids = tm.ids;
		this.offsets = tm.offsets;
		int[] m = new int[e];
		System.arraycopy(idsToPositions, 0, m, 0, idsToPositions.length);
		this.idsToPositions = m;
	}

	public void trim() {
		int[] m = new int[usedCount];
		System.arraycopy(idsToPositions, 0, m, 0, usedCount);
		idsToPositions = m;
		byte[] dd = new byte[ub];
		System.arraycopy(ls, 0, dd, 0, ub);
		this.ls = dd;
	}
	
	int lastId;
	private String internalDecode;

	private String decode(int i) {
		if (lastId==i){
			return internalDecode;
		}
		lastId=i;
		i--;
		int a1 = 0;
		int size = ub;
		for (int a = i; a < size; a++) {
			byte b = ls[a];
			if (b == 0) {
				break;
			} else {
				a1++;
			}
		}
		internalDecode = internalDecode(i, a1);
		return internalDecode;
	}
	ByteBuffer b;
	static char[] ss=new char[500];

	protected String internalDecode(int i, int a1) {
		int decode = decoder.decode(ls, i,a1,ss);
		String string = new String(ss,0,decode);
		return string;		
	}

	public int size() {
		return idsToPositions.length;
	}
	
}