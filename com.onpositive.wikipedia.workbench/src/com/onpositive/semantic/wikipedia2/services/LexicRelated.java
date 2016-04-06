package com.onpositive.semantic.wikipedia2.services;

import java.lang.reflect.Constructor;
import java.util.ArrayList;

import com.onpositive.semantic.wordnet.AbstractWordNet;
import com.onpositive.text.analysis.AbstractParser;
import com.onpositive.text.analysis.IParser;
import com.onpositive.text.analysis.ParserComposition;

public class LexicRelated {

	AbstractWordNet wordnet;
	
	public LexicRelated(AbstractWordNet wordnet) {
		super();
		this.wordnet = wordnet;
	}

	public ParserComposition createParsers(Class<?>[] array,
			boolean isGloballyRecursive) {

		ArrayList<IParser> list = new ArrayList<IParser>();
		for (Class<?> clazz : array) {
			IParser parser = createParser(clazz);
			if (parser != null) {
				list.add(parser);
			}
		}
		IParser[] arr = list.toArray(new IParser[list.size()]);
		ParserComposition result = new ParserComposition(isGloballyRecursive,
				arr);
		return result;
	}

	private boolean extendsClass(Class<?> clazz, Class<?> parent) {
		boolean isParser = false;
		for (Class<?> cl = clazz; cl != null; cl = cl.getSuperclass()) {
			isParser = (cl == parent);
			if (isParser) {
				break;
			}
		}
		return isParser;
	}

	private boolean hasInterface(Class<?> clazz, Class<IParser> iClass) {
		boolean isParser = false;
		for (Class<?> cl = clazz; cl != null; cl = cl.getSuperclass()) {

			if (cl == iClass) {
				return true;
			}

			Class<?>[] interfaces = cl.getInterfaces();
			if (interfaces == null) {
				continue;
			}
			for (Class<?> i : interfaces) {
				if (hasInterface(i, iClass)) {
					return true;
				}
			}
		}
		return isParser;
	}

	private IParser createParser(Class<?> clazz) {

		boolean isParser = extendsClass(clazz, AbstractParser.class)
				|| hasInterface(clazz, IParser.class);
		if (!isParser) {
			return null;
		}
		Constructor<?> constr = null;
		Constructor<?>[] constructors = clazz.getConstructors();
		for (Constructor<?> c : constructors) {
			Class<?>[] params = c.getParameterTypes();
			if (params.length == 0) {
				constr = c;
			} else if (params.length == 1) {
				Class<?> paramClass = params[0];
				if (extendsClass(paramClass, AbstractWordNet.class)) {
					constr = c;
					break;
				}
			}
		}
		if (constr == null) {
			return null;
		}
		IParser instance = null;
		try {
			if (constr.getParameterTypes().length == 0) {
				instance = (IParser) constr.newInstance();
			} else {
				instance = (IParser) constr.newInstance(this.wordnet);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return instance;
	}
}
