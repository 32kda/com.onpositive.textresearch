package com.onpositive.wikipedia.workbench;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import com.onpositive.text.analysis.lexic.WordFormToken;
import com.onpositive.wikipedia.workbench.words.primary.SimpleTitleModel;

import junit.framework.TestCase;

public class MainWordTestCase extends TestCase {

	public static void test0() {
		BufferedReader rs = new BufferedReader(new InputStreamReader(
				MainWordTestCase.class.getResourceAsStream("mainWord.txt"), Charset.forName("UTF-8")));
		int num = 0;
		int successCount=0;
		int samplesCount=0;
		int currentSuccessCount=0;
		while (true) {
			try {
				String line = rs.readLine();
				num++;
				if (line == null) {
					break;
				}
				if (line.length() > 0) {
					line=line.trim();
					if (!line.endsWith("1")&&!line.endsWith("0")){
						System.out.println("Error:"+line+":"+num);
						continue;
					}
					samplesCount++;
					boolean success=line.endsWith("1");
					if (success){
						successCount++;
					}
					final int lastIndexOf = line.lastIndexOf("-");
					String txt=line.substring(0, lastIndexOf).trim();
					String word=line.substring(lastIndexOf+1);
					word=word.substring(0, word.length()-1).trim();
					final SimpleTitleModel simpleTitleModel = new SimpleTitleModel(txt);
					final WordFormToken mainWord = simpleTitleModel.getMainWord();
					if (mainWord==null){
						continue;
					}
					String sw=mainWord.getShortStringValue().trim();
					if (success&&sw.equals(word)){
						currentSuccessCount++;
					}
					if (num%100==0){
						System.out.println("Processed:"+num);
					}
				}
			} catch (IOException e) {

			}
		}
		System.out.println("Samples count:"+samplesCount+" Success count:"+successCount+" Current success count:"+currentSuccessCount);
		TestCase.assertTrue(currentSuccessCount>4350);
	}
}
