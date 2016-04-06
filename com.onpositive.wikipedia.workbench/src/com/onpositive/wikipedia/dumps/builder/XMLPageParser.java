package com.onpositive.wikipedia.dumps.builder;

import java.io.IOException;
import java.io.Reader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


public class XMLPageParser {

	
	
	
	public void visitContent(Reader rs,final IPageVisitor visitor){
		try {

			SAXParserFactory.newInstance().newSAXParser().parse(new InputSource(rs), new DefaultHandler(){
				
				StringBuilder text=new StringBuilder();
				int id;
				String title;

				String lastElement;
				
				boolean inRevision;
				private int ns;
				
				@Override
				public void startElement(String uri, String localName,
						String qName, Attributes attributes)
						throws SAXException {
					lastElement=qName;
					if (qName.equals("text")){
						text.delete(0, text.length());
					}
					if (qName.equals("revision")){
						inRevision=true;
					}
					super.startElement(uri, localName, qName, attributes);
				}
				@Override
				public void characters(char[] ch, int start, int length)
						throws SAXException {
					if (lastElement.equals("text")){
						text.append(ch,start,length);						
					}
					if (lastElement.equals("title")){
						title=new String(ch,start,length);						
					}
					if (lastElement.equals("ns")){
						try{
						ns=Integer.parseInt(new String(ch,start,length));
						}catch (NumberFormatException e) {
							
						}
												
					}
					if (lastElement.equals("id")&&!inRevision){
						try{
						id=Integer.parseInt(new String(ch,start,length));
						}catch (NumberFormatException e) {
							
						}
					}
					super.characters(ch, start, length);
				}
				
				@Override
				public void endElement(String uri, String localName,
						String qName) throws SAXException {
					if (qName.equals("revision")){
						inRevision=false;
					}
					if (qName.equals("page")){
						PageModel model = new PageModel(id, ns,title, text.toString());
						visitor.visit(model);
					}
					lastElement="";
					super.endElement(uri, localName, qName);
				}
			});
		} catch (SAXException e) {
			
		} catch (IOException e) {
			
		} catch (ParserConfigurationException e) {
			
		}
	}
}
