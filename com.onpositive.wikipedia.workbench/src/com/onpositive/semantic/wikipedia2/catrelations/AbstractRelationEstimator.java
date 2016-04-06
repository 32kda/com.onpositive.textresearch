package com.onpositive.semantic.wikipedia2.catrelations;

import java.io.PrintStream;

import com.onpositive.compactdata.TwoIntToByteMap;
import com.onpositive.semantic.wikipedia2.WikiEngine2;

public abstract class AbstractRelationEstimator {

	
	protected final String kind;
	protected final String id;
	protected final String name;
	
	public AbstractRelationEstimator(String kind, String id, String name) {
		super();
		this.kind = kind;
		this.id = id;
		this.name = name;
	}

	public abstract byte relation(int c1,int c2,PrintStream log); 
	
	@Override
	public String toString() {
		return id+"("+kind+")";
	}

	public int noRelation() {
		return Byte.MIN_VALUE;
	}
	
	protected TwoIntToByteMap run(WikiEngine2 engine,PrintStream ps){
		TwoIntToByteMap resultMap=new TwoIntToByteMap();
		for (int c: engine.getCategoryRenumberer().getIds()){
			int[] subCategories = engine.getSubCategories(c);
			for (int sub:subCategories){
				byte relation = relation(c, sub,ps);
				if (relation!=noRelation()){
					resultMap.put(c, sub, relation);
					if (ps!=null){
						String string = engine.getCategoryTitles().get(c);
						String string1 = engine.getCategoryTitles().get(sub);
						ps.println(string+"->"+string1+"="+relation);
					}
				}				
			}
		}
		return resultMap;
	}
}
