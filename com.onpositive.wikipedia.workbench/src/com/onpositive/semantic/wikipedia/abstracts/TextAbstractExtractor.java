package com.onpositive.semantic.wikipedia.abstracts;

import java.io.BufferedReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;

public class TextAbstractExtractor {

	public static String replaceImportantTemplates(String content) {
		if (content==null||content.length()==0){
			return "";
		}
		while (true) {
			int indexOf = content.toLowerCase().indexOf("{{датарождения");
			if (indexOf != -1) {
				int indexOf2 = content.indexOf("}}",indexOf);
				if (indexOf2!=-1){
					content=content.substring(0,indexOf)
					+replaceDate(content.substring(indexOf+"{{ДатаРождения".length()+1,indexOf2))
					+content.substring(indexOf2+2);
				}
				else{
					break;
				}
			}
			else{
				break;
			}
		}
		while (true) {
			int indexOf = content.toLowerCase().indexOf("{{датасмерти");
			if (indexOf != -1) {
				int indexOf2 = content.indexOf("}}",indexOf);
				if (indexOf2!=-1){
					content=content.substring(0,indexOf)
					+replaceDate(content.substring(indexOf+"{{ДатаСмерти".length()+1,indexOf2))
					+content.substring(indexOf2+2);
				}
			}
			else{
				break;
			}
		}
		while (true) {
			int indexOf = content.toLowerCase().indexOf("{{флагификация");
			if (indexOf != -1) {
				int indexOf2 = content.indexOf("}}",indexOf);
				if (indexOf2!=-1){
					try{
					content=content.substring(0,indexOf)
					+content.substring(indexOf+"{{Флагификация".length()+1,indexOf2)
					+content.substring(indexOf2+2);
					}catch (StringIndexOutOfBoundsException e){
						break;
					}
				}
				else{
					break;
				}
			}
			else{
				break;
			}
		}
//		while (true) {
//			int indexOf = content.indexOf("{{Флаг");
//			if (indexOf != -1) {
//				int indexOf2 = content.indexOf("}}",indexOf);
//				if (indexOf2!=-1){
//					content=content.substring(0,indexOf)
//					+content.substring(indexOf+"{{Флаг".length()+1,indexOf2)
//					+content.substring(indexOf2+2);
//				}
//			}
//			else{
//				break;
//			}
//		}
		return content;
	}

	private static String replaceDate(String substring) {
		return substring.replace('|', '.');
	}

