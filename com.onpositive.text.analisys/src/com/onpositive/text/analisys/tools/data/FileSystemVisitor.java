package com.onpositive.text.analisys.tools.data;

import java.io.File;

public abstract class FileSystemVisitor {


	public void visit(File file){
		if(file.isDirectory()){
			visitDirectory(file);
		}
		else{
			visitFile(file);
		}
	}

	protected abstract void visitFile(File file);
	

	protected void visitDirectory(File folder) {
		File[] files = folder.listFiles();
		for(File file : files){
			visit(file);
		}
	}
	
	

}
