package com.onpositive.semantic.wikipedia.abstracts;

import java.io.PrintWriter;
import java.io.Writer;

public class TextAbstractsPrinter{

	PrintWriter out;
	
	public TextAbstractsPrinter(Writer out) {
		this.out=new PrintWriter(out);
		lastNewLine=true;
	}

	boolean lastNewLine;
	public void println() {
		if (!lastNewLine){
		out.println();
		lastNewLine=true;
		}
	}
	
	public void println(String text){
		out.println(text);
		lastNewLine=false;
	}

	public void close() {
		out.close();
	}
}
