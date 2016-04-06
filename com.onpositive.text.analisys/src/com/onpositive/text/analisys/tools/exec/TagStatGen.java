package com.onpositive.text.analisys.tools.exec;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import com.carrotsearch.hppc.ObjectArrayList;
import com.carrotsearch.hppc.cursors.ObjectCursor;
import com.onpositive.semantic.wordnet.composite.CompositeWordnet;
import com.onpositive.text.analisys.tools.TagStatGenerator;
import com.onpositive.text.analisys.tools.TagStatGenerator.TypedWord;
import com.onpositive.text.analisys.tools.data.HtmlRemover;
import com.onpositive.text.analysis.IToken;

public class TagStatGen {
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
	
	public static void main(String[] args) {
		if (args.length != 3) {
			System.err.println("Usage: java -jar tagsg.jar FILENAME TAGSTAT TYPEDWORDSTAT\n");
			return;
		}

 		String filename = args[0];
		
		String contents = "";
		
 		DataOutputStream tagout = null;

 		PrintWriter twout = null;
 		
 		try {
 			contents = readFile(filename, Charset.availableCharsets().get("windows-1251"));
			contents = HtmlRemover.removeHTML(contents);
			tagout = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(args[1], false)));
			twout = new PrintWriter(args[2]); 					
		} catch (IOException e) {
			System.err.println("IO error: " + e.getMessage());
			return;
		}
 		TagStatGenerator sg = new TagStatGenerator(CreateWordnet());

		try {
			sg.setOnProcess((x,y)->System.err.print("\r[" + x + "/" + y + "]"));
			List<IToken> processed = sg.parse(contents);
		
			ObjectArrayList<short[]> grammemStats = new ObjectArrayList<short[]>();
			ObjectArrayList<TagStatGenerator.TypedWord> twStats = new ObjectArrayList<TagStatGenerator.TypedWord>(); 
			
			sg.computeStats(processed, grammemStats, twStats);
			
			for (ObjectCursor<short[]> vv : grammemStats) {
				for (short v : vv.value) tagout.writeShort(v);
				tagout.writeShort(-1);
			}
			
			for (ObjectCursor<TypedWord> tws : twStats)
				twout.println(tws.value.word1 + "\t" + tws.value.word2 + "\t" + tws.value.grammem1 + "\t" + tws.value.grammem2);
						
			tagout.flush();
			tagout.close();
			twout.flush();
			twout.close();
		} catch (Exception e) {
			System.err.println("FAIL " + filename + ": " + e.getMessage());
		}		
 		
	}

}
