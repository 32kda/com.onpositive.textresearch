package com.onpositive.semantic.wikipedia2.services;

import java.io.File;
import java.io.IOException;

import com.onpositive.semantic.wikipedia2.WikiEngine2;
import com.onpositive.semantic.wikipedia2.WikiEngineService;
import com.onpositive.semantic.words3.hds.IntToIntArrayMap;


public abstract class AbstractIntIntArrayIndex extends WikiEngineService{

	private static final int[] INTS = new int[0];
	protected IntToIntArrayMap map;
	
	public AbstractIntIntArrayIndex(WikiEngine2 engine) {
		super(engine);
	}

	@Override
	protected void build(WikiEngine2 engine)  {
		map=new IntToIntArrayMap();
		prebuild();
		int[] categoryKeys = getKeySet();
		for (int a:categoryKeys){
			int[] instanceCat = calcArray(a);
			if (instanceCat!=null&&instanceCat.length>0){
				map.add(a, instanceCat);
			}					
			if (a%1000==0){
				System.out.println("Building "+this.getClass().getSimpleName()+" "+a+" of "+categoryKeys.length+ " completed");
			}
		}
	}
	protected void prebuild() {
		
	}

	public int[] values(int v){
		int[] is = map.get(v);
		if (is==null){
			return INTS;
		}
		return is;
	}

	protected abstract int[] calcArray(int a) ;

	protected abstract int[] getKeySet() ;


	@Override
	protected void doLoad(File fl) throws IOException {
		map=new IntToIntArrayMap(fl);
	}

	@Override
	protected void doSave(File fl) throws IOException {
		map.store(fl);
	}
}
