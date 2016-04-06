package com.onpositive.semantic.wikipedia2.services;

import java.io.File;
import java.io.IOException;

import com.onpositive.semantic.wikipedia2.WikiEngine2;
import com.onpositive.semantic.wikipedia2.WikiEngineService;
import com.onpositive.semantic.words3.hds.IntIntMap;


public abstract class AbstractIntIntIndex extends WikiEngineService{

	protected IntIntMap data;
	
	public AbstractIntIntIndex(WikiEngine2 engine) {
		super(engine);
	}

	@Override
	protected void build(WikiEngine2 engine)  {
		int[] categoryKeys = getKeySet();
		IntIntMap value=new IntIntMap();
		data=value;
		int count=0;
		for (int a:categoryKeys){
			int instanceCat = calc(a);
			if (instanceCat!=-1){
				value.put(a, instanceCat);
			}					
			if (count%100000==0){
				System.out.println("Building "+this.getClass().getSimpleName()+" "+count+" of "+categoryKeys.length+ " completed"+" index entries:"+value.size());
			}
			count++;
		}
		System.out.println(value.size());
	}
	public int value(int v){
		int is = data.get(v);
		return is;
	}

	protected abstract int calc(int a) ;

	protected abstract int[] getKeySet() ;

	@Override
	protected void doLoad(File fl) throws IOException {
		data=new IntIntMap(fl);
	}
	@Override
	protected void doSave(File fl) throws IOException {
		data.store(fl);
	}
}