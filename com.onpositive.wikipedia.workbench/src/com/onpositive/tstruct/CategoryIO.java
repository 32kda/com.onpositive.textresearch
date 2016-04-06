package com.onpositive.tstruct;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

public class CategoryIO {

	static JAXBContext newInstance;
	static {
		try {
			newInstance = JAXBContext.newInstance(CategoryNode.class);
		} catch (JAXBException e) {
			e.printStackTrace();
		}
	}

	public static CategoryNode read(String path) {
		try {

			return (CategoryNode) newInstance.createUnmarshaller().unmarshal(
					new InputStreamReader(new FileInputStream(path), Charset
							.forName("UTF-8")));
		} catch (JAXBException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			//лe.printStackTrace();
		}
		return null;
	}

	public static void write(CategoryNode node, String path) {
		try {
			newInstance.createMarshaller().marshal(
					node,
					new OutputStreamWriter(new FileOutputStream(path), Charset
							.forName("UTF-8")));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (JAXBException e) {
			e.printStackTrace();
		}
	}
}