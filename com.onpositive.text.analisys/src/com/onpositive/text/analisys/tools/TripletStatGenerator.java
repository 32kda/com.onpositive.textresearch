package com.onpositive.text.analisys.tools;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class TripletStatGenerator {
	class Triplet {
		public int first;
		public int second;
		public int third;
		public Triplet(int first, int second, int third) { this.first = first; this.second = second; this.third = third; }
		
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + first;
			result = prime * result + second;
			result = prime * result + third;
			return result;
		}
				
		@Override
		public boolean equals(Object other) {
			if (this == other) return true;
			if (!(other instanceof Triplet)) return false;
			Triplet another = (Triplet) other;
			return this.first == another.first && this.second == another.second && this.third == another.third;  
		}

//		@Override
//		public int hashCode() {
//			return first << 16 + second << 8 + third;
//		}
	}
	
	public String d_in, f_out;	
	HashMap<Triplet, Integer> stats; 
	
	public TripletStatGenerator(String in, String out) throws IOException { 
		d_in = in;
		f_out = out;
		
		stats = new HashMap<TripletStatGenerator.Triplet, Integer>();
	}
	
	public void runFor(File f) throws IOException {		
		DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(f)));
		ArrayList<Integer> chain = new ArrayList<Integer>();
		try {
			while (true) {
				int val = in.readInt();
				if (val != -1) 
					chain.add(val);
				else {
					for (int i = 0; i < chain.size() - 2; i++) {
						Triplet tr = new Triplet(chain.get(i), chain.get(i + 1), chain.get(i + 2));
						int curr = stats.containsKey(tr) ? stats.get(tr) : 0;
						if (stats.containsKey(tr)) stats.put(tr, curr);
						else stats.put(tr,  1);
					}
					chain.clear();
				}
			}
		} 
		catch (EOFException e) {} 
		finally {
			in.close();
		}
	}
	
	
	public void run() throws IOException {
		File dir = new File(d_in);
		if (dir.isDirectory() == false) return;
		File[] list = dir.listFiles();
		
		for (int i = 0; i < list.length; i++) {
			System.out.print("\r" + i + "/" + list.length + ": " + stats.size() + " entries found.            ");
			this.runFor(list[i]);
		}			
		
		DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(f_out, false)));
		System.out.println("\n\nStat: " + stats.size() + " entries");
		for(Triplet stat : stats.keySet()) {
			out.writeInt(stat.first);
			out.writeInt(stat.second);
			out.writeInt(stat.third);
			out.writeInt(stats.get(stat));
		}
		out.flush();		
		out.close();
	}
	
	public static void main(String[] args) throws IOException {
		if (args.length != 2) {
			System.out.println("Usage: java -jar tsg.jar INPUTDIR OUTPUT");
			return;
		}
		new TripletStatGenerator(args[0], args[1]).run();
	}
}
