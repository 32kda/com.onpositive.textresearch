package com.onpositive.text.analisys.tools;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.onpositive.text.analysis.CompositToken;
import com.onpositive.text.analysis.IToken;
import com.onpositive.text.analysis.lexic.ComplexClause;
import com.onpositive.text.analysis.syntax.ClauseToken;
import com.onpositive.text.analysis.syntax.PrepositionGroupToken;
import com.onpositive.text.analysis.syntax.SyntaxToken;

public class TokenPrinter {

	private static final Set<Class<?>> printTreeClasses = new HashSet<Class<?>>(Arrays.asList(
			SyntaxToken.class, ClauseToken.class, PrepositionGroupToken.class, CompositToken.class, ComplexClause.class));
	
	private static final String childOffStr = "  ";
	
	public static String testTokenPrint(List<IToken> tokens){
		StringWriter wr=new StringWriter();
		PrintWriter pr=new PrintWriter(wr);
			for(IToken token : tokens){
				String s1 = printToken(token,0);
				pr.println(s1);
			}
		return wr.toString();	
	}
	
	public static String printToken(IToken token, int off){
		
		StringBuilder offsetBld = new StringBuilder();
		for(int i = 0 ; i < off ; i ++){
			offsetBld.append(" ");
		}
		String offStr = offsetBld.toString();
		
		StringBuilder bld = new StringBuilder();
		
		bld.append(offStr);
		bld.append(TokenTypeResolver.getResolvedType(token));
		
		if(printTreeClasses.contains(token.getClass())){
			IToken mainGroup = token instanceof SyntaxToken ? ((SyntaxToken)token).getMainGroup() : null;
			List<IToken> children = token.getChildren();
			bld.append("(");
			if(token.getType()==IToken.TOKEN_TYPE_CLAUSE){
				ClauseToken ct = (ClauseToken) token;
				{
					bld.append("\n");
					SyntaxToken subject = ct.getSubject();
					String childStr = subject != null? printToken(subject,off + 2).trim() : "no subject";
					bld.append(offStr).append(childOffStr).append("<subject>");
					bld.append(childStr);
				}
				{
					bld.append("\n");
					SyntaxToken predicate = ct.getPredicate();
					String childStr = predicate != null ? printToken(predicate,off + 2).trim() : "no predicate";;
					bld.append(offStr).append(childOffStr).append("<predicate>");
					bld.append(childStr);
				}				
			}
			else if(token.getType() == IToken.TOKEN_TYPE_PREPOSITION_GROUP){
				PrepositionGroupToken pgt = (PrepositionGroupToken) token;
				{
					bld.append("\n");
					SyntaxToken prepToken = pgt.getPrepToken();
					String childStr = printToken(prepToken,off + 2).trim();
					bld.append(offStr).append(childOffStr).append("<preposition>");
					bld.append(childStr);
				}
				{
					bld.append("\n");
					SyntaxToken word = pgt.getWord();
					String childStr = word != null ? printToken(word,off + 2).trim() : "no predicate";;
					bld.append(offStr).append(childOffStr);
					bld.append(childStr);
				}
			}
			else{
				for(int i = 0 ; i < children.size() ; i++){
					bld.append("\n");
					IToken ch = children.get(i);
					String childStr = printToken(ch,off + 2);
					if(ch==mainGroup){
						bld.append(offStr).append(childOffStr).append("<main>");
						childStr = childStr.trim();
					}
					bld.append(childStr);
				}
			}
			bld.append("  )");
		}
		else{
			bld.append(" ").append(token.getStableStringValue());
		}
		String result = bld.toString();
		return result;
	}
}
