package com.onpositive.text.morphology.dataset.prepare;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class DataSetProvider {
	
	private static final String CACHE_FILE_NAME = "cache.obj";
	private static final String PREPS_CACHE_FILE_NAME = "preps.obj";
	private static List<DataSetTestSample> samples;
	protected static File tst=new File(CACHE_FILE_NAME);
	protected static File additionalTst=new File(PREPS_CACHE_FILE_NAME);
	
	@SuppressWarnings("unchecked")
	public static List<DataSetTestSample>getInstance(){
		if (samples != null) {
			return samples;
		}
		try{
			samples = new ArrayList<DataSetTestSample>();
			InputStream readFrom=DataSetProvider.class.getResourceAsStream(CACHE_FILE_NAME);
			if (readFrom==null){
				readFrom=new FileInputStream(tst);
			}
			ObjectInputStream inputStream=new ObjectInputStream(new BufferedInputStream(readFrom));
			samples.addAll((List<DataSetTestSample>) inputStream.readObject());
			 readFrom=DataSetProvider.class.getResourceAsStream(PREPS_CACHE_FILE_NAME);
			if (readFrom==null){
				readFrom=new FileInputStream(additionalTst);
			}
			inputStream=new ObjectInputStream(new BufferedInputStream(readFrom));
			samples.addAll((List<DataSetTestSample>) inputStream.readObject());
			return samples;
		} catch (Exception e){
			samples = new ArrayList<DataSetTestSample>();
			DataSetPreparation dataSetPreparation = new RegularDataSetPreparation();
			dataSetPreparation.prepareTestingData();
			samples.addAll(storePrepared(dataSetPreparation, tst));
			dataSetPreparation = new AdditionalPartDatasetPreparation();
			dataSetPreparation.prepareTestingData();
			samples.addAll(storePrepared(dataSetPreparation, additionalTst));
			return samples;
		}
	}
	protected static List<DataSetTestSample> storePrepared(
			DataSetPreparation dataSetPreparation, File file) {
		List<DataSetTestSample>rs=dataSetPreparation.list;
		try {
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
			objectOutputStream.writeObject(rs);
			objectOutputStream.close();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return rs;
	}
//	Random samples=new Random(2323232);
	
	
}
