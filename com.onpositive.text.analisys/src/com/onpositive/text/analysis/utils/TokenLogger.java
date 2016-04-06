package com.onpositive.text.analysis.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import com.onpositive.text.analysis.IToken;

public class TokenLogger implements ILogger {
	
	public TokenLogger(File file) {
		super();
		this.file = file;
	}

	public TokenLogger(String path) {
		this.file = new File(path);
	}

	private static final String lineSeparator = System.getProperty("line.separator");
	
	private File file;
	
	/* (non-Javadoc)
	 * @see com.onpositive.text.analysis.utils.ILogger#clean()
	 */
	@Override
	public ILogger clean(){
		if(!file.exists()){
			return this;
		}
		try {
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(new byte[0]);
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return this;
	}
	
	/* (non-Javadoc)
	 * @see com.onpositive.text.analysis.utils.ILogger#writeTokens(com.onpositive.text.analysis.IToken)
	 */
	@Override
	public ILogger writeTokens(IToken... tokens){
		for(IToken t : tokens){
			writeToken(t);
		}
		return this;
	}
	
	/* (non-Javadoc)
	 * @see com.onpositive.text.analysis.utils.ILogger#writeTokens(java.lang.Iterable)
	 */
	@Override
	public ILogger writelnTokens(Iterable<IToken> tokens){
		for(IToken t : tokens){
			writeToken(t);
			newLine();
		}
		return this;
	}
	
	/* (non-Javadoc)
	 * @see com.onpositive.text.analysis.utils.ILogger#writeTokens(com.onpositive.text.analysis.IToken)
	 */
	@Override
	public ILogger writelnTokens(IToken... tokens){
		for(IToken t : tokens){
			writeToken(t);
			newLine();
		}
		return this;
	}
	
	/* (non-Javadoc)
	 * @see com.onpositive.text.analysis.utils.ILogger#writeTokens(java.lang.Iterable)
	 */
	@Override
	public ILogger writeTokens(Iterable<IToken> tokens){
		for(IToken t : tokens){
			writeToken(t);
		}
		return this;
	}
	
	/* (non-Javadoc)
	 * @see com.onpositive.text.analysis.utils.ILogger#writeToken(com.onpositive.text.analysis.IToken)
	 */
	@Override
	public ILogger writeToken(IToken token){
		
		String str = token.toString();		
		return writeString(str);
	}
	
	

	/* (non-Javadoc)
	 * @see com.onpositive.text.analysis.utils.ILogger#writeString(java.lang.String)
	 */
	@Override
	public ILogger writeString(String str) {
	
		try {
			if(!file.exists()){
				file.createNewFile();
			}
			FileOutputStream fos = new FileOutputStream(file,true);			
			fos.write(str.getBytes("UTF-8"));
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return this;
	}
	
	public ILogger writelnString(String str) {		
		return writeString(str + lineSeparator);
	}

	@Override
	public ILogger newLine(int count) {
		StringBuilder bld = new StringBuilder();
		for(int i = 0 ; i < count ; i++){
			bld.append(lineSeparator);
		}
		return writeString(lineSeparator);
	}

	@Override
	public ILogger newLine() {
		return writeString(lineSeparator);
	}

	@Override
	public ILogger writelnToken(IToken token) {
		writeToken(token);
		return newLine();
	}

}
