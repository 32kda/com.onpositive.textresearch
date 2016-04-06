package com.onpositive.text.analisys.tests;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.onpositive.text.analisys.tools.TokenTypeResolver;
import com.onpositive.text.analysis.IToken;
import com.onpositive.text.analysis.lexic.PrimitiveTokenizer;

public class PrimitiveTokenizerTest {
	
	private final static String TEST_RESULTS_PATH = "C:/workspaces/TestAnalysis/GIT/text-analisis/com.onpositive.text.analisys.tests/TestResults/tokenizerTest.txt";


	public static void main(String[] args) {
	
		String str = "'''T-34''' (разг. ''«тридцатьчетвёрка»'') — [[Союз Советских Социалистических Республик|советский]] [[средний танк]] периода [[Великая Отечественная война|Великой Отечественной войны]], выпускался серийно с [[1940 год]]а, был основным танком [[Рабоче-крестьянская Красная армия|РККА]] до первой половины [[1944 год]]a, когда на смену ему пришёл танк модификации [[Т-34-85]]. Самый массовый средний танк [[Вторая мировая война|Второй мировой войны]]<ref name=\"СТ3418\">{{книга|автор=М. Барятинский.|заглавие=Средний танк Т-34|страницы=18}}</ref><ref name=\"СТ34-859\">{{книга|автор=М. Барятинский.|заглавие=Средний танк Т-34-85|страницы=9}}</ref><ref name=\"НТ3459\">{{книга|автор=И. Желтов и др.|заглавие=Неизвестный Т-34|страницы=59}}</ref>.";
		
		PrimitiveTokenizer pt = new PrimitiveTokenizer();
		List<IToken> tokens = pt.tokenize(str);
		
		int maxLength = 0;
		for(IToken u : tokens ){
			maxLength = Math.max(maxLength, u.getLength());
		}
		
		File f = new File(TEST_RESULTS_PATH);		
		try {
			if(!f.exists()){
				f.getParentFile().mkdirs();
				f.createNewFile();
			}
			PrintStream stream = new PrintStream(f);			
			for(IToken t : tokens ){
				stream.print( t.getStringValue() );
				
				int spacesCount = maxLength + 3 - t.getLength();
				for(int j = 0 ; j < spacesCount ; j++){
					stream.print(" ");
				}
				
				stream.println(TokenTypeResolver.getResolvedType(t) + ", ");
			}
			stream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
