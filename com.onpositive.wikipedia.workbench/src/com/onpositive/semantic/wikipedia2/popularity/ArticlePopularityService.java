package com.onpositive.semantic.wikipedia2.popularity;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.URLDecoder;

import com.onpositive.semantic.wikipedia2.WikiEngine2;
import com.onpositive.semantic.wikipedia2.WikiEngineService;
import com.onpositive.semantic.words3.hds.IntArrayList;

public final class ArticlePopularityService extends WikiEngineService{
	
	protected IntArrayList popularityData;
	protected IntArrayList userPopulatity;
	
	public int getOriginalPopularity(int document){
		return popularityData.get(document);
	}
	
	public int getUserPopularity(int document){
		return up.userPopularityData.get(document);
	}
	public int getTotalPopularity(int document){
		return up.userPopularityData.get(document)+popularityData.get(document);
	}
	public class UserAdjustedPopularity {

		protected DataOutputStream transactionLog;
		
		protected IntArrayList userPopularityData;
		
		public UserAdjustedPopularity(WikiEngine2 engine) {
			File fl=new File(engine.getLocation());
			File userPopularity=new File(fl,"userPopularity.dat");
			int[] mmmm=new int[engine.getDocumentIDs().length+1];
			if (userPopularity.exists()){
				try {
					long len=userPopularity.length();
					DataInputStream ds=new DataInputStream(new BufferedInputStream(new FileInputStream(userPopularity)));
					for (int a=0;a<len/8;a++){
						int pos=ds.readInt();
						int amount=ds.readInt();
						mmmm[pos]+=amount;
							
					}
					ds.close();
				} catch (IOException e) {
					throw new IllegalStateException(e);
				}
			}
			userPopularityData=new IntArrayList(mmmm.length+1);
			for (int q:mmmm){
				userPopularityData.add(q);
			}
			try {
				transactionLog=new DataOutputStream(new FileOutputStream(userPopularity,true));
			} catch (FileNotFoundException e) {
				throw new IllegalStateException(e);
			}
			
		}

		public void logChange(int document,int amount){
			try{
			transactionLog.writeInt(document);
			transactionLog.writeInt(amount);
			transactionLog.flush();
			int i = userPopularityData.get(document);
			userPopularityData.put(document, i+amount);
			}catch (Exception e) {
				throw new IllegalStateException(e);
			}
		}
	}

	
	UserAdjustedPopularity up;
	
	public ArticlePopularityService(WikiEngine2 engine) {
		super(engine);
		up=new UserAdjustedPopularity(engine);
	}
	
	int[] parsePopularity(File fl,WikiEngine2 engine,String langCode) throws FileNotFoundException{
		int count=0;
		int incorrectCount=0;
		int[] mmmm=new int[engine.getDocumentIDs().length+1];
		for (File f:fl.listFiles()){
			if (f.getName().startsWith("pagecounts")){
				BufferedReader r=new BufferedReader(new FileReader(f));
				while (true){
					try {
						String readLine = r.readLine();
						if (readLine==null){
							break;
						}
						int k=readLine.indexOf(' ');
						int k1=readLine.indexOf(' ',k+1);
						String projectCode=readLine.substring(0,k);
						
						String documentCode=readLine.substring(k+1,k1);
						String viewsCode=readLine.substring(k1+1);
						
						if (projectCode.equalsIgnoreCase(langCode)){
							
							try{
							documentCode=URLDecoder.decode(documentCode, "UTF-8");
							int i = engine.getPageTitles().get(documentCode);
							if(i>=0){
								int indexOf = viewsCode.indexOf(' ');
								if (indexOf>0){
									int c=Integer.parseInt(viewsCode.substring(0,indexOf));
									mmmm[i]+=c;
								}
								count++;
							}
							else{
								incorrectCount++;
							}
							//System.out.println(documentCode);
							}catch (Exception e) {
								incorrectCount++;
								//System.err.println(documentCode);
								// TODO: handle exception
							}
							
						}
						//System.out.println("A");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		System.out.println(count+":"+incorrectCount);
		return mmmm;
	}

	@Override
	protected void doLoad(File fl) throws IOException {
		popularityData=new IntArrayList(fl);
	}

	@Override
	protected void build(WikiEngine2 enfine) {
		if (popularityData==null){
			File file = new File(new File(engine.getLocation()).getParentFile(),"popularity");
			try {
				int[] parsePopularity = parsePopularity(file, engine, "ru");
				popularityData=new IntArrayList();
				for (int q:parsePopularity){
					popularityData.add(q);
				}
				
			} catch (IOException e) {
				throw new IllegalStateException();
			}
		}
	}

	@Override
	protected void doSave(File fl) throws IOException {
		popularityData.store(new File(engine.getLocation(),getFileName()));
	}

	@Override
	public String getFileName() {
		return "popularityOrig.dat";
	}

	public void vote(int pageId, int i) {
		up.logChange(pageId, i);
	}
}
