package com.onpositive.text.analisys.tools.data;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;

public class FileTextLoader {

	public FileTextLoader(String charset) {
		super();
		this.charset = charset;
	}

	private String charset;
	
	public String load(File file){
		
		try {
			FileInputStream fis = new FileInputStream(file);
			BufferedInputStream bis = new BufferedInputStream(fis);		
		
			byte[] buf = new byte[1024];
			
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			int l = 0;
			
			while( (l = bis.read(buf)) >= 0 ){
				baos.write(buf, 0, l);
			}
		
			String result = new String(baos.toByteArray(), charset);
			return result;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
