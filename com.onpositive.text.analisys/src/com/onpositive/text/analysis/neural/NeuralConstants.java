package com.onpositive.text.analysis.neural;

import java.util.HashMap;
import java.util.Map;

import com.onpositive.semantic.wordnet.Grammem;
import com.onpositive.semantic.wordnet.Grammem.*;

public class NeuralConstants {

	@SuppressWarnings("unchecked")
	public static final Class<? extends Grammem>[] USED_PROPS = (Class<? extends Grammem>[]) new Class<?>[] {PartOfSpeech.class, Case.class, Gender.class, SingularPlural.class, VerbKind.class, TransKind.class };
	
	public static final Map<Class<?extends Grammem>, Integer> BITS_PER_PROP = new HashMap<Class<? extends Grammem>, Integer>();
	
	public static final int TOKEN_WINDOW_SIZE = 3;
	
	static {
		BITS_PER_PROP.put(PartOfSpeech.class,5);
		BITS_PER_PROP.put(Case.class,4);
		BITS_PER_PROP.put(Gender.class,3);
		BITS_PER_PROP.put(SingularPlural.class,3);
		BITS_PER_PROP.put(VerbKind.class,2);
		BITS_PER_PROP.put(TransKind.class,2);
	}
	
}
