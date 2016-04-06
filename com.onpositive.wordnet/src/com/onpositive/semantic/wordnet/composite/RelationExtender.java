package com.onpositive.semantic.wordnet.composite;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.HashSet;

import com.onpositive.semantic.wordnet.AbstractWordNet;

public class RelationExtender {

	protected AbstractWordNet wordnet;

	protected class RelationNode {

		public RelationNode(String basicTerm) {
			this.name = basicTerm;
		}

		protected String name;

		HashSet<String> upLinks = new HashSet<String>();
		HashSet<String> downLinks = new HashSet<String>();

	}

	protected HashMap<String, RelationNode> relations = new HashMap<String, RelationExtender.RelationNode>();
	
	public boolean isSub(String s1,String s2){
		RelationNode relationNode = relations.get(s1);
		if (relationNode!=null&&relationNode.downLinks.contains(s2)){
			return true;
		}		
		return false;
	}

	protected void load(BufferedReader rs) {
		while (true) {
			try {
				String line = rs.readLine();
				if (line == null) {
					break;
				}
				int indexOf = line.indexOf("->");
				String basicTerm = line.substring(0, indexOf);
				String otherTermWithRel = line.substring(indexOf + 2);
				int lastIndexOf = otherTermWithRel.lastIndexOf(' ');
				if (lastIndexOf != -1) {
					String anotherTerm = otherTermWithRel.substring(0,
							lastIndexOf).trim();
					String rr = otherTermWithRel.substring(lastIndexOf + 1)
							.trim();
					if (rr.equalsIgnoreCase("Y")) {
						getOrCreateNode(basicTerm, anotherTerm);
					}
					if (rr.equalsIgnoreCase("I")) {
						getOrCreateNode(anotherTerm, basicTerm);
					}
					if (rr.equalsIgnoreCase("S")) {
						getOrCreateNode(anotherTerm, basicTerm);
					}
				}

			} catch (IOException e) {
				throw new IllegalStateException();
			}
		}
	}

	private void getOrCreateNode(String basicTerm, String anotherTerm) {
		RelationNode relationNode = getNode(basicTerm);
		RelationNode relationNode1 = getNode(anotherTerm);
		relationNode.downLinks.add(anotherTerm);
		relationNode1.upLinks.add(basicTerm);
	}

	RelationNode getNode(String basicTerm) {
		RelationNode relationNode = relations.get(basicTerm);
		if (relationNode == null) {
			relationNode = new RelationNode(basicTerm);
			relations.put(basicTerm, relationNode);
		}
		return relationNode;
	}

	public void load(String path) {
		try {
			BufferedReader rs = new BufferedReader(new InputStreamReader(
					new FileInputStream(path), "UTF-8"));
			load(rs);
			rs.close();
		} catch (UnsupportedEncodingException e) {
			throw new IllegalArgumentException(e);
		} catch (FileNotFoundException e) {
			throw new IllegalArgumentException(e);
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}

	public static void main(String[] args) {
		RelationExtender relationExtender = new RelationExtender();
		relationExtender.load("D:/all2_nataly.txt");
		System.out.println(relationExtender.relations.size());
	}
}
