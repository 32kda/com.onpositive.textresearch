package com.onpositive.text.analisys.tools.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

public class LogWriter {

	public static final String LINE_SEPARATOR = System.getProperty("line.separator");

	private PrintWriter fw;
	
	public LogWriter(String path) {
		super();
		this.file = new File(path);
		try {
			fw = new PrintWriter(file, "UTF-8");
		} catch (Exception e) {}
	}

	private File file;
	
	public void reset() {
		
		if (file.exists()) {
			try {
				FileOutputStream fos = new FileOutputStream(file);
				fos.write(new byte[0]);
				fos.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void writeLn(String str) {
		fw.write(str);
		fw.write(LINE_SEPARATOR);		
	}

}