	public static String killTemplates(String text) {
		int bracketsBlockSize = 2;

		// if( templateManager == null )
		// templateManager = new WikiTemplateManager() ;
		//
		ArrayList<Integer> templateBounds = new ArrayList<Integer>();
		templateBounds.add(0);
		if (text == null) {
			return "";
		}
		int l = text.length();
		int weight = 0;
		for (int i = 0; i < l; i++) {
			if (text.startsWith("{{", i)) {
				weight++;
				if (weight == 1)
					templateBounds.add(i);
			} else if (text.startsWith("}}", i)) {
				if (weight > 0) {
					weight--;
					if (weight == 0)
						templateBounds.add(i + bracketsBlockSize);
				}
			}
		}
		templateBounds.add(text.length());

		// System.out.print(text+ "\n\n");

		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < templateBounds.size(); i += 2) {
			try {
				if (i + 1 < templateBounds.size()) {
					builder.append(text.substring(templateBounds.get(i),
							templateBounds.get(i + 1)));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return builder.toString().trim();
	}

	public String doExtract(String s) {
		s = initialCleanup(replaceImportantTemplates(s));
		RootElement element = new RootElement();
		BufferedReader rr = new BufferedReader(new StringReader(s));
		try {
			while (true) {
				String readLine = rr.readLine();
				if (readLine == null) {
					break;
				}
				element.addLine(readLine.trim());
			}
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
		element.accept(new TableExtractor());
		ImageExtractor visitor = new ImageExtractor();
		do{
		visitor.extractedCount=0;
		element.accept(visitor);
		}while (visitor.extractedCount>0);
		//element.accept(new ImageExtractor());
		element.accept(new CleanupVisitor());
		
		element.accept(new CleanupVisitor2());
		int estimate = new TextAbstractEstimator().estimate(element);
		if (estimate<0){
			return "";
		}
		StringWriter out = new StringWriter();
		element.printElement(new TextAbstractsPrinter(out));
		return out.toString();
	}
	
	public String[] doExtractImages(String s) {
		s = initialCleanup(replaceImportantTemplates(s));
		RootElement element = new RootElement();
		BufferedReader rr = new BufferedReader(new StringReader(s));
		try {
			while (true) {
				String readLine = rr.readLine();
				if (readLine == null) {
					break;
				}
				element.addLine(readLine.trim());
			}
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
		element.accept(new TableExtractor());
		ImageExtractor visitor = new ImageExtractor();
		do{
		visitor.extractedCount=0;
		element.accept(visitor);
		}while (visitor.extractedCount>0);
		final ArrayList<String>result=new ArrayList<String>();
		element.accept(new TextElementVisitor() {
			
			@Override
			public void visit(TextAbstractElement element) {
				if (element instanceof ImageElement){
					ImageElement el=(ImageElement) element;
					String text = el.text.toLowerCase();
					if (text.endsWith(".jpg")||text.endsWith(".gif")||text.endsWith(".png")){
						result.add(el.text);
					}
				}
			}
		});
		return result.toArray(new String[result.size()]);
	}

	public static String initialCleanup(String s) {
		s = killTemplates(s);
		String text = s;
		text = text.replaceAll("<ref>(.)*</ref>", "");
		text = text.replaceAll("<gallery>(.)*</gallery>", "");
		while (true){
			int indexOf = text.indexOf("<gallery");
			if (indexOf!=-1){
				int indexOf2 = text.indexOf("</gallery>",indexOf);
				if(indexOf2!=-1){
					text=text.substring(0,indexOf)+text.substring(indexOf2+"</gallery>".length());
				}
				else{
					text=text.substring(0,indexOf);
				}
			}
			else{
				break;
			}
		}
		// text = text.replace("</ref>", "");
		text = text.replace("__NOTOC__", "");
		text = text.replace("__TOC__", "");
		text = text.replace("<noinclude>", "");
		text = text.replace("</noinclude>", "");
		text = text.replace("<strike>", "");
		text = text.replace("</strike>", "");
		text = text.replace("<nowiki>", "");
		text = text.replace("<nowiki/>", "");
		text = text.replace("</nowiki>", "");
		text = text.replace("<p>", "");
		text = text.replace("<br>", "\r\n");
		text = text.replace("<br/>", "\r\n");
		text = text.replace("<br />", "\r\n");
		text = text.replace("<pre>", "");
		text = text.replace("</pre>", "");
		text = text.replace("</p>", "");
		text = text.replace("<blockquote>", "");
		text = text.replace("</blockquote>", "");		
		text = text.replace("<code>", "");
		text = text.replace("</code>", "");
		text = text.replace("<ins>", "");
		text = text.replace("</ins>", "");
		text = text.replace("<i>", "");
		text = text.replace("</i>", "");
		text = text.replace("<b>", "");
		text = text.replace("</b>", "");
		text = text.replace("<small>", "");
		text = text.replace("</small>", "");
		text = text.replace("<cite>", "");
		text = text.replace("</cite>", "");		
		text = text.replace("<tt>", "");
		text = text.replace("</tt>", "");
		text = cleanRef(text);
		int iterations=0;
		while (iterations<50) {
			int ll = text.indexOf("<!--");
			if (ll != -1) {
				int indexOf = text.indexOf("-->",ll);
				if (indexOf<0){
					text = text.substring(0, ll);					
				}
				else{
					text = text.substring(0, ll) + text.substring(indexOf + 3);					
				}
				iterations++;
			} else {
				break;
			}
		}
		text = killStylingMarkup(text);
		return text;
	}

	private static String killStylingMarkup(String text) {
		text = text.replace((CharSequence) "'''", "");
		text = text.replace((CharSequence) "''", "");
		text = text.replace((CharSequence) "&nbsp;", "");
		text = text.replace((CharSequence) "&lt;", "<");
		text = text.replace((CharSequence) "&gt;", ">");
		text = text.replace((CharSequence) "&quot;", "\"");
		text = text.replace((CharSequence) "&amp;", "&");
		return text;
	}

	public static String cleanRef(String text) {
		while (true) {
			int indexOf2 = text.indexOf("<ref");
			if (indexOf2 != -1) {
				String sm = text.substring(0, indexOf2);
				int len = 6;
				int indexOf3 = text.indexOf("</ref>", indexOf2);
				int indexOf4 = text.indexOf("/>", indexOf2);
				int indexOf5 = text.indexOf(">", indexOf2);
				if (indexOf4!=-1&&indexOf4<indexOf5&&(indexOf4<indexOf3||indexOf3==-1)){
					indexOf3=indexOf4;
					len = 2;
				}
				if (indexOf3 == -1) {
					len = 2;
				}
				if (indexOf3 != -1) {
					String sm1 = text.substring(indexOf3 + len);
					text = sm + sm1;
				} else {
					break;
				}
			} else {
				break;
			}
		}
		return text;
	}

}
