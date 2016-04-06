package com.onpositive.semantic.words3.prediction;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.onpositive.semantic.words3.ReadOnlyWordNet;
import com.onpositive.semantic.words3.hds.StringToByteTrie;

public class DefaultPredictionService implements IPredictionService {

	@Override
	public IPredictionHelper getPredictionHelper(ReadOnlyWordNet wordNet) {
		DefaultPredictionDataProvider provider = new DefaultPredictionDataProvider();
		String version = wordNet.getVersion();
		InputStream predictionData = provider.getPredictionData();
		OutputStream outputStream = null;
		try {
			if (predictionData == null || !version.equals(PredictionUtil.getVersion(predictionData))) {
				outputStream = provider.getPredictionDataOutputStream();
				PredictionUtil.rebuildPredictionVocab(outputStream, wordNet, version);
				outputStream.close();
				predictionData = provider.getPredictionData();
			}
			StringToByteTrie basesTrie = new StringToByteTrie();
			StringToByteTrie endingsTrie = new StringToByteTrie();
			new DataInputStream(predictionData).readUTF(); //Skip version
			basesTrie.read(predictionData);
			endingsTrie.read(predictionData);
			return new TriePredictionHelper(wordNet, basesTrie, endingsTrie);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} finally {
			if (outputStream != null)
				try {outputStream.close();} catch (IOException e) {}
		}
	}

}
