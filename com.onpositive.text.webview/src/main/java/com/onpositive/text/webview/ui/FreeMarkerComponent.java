package com.onpositive.text.webview.ui;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Locale;

import com.onpositive.text.webview.ui.annotations.View;

import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class FreeMarkerComponent implements IComponent{

	 Configuration cfg=new Configuration(){
			public String getEncoding(Locale locale) {
				return "UTF-8";
			}
		};
		public FreeMarkerComponent(){
			TemplateLoader tl=new ClassTemplateLoader(FreeMarkerComponent.class,"/");
			cfg.setTemplateLoader(tl);
			try {
				template = cfg.getTemplate(this.getClass().getAnnotation(View.class).value());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		//cfg.
		Template template;
		
	
	@Override
	public String render() {
		try {
			StringWriter wrt=new StringWriter();
			template.process(this, wrt);
			return wrt.toString();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TemplateException e) {
			e.printStackTrace();
		}
		return "Error:"+this.getClass().getName();
	}

	public String toString(){
		return render();
	}
}
