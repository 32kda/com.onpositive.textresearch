package com.onpositive.text.analysis.tokenstore;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.onpositive.semantic.wordnet.GrammarRelation;

public class RelationStore {

	public static void store(GrammarRelation[]gr,DataOutputStream str) throws IOException{
		if (gr.length>100){
			throw new IllegalArgumentException();
		}
		str.writeByte(gr.length);
		for (int a=0;a<gr.length;a++){
			GrammarRelation g=gr[a];
			str.writeInt(g.conceptId);
			str.writeInt(g.relation);
		}
	}
	
	public static GrammarRelation[] read(DataInputStream str) throws IOException{
		return null;
	}

}
