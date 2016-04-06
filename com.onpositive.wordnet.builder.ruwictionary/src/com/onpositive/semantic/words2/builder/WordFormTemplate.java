package com.onpositive.semantic.words2.builder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.LinkedHashSet;

import com.onpositive.semantic.wordnet.GrammarRelation;
import com.onpositive.semantic.wordnet.Grammem;
import com.onpositive.semantic.words2.SimpleWordNet;
import com.onpositive.semantic.words2.Word;
import com.onpositive.semantic.words2.WordNet;

public class WordFormTemplate implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	boolean correct = true;
	String title;

	WordNet wordNet;

	protected IFormRule formRule = new NounFormRule();
	
	protected String templateLink;

	public WordFormTemplate(String title, String str, WordNet wi, int kind)
			throws IOException {
		this.title = title;
		this.wordNet = wi;
		if (kind == Grammem.PartOfSpeech.NOUN.intId) {
			formRule = new NounFormRule();
		}
		if (kind ==Grammem.PartOfSpeech.ADJF.intId) {
			formRule = new AdjectiveFormRule();
		}
		if (kind ==Grammem.PartOfSpeech.ADJS.intId) {
			formRule = new AdjectiveFormRule();
		}
		if (kind ==Grammem.PartOfSpeech.VERB.intId) {
			formRule = new AdjectiveFormRule();
		}
		BufferedReader rs = new BufferedReader(new StringReader(str));
		while (true) {
			String readLine = rs.readLine();
			
			if (readLine == null) {
				break;
			}
			if (readLine.startsWith("{{прил ru")){//handle adjective
				//ссылка на шаблон
				templateLink=readLine.substring(2);
			}
			int indexOf = readLine.indexOf('=');
			if (indexOf != -1) {
				String value = readLine.substring(indexOf + 1).trim();
				value = value.replace((CharSequence) "{{{основа}}}", "#0");
				value = value.replace((CharSequence) "{{{основа1}}}", "#1");
				value = value.replace((CharSequence) "{{{основа2}}}", "#2");
				value = value.replace((CharSequence) "{{{основа3}}}", "#3");
				value = value.replace((CharSequence) "{{{основа|{{{1}}}}}}",
						"#0");
				;
				if (readLine.length() > 2 && indexOf > 0) {
					String propName = readLine.substring(1, indexOf).trim()
							.replace('-', '_');
					if (propName != null) {
						try {
							Field declaredField = getField(propName);
							if (declaredField!=null){
							declaredField.setAccessible(true);
							declaredField.set(formRule, value);
							}
						} catch (NoSuchFieldException e) {

						} catch (SecurityException e) {
							e.printStackTrace();
						} catch (IllegalArgumentException e) {
							e.printStackTrace();
						} catch (IllegalAccessException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	}
	
	@SuppressWarnings("rawtypes")
	static HashMap<Class, HashMap<String, Field>>fs=new HashMap<Class, HashMap<String,Field>>();
	@SuppressWarnings("rawtypes")
	static HashMap<Class, HashMap<Field, Integer>>mm=new HashMap<Class, HashMap<Field,Integer>>();

	private Field getField(String propName) throws NoSuchFieldException {
		Class<? extends IFormRule> class1 = formRule.getClass();
		HashMap<String, Field> hashMap = fs.get(class1);
		if (hashMap==null){
			hashMap=new HashMap<String, Field>();
			HashMap<Field, Integer> hashMap1 = new HashMap<Field,Integer>();
			for (Field f:class1.getDeclaredFields()){
				f.setAccessible(true);
				FieldMapping annotation = f.getAnnotation(FieldMapping.class);
				if (annotation!=null){
					hashMap.put(annotation.value(),f);
					hashMap1.put(f, annotation.relation());
					continue;
				}
				else{
					hashMap.put(f.getName(), f);
					try{
					Field declaredField = class1.getDeclaredField(f.getName().toUpperCase());
					declaredField.setAccessible(true);
					try {
						Object object = declaredField.get(null);
						hashMap1.put(f, (Integer) object);
						continue;
					} catch (IllegalArgumentException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					}catch (NoSuchFieldException e) {
						// TODO: handle exception
					}
					try{
						Field declaredField = class1.getDeclaredField(f.getName()+"_code");
						declaredField.setAccessible(true);
						try {
							Object object = declaredField.get(null);
							hashMap1.put(f, (Integer) object);
						} catch (IllegalArgumentException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IllegalAccessException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						}catch (NoSuchFieldException e) {
							// TODO: handle exception
						}
				}
			}
			fs.put(class1, hashMap);
			mm.put(class1, hashMap1);
		}
		return hashMap.get(propName);
	}

	static char[] lc = "а́е́и́о́у́ы́э́ю́я́".toCharArray();
	static char[] lc1 = "aеиоуыэюя".toCharArray();

	

	public void build(Word w,TempWordInfo inf) {
		boolean pt=inf.pt;
		boolean st=inf.st;
		
		Field[] declaredFields = formRule.getClass().getDeclaredFields();
		boolean allCorrect = true;
		for (Field f : declaredFields) {
			try {
				if (pt&&formRule instanceof NounFormRule){
					if (f.getName().endsWith("_sg")){
						continue;
					}
				}
				if (st&&formRule instanceof NounFormRule){
					if (f.getName().endsWith("_pl")){
						continue;
					}
				}
				f.setAccessible(true);
				if (f.getType() != String.class) {
					continue;
				}
				String object = (String) f.get(formRule);
				String pl = object;
				if (object != null && object.equals("—")) {
					continue;
				}
				if (inf.foundation == null && inf.foundation1 == null
						&& object != null) {
					try {
						inf.foundation = w.getBasicForm().substring(0, w.getBasicForm().length()
								- (object.length() - 2));
					} catch (Exception e) {

					}
				}
				if (pl == null) {
					allCorrect=false;
					continue;
				}
				if (inf.foundation != null) {
					pl = pl.replace((CharSequence) "#0", inf.foundation);
				}
				if (inf.foundation1 != null) {
					pl = pl.replace((CharSequence) "#1", inf.foundation1);
				}
				if (inf.foundation2 != null) {
					pl = pl.replace((CharSequence) "#2", inf.foundation2);
				}
//				if (w.foundation3 != null) {
//					pl = pl.replace((CharSequence) "#3", w.foundation3);
//				}
				if (pl != null) {
					String replace = pl
							.replace((CharSequence) ("" + lc[1]), "").replace(
									(CharSequence) "?", "");
					if (replace != null && replace.length() > 0
							&& replace.indexOf('#') == -1) {
						
						String[] rr=replace.split("<br />");
						if (rr.length==1){
							rr=replace.split("<br/>");
						}
						if (rr.length==1){
							rr=replace.split("<br>");
						}
						for (String s: rr){
						GrammarRelation relation = getRelation((SimpleWordNet) wordNet, w,f,formRule);
						if (relation!=null){
						wordNet.registerWordForm(s, relation);
						}
						}
						continue;
					}
				}
				allCorrect=false;
			} catch (IllegalArgumentException e) {

			} catch (IllegalAccessException e) {

			}
		}
		
		
	}

	private GrammarRelation getRelation(SimpleWordNet wn, Word w, Field f, IFormRule formRule2) {
		Class<?> declaringClass = f.getDeclaringClass();
		HashMap<Field, Integer> hashMap = mm.get(declaringClass);
		Integer integer = hashMap.get(f);
		if (f.getName().equals("pt")||f.getName().equals("st")){
			return null;
		}
		LinkedHashSet<Grammem>grSet=new LinkedHashSet<Grammem>();
		if (formRule instanceof NounFormRule){
			LinkedHashSet<Grammem> relations = NounRelations.getRelations(integer);
			grSet=relations;
		}
		if (formRule instanceof VerbFormRule){
			grSet=VerbRelations.getRelations(integer);
		}
		if (formRule instanceof AdjectiveFormRule){
			grSet=AdjectiveRelations.getRelations(integer);
		}
		int code=wn.getGrammemCode(grSet);
		return new GrammarRelation(wn,w, code);
	}
	
	@SuppressWarnings("rawtypes")
	transient HashMap<Class, HashMap<Field, Integer>>map=new HashMap<Class, HashMap<Field,Integer>>();

	public void register(Word word,TempExtras e,TempWordInfo inf) {
		if (templateLink!=null){
			processOverride(e);
		}
		build((Word) word, inf);
	}

	private void processOverride(TempExtras ex) {
		WordFormTemplate findTemplate = ex.findTemplate("Шаблон:"+templateLink);
		if (findTemplate!=null){
			IFormRule formRule2 = findTemplate.formRule;
			map.get(formRule2.getClass());
			Field[] declaredFields = formRule2.getClass().getDeclaredFields();
			for (Field f:declaredFields){
				f.setAccessible(true);
				if (!Modifier.isFinal(f.getModifiers())){
					try {
						Object object = f.get(formRule2);
						Object object1 = f.get(this.formRule);
						if(object1==null){
						f.set(this.formRule, object);
						}
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					}
				}
			}
		}
		else{
			System.out.println("Error");
		}
		//
		templateLink=null;
	}
	
}