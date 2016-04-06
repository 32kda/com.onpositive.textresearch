package com.onpositive.semantic.words3.hds;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class StringArray extends BufferStorable{

	public String[]data;
	
	@Override
	protected void init(ByteBuffer buffer, int position) {
		int len=buffer.getInt(position);
		data=new String[len];
		position+=4;
		for (int a=0;a<len;a++){
			int q=buffer.getInt(position);
			char[]str=new char[q];
			position+=4;
			for (int i=0;i<q;i++){
				str[i]=buffer.getChar(position);
				position+=2;
			}
			data[a]=new String(str);
		}
	}

	@Override
	protected byte[] toByteArray() {
		try{
		ByteArrayOutputStream bs=new ByteArrayOutputStream();
		DataOutputStream ds=new DataOutputStream(bs);
		ds.writeInt(data.length);
		for (int a=0;a<data.length;a++){
			ds.writeInt(data[a].length());
			for (int i=0;i<data[a].length();i++){
				ds.writeChar(data[a].charAt(i));
			}
		}
		ds.close();
		return bs.toByteArray();
		}catch (IOException e){
			throw new IllegalStateException(e);
		}
	}
	
}