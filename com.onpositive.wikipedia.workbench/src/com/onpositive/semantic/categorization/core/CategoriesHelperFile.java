package com.onpositive.semantic.categorization.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class CategoriesHelperFile {

	static CategoriesHelperFile instance;

	private UICategories categories;
	
	private CategoriesHelperFile(){
		try {
			if(getFile().exists()) {
				Document newDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new FileInputStream(getFile())));
				categories = new UICategories();
				categories.load(newDocument);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
	}
	
	public static synchronized CategoriesHelperFile getInstance() {
		if(instance == null) {
			instance = new CategoriesHelperFile();
		}
		return instance;
	}
	
	File getFile() {
		return new File(SearchSystem.getRootIndexPath().toFile(), "categories.xml");
	}
	
	public HashSet<String> getMappings(String category) {
		if(categories == null) return Sets.newHashSet();
		return categories.getMappings(category);
	}
	
	public ArrayList<UICategory> getElements() {
		if(categories == null) return Lists.newArrayList();
		return categories.getElements();
	}
	
}
