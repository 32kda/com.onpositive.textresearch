package com.onpositive.semantic.words3.hds;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;


public class StringArrayStorage extends PositionedDataStorage<StringArray>{
	@Override
	protected com.onpositive.semantic.words3.hds.StringArray createInstance() {
		return new StringArray();
	}
	
	public StringArrayStorage() {
		super();
	}

	public StringArrayStorage(ByteBuffer buffer, int offset) {
		super(buffer, offset);
	}

	public StringArrayStorage(File f) throws IOException {
		super(f);
	}

	public static void main(String[] args) {
		StringArrayStorage stringArrayStorage = new StringArrayStorage();
		for(int a=0;a<1000;a++){
			StringArray ar=new StringArray();
			ar.data=new String[a];
			for(int j=0;j<a;j++){
				ar.data[j]=""+j;
			}
			stringArrayStorage.add(ar);
		}
		try {
			File createTempFile = File.createTempFile("FFRF", "DDDD");
			stringArrayStorage.store(createTempFile);
			StringArrayStorage s1=new StringArrayStorage(createTempFile);
			for (int i=1;i<1000;i++){
				StringArray stringArray = s1.get(i);
				System.out.println(stringArray);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
