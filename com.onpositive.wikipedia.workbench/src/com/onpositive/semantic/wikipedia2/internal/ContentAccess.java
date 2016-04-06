package com.onpositive.semantic.wikipedia2.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import com.onpositive.semantic.wikipedia2.WikiEngine2;

public final class ContentAccess {

	protected RandomAccessFile contentfile;
	private ByteBuffer pages;
	private IntBuffer pageAddrs;
	private int capacity;
	
	public ContentAccess(File fl) throws FileNotFoundException {
		contentfile=new RandomAccessFile(new File(fl,WikiEngine2.ARTICLE_CONTENT), "r");
		try {
			File file = new File(fl,WikiEngine2.ARTICLE_CATALOG);
			FileInputStream in = new FileInputStream(file);
			int catalogSize=(int) file.length();
			pages = ByteBuffer.allocateDirect(catalogSize);
			pages.position(0);
			pageAddrs=pages.asIntBuffer();
			capacity = pageAddrs.capacity();
			in.getChannel().read(pages);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String getPage(int pageId){
		if (pageId>0&&pageId<capacity){
			long l = (long)pageAddrs.get(pageId+2);
			if (l<0){
				l=-l+Integer.MAX_VALUE;
			}
			long position=8*l;
			return read(position);
		}		
		return null;
	}
	
	
	protected String read(long position){
		try{
		contentfile.seek(position);
		int readInt = contentfile.readInt();
		byte[] bs = new byte[readInt];
		contentfile.readFully(bs);
		return new String(bs,"UTF-8");
		}catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}
}
