package com.onpositive.semantic.wikipedia.properties;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class PropertyExtractor {

	public static class RawProperty {
		public RawProperty(int q, String pName, String value2) {
			this.documentId = q;
			this.name = pName;
			this.value = value2;
		}

		public String name;
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + documentId;
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			result = prime * result + ((value == null) ? 0 : value.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			RawProperty other = (RawProperty) obj;
			if (documentId != other.documentId)
				return false;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			if (value == null) {
				if (other.value != null)
					return false;
			} else if (!value.equals(other.value))
				return false;
			return true;
		}

		public int documentId;
		public String value;
		
		@Override
		public String toString() {
			return name+":"+value;
		}
	}

	public List<RawProperty> extract(int q, String plainContent) {
		ArrayList<RawProperty> props = new ArrayList<PropertyExtractor.RawProperty>();
		boolean inRef=false;
		int inTemplate=0;
		if (plainContent != null) {
			BufferedReader d = new BufferedReader(
					new StringReader(plainContent));
			while (true) {
				try {
					String line = d.readLine();
					
					if (line == null) {
						break;
					}
					
					line = line.trim();
					int indexOf = line.indexOf("<ref>");
					if (indexOf!=-1){
						inRef=true;
					}
					indexOf = line.indexOf("</ref>");
					if (indexOf!=-1){
						inRef=false;
					}
					if (inRef){
						continue;
					}
					if (line.startsWith("{{external media")){
						inRef=true;
					}
					if (inRef&& line.endsWith("}}")){
						inRef=false;
					}
					if (inRef){
						continue;
					}
					int cc=0;
					for (int a=0;a<line.length();a++){
						char c=line.charAt(a);
						if (c=='{'&&cc=='{'){
							inTemplate++;
						}
						if (c=='}'&&cc=='}'){
							if (inTemplate>0){
							inTemplate--;
							}
						}
						cc=c;
					}
					
					if (inTemplate<=0){
						continue;
					}
					if (line.length() > 1 && line.charAt(0) == '|') {
						int vStart = line.indexOf('=');
						if (vStart != -1) {
							String pName = line.substring(1, vStart).trim()
									.toLowerCase();

							if (pName.indexOf('[') != -1) {
								continue;
							}
							if (pName.indexOf('|') != -1) {
								continue;
							}
							if (pName.indexOf('=') != -1) {
								continue;
							}
							if (pName.indexOf(',') != -1) {
								continue;
							}
							if (pName.startsWith("align")) {
								continue;
							}
							if (pName.contains("bgcolor")) {
								continue;
							}
							if (pName.contains("style")) {
								continue;
							}
							if (pName.contains("colspan")) {
								continue;
							}
							
							String value = line.substring(vStart + 1).trim();
							String text=value;
							while (true){
								int indexOf2 = text.indexOf("<ref");
								if (indexOf2!=-1){
									String sm=text.substring(0,indexOf2);
									int len=6;
									int indexOf3 = text.indexOf("</ref>", indexOf2);
									if (indexOf3==-1){
										indexOf3 = text.indexOf("/>", indexOf2);
										len=2;
									}
									if (indexOf3!=-1){
										String sm1=text.substring( indexOf3+len);
										text=sm+sm1;
									}
									else{
										break;
									}
									
								}
								else{
									break;
								}
							}
							text = text.replaceAll("<ref>(.)*</ref>", "");
							text = text.replaceAll("<!--(.)*-->", "");
							value=text;
							RawProperty perD = new RawProperty(q, pName.trim(), value);
							props.add(perD);
						}
					}
				} catch (IOException e) {

				}
			}
		}
		return props;
	}
}
