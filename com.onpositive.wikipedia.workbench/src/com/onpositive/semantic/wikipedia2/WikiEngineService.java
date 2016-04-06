package com.onpositive.semantic.wikipedia2;

import java.io.File;
import java.io.IOException;

public abstract class WikiEngineService {

	protected final WikiEngine2 engine;

	public WikiEngineService(WikiEngine2 engine) {
		super();
		this.engine = engine;
		File fl= new File(engine.getLocation(),getFileName());
		if (fl.exists())
		{
			try {
				doLoad(fl);
			} catch (IOException e) {
				buildAndSave(engine, fl);
			}
		}
		else{
			buildAndSave(engine, fl);
		}
	}
	private void buildAndSave(WikiEngine2 engine, File fl) {
		try {
			build(engine);
			doSave(fl);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}
	protected abstract void doLoad(File fl) throws IOException;
	protected abstract void build(WikiEngine2 enfine);
	protected abstract void doSave(File fl)throws IOException;
	
	public abstract String getFileName();
}
