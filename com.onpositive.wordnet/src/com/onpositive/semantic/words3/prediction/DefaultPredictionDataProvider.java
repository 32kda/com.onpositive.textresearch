package com.onpositive.semantic.words3.prediction;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class DefaultPredictionDataProvider implements IPredictionDataProvider {

	@Override
	public InputStream getOriginalWordNetData() {
		// TODO Not supported yet
		return null;
	}

	@Override
	public InputStream getPredictionData() {
		String tmpDir = System.getProperty("java.io.tmpdir");
		File dataDir = new File(tmpDir,"prediction");
		dataDir.mkdirs();
		File dataFile = new File(dataDir, "prediction.dat");
		if (dataFile.exists()) {
			try {
				return new BufferedInputStream(new FileInputStream(dataFile));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	@Override
	public OutputStream getPredictionDataOutputStream() {
		String tmpDir = System.getProperty("java.io.tmpdir");
		File dataDir = new File(tmpDir,"prediction");
		dataDir.mkdirs();
		File dataFile = new File(dataDir, "prediction.dat");
		try {
			return new BufferedOutputStream(new FileOutputStream(dataFile));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

}
