package com.onpositive.text.analysis.neural;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.onpositive.semantic.wordnet.Grammem;

@SuppressWarnings("unchecked")
public class BinaryFlagDataSetGenerator implements IDataSetGenerator {
	
	private static List<Grammem> usedGrammems = new ArrayList<Grammem>();
	
	static {
		try {
			for (Class<? extends Grammem> clazz : NeuralConstants.USED_PROPS) {
				Field allField = clazz.getDeclaredField("all");
				usedGrammems.addAll((Collection<Grammem>) allField.get(clazz));
			}
		} catch (NoSuchFieldException e) {
			// Shouldn't happen
			e.printStackTrace();
		} catch (SecurityException e) {
			// Shouldn't happen
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// Shouldn't happen
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// Shouldn't happen
			e.printStackTrace();
		}
	}
	
	private Grammem[] grammemsArray = usedGrammems.toArray(new Grammem[0]); 

	@Override
	public int getDatasetSize() {
		return grammemsArray.length;
	}

	@Override
	public double[] generateDataset(Collection<Grammem> grammems) {
		double[] dataset = new double[grammemsArray.length];
		for (int i = 0; i < grammemsArray.length; i++) {
			if (grammems.contains(grammemsArray[i])) {
				dataset[i] = 1;
			}
		}
		return dataset;
	}

}
