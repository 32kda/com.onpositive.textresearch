package com.onpositive.text.analisys.tools.exec;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.onpositive.semantic.wordnet.composite.CompositeWordnet;
import com.onpositive.text.analisys.tools.WFTConflictStat;
import com.onpositive.text.analisys.tools.WFTConflictStat.ConflictInfo;
import com.onpositive.text.analisys.tools.data.HtmlRemover;
import com.onpositive.text.analysis.IToken;

public class StatGen {
	
	public static class Writer {		
		PrintWriter pw = null;
		PrintStream out = null;
		public Writer(String filename) throws IOException {
			pw = new PrintWriter(new BufferedWriter(new FileWriter(filename, true)));
		}
		
		public Writer() {}

		public void dispose() {
			try {
				if (pw != null) pw.close();
				if (out != null) out.close();
			} catch (Throwable e) {
				
			}
		}
		
		public void println(String str) {
			if (pw != null) {
				pw.println(str);
				pw.flush();
		
			}
			else if (out != null) out.println(str);
			else System.out.println(str);
		}
	}
	
	private static String readFile(String path, Charset encoding) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);
	}
	
	public static CompositeWordnet CreateWordnet() {
		CompositeWordnet wn=new CompositeWordnet();
		wn.addUrl("/numerics.xml");
		wn.addUrl("/dimensions.xml");
		wn.addUrl("/modificator-adverb.xml");
		wn.addUrl("/prepositions.xml");
		wn.addUrl("/conjunctions.xml");
		wn.addUrl("/modalLikeVerbs.xml");
		wn.addUrl("/participles.xml");
		wn.prepare();
		return wn;
	}
	
	static Writer out;
	
	public static void main(String [] args) {
		
 		if (args.length < 1) {
			System.err.println("Usage: java -jar statgen.jar FILENAME [OUTPUT]\n");
			return;
		}
 		
 		out = null;
 		try {
			out = args.length == 2 ? new StatGen.Writer(args[1]) : new Writer();
		} catch (IOException e1) {
			e1.printStackTrace();
			return;
		}
 		
 			
		WFTConflictStat sg = new WFTConflictStat(CreateWordnet());
		
		String filename = args[0];
		
		String contents = "";
		
		try {			
			contents = readFile(filename, Charset.availableCharsets().get("windows-1251"));
			contents = HtmlRemover.removeHTML(contents);
		} catch (IOException e) {
			System.err.println("IO error on " + filename + ": " + e.getMessage());
			return;
		}
		try {
			
			sg.setOnProcess((x,y)->System.err.print("\r[" + x + "/" + y + "]"));
			List<IToken> processed = sg.parse(contents);
			
			if (processed == null || processed.size() == 0)
				System.err.println("FAIL " + filename + ": No tokens produced.");
			else {
				List<List<ConflictInfo[]>> array = sg.getStatistic(processed);				 
				for (List<ConflictInfo[]> curr : array)
					curr.forEach(x->out.println(PrintConflicts(x)));
				
			}
		} catch (Exception e) {
			System.err.println("FAIL " + filename + ": " + e.getMessage());
		}
		out.dispose();
	}
	
	public static String PrintConflicts(ConflictInfo[] conflicts) {
		try {
			if (conflicts == null || conflicts.length == 0) return null;
			
			JSONArray cs = new JSONArray();
			
			for (ConflictInfo c : conflicts) 
			{
				if (c.wft == null) continue;
				JSONObject conflict = new JSONObject();
				conflict.put("word", c.wft.getBasicForm());
				conflict.put("partofspeech", c.wft.getPartOfSpeech() != null ? c.wft.getPartOfSpeech().toString() : "UNKNOWN");
				conflict.put("tokens", (c.tokenTypes != null ? new JSONArray(c.tokenTypes) : new JSONArray()));				
				cs.put(conflict);
			}
			return cs.toString();
		} catch (JSONException e) {
			return null;
		}
	}
}
