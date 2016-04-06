package com.onpositive.text.analisys.tests;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import com.onpositive.text.analisys.tools.TokenPrinter;
import com.onpositive.text.analisys.tools.TokenTypeResolver;
import com.onpositive.text.analysis.BasicCleaner;
import com.onpositive.text.analysis.CompositToken;
import com.onpositive.text.analysis.IParser;
import com.onpositive.text.analysis.IToken;
import com.onpositive.text.analysis.ParserComposition;
import com.onpositive.text.analysis.TokenRegistry;
import com.onpositive.text.analysis.lexic.ComplexClause;
import com.onpositive.text.analysis.lexic.DateToken;
import com.onpositive.text.analysis.lexic.DimensionToken;
import com.onpositive.text.analysis.lexic.ScalarToken;
import com.onpositive.text.analysis.lexic.dimension.Unit;
import com.onpositive.text.analysis.syntax.ClauseToken;
import com.onpositive.text.analysis.syntax.PrepositionGroupToken;
import com.onpositive.text.analysis.syntax.SentenceToken;
import com.onpositive.text.analysis.syntax.SyntaxToken;
import com.onpositive.text.analysis.utils.TokenLogger;

public class ParserTest extends TestCase {
	
	protected ParserComposition composition;
	
	private boolean printFlag = true;
	
	protected void togglePrint(boolean print) { printFlag = print; }
	
	protected void setParsers(IParser... parsers){
		if(parsers==null||parsers.length==0){
			return;
		}
		else{
			this.composition = new ParserComposition(parsers);
		}
	}
	
	protected List<IToken> process(String str) {
		return process(str, printFlag);
	}
	
	protected List<IToken> process(String str, boolean print) {	
		TokenRegistry.clean();
		List<IToken> processed = composition.parse(str);
		ArrayList<IToken> list = new ArrayList<IToken>();
		for(IToken t : processed){
			if(t instanceof SentenceToken){
				list.addAll(new BasicCleaner().clean(t.getChildren()));
			}
			else{
				list.add(t);
			}
		}
		if (print)
			printTokens(list);
		return list;
	}	
	
	
	public static void printTokens(List<IToken> processed) {
		
		System.out.println();
		System.out.println("-----");
		
		if(processed==null||processed.isEmpty()){
			return;
		}
		
		int l = (""+processed.get(processed.size()-1).getEndPosition()).length();
		
		for(IToken t : processed){
			System.out.format("%0" + l + "d", t.getStartPosition());
			System.out.print("-");
			System.out.format("%0" + l + "d", t.getEndPosition());
			System.out.println( " " + printToken(t, l+l+2).trim());//TokenTypeResolver.getResolvedType(t) + " " + t.getStringValue());
		}
	}
	
	protected static void assertTestDimension(double value, Unit unit,List<IToken> tk){
		boolean found=false;
		for (IToken z:tk){
			if (z instanceof DimensionToken){
				DimensionToken k=(DimensionToken) z;
				if (k.getValue()==value){
					Unit unit0 = k.getUnit();
					String shortName0 = unit0.getShortName().toLowerCase();
					String shortName = unit.getShortName().toLowerCase();
					if(shortName0.equals(shortName)){
						if(unit0.getKind()==unit.getKind()){
							found = true;
						}
					}
				}
			}
		}
		TestCase.assertTrue(found);
	}
	
	protected static void assertTestDimension(Double[] values, Unit[] units,List<IToken> tk) {
		int ind = 0 ;
		for (IToken z:tk){
			if (z instanceof DimensionToken){
				DimensionToken k=(DimensionToken) z;
				if (k.getValue()==values[ind]){
					Unit unit0 = k.getUnit();
					Unit unit = units[ind];
					String shortName = unit.getShortName().toLowerCase();
					String shortName0 = unit0.getShortName().toLowerCase();
					if(shortName0.equals(shortName)){
						if(unit0.getKind()==unit.getKind()){
							ind++;
						}
					}					
				}
			}
		}
		TestCase.assertTrue(ind==values.length);
	}
	
	void assertTestScalar(double value,List<IToken>tk){
		boolean found=false;
		for (IToken z:tk){
			if (z instanceof ScalarToken){
				ScalarToken k=(ScalarToken) z;
				if (k.getValue()==value){
					found=true;
				}
			}
		}
		TestCase.assertTrue(found);
	}
	void assertTestDate(Integer year,Integer month,Integer day,List<IToken>tk){
		boolean found=false;
		for (IToken z:tk){
			if (z instanceof DateToken){
				DateToken k=(DateToken) z;
				if (year!=null){
					TestCase.assertEquals(year, k.getYear());
					found=true;
				}
				if (month!=null){
					TestCase.assertEquals(month, k.getMonth());
					found=true;
				}
				if (day!=null){
					TestCase.assertEquals(day, k.getDay());
					found=true;
				}
			}
		}
		TestCase.assertTrue(found);
	}
	
	public static String printToken(IToken token, int off){
		return TokenPrinter.printToken(token, off);
	}
	
	protected static void assertTestTokenPrint(List<IToken> tokens, String... print){
		boolean gotPrint = false;
l0:		for(String s : print){
			String str = s.replaceAll("(\\s|\\,)", "");			
			for(IToken token : tokens){
				String s1 = printToken(token,0).replaceAll("(\\s|\\,)", "");
				if(str.equals(s1)){
					gotPrint = true;
					break l0;
				}
			}
		}
		
		TestCase.assertTrue(gotPrint);
	}
	
	
	
	
	protected static void assertTestTokenPrintContains(String print, List<IToken> tokens){
		String str = print.replaceAll("(\\s|\\,)", "");
		boolean gotPrint = false;
		for(IToken token : tokens){
			String s1 = printToken(token,0).replaceAll("(\\s|\\,)", "");
			if(s1.contains(str)){
				gotPrint = true;
				break;
			}
		}
		TestCase.assertTrue(gotPrint);
	}
	
	public void setLogger(IParser parser){
		String loggerPath = System.getProperty("loggerPath");
		if(loggerPath == null){
			return;
		}
		TokenLogger logger = new TokenLogger(loggerPath);
		logger.clean();
		parser.setLogger(logger);
	}
}
