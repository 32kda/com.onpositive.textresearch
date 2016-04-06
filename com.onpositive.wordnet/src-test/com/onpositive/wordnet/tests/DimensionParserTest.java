package com.onpositive.wordnet.tests;


import java.io.InputStream;
import java.io.InputStreamReader;
import com.onpositive.semantic.wordnet.TextElement;
import com.onpositive.semantic.wordnet.WordNetProvider;
import com.onpositive.semantic.wordnet.edit.IWordNetEditInterface;
import com.onpositive.semantic.wordnet.edit.WordNetPatch;

import junit.framework.TestCase;


public class DimensionParserTest extends TestCase{
	

	
	public void testAA() {
		
		try {
			InputStream stream = DimensionParserTest.class.getResourceAsStream("tst.xml");
			System.out.println("Stream " + stream);
			InputStreamReader isr = new InputStreamReader(stream,"UTF-8");
//			URL is = Thread.currentThread().getContextClassLoader().getResource("tst.xml");			
//			InputStreamReader isr = new InputStreamReader(is.openStream(),"UTF-8");
			WordNetPatch parse = WordNetPatch.parse(isr);
//			TestCase.assertEquals(14, parse.size());
			IWordNetEditInterface editable = WordNetProvider.editable(WordNetProvider.getInstance());
			TextElement wordElement3 = editable.getWordNet().getWordElement("метр");
			TestCase.assertTrue(wordElement3!=null);
			TextElement[] possibleContinuations = editable.getWordNet().getPossibleContinuations(wordElement3);
			parse.execute(editable);

		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}
