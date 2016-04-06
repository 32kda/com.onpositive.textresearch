package com.onpositive.semantic.wikipedia2.datesearch;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import com.carrotsearch.hppc.IntObjectOpenHashMapSerialzable;
import com.onpositive.semantic.search.core.date.IFreeFormDate;
import com.onpositive.semantic.search.core.date.LongToDateFactory;
import com.onpositive.semantic.wikipedia2.WikiEngine2;
import com.onpositive.semantic.wikipedia2.datesearch.DateStore.DatesInfo;
import com.onpositive.semantic.wikipedia2.services.RedirectsMap;
import com.onpositive.semantic.words3.hds.StringVocabulary;

public class DocumentDateModel {

	protected ArrayList<IFreeFormDate> dates = new ArrayList<IFreeFormDate>();
	
	public DocumentDateModel() {
	}

	public static DocumentDateModel2 build(WikiEngine2 engine,int q) {
		String plainTextAbstract = engine.getTextAbstract(q);
		//System.out.println(plainTextAbstract);
		int[] direct = engine.getPageToParentCategories().getInverse(q);
		String[] ct=new String[direct.length];
		String title=engine.getPageTitles().get(q);
		//System.out.println(engine.getPageTitles().get(q));
		int a=0;
		for (int c:direct){
			StringVocabulary categoryTitles = engine.getCategoryTitles();
			try{
			String string = categoryTitles.get(c);
			ct[a++]=string;
			}catch (Exception e) {
				e.printStackTrace();
			}
		}
		DocumentDateModel2 buildModel = DocumentDateModel2.buildModel(plainTextAbstract,ct);
		buildModel.validateYears();
		return buildModel;
	}
	
	static void buildRoughDates(WikiEngine2 engine){
		File fl=getFile(engine);
		if (fl.exists()){
			return;
		}
		try {
			DataOutputStream stream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(fl)));
			buildRoughDateIndex(engine, stream);
			stream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	static File getFile(WikiEngine2 engine) {
		return new File(engine.getLocation(),"roughDates.dat");
	}

	static void buildRoughDateIndex(WikiEngine2 engine,DataOutputStream stream) throws IOException {
		int[] articleKeys = engine.getDocumentIDs();
		RedirectsMap index = engine.getIndex(RedirectsMap.class);
		int i = 0;
		for (int a : articleKeys) {
			if (i % 1000 == 0) {
				System.out.println(i);
			}
			if (index.isRedirect(a)){
				i++;
				continue;
			}
			DocumentDateModel2 build = build(engine,a);
			if (!build.dates.isEmpty()){
				stream.writeInt(a);
				stream.writeInt(build.dates.size());
				for (int q=0;q<build.dates.size();q++){
					IFreeFormDate date=build.dates.get(q);
					stream.writeLong(date.pack());					
				}
				
			}
			i++;
		}
	}

	public static DateStore getDateStore(WikiEngine2 engine) {
		buildRoughDates(engine);
		final IntObjectOpenHashMapSerialzable<DateStore.DatesInfo>map=new IntObjectOpenHashMapSerialzable<DateStore.DatesInfo>();
		try {
			DataInputStream stream = new DataInputStream(new BufferedInputStream(new FileInputStream(getFile(engine))));
			
			while (true){
				int docNumber = stream.readInt();
				int count=stream.readInt();
				DatesInfo si=new DatesInfo(count);
				map.put(docNumber, si);
				for (int a=0;a<count;a++){
					long readLong = stream.readLong();
					IFreeFormDate unpack = LongToDateFactory.unpack(readLong);
					if (unpack==null){
						throw new IllegalStateException();
					}
					IFreeFormDate date=unpack;
					si.dates[a]=date;
				}
			}	
			
		} 
		catch (EOFException e) {
			return new DateStore() {
				
				@Override
				public DatesInfo getInfo(int d) {
					return map.get(d);
				}
			};
		}
		catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}
}
