package com.onpositive.semantic.wikipedia2.fulltext;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import com.google.common.collect.Maps;
import com.onpositive.semantic.wikipedia2.WikiEngine2;
import com.onpositive.wikipedia.dumps.builder.Porter;

public class StatFile {
	
	
	private WikiEngine2 engine;

	ArrayList<PCD> statistics = null;
	
	public static class StatFileProvider {

		Map<WikiEngine2,StatFile> stats = Maps.newHashMap();
		
		private static StatFileProvider instance; 
		
		private StatFileProvider() {
		}

		public static StatFileProvider getInstance() {
			if(instance == null) {
				instance = new StatFileProvider();
			}
			return instance;
		}
		
		public synchronized StatFile get(WikiEngine2 engine) {
			StatFile statFile = stats.get(engine);
			if(statFile == null) {
				statFile = StatFile.build(engine);
				stats.put(engine, statFile);
			}
			return statFile;
		}
		
	}

	
	private StatFile(WikiEngine2 engine) {
		this.engine = engine;
	}
	
	File getFile() {
		return new File(engine.getLocation(), "stat.txt");
	}
	
	private ArrayList<PCD> buildWordTable(WikiEngine2 engine) {
		int[] articleKeys = engine.getDocumentIDs();
		final HashMap<String, PCD> str = new HashMap<String, PCD>(8000000);
		int pos=0;
		for (int q : articleKeys) {
			pos++;
			if (pos%1000==0){
				System.out.println("Processed: "+pos);
			}
			String plainContent = engine.getDocumentAbstract(q);
			String text = plainContent;
			if (text == null) {
				text = "";
			}
			StringBuilder bld = new StringBuilder();
			HashSet<String> cstr = new HashSet<String>();
			;
			for (int a = 0; a < text.length(); a++) {
				char c = text.charAt(a);
				if (Character.isLetter(c)||Character.isDigit(c)||c=='-') {
					bld.append(Character.toLowerCase(c));
				} else {
					String string = bld.toString();
					if (string.length() > 3) {
						boolean allRussian = true;
						
						if (allRussian) {
							String stem = StemProvider.getInstance().stem(string);
							PCD integer = str.get(stem);
							if (integer == null) {
								integer = new PCD(stem);
								str.put(stem, integer);
							}
							integer.count++;
							if (cstr.add(stem)) {
								integer.documentCount++;
							}
						}
					}
					bld.delete(0, bld.length());
				}
			}
		}
		ArrayList<PCD> result = new ArrayList<PCD>(str.size());
		
		for (String s : str.keySet()) {
			PCD pcd = str.get(s);
			if (pcd.documentCount > 4) {
				if (pcd.str.length() > 3) {
					result.add(pcd);
				}
			}
		}
		return result;
	}
	
	void save() throws UnsupportedEncodingException, FileNotFoundException {
		PrintWriter rrr = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(getFile()), "UTF-8")));
		for (PCD q : statistics) {
			rrr.print(q.str);
			rrr.print(',');
			rrr.print(q.count);
			rrr.print(',');
			rrr.print(q.documentCount);
			rrr.println();
		}
		rrr.close();
	}
	
	protected void load() throws UnsupportedEncodingException, FileNotFoundException {
		statistics = new ArrayList<PCD>();
		BufferedReader rs = new BufferedReader(new InputStreamReader(new FileInputStream(getFile()), "UTF-8"));
		while (true) {
			try {
				String readLine = rs.readLine();
				if (readLine == null) {
					break;
				}
				int idP = readLine.indexOf(',');
				String wordStrem = readLine.substring(0, idP);

				int vp = readLine.indexOf(',', idP + 1);

				int wc = Integer.parseInt(readLine.substring(idP + 1, vp));
				int dc = Integer.parseInt(readLine.substring(vp + 1));
				PCD r = new PCD(wordStrem);
				r.count = wc;
				r.documentCount = dc;
				statistics.add(r);
			} catch (IOException e) {
				e.printStackTrace();
				break;
			}
		}
		try {
			rs.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean exists() {
		return getFile().exists();
	}
	
	private void build() {
		System.out.println("Building word stat");
		statistics = buildWordTable(engine);
		System.out.println("Total usable foundations:" + statistics.size());
		try {
			save();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}
	
	public ArrayList<PCD> getStatistics() {
		return statistics;
	}
	
	private static StatFile build(WikiEngine2 engine) {
		StatFile statFile = new StatFile(engine);
		if(!statFile.exists()) {
			statFile.build();
		} else {
			System.out.println("Loading word stat");
			try {
				statFile.load();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		return statFile;
	}
	static class PCD {
		public PCD(String stem) {
			this.str = stem;
		}

		String str;
		int count;
		int documentCount;
	}
}