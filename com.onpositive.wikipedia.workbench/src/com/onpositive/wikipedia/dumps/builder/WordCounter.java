package com.onpositive.wikipedia.dumps.builder;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.HashSet;

public class WordCounter {

	static class PCD{
		public PCD(String stem) {
			this.str=stem;
		}
		String str;
		int count;
		int documentCount;
	}
	
	public static void main(String[] args) {
		String arg0 = "C:\\ruwiki\\ruwiki-20140306-pages-articles.xml";
		final HashMap<String, PCD> str = new HashMap<String, PCD>(
				8000000);
		IPageVisitor visitor = new IPageVisitor() {

			
			int counter = 0;
			HashSet<Character> ssm = new HashSet<Character>();

			{
				//for (int a = 0; a < russianLetters.length(); a++) {
				//	ssm.add(russianLetters.charAt(a));
				//}
			}

			public void visit(PageModel model) {
				counter++;
				String text = model.getText();
				StringBuilder bld = new StringBuilder();
				HashSet<String>cstr=new HashSet<String>();;
				for (int a = 0; a < text.length(); a++) {
					char c = text.charAt(a);
					if (Character.isLetter(c)) {
						bld.append(Character.toLowerCase(c));
					} else {
						String string = bld.toString();
						if (string.length() > 1) {
							boolean allRussian = true;
							for (int b = 0; b < string.length(); b++) {
								if (!ssm.contains(string.charAt(b))) {
									allRussian = false;
									break;
								}
							}
							if (allRussian) {
								String stem = Porter.stem(string);
								PCD integer = str.get(stem);
								if (integer == null) {
									integer=new PCD(stem);
									str.put(stem, integer);
								}
								integer.count++;
								if (cstr.add(stem)){
									integer.documentCount++;
								}
							}
						}
						bld.delete(0, bld.length());
					}
				}
				if (counter % 1000 == 0) {

					System.out.println(str.size() + ":" + counter);
				}
			}
		};
		try {
			new XMLPageParser().visitContent(
					new InputStreamReader(new BufferedInputStream(
							new FileInputStream(arg0)), Charset
							.forName("UTF-8")), visitor);
			try {
				PrintWriter rrr=new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream("C:\\ruwiki\\stat.txt"), "UTF-8")));
				
				for (PCD q:str.values()){
					rrr.print(q.str);
					rrr.print(',');
					rrr.print(q.count);
					rrr.print(',');
					rrr.print(q.documentCount);
					rrr.println();
				}
				rrr.close();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}