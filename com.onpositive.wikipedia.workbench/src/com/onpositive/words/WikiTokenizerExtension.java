package com.onpositive.words;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;

import com.onpositive.text.analysis.AbstractToken;
import com.onpositive.text.analysis.IToken;
import com.onpositive.text.analysis.lexic.ITokenizerExtension;
import com.onpositive.text.analysis.lexic.PrimitiveTokenizer;

public class WikiTokenizerExtension implements ITokenizerExtension {

	static HashMap<String, String> entities = new HashMap<String, String>();

	static HashMap<String, String> tagmapping = new HashMap<String, String>();

	static {
		try {
			InputStream resourceAsStream = WikiTokenizerExtension.class
					.getResourceAsStream("entities.txt");
			BufferedReader rs = new BufferedReader(new InputStreamReader(
					resourceAsStream));
			while (true) {
				String line = rs.readLine();
				if (line == null) {
					break;
				}
				if (line.startsWith("#")) {
					continue;
				}
				int indexOf = line.indexOf(' ');
				char value = (char) Integer.parseInt(line
						.substring(indexOf + 1));
				String entity = "&" + line.substring(0, indexOf) + ";";
				entities.put(entity, "" + value);
				tagmapping.put("<br>", "\n");
				tagmapping.put("<br/>", "\n");
				tagmapping.put("<sup>", "^");
			}
		} catch (IOException e) {
			throw new IllegalStateException();
		}
	}

	public static String preprocess(String str) {
		StringBuilder global = new StringBuilder();
		l2: for (int pos = 0; pos < str.length(); pos++) {
			char c = str.charAt(pos);
			if (c == '&') {
				StringBuilder bld = new StringBuilder();
				for (int a = pos; a < str.length(); a++) {
					char ch = str.charAt(a);
					bld.append(ch);
					if (ch == ';') {
						String string = entities.get(bld.toString());
						if (string != null) {
							global.append(string);
						}
						pos = a;
						continue l2;
					}
				}
			}
			if (c == '<') {
				StringBuilder bld = new StringBuilder();
				bld.append(c);
				boolean br = true;
				for (int a = pos + 1; a < str.length(); a++) {
					char ch = str.charAt(a);
					if (br && !Character.isLetter(ch) && ch != '/') {
						break;
					}
					if (ch != '/') {
						br = false;
					}
					bld.append(ch);
					if (ch == '>') {
						String string = tagmapping.get(bld.toString());
						if (string != null) {
							global.append(string);
						}
						pos = a;
						continue l2;
					}
				}
			}
			global.append(c);
		}
		return global.toString();
	}

	// [[Нобелевская премия по экономике|Нобелевскую премию по экономике]]

	public static void main(String[] args) {
		System.out.println(new WikiTokenizerExtension().readUnit("[[молоко]]3",
				0));
	}

	@Override
	public IToken readUnit(String str, int pos) {
		char c = str.charAt(pos);
		if (c == '[') {
			if (pos < str.length() - 1) {
				char c1 = str.charAt(pos + 1);
				if (c1 == '[') {
					StringBuilder bld = new StringBuilder();
					// boolean br=true;
					boolean lastClose = false;
					for (int a = pos + 2; a < str.length(); a++) {
						char ch = str.charAt(a);
						if (ch == ']') {
							if (lastClose) {
								String link = null;
								String caption = null;
								String trim = bld.toString();
								int indexOf = trim.indexOf("|");
								if (indexOf != -1) {
									caption = trim.substring(indexOf + 1)
											.trim();
									link = trim.substring(0, indexOf).trim();
								} else {

									link = trim;
									caption = link;
								}
								List<IToken> tokenize = new PrimitiveTokenizer()
										.tokenize(caption);
								if (tokenize.size() > 0) {
									a++;
									if (tokenize.size() == 1) {
										AbstractToken iToken = (AbstractToken) tokenize
												.get(0);
										iToken.setLink(link);
										iToken.setStartPosition(pos);
										iToken.setEndPosition(a);
										return iToken;
									} else {

									}
									LinkToken ts = new LinkToken(caption, pos,
											a);
									for (IToken ca : tokenize) {
										AbstractToken tk = (AbstractToken) ca;
										tk.setStartPosition(tk
												.getStartPosition() + pos + 2);
										tk.setEndPosition(tk.getEndPosition()
												+ pos + 2);
									}
									AbstractToken tk = (AbstractToken) tokenize.get(0);
									tk.setStartPosition(pos);
									tk = (AbstractToken) tokenize.get(tokenize.size()-1);
									tk.setEndPosition(a);
									ts.addChildren(tokenize);
									ts.setLink(link);
									return ts;
								}
							} else {
								lastClose = true;
								continue;
							}
						}
						bld.append(ch);
					}
				}
			}
		}
		return null;
	}

}
