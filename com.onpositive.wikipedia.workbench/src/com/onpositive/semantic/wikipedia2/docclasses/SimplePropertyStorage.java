package com.onpositive.semantic.wikipedia2.docclasses;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import com.onpositive.semantic.wikipedia2.WikiEngine2;

public class SimplePropertyStorage {

	static class Pair implements Comparable<Pair> {
		String name;
		int value;

		@Override
		public int compareTo(Pair o) {
			return this.value - o.value;
		}
	}

	static HashSet<String> pProp = new HashSet<String>();
	static HashSet<String> lProp = new HashSet<String>();



	public static File getPropertiesFile(WikiEngine2 wikiEngine) {
		File file = new File(wikiEngine.getLocation(), "props.dat");
		if (!file.exists()) {
			try {
				buildFile(wikiEngine, file);
			} catch (UnsupportedEncodingException e) {
				throw new IllegalStateException();
			} catch (FileNotFoundException e) {
				throw new IllegalStateException(e);
			}
		}
		return file;
	}
	
	public static abstract class PropertyVisitor{
		public abstract void visit (int document,String pName,String value);
	}
	
	public static void visitPropertiesFile(PropertyVisitor visitor,WikiEngine2 eng) throws IOException{
		BufferedReader rs=new BufferedReader(new FileReader(getPropertiesFile(eng)));
		while (true){
			String readLine=rs.readLine();
			if (readLine==null){
				break;
			}
			int idP = readLine.indexOf(',');
			int id = Integer.parseInt(readLine.substring(0, idP));
			int vp = readLine.indexOf(',', idP + 1);
			if (vp==-1)
			{
				continue;
			}

			String pName = readLine.substring(idP + 1, vp);
			String value=readLine.substring(vp+1);
			visitor.visit(id, pName, value);
		}
	}

	private static void buildFile(WikiEngine2 wikiEngine, File file)
			throws UnsupportedEncodingException, FileNotFoundException {
		PrintStream out = new PrintStream(new BufferedOutputStream(
				new FileOutputStream(file)), true, "UTF-8");
		int[] articleKeys = wikiEngine.getDocumentIDs();
		HashMap<String, Integer> pNames = new HashMap<String, Integer>();
		int a = 0;
		for (int q : articleKeys) {
			a++;
			if (a % 1000 == 0) {
				System.out.println(a);
			}
			String plainContent = wikiEngine.getPlainContent(q);
			if (plainContent != null) {
				BufferedReader d = new BufferedReader(new StringReader(
						plainContent));
				while (true) {
					try {
						String line = d.readLine();
						if (line == null) {
							break;
						}
						line = line.trim();
						if (line.startsWith("{{")){
							if (!line.contains("}}")){
								out.print(q);
								out.print(',');
								out.print("template");
								out.print(',');
								String templateName = line.substring(2);
								out.print(templateName);
								out.println();
							}
						}
						if (line.length() > 1 && line.charAt(0) == '|') {
							int vStart = line.indexOf('=');
							if (vStart != -1) {
								String pName = line.substring(1, vStart).trim()
										.toLowerCase();

								if (pName.indexOf('[') != -1) {
									continue;
								}
								if (pName.indexOf('|') != -1) {
									continue;
								}
								if (pName.indexOf('=') != -1) {
									continue;
								}
								if (pName.indexOf(',') != -1) {
									continue;
								}
								if (pName.startsWith("align")) {
									continue;
								}
								if (pName.contains("bgcolor")) {
									continue;
								}
								if (pName.contains("style")) {
									continue;
								}
								if (pName.contains("colspan")) {
									continue;
								}
								Integer integer = pNames.get(pName);
								String value = line.substring(vStart + 1)
										.trim();
								out.print(q);
								out.print(',');
								out.print(pName);
								out.print(',');
								out.print(value);
								out.println();
								if (integer == null) {
									integer = 0;
								}
								pNames.put(pName, integer + 1);
							}
						}
					} catch (IOException e) {

					}
				}
			}
		}
		ArrayList<Pair> ps = new ArrayList<SimplePropertyStorage.Pair>();
		for (String q : pNames.keySet()) {
			Pair c = new Pair();
			c.name = q;
			c.value = pNames.get(q);
			ps.add(c);
		}
		Collections.sort(ps);
		for (Pair p : ps) {
			System.out.println(p.name + ":" + p.value);
		}
	}
}