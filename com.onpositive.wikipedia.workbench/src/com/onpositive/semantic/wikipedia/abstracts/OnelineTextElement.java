package com.onpositive.semantic.wikipedia.abstracts;

public class OnelineTextElement extends TextAbstractElement {

	private static final int MIN_WORD_COUNT = 5;

	protected String text;
	protected String cleared_text;
	protected boolean hasLineBreak;
	CompositeTextElement parent;

	public void reparent(CompositeTextElement tm) {
		this.parent = tm;
	}

	@Override
	public void setParent(CompositeTextElement compositeTextElement) {
		this.parent=compositeTextElement;
	}
	public OnelineTextElement(String text, CompositeTextElement parent) {
		super();
		while (text.startsWith(":")) {
			text = text.substring(1);
		}
		while (text.startsWith(";")) {
			text = text.substring(1);
		}
		this.parent = parent;
		this.text = text;
		cleared_text = innerClear(text);
		cleared_text = clearArtefacts(cleared_text);
		cleared_text=cleared_text.replace(" , ",", ");
		cleared_text=cleared_text.replace(" , ",", ");
		cleared_text=cleared_text.replace(" , ",", ");
		
	}

	private String clearArtefacts(String text2) {
		int position=0;
		while (true) {
			int indexOf = text2.indexOf("(",position);
			if (indexOf != -1) {
				int indexOf2 = text2.indexOf(")", indexOf);
				if (indexOf2 != -1) {
					String parenContentt = text2.substring(indexOf + 1, indexOf2);
					parenContentt=cliearParenContent(parenContentt);
					
					String string = parenContentt.trim().isEmpty()?"":'('+parenContentt
												+')';
					text2 = text2.substring(0, indexOf) + string + text2.substring(indexOf2 + 1);
					position=indexOf+1;
				} else {
					return text2;
				}
			} else {
				break;
			}
		}
		return text2;
	}

	private String cliearParenContent(String linkText) {
		int length = linkText.length();
		boolean foundLetter=false;
		StringBuilder bld=new StringBuilder();
		for (int a=0;a<length;a++){
			char c=linkText.charAt(a);
			if (c=='�'){
				boolean isOk=false;
				for (int b=a+1;b<length;b++){
					char k=linkText.charAt(b);
					if (k==','){
						break;
					}
					if (k=='.'){
						break;
					}
					if (k==';'){
						break;
					}
					if (Character.isLetter(c)||Character.isDigit(k)){
						isOk=true;
						break;
					}	
				}
				if (!isOk){
					continue;
				}
			}
			if (foundLetter){
				bld.append(c);
			}
			else{
				if (Character.isLetter(c)||Character.isDigit(c)){
					foundLetter=true;
					bld.append(c);
				}
			}
		}
		return bld.toString();
	}

	static String innerClear(String text2) {
		while (true) {
			int indexOf = text2.indexOf("[[");
			if (indexOf != -1) {
				int indexOf2 = balancedIndex( text2, indexOf);
				if (indexOf2 != -1&&indexOf2!=0) {
					String linkText = text2
							.substring(indexOf + 2, indexOf2 - 1);
					int indexOf3 = linkText.indexOf('|');
					if (indexOf3 != -1) {
						linkText = linkText.substring(indexOf3 + 1);
					}
					text2 = text2.substring(0, indexOf) + linkText
							+ text2.substring(indexOf2 + 1);
				} else {
					return text2;
				}
			} else {
				break;
			}
		}
		while (true) {
			int indexOf = text2.indexOf("[http:");
			if (indexOf != -1) {
				int indexOf2 = text2.indexOf("]", indexOf);
				if (indexOf2 != -1) {
					String linkText = text2.substring(indexOf + 2, indexOf2);
					int indexOf3 = linkText.indexOf(' ');
					if (indexOf3 != -1) {
						linkText = linkText.substring(indexOf3 + 1);
					}
					text2 = text2.substring(0, indexOf) + linkText
							+ text2.substring(indexOf2 + 1);
				} else {
					return text2;
				}
			} else {
				break;
			}
		}
		// [http://
		return text2;
	}

	protected static int balancedIndex( String text2, int start) {
		char pc = 'e';
		int level = 0;
		for (int a = start; a < text2.length(); a++) {
			char c = text2.charAt(a);
			if (c == '[' && pc == '[') {
				level++;
			}
			if (c == ']' && pc == ']') {
				level--;
				if (level == 0) {
					return a;
				}
			}
			pc = c;
		}
		int lastIndexOf = text2.lastIndexOf("]]");
		if (lastIndexOf>start){
			return lastIndexOf;
		}
		if (lastIndexOf>0){
			return text2.length()-2;
		}
		return lastIndexOf;
	}

	public static boolean looksLikeActualParagraph(String txt) {
		boolean lastLetter = false;
		int wc = 0;
		for (int a = 0; a < txt.length(); a++) {
			char c = txt.charAt(a);
			if (c == '{' || c == '}') {
				return false;
			}
			if (Character.isLetter(c)) {
				lastLetter = true;
			}
			boolean whitespace = Character.isWhitespace(c);
			if (lastLetter && whitespace) {
				wc++;
			}
			if (whitespace) {
				lastLetter = false;
			}
		}
		if (wc > MIN_WORD_COUNT) {
			return true;
		}
		return false;
	}

	public static boolean looksLikeHeader(String txt) {
		boolean hasLetter = false;
		boolean hasColon = false;
		for (int a = 0; a < txt.length(); a++) {
			char c = txt.charAt(a);
			if (c == ':') {
				hasLetter = true;
			}
			if (Character.isLetter(c)) {
				hasColon = true;
			}
		}
		return hasColon && hasLetter;
	}

	static final TextAbstractElement[] textAbstractElements = new TextAbstractElement[0];

	@Override
	protected TextAbstractElement[] getChildren() {
		return textAbstractElements;
	}

	@Override
	public void printElement(TextAbstractsPrinter printer) {
		printer.println(cleared_text);
		if (hasLineBreak) {
			printer.println();
		}
	}

	@Override
	protected TextAbstractElement addLine(String textLine) {
		if (textLine.trim().isEmpty()) {
			hasLineBreak = true;
			return this;
		}
		return null;
	}

	@Override
	protected TextAbstractElement getParent() {
		return parent;
	}
}
