package com.onpositive.wikipedia.workbench;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import com.onpositive.semantic.wikipedia2.catrelations.isa.HeadWordCriteria;

import junit.framework.TestCase;

public class CategoryIsATest extends TestCase {

	public static void test0() {
		BufferedReader rs = new BufferedReader(new InputStreamReader(
				CategoryIsATest.class.getResourceAsStream("list.txt"), Charset.forName("UTF-8")));
		int num = 0;
		int successCount=0;
		int samplesCount=0;
		int currentSuccessCount=0;
		int expectedSuccessError=0;

		int expectedFailureError=0;
		while (true) {
			try {
				String line = rs.readLine();
				num++;
				if (line == null) {
					break;
				}
				if (line.length() > 0) {
					line=line.trim();
					if (!line.endsWith("1")&&!line.endsWith("0")&&!line.endsWith("2")){
						System.out.println("Error:"+line+":"+num);
						continue;
					}
					if (line.endsWith("2")){
						continue;
					}
					samplesCount++;
					boolean expectedSuccess=line.endsWith("1");
					boolean unkwnown=line.endsWith("2");
					final int indexOf = line.indexOf("->");
					String categoryName=line.substring(0, indexOf).trim();
					String categoryName1=line.substring(indexOf+2);
					categoryName1=categoryName1.substring(0, categoryName1.length()-1).trim();
					final boolean simpleIsA = HeadWordCriteria.isSimpleIsA(categoryName, categoryName1);
					if (simpleIsA&&expectedSuccess){
						currentSuccessCount++;
						continue;
					}
					if (!simpleIsA&&!expectedSuccess){
						currentSuccessCount++;
						continue;
					}
					if (expectedSuccess){
						expectedSuccessError++;
					}
					else{
						System.out.println(line);
						
						
						expectedFailureError++;
					}
					
				}
			} catch (IOException e) {
				
			}
		}
		System.out.println("Samples count:"+samplesCount+" Success count:"+successCount+" Current success count:"+currentSuccessCount);
		System.out.println("Expected success errors:"+expectedSuccessError);
		System.out.println("Expected failure errors:"+expectedFailureError);
		TestCase.assertTrue(currentSuccessCount>4350);
	}
}
