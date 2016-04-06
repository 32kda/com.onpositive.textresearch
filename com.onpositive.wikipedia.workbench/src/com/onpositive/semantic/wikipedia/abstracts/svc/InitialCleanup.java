package com.onpositive.semantic.wikipedia.abstracts.svc;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;

import com.onpositive.semantic.wikipedia.abstracts.TextAbstractExtractor;

public class InitialCleanup {

	public static String initialCleanup(String s) {
		s = TextAbstractExtractor.initialCleanup(TextAbstractExtractor
				.replaceImportantTemplates(s));
		BufferedReader rr = new BufferedReader(new StringReader(s));
		StringWriter stringWriter = new StringWriter();
		PrintWriter str = new PrintWriter(stringWriter);
		ArrayList<String> lines = new ArrayList<String>();
		try {
			while (true) {
				String readLine = rr.readLine();
				if (readLine == null) {
					break;
				}
				if (gottaEnd(readLine)) {
					break;
				}
				if (readLine.contains("[[") && !readLine.contains("]]")) {
					continue;
				}
				readLine = readLine.replace(", ,", "");
				readLine = readLine.replace("()", "");
				readLine = readLine.replace("( )", "");
				readLine = readLine.replace("(, )", "");
				readLine = readLine.replace(",  — , ", "");
				// readLine=readLine.trim();
				readLine = clearArtefacts(readLine);
				// if (!readLine.trim().isEmpty()) {
				lines.add(readLine);
				// }
				// str.println(readLine);
				// element.addLine(readLine.trim());
			}
			boolean lastq = false;
			String lastLine = null;
			for (String sm : lines) {
				if (sm.trim().startsWith("=")) {
					if (lastq) {
						continue;
					} else {
						lastq = true;
					}
				} else {
					 if (!sm.trim().isEmpty()){
					lastq = false;
					 }
				}
				if (lastLine != null && !lastq) {
					str.println(lastLine);
				}
				lastLine = sm;
			}
			if (!lastq && lastLine != null) {
				str.println(lastLine);
			}
			str.close();
			// str.close();
			return stringWriter.toString();
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	private static String clearArtefacts(String text2) {
		int position = 0;
		while (true) {
			int indexOf = text2.indexOf("(", position);
			if (indexOf != -1) {
				int indexOf2 = text2.indexOf(")", indexOf);
				if (indexOf2 != -1) {
					String parenContentt = text2.substring(indexOf + 1,
							indexOf2);
					parenContentt = cliearParenContent(parenContentt);

					String string = parenContentt.trim().isEmpty() ? ""
							: '(' + parenContentt + ')';
					text2 = text2.substring(0, indexOf) + string
							+ text2.substring(indexOf2 + 1);
					position = indexOf + 1;
				} else {
					return text2;
				}
			} else {
				break;
			}
		}
		return text2;
	}

	private static String cliearParenContent(String linkText) {
		int length = linkText.length();
		boolean foundLetter = false;
		StringBuilder bld = new StringBuilder();
		for (int a = 0; a < length; a++) {
			char c = linkText.charAt(a);
			if (c == '(') {
				boolean isOk = false;
				for (int b = a + 1; b < length; b++) {
					char k = linkText.charAt(b);
					if (k == ',') {
						break;
					}
					if (k == '.') {
						break;
					}
					if (k == ';') {
						break;
					}
					if (Character.isLetter(k) || Character.isDigit(k)||k=='[') {
						isOk = true;
						break;
					}
				}
				if (!isOk) {
					continue;
				}
			}
			if (foundLetter) {
				bld.append(c);
			} else {
				if (Character.isLetter(c) || Character.isDigit(c)||c=='[') {
					foundLetter = true;
					bld.append(c);
				}
			}
		}
		return bld.toString();
	}

	static String[] breakers = new String[] { "Ссылки", "Издания", "Источники",
			"См. также", "Литература", "Примечания","Книги и публикации","Технические характеристики" };

	private static boolean gottaEnd(String readLine) {
		for (String s : breakers) {
			if (readLine.contains(s)) {
				return true;
			}
		}
		return false;
	}
}
