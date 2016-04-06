package com.onpositive.wikipedia.dumps.builder;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

public class CounterReader {

	public static void main(String[] args) throws IOException {
		try {
			BufferedReader rs=new BufferedReader(new InputStreamReader(new FileInputStream("C:\\ruwiki\\stat.txt"),"UTF-8"));
			int count=0;
			while (true){
				String sm=rs.readLine();
				if (sm==null){
					break;
				}
				String[] split = sm.split(",");
				Integer m=Integer.parseInt(split[2]);
				if (m>6){
					count++;
					
				}				
			}
			System.out.println(count);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}