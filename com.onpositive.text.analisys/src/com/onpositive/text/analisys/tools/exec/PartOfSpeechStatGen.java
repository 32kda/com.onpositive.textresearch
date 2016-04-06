package com.onpositive.text.analisys.tools.exec;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import com.onpositive.semantic.wordnet.composite.CompositeWordnet;
import com.onpositive.text.analisys.tools.LexicParserStatGenerator;
import com.onpositive.text.analisys.tools.data.HtmlRemover;
import com.onpositive.text.analysis.IToken;

public class PartOfSpeechStatGen {

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
	
	
	public static void main(String [] args) {
		
 		if (args.length != 2) {
			System.err.println("Usage: java -jar statgen.jar FILENAME OUTPUT\n");
			return;
		}
	
 		DataOutputStream out = null; 		
 		try {
			out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(args[1], false)));
		} catch (IOException e1) {
			e1.printStackTrace();
			return;
		}
 		LexicParserStatGenerator sg = new LexicParserStatGenerator(CreateWordnet());
		
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
		
			List<Integer[]> stats = sg.stats(processed);
			
			for (Integer[] vv : stats) {
				for (Integer v : vv) out.writeInt(v);
				out.writeInt(-1);
			}
			out.flush();
			out.close();			
		} catch (Exception e) {
			System.err.println("FAIL " + filename + ": " + e.getMessage());
		}		
	}	
}
