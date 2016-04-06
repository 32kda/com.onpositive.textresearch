package com.onpositive.renderer.wordnet;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import com.onpositive.semantic.wordnet.WordNetProvider;
import com.onpositive.text.analisys.tools.TokenPrinter;
import com.onpositive.text.analysis.BasicCleaner;
import com.onpositive.text.analysis.IToken;
import com.onpositive.text.analysis.syntax.SentenceToken;
import com.onpositive.text.analysis.syntax.SyntaxParser;
import com.onpositive.text.webview.DemoComponent;

public class SyntaxParseComponent extends DemoComponent{

	@Override
	public String getDescription() {
		return "Синтаксический разбор. Введите строку для разбора:";
	}

	@Override
	public String getTitle() {
		return "Синтаксический разбор";
	}

	@Override
	public String getId() {
		return "syntax";
	}

	@Override
	public String getOutput() {
		return render(this.getText());
	}
	public static String render(String queryString){
		SyntaxParser ps=new SyntaxParser(WordNetProvider.getInstance());
		List<IToken> parse = ps.parse(queryString);
		List<IToken> process = process(parse);
		String testTokenPrint = testTokenPrint(process);
		
		String header=process.size()>1?("<h4>"+process.size()+" разбора"+"</h4>"):"<h4>Разбор:</h4>";
		return header+testTokenPrint;
	}
	public static String testTokenPrint(List<IToken> tokens){
		StringWriter wr=new StringWriter();
		PrintWriter pr=new PrintWriter(wr);
			for(IToken token : tokens){
				String s1 = TokenPrinter.printToken(token,0);
				pr.print("<code><pre>");
				pr.println(s1);
				pr.println("</pre></code><hr/>");
			}
		return wr.toString();	
	}
	static List<IToken> process(List<IToken>processed){
		ArrayList<IToken> list = new ArrayList<IToken>();
		for(IToken t : processed){
			if(t instanceof SentenceToken){
				list.addAll(new BasicCleaner().clean(t.getChildren()));
			}
			else{
				list.add(t);
			}
		}
		return list;
	}

}
