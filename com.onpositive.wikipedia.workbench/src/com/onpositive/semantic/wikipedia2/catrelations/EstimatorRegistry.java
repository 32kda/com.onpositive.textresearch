package com.onpositive.semantic.wikipedia2.catrelations;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;

import com.onpositive.compactdata.TwoIntToByteMap;
import com.onpositive.semantic.wikipedia2.WikiEngine2;

public class EstimatorRegistry {

	protected ArrayList<AbstractRelationEstimator>allEstimators=new ArrayList<AbstractRelationEstimator>();

	HashSet<String>turnedOf;

	private WikiEngine2 engine;
	
	private EstimatorRegistry(WikiEngine2 eng) {
		location = eng.getLocation();
		this.engine=eng;
		initEstimators();
	}
	private void initEstimators() {
		
	}
	public File getRelationsFolder(){
		File file = new File(location,"relations");
		if (!file.exists()){
			file.mkdir();
		}
		return file;
	}

	public AbstractRelationEstimator[] getAvailableEstimators(){
		return allEstimators.toArray(new AbstractRelationEstimator[allEstimators.size()]);		
	}
	
	static IdentityHashMap<WikiEngine2, EstimatorRegistry> instances=new IdentityHashMap<WikiEngine2, EstimatorRegistry>();

	private String location;
	
	protected HashMap<AbstractRelationEstimator, TwoIntToByteMap>maps=new HashMap<AbstractRelationEstimator, TwoIntToByteMap>();
	
	public void clean(AbstractRelationEstimator estimator){
		maps.remove(estimator);
		new File(getDataFile(estimator)).delete();
	}
	public void rebuild(AbstractRelationEstimator estimator){
		clean(estimator);
		getMap(estimator);
	}
	
	public void clean(){
		maps.clear();
		File relationsFolder = getRelationsFolder();
		for (File f:relationsFolder.listFiles()){
			f.delete();
		}
	}
	
	public TwoIntToByteMap getMap(AbstractRelationEstimator estimator){
		TwoIntToByteMap twoIntToByteMap = maps.get(estimator);
		if (twoIntToByteMap!=null){
			return twoIntToByteMap;
		}
		if (new File(getDataFile(estimator)).exists()){
			twoIntToByteMap=new TwoIntToByteMap();
			try {
				twoIntToByteMap.read(getDataFile(estimator));
				maps.put(estimator, twoIntToByteMap);
				return twoIntToByteMap;
			} catch (IOException e) {
				e.printStackTrace();
			}
			twoIntToByteMap=runEstimator(estimator, true);
			maps.put(estimator, twoIntToByteMap);
			return twoIntToByteMap;
		}
		return twoIntToByteMap;		
	}
	
	protected TwoIntToByteMap runEstimator(AbstractRelationEstimator estimator,boolean log){
		PrintStream ps=null;
		try{
		if (log){
			ps=new PrintStream(new BufferedOutputStream(new FileOutputStream(new File(getRelationsFolder(),estimator.id+".log"))));
		}
		TwoIntToByteMap resultMap=estimator.run(engine, ps);
		if (ps!=null){
			ps.close();
		}
		resultMap.write(getDataFile(estimator));
		return resultMap;
		}catch (Exception e) {
			throw new IllegalStateException(e);
		}		
	}
	
	public String getDataFile(AbstractRelationEstimator estimator) {
		return new File(getRelationsFolder(),estimator.id).getAbsolutePath();
	}
	
	public static EstimatorRegistry getInstance(WikiEngine2 eng){
		EstimatorRegistry estimatorRegistry = instances.get(eng);
		if (estimatorRegistry==null){
			EstimatorRegistry estimatorRegistry2 = new EstimatorRegistry(eng);
			instances.put(eng, estimatorRegistry2);
			return estimatorRegistry2;
		}
		return estimatorRegistry;
	}
}